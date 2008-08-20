// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.caja.tools.jsdoc;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.lexer.TokenConsumer;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.ObjectConstructor;
import com.google.caja.parser.js.Parser;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.MessageType;
import com.google.caja.reporting.RenderContext;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.tools.BuildCommand;
import com.google.caja.util.Callback;
import com.google.caja.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Has a main method that given a set of files produces JSON documentation.
 *
 * @author mikesamuel@gmail.com
 */
public class JsdocMain {
  private final MessageQueue mq = new SimpleMessageQueue();
  private final MessageContext mc = new MessageContext();
  { mc.inputSources = new ArrayList<InputSource>(); }
  private final FileSystem fs;
  private final AnnotationHandlers handlers = new AnnotationHandlers(mc);
  private final Appendable errStream;
  private final Set<String> packages = new HashSet<String>();
  private final List<String> sourcePaths = new ArrayList<String>();
  private final List<ParseTreeNode> sources = new ArrayList<ParseTreeNode>();
  private final List<Pair<String, String>> initFiles
      = new ArrayList<Pair<String, String>>();

  JsdocMain(FileSystem fs, Appendable errStream) {
    this.fs = fs;
    this.errStream = errStream;
  }

  public static void main(String[] paths) {
    JsdocMain jsdm = new JsdocMain(new RealFileSystem(), System.err);
    List<String> initPaths = new ArrayList<String>();
    List<String> sourcePaths = new ArrayList<String>();
    for (int i = 0; i < paths.length; ++i) {
      if ("--init_file".equals(paths[i])) {
        initPaths.add(paths[++i]);
      } else {
        sourcePaths.add(paths[i]);
      }
    }
    System.exit(jsdm.run(initPaths, sourcePaths, System.out) ? 0 : -1);
  }

  public static class Builder implements BuildCommand {
    public void build(List<File> inputs, List<File> deps, File output)
        throws IOException {
      List<String> files = pathList(inputs);
      FileSystem fs = new RestrictedFileSystem(files);
      JsdocMain jsdm = new JsdocMain(fs, System.err);

      Writer buf = new StringWriter();
      if (jsdm.run(Collections.<String>emptyList(), pathList(inputs), buf)) {
        Writer out = new OutputStreamWriter(
            new FileOutputStream(output), "UTF-8");
        try {
          out.write("var jsonDocRoot = ");
          out.write(buf.toString());
          out.write(";");
        } finally {
          out.close();
        }
      }
    }
  }
  private static List<String> pathList(List<File> files) throws IOException {
    List<String> paths = new ArrayList<String>();
    for (File file : files) {
      paths.add(file.getCanonicalPath());
    }
    return paths;
  }

  boolean run(List<String> initPaths, List<String> srcPaths, Appendable out) {
    try {
      addInitPaths(initPaths);
      classifyFiles(srcPaths);
      if (mq.hasMessageAtLevel(MessageLevel.FATAL_ERROR)) { return false; }
      parseInputs();
      if (mq.hasMessageAtLevel(MessageLevel.FATAL_ERROR)) { return false; }
      try {
        Jsdoc jsd = new Jsdoc(handlers, mc, mq);
        for (Pair<String, String> initFile : initFiles) {
          jsd.addInitFile(initFile.a, initFile.b);
        }
        for (ParseTreeNode source : sources) { jsd.addSource(source); }
        for (Pair<InputSource, Comment> pkg : getPackageDocs()) {
          jsd.addPackage(pkg.a, pkg.b);
        }
        if (mq.hasMessageAtLevel(MessageLevel.FATAL_ERROR)) { return false; }
        ObjectConstructor docs = jsd.extract();
        if (!mq.hasMessageAtLevel(MessageLevel.FATAL_ERROR)) {
          render(docs, out);
        }
      } catch (JsdocException ex) {
        ex.toMessageQueue(mq);
      }
      return !mq.hasMessageAtLevel(MessageLevel.ERROR);
    } finally {
      reportMessages();
    }
  }

