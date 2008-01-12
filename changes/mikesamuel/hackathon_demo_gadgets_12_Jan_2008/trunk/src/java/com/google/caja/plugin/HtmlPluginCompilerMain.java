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
import com.google.caja.plugin.stages.OpenTemplateStage;
import com.google.caja.plugin.stages.ConsolidateCodeStage;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
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

  private static final Option JS_NAME =
      new Option("n", "js_name", true,
          "Plugin JS variable name");

  private static final Option CSS_PREFIX =
      new Option("p", "css_prefix", true,
          "Plugin CSS namespace prefix");

  private static final Option ROOT_DIV_ID =
      new Option("r", "root_div_id", true,
          "ID of root <div> into which generated JS will inject content");

  private static final Options options = new Options();

  static {
    options.addOption(INPUT);
    options.addOption(OUTPUT_JS);
    options.addOption(OUTPUT_CSS);
    options.addOption(JS_NAME);
    options.addOption(CSS_PREFIX);
    options.addOption(ROOT_DIV_ID);
  }

  private File inputFile = null;
  private File outputJsFile = null;
  private File outputCssFile = null;
  private String jsName = null;
  private String cssPrefix = null;
  private String rootDivId = null;

  private HtmlPluginCompilerMain() {}

  public static void main(String[] argv) {
    System.exit(new HtmlPluginCompilerMain().run(argv));
  }

  private int run(String[] argv) {
    try {
      int rc = processArguments(argv);
      if (rc != 0) return rc;

      PluginEnvironment env = new FileSystemEnvironment(
          inputFile.getParentFile());
    
      HtmlPluginCompiler compiler =
          new HtmlPluginCompiler(
              new SimpleMessageQueue(),
              new PluginMeta(jsName, cssPrefix, "", rootDivId,
                             PluginMeta.TranslationScheme.CAJA,
                             env));
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
        compiler.addInput(
            new AncestorChain<DomTree.Fragment>(parseHtmlFromFile(inputFile)));

        if (!compiler.run()) {
          throw new RuntimeException();
        }
      } catch (ParseException e) {
        e.toMessageQueue(compiler.getMessageQueue());
        return -1;
      } catch (IOException ex) {
        System.err.println(ex);
        return -1;
      } finally {
        try {
          for (Message m : compiler.getMessageQueue().getMessages()) {
            System.err.print(m.getMessageLevel().name() + ": ");
            m.format(compiler.getMessageContext(), System.err);
            System.err.println();
          }
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }

      writeFile(outputJsFile, compiler.getJavascript());
      writeFile(outputCssFile, compiler.getCss());

      return 0;
    } catch (Throwable th) {
      th.printStackTrace();
      return -1;
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
    if (!inputFile.exists())
      return usage("File \"" + inputFile + "\" does not exist");
    if (!inputFile.isFile())
      return usage("File \"" + inputFile + "\" is not a regular file");

    outputJsFile =
        cl.getOptionValue(OUTPUT_JS.getOpt()) == null ?
            substituteExtension(inputFile, "js") :
            new File(cl.getOptionValue(OUTPUT_JS.getOpt()));

    outputCssFile =
        cl.getOptionValue(OUTPUT_CSS.getOpt()) == null ?
            substituteExtension(inputFile, "css") :
            new File(cl.getOptionValue(OUTPUT_CSS.getOpt()));

    jsName = cl.getOptionValue(JS_NAME.getOpt());
    if (jsName == null)
      return usage("Option \"" + JS_NAME.getLongOpt() + "\" missing");

    cssPrefix = cl.getOptionValue(CSS_PREFIX.getOpt());
    if (cssPrefix == null)
      return usage("Option \"" + CSS_PREFIX.getLongOpt() + "\" missing");

    rootDivId = cl.getOptionValue(ROOT_DIV_ID.getOpt());
    if (rootDivId == null)
      return usage("Option \"" + ROOT_DIV_ID.getLongOpt() + "\" missing");

    return 0;
  }

  private int usage(String msg) {
    System.out.println(msg);
    new HelpFormatter().printHelp(getClass().getName(), options);
    return -1;
  }

  private DomTree.Fragment parseHtmlFromFile(File f)
      throws IOException, ParseException {
    InputSource is = new InputSource(f.toURI());
    Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");
    try {
      return DomParser.parseFragment(DomParser.makeTokenQueue(is, in, false));
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
