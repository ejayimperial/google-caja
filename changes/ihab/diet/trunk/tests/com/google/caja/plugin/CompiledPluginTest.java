// Copyright 2007 Google Inc. All Rights Reserved
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
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.css.CssTree;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.RhinoTestBed;
import com.google.caja.util.TestUtil;
import junit.framework.AssertionFailedError;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * End-to-end tests that compiles a plugin to javascript and runs the
 * javascript under Rhino to test the base plugin implementation.
 *
 * <p>This also serves as a test of plugin-base.js</p>
 *
 * @author mikesamuel@gmail.com
 */
public class CompiledPluginTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    TestUtil.enableContentUrls();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testTestFramework() throws Exception {
    // Make sure that assertions in javascript can fail
    try {
      execPlugin("console.log(\"testing test framework.  'foo' != 'bar'\");\n" +
                 "assertEquals('foo', 'bar');");
      fail("javascript asserts are borked");
    } catch (AssertionFailedError e) {
      // pass
    } catch (Exception e) {
      fail("javascript asserts are borked: " + e.toString());
    }
  }

  /**
   * compiles a plugin, and runs it.  Since Rhino does not have a window
   * context, we fake one.
   */
  private void execPlugin(String tests, PluginFile... pluginFiles)
      throws IOException, ParseException {
    PluginMeta meta = new PluginMeta();
    MessageContext mc = new MessageContext();
    MessageQueue mq = new EchoingMessageQueue(
        new PrintWriter(new OutputStreamWriter(System.err)), mc);
    PluginCompiler pc = new PluginCompiler(meta, mq);

    pc.setMessageContext(mc);

    List<InputSource> srcs = new ArrayList<InputSource>();
    for (PluginFile pluginFile : pluginFiles) {
      InputSource is = new InputSource(URI.create(pluginFile.source));
      srcs.add(is);
      CharProducer cp = CharProducer.Factory.create(pluginFile.input, is);
      try {
        ParseTreeNode input = PluginCompilerMain.parseInput(is, cp, mq);
        pc.addInput(new AncestorChain<ParseTreeNode>(input));
      } finally {
        cp.close();
      }
    }

    boolean success = pc.run();
    if (!success) {
      StringBuilder sb = new StringBuilder();
      sb.append("Failed to compile plugin");
      for (Message msg : mq.getMessages()) {
        sb.append("\n");
        msg.format(mc, sb);
      }
      fail(sb.toString());
    }

    StringBuilder buf = new StringBuilder();
    for (ParseTreeNode output : pc.getOutputs()) {
      if (output instanceof CssTree) { continue; }
      RenderContext rc = new RenderContext(mc, output.makeRenderer(buf, null));
      output.render(rc);
      buf.append("\n\n");
    }

    String htmlStubUrl = TestUtil.makeContentUrl("<html><head/><body/></html>");

    String compiledPlugin = buf.toString();

    RhinoTestBed.Input[] inputs = new RhinoTestBed.Input[] {
        // Browser Stubs
        new RhinoTestBed.Input(getClass(), "/js/jqueryjs/runtest/env.js"),
        // Console Stubs
        new RhinoTestBed.Input(getClass(), "console-stubs.js"),
        // Plugin Framework
        new RhinoTestBed.Input(getClass(), "/com/google/caja/caja.js"),
        new RhinoTestBed.Input(getClass(), "unicode.js"),
        new RhinoTestBed.Input(getClass(), "html4-defs.js"),
        new RhinoTestBed.Input(getClass(), "css-defs.js"),
        new RhinoTestBed.Input(getClass(), "html-sanitizer.js"),
        new RhinoTestBed.Input(getClass(), "domita.js"),
        // Initialize the DOM
        new RhinoTestBed.Input(
            // Document not defined until window.location set.
            new StringReader("location = '" + htmlStubUrl + "';\n"),
            "dom"),
        // Make the assertTrue, etc. functions available to javascript
        new RhinoTestBed.Input(getClass(), "asserts.js"),
        new RhinoTestBed.Input(new StringReader(
            "var uriCallback = { rewrite: function (uri, mimeType) {\n"
            + "  return 'http://proxy/?uri=' + encodeURIComponent(uri);\n"
            + "} };\n"
            + "var testImports = ___.getNewModuleHandler().getImports();\n"
            + "testImports.exports = {};\n"
            + "attachDocumentStub('-post', uriCallback, testImports);\n"
            + "testImports.log = ___.simpleFrozenFunc(function(s) {\n"
            + "  console.log(s);\n"
            + "});"),
            "container"),
        // The Plugin
        new RhinoTestBed.Input(
            new StringReader(compiledPlugin), getName() + "-plugin.js"),
        // The tests
        new RhinoTestBed.Input(
            new StringReader(tests), getName() + "-tests.js"),
        };

    for (Message msg : mq.getMessages()) {
      buf.append('\n');
      buf.append(msg.getMessageType().toString()).append(" : ")
         .append(msg.getMessageParts().get(0));
    }

    System.err.println(buf.toString());

    RhinoTestBed.runJs(null, inputs);
  }

  private static class PluginFile {
    public final Reader input;
    public final String source;
    public PluginFile(String resource) throws IOException {
      this.source = resource;
      this.input = new InputStreamReader(
          RhinoTestBed.Input.class.getResourceAsStream(resource), "UTF-8");
    }
    /** @param source file path or url from which the javascript came. */
    public PluginFile(Reader input, String source) {
      this.input = input;
      this.source = source;
    }
  }
}
