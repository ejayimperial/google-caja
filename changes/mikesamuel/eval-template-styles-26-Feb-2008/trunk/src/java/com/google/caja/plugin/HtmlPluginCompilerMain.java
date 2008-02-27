// Copyright (C) 2007 Google Inc.
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

package com.google.caja.plugin;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.html.DomParser;
import com.google.caja.parser.html.DomTree;
import com.google.caja.parser.html.OpenElementStack;
import com.google.caja.plugin.stages.OpenTemplateStage;
import com.google.caja.plugin.stages.ConsolidateCodeStage;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.util.Pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.ListIterator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Executable that invokes {@link HtmlPluginCompiler}.
 *
 * @author ihab.awad@gmail.com
 */
public final class HtmlPluginCompilerMain {
  private static final Option INPUT =
      new Option("i", "input", true,
          "Input file path containing mixed HTML, JS and CSS");

  private static final Option OUTPUT_JS =
      new Option("j", "output_js", true,
          "Output file path for translated JS" +
          " (defaults to input with \".js\")");

  private static final Option OUTPUT_CSS =
      new Option("c", "output_css", true,
          "Output file path for translated CSS" +
          " (defaults to input with \".css\")");

  private static final Option CSS_PREFIX =
      new Option("p", "css_prefix", true,
          "Plugin CSS namespace prefix");

  private static final Options options = new Options();

  static {
    options.addOption(INPUT);
    options.addOption(OUTPUT_JS);
    options.addOption(OUTPUT_CSS);
    options.addOption(CSS_PREFIX);
  }

  private File inputFile = null;
  private File outputJsFile = null;
  private File outputCssFile = null;
  private String cssPrefix = null;

  private HtmlPluginCompilerMain() {}

  public static void main(String[] argv) {
    int result;
    try {
      result = new HtmlPluginCompilerMain().run(argv);
    } catch (Throwable th) {
      th.printStackTrace();
      result = -1;
    }
    if (result != 0) {
      System.exit(result);
    }
  }