  /**
   * Build the list of files that run before we take the first snapshot.
   * These JavaScript files set up the Rhino environment so that the code
   * to doc will run properly.
   */
  private void addInitPaths(List<String> paths) {
    for (String path : paths) {
      try {
        String f = fs.canonicalPath(path);
        if (!fs.exists(f)) {
          mq.addMessage(MessageType.NO_SUCH_FILE, fs.toInputSource(f));
          continue;
        }
        initFiles.add(Pair.pair(f, drain(fs.read(f))));
      } catch (IOException ex) {
        mq.addMessage(
            MessageType.IO_ERROR, MessagePart.Factory.valueOf(path));
      }
    }
  }

  /** Build the list of source files and packages from inputs. */
  private void classifyFiles(List<String> paths) {
    for (String path : paths) {
      try {
        String f = fs.canonicalPath(path);
        if (!fs.exists(f)) {
          mq.addMessage(MessageType.NO_SUCH_FILE, fs.toInputSource(f));
          continue;
        }
        if (fs.isFile(f)) {
          packages.add(fs.dirname(f));
          if (!"package.html".equals(fs.basename(f))) {
            sourcePaths.add(f);
          }
        } else if (fs.isDirectory(f)) {
          packages.add(f);
        }
      } catch (IOException ex) {
        mq.addMessage(
            MessageType.IO_ERROR, MessagePart.Factory.valueOf(path));
      }
    }
  }

  /** Parse input source files. */
  private void parseInputs() {
    for (String path : sourcePaths) {
      InputSource is = fs.toInputSource(path);
      mc.inputSources.add(is);
      try {
        CharProducer cp = fs.read(path);
        try {
          JsLexer lexer = new JsLexer(cp, false);
          JsTokenQueue tq = new JsTokenQueue(lexer, is);
          Parser p = new Parser(tq, mq);
          sources.add(p.parse());
        } finally {
          cp.close();
        }
      } catch (IOException ex) {
        mq.addMessage(MessageType.IO_ERROR, is);
      } catch (ParseException ex) {
        ex.toMessageQueue(mq);
      }
    }
  }

  /**
   * Store package documentation from <tt>package.html</tt> in the JSON output.
   */
  private List<Pair<InputSource, Comment>> getPackageDocs() {
    List<Pair<InputSource, Comment>> pkgs
        = new ArrayList<Pair<InputSource, Comment>>();
    for (String packagePath : packages) {
      try {
        String packageFile = fs.join(packagePath, "package.html");
        if (!fs.exists(packageFile)) { continue; }
        CharProducer cp = fs.read(packageFile);
        try {
          Comment cmt = CommentParser.parseStructuredComment(cp);
          pkgs.add(Pair.pair(fs.toInputSource(packagePath), cmt));
        } catch (ParseException ex) {
          ex.toMessageQueue(mq);
          continue;
        } finally {
          cp.close();
        }
      } catch (IOException ex) {
        mq.addMessage(MessageType.IO_ERROR, fs.toInputSource(packagePath));
      }
    }
    return pkgs;
  }

  private void reportMessages() {
    // Report the most serious problems first.
    MessageLevel maxMessageLevel = MessageLevel.SUMMARY;
    for (Message msg : mq.getMessages()) {
      MessageLevel ml = msg.getMessageLevel();
      if (ml.compareTo(maxMessageLevel) > 0) { maxMessageLevel = ml; }
    }
    for (Message msg : mq.getMessages()) {
      if (msg.getMessageLevel() != maxMessageLevel) { continue; }
      try {
        errStream.append(msg.getMessageLevel().name()).append(' ');
        msg.format(mc, errStream);
        errStream.append('\n');
      } catch (IOException ex) {
        // Can't recover from IOExceptions on errStream
        ex.printStackTrace();
      }
    }
  }

