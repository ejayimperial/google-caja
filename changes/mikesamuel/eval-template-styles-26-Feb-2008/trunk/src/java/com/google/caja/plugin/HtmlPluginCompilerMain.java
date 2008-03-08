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
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.MessageType;
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

/**
 * Executable that invokes {@link HtmlPluginCompiler}.
 *
 * @author ihab.awad@gmail.com
 */
public final class HtmlPluginCompilerMain {
  final MessageQueue mq;

  private HtmlPluginCompilerMain() {
    mq = new SimpleMessageQueue();
  }

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
    Config config = new Config(getClass(), System.err,
                               "Cajoles an HTML file to JS and CSS.");
    if (!config.processArguments(argv)) {
      return -1;
    }

    MessageLevel maxMessageLevel;
    MessageContext mc = null;
    try {
      HtmlPluginCompiler compiler = new HtmlPluginCompiler(
          mq, new PluginMeta(config.getCssPrefix()));
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
        for (File input : config.getInputFiles()) {
          compiler.addInput(
              new AncestorChain<DomTree.Fragment>(
                  parseHtmlFromFile(input, mq)));
        }

        if (!compiler.run()) {
          throw new RuntimeException();
        }
      } catch (ParseException e) {
        e.toMessageQueue(compiler.getMessageQueue());
        return -1;
      } catch (IOException e) {
        mq.addMessage(MessageType.
            IO_ERROR, MessagePart.Factory.valueOf(e.toString()));
        return -1;
      }

      writeFile(config.getOutputJsFile(), compiler.getJavascript());
      writeFile(config.getOutputCssFile(), compiler.getCss());

    } finally {
      if (mc == null) { mc = new MessageContext(); }

      maxMessageLevel = dumpMessages(mq, mc, System.err);
    }

    return MessageLevel.ERROR.compareTo(maxMessageLevel) > 0 ? 0 : -1;
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
      mq.addMessage(MessageType.IO_ERROR,
                    MessagePart.Factory.valueOf(e.toString()));
    }
  }

  /**
   * Dumps messages to the given output stream, returning the highest message
   * level seen.
   */
  static MessageLevel dumpMessages(
      MessageQueue mq, MessageContext mc, Appendable out) {
    MessageLevel maxLevel = MessageLevel.values()[0];
    for (Message m : mq.getMessages()) {
      MessageLevel level = m.getMessageLevel();
      if (maxLevel.compareTo(level) < 0) { maxLevel = level; }
    }
    MessageLevel ignoreLevel = null;
    if (maxLevel.compareTo(MessageLevel.LINT) < 0) {
      // If there's only checkpoints, be quiet.
      ignoreLevel = MessageLevel.LOG;
    }
    try {
      for (Message m : mq.getMessages()) {
        MessageLevel level = m.getMessageLevel();
        if (ignoreLevel != null && level.compareTo(ignoreLevel) <= 0) {
          continue;
        }
        out.append(level.name() + ": ");
        m.format(mc, out);
        out.append("\n");

        if (maxLevel.compareTo(level) < 0) { maxLevel = level; }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return maxLevel;
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