  private int run(String[] argv) {
    MessageQueue mq = new SimpleMessageQueue();
    MessageContext mc = null;

    try {
      int rc = processArguments(argv);
      if (rc != 0) { return rc; }

      PluginEnvironment env = new FileSystemEnvironment(
          inputFile.getParentFile());

      HtmlPluginCompiler compiler = new HtmlPluginCompiler(
          mq, new PluginMeta(cssPrefix, env));

      mc = compiler.getMessageContext();

      for (ListIterator<Pipeline.Stage<Jobs>> it
               = compiler.getCompilationPipeline().getStages().listIterator();
           it.hasNext();) {
        Pipeline.Stage<Jobs> stage = it.next();
        if (stage instanceof ConsolidateCodeStage) {
          it.add(new OpenTemplateStage());
          break;
        }
      }
      try {
        compiler.addInput(new AncestorChain<DomTree.Fragment>(
            parseHtmlFromFile(inputFile, mq)));

        if (!compiler.run()) {
          throw new RuntimeException();
        }
      } catch (ParseException e) {
        e.toMessageQueue(compiler.getMessageQueue());
        return -1;
      } catch (IOException ex) {
        System.err.println(ex);
        return -1;
      }

      writeFile(outputJsFile, compiler.getJavascript());
      writeFile(outputCssFile, compiler.getCss());

      return 0;
    } catch (Throwable th) {
      th.printStackTrace();
      return -1;
    } finally {
      if (mc == null) { mc = new MessageContext(); }
      try {
        for (Message m : mq.getMessages()) {
          System.err.print(m.getMessageLevel().name() + ": ");
          m.format(mc, System.err);
          System.err.println();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private int processArguments(String[] argv) {
    CommandLine cl;
    try {
      cl = new BasicParser().parse(options, argv);
    } catch (org.apache.commons.cli.ParseException e) {
      throw new RuntimeException(e);
    }

    if (cl.getOptionValue(INPUT.getOpt()) == null)
      return usage("Option \"" + INPUT.getLongOpt() + "\" missing");
    inputFile = new File(cl.getOptionValue(INPUT.getOpt()));
    if (!inputFile.exists()) {
      return usage("File \"" + inputFile + "\" does not exist");
    }
    if (!inputFile.isFile()) {
      return usage("File \"" + inputFile + "\" is not a regular file");
    }

    outputJsFile =
        cl.getOptionValue(OUTPUT_JS.getOpt()) == null ?
            substituteExtension(inputFile, "js") :
            new File(cl.getOptionValue(OUTPUT_JS.getOpt()));

    outputCssFile =
        cl.getOptionValue(OUTPUT_CSS.getOpt()) == null ?
            substituteExtension(inputFile, "css") :
            new File(cl.getOptionValue(OUTPUT_CSS.getOpt()));

    cssPrefix = cl.getOptionValue(CSS_PREFIX.getOpt());
    if (cssPrefix == null) {
      return usage("Option \"" + CSS_PREFIX.getLongOpt() + "\" missing");
    }

    return 0;
  }

  private int usage(String msg) {
    System.out.println(msg);
    new HelpFormatter().printHelp(getClass().getName(), options);
    return -1;
  }

  private DomTree.Fragment parseHtmlFromFile(File f, MessageQueue mq)
      throws IOException, ParseException {
    InputSource is = new InputSource(f.toURI());
    Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");
    try {
      return DomParser.parseFragment(
          DomParser.makeTokenQueue(is, in, false),
          OpenElementStack.Factory.createHtml5ElementStack(mq));
    } finally {
      in.close();
    }
  }

  private void writeFile(File path, ParseTreeNode contents) {
    Writer w;
    try {
      w = new BufferedWriter(new FileWriter(path, false));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (contents != null) {
      RenderContext rc = new RenderContext(new MessageContext(), w, true);
      try {
        contents.render(rc);
        w.write("\n");
      } catch (IOException e)  {
        throw new RuntimeException(e);
      }
    }

    try {
      w.flush();
      w.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private File substituteExtension(File originalPath, String extension) {
    String originalPathString = originalPath.getAbsolutePath();
    String basePath = originalPathString.indexOf('.') == -1 ?
        originalPathString :
        originalPathString.substring(0, originalPathString.lastIndexOf('.'));
    return new File(basePath + '.' + extension);
  }
}

final class FileSystemEnvironment implements PluginEnvironment {
  private final File directory;

  FileSystemEnvironment(File directory) {
    this.directory = directory;
  }
  
  public CharProducer loadExternalResource(
      ExternalReference ref, String mimeType) {
    File f = toFileUnderSameDirectory(ref.getUri());
    if (f == null) { return null; }
    try {
      return CharProducer.Factory.create(
          new InputStreamReader(new FileInputStream(f), "UTF-8"),
          new InputSource(f.toURI()));
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex);
    } catch (FileNotFoundException ex) {
      return null;
    }
  }

  public String rewriteUri(ExternalReference ref, String mimeType) {
    File f = toFileUnderSameDirectory(ref.getUri());
    if (f == null) { return null; }
    return f.toURI().relativize(directory.toURI()).toString();
  }

  private File toFileUnderSameDirectory(URI uri) {
    if (!uri.isAbsolute()
        && !uri.isOpaque()
        && uri.getScheme() == null
        && uri.getAuthority() == null
        && uri.getFragment() == null
        && uri.getPath() != null
        && uri.getQuery() == null
        && uri.getFragment() == null) {
      File f = new File(new File(directory, ".").toURI().resolve(uri));
      // Check that f is a descendant of directory
      for (File tmp = f; tmp != null; tmp = tmp.getParentFile()) {
        if (directory.equals(tmp)) {
          return f;
        }
      }
    }
    return null;
  }
}