  private void render(final ParseTreeNode node, Appendable out) {
    TokenConsumer tc = node.makeRenderer(out, new Callback<IOException>() {
      public void handle(IOException ex) {
        mq.addMessage(MessageType.IO_ERROR, node.getFilePosition());
      }
    });
    node.render(new RenderContext(mc, tc));
    tc.noMoreTokens();
  }

  private static String drain(CharProducer cp) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int ch; (ch = cp.read()) >= 0;) {
      sb.append((char) ch);
    }
    return sb.toString();
  }

  /** Abstracts away the file system. */
  public static interface FileSystem {
    String basename(String path);
    String canonicalPath(String path) throws IOException;
    String dirname(String path);
    boolean exists(String path);
    boolean isFile(String path);
    boolean isDirectory(String path);
    String join(String dir, String filename);
    CharProducer read(String path) throws IOException;
    Iterable<String> split(String path);
    InputSource toInputSource(String path);
  }

  public static class RealFileSystem implements FileSystem {
    public String basename(String path) {
      return new File(path).getName();
    }

    public String canonicalPath(String path) throws IOException {
      return new File(path).getCanonicalPath();
    }

    public String dirname(String path) {
      return new File(path).getParent();
    }

    public boolean exists(String path) {
      return new File(path).exists();
    }

    public boolean isDirectory(String path) {
      return new File(path).isDirectory();
    }

    public boolean isFile(String path) {
      return new File(path).isFile();
    }

    public String join(String dir, String filename) {
      return new File(dir, filename).getPath();
    }

    public CharProducer read(String path) throws IOException {
      return CharProducer.Factory.create(
          new InputStreamReader(new FileInputStream(path), "UTF-8"),
          toInputSource(path));
    }

    public InputSource toInputSource(String path) {
      return new InputSource(new File(path).toURI());
    }

    public Iterable<String> split(String path) {
      while (path.startsWith(File.separator)) {
        path = path.substring(File.separator.length());
      }
      while (path.endsWith(File.separator)) {
        path = path.substring(0, path.length() - File.separator.length());
      }
      if ("".equals(path)) { return Collections.emptyList(); }
      return Arrays.asList(
          path.split("(:" + Pattern.quote(File.separator) + ")+"));
    }
  }

  public static class RestrictedFileSystem implements FileSystem {
    private final Set<String> files = new HashSet<String>();

    private RestrictedFileSystem(Collection<String> files) {
      this.files.addAll(files);
    }

    public String basename(String path) {
      return new File(path).getName();
    }

    public String canonicalPath(String path) throws IOException {
      String canonPath = new File(path).getCanonicalPath();
      allowedFile(canonPath);
      return canonPath;
    }

    public String dirname(String path) {
      return new File(path).getParent();
    }

    public boolean exists(String path) {
      return files.contains(path) && new File(path).exists();
    }

    public boolean isDirectory(String path) {
      return files.contains(path) && new File(path).isDirectory();
    }

    public boolean isFile(String path) {
      return files.contains(path) && new File(path).isFile();
    }

    public String join(String dir, String filename) {
      return new File(dir, filename).getPath();
    }

    public CharProducer read(String path) throws IOException {
      File f = allowedFile(path);
      return CharProducer.Factory.create(
          new InputStreamReader(new FileInputStream(f), "UTF-8"),
          toInputSource(path));
    }

    public InputSource toInputSource(String path) {
      return new InputSource(new File(path).toURI());
    }

    public Iterable<String> split(String path) {
      while (path.startsWith(File.separator)) {
        path = path.substring(File.separator.length());
      }
      while (path.endsWith(File.separator)) {
        path = path.substring(0, path.length() - File.separator.length());
      }
      if ("".equals(path)) { return Collections.emptyList(); }
      return Arrays.asList(
          path.split("(:" + Pattern.quote(File.separator) + ")+"));
    }

    private File allowedFile(String path) throws IOException {
      if (!files.contains(path)) { throw new FileNotFoundException(path); }
      return new File(path);
    }
  }
}
