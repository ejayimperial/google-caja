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

import com.google.caja.lexer.FilePosition;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.quasiliteral.CajitaRewriter;
import com.google.caja.parser.html.DomTree;
import com.google.caja.parser.js.Block;
import com.google.caja.render.JsPrettyPrinter;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageType;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.RhinoTestBed;
import com.google.caja.util.TestUtil;
import com.google.caja.util.CajaTestCase;

import junit.framework.AssertionFailedError;

/**
 * End-to-end tests that compile a gadget to javascript and run the
 * javascript under Rhino to test them.
 *
 * @author stay@google.com (Mike Stay)
 */
public class HtmlCompiledPluginTest extends CajaTestCase {

  @Override
  protected void setUp() throws Exception {
    TestUtil.enableContentUrls();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  // TODO(metaweta): Move as many of these as possible to
  // CajitaRewriterTest using assertConsistent and the rest to
  // DebuggingSymbolsStageTest
  public void testEmptyGadget() throws Exception {
    execGadget("", "");
  }

  public void testTestingFramework() throws Exception {
    try {
      // Make sure our JSUnit failures escape the try blocks that allow
      // execution to continue into subsequent script blocks.
      execGadget("<script>fail('hiya');</script>", "");
    } catch (AssertionFailedError ex) {
      String message = ex.getMessage();
      String shortMessage = message.substring(
          message.indexOf(": ") + 2, message.indexOf("\n"));
      assertEquals("hiya", shortMessage);
      return;
    }
    fail("Expected failure");
  }

  public void testVariableRefInHandlerFunction() throws Exception {
    execGadget(
        "  <script type='text/javascript'>"
        + "var foo;"
        + "</script>"
        + "<a onclick='foo + bar;'>foo</a>",
        "");
  }

  /**
   * Tests that the container can get access to
   * "virtual globals" defined in cajoled code.
   */
  public void testWrapperAccess() throws Exception {
    // TODO(ihab.awad): SECURITY: Re-enable by reading (say) x.foo, and
    // defining the property IMPORTS___.foo.
    if (false) {
    execGadget(
        "<script>x='test';</script>",
        "if (___.getNewModuleHandler().getImports().x != 'test') {" +
          "fail('Cannot see inside the wrapper');" +
        "}"
        );
    }
  }

  /**
   * Tests that Array.prototype cannot be modified.
   */
  public void testFrozenArray() throws Exception {
    execGadget(
        "<script>" +
        "var success = false;" +
        "try {" +
          "Array.prototype[4] = 'four';" +
        "} catch (e){" +
          "success = true;" +
        "}" +
        "if (!success) fail('Array not frozen.');" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that Object.prototype cannot be modified.
   */
  public void testFrozenObject() throws Exception {
    execGadget(
        "<script>" +
        "var success = false;" +
        "try {" +
          "Object.prototype.x = 'X';" +
        "} catch (e){" +
          "success = true;" +
        "}" +
        "if (!success) fail('Object not frozen.');" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that eval is uncallable.
   */
  public void testEval() throws Exception {
    execGadget(
        "<script>var success=false;" +
          "try{eval('1');}catch(e){success=true;}" +
          "if (!success)fail('Outer eval is accessible.')</script>",
        ""
        );
  }

  /**
   * Tests that cajoled code can't construct new Function objects.
   */
  public void testFunction() throws Exception {
    execGadget(
        "<script>var success=false;" +
          "try{var f=new Function('1');}catch(e){success=true;}" +
          "if (!success)fail('Function constructor is accessible.')</script>",
        ""
        );
  }

  /**
   * Tests that constructors are inaccessible.
   */
  public void testConstructor() throws Exception {
    try {
      execGadget(
          "<script>function x(){}; var F = x.constructor;</script>",
          ""
          );
    } catch (junit.framework.AssertionFailedError e) {
      // pass
    }
  }

  /**
   * Tests that arguments to functions are not mutable through the
   * arguments array.
   */
  public void testMutableArguments() throws Exception {
    execGadget(
        "<script>" +
        "function f(a) {" +
          "try{" +
            "arguments[0] = 1;" +
            "if (a) fail('Mutable arguments');" +
          "} catch (e) {" +
             // pass
          "}" +
        "}" +
        "f(0);" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that the caller attribute is unreadable.
   */
  public void testCaller() throws Exception {
    execGadget(
        "<script>" +
        "function f(x) {" +
          "if (arguments.caller || f.caller) fail('caller is accessible');" +
        "}" +
        "f(1);" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that the callee attribute is unreadable.
   */
  public void testCallee() throws Exception {
    execGadget(
        "<script>" +
        "function f(x) {" +
          "if (arguments.callee || f.callee) fail('caller is accessible');" +
        "}" +
        "f(1);" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that arguments are immutable from another function's scope.
   */
  public void testCrossScopeArguments() throws Exception {
    execGadget(
        "<script>" +
        "function f(a) {" +
          "g();" +
          "if (a) fail('Mutable cross scope arguments');" +
        "}\n" +
        "function g() {" +
          "if (f.arguments) " +
            "f.arguments[0] = 1;" +
        "}" +
        "f(0);" +
        "</script>",
        ""
        );
  }

  /**
   * Tests that exceptions are not visible outside of the catch block.
   */
  public void testCatch() throws Exception {
    try {
      execGadget(
          "<script>" +
          "var e = 0;" +
          "try{ throw 1; } catch (e) {}" +
          "if (e) fail('Exception visible out of proper scope');" +
          "</script>",
          ""
          );
      fail("Exception that masks var should not pass");
    } catch (AssertionFailedError e) {
      // pass
    }
  }

  /**
   * Tests that setTimeout is uncallable.
   */
  public void testSetTimeout() throws Exception {
    execGadget(
        "<script>var success=false;try{setTimeout('1',10);}" +
        "catch(e){success=true;}" +
        "if(!success)fail('setTimeout is accessible');</script>",
        ""
        );
  }

  /**
   * Tests that Object.watch is uncallable.
   */
  public void testObjectWatch() throws Exception {
    execGadget(
        "<script>var x={}; var success=false;" +
        "try{x.watch(y, function(){});}" +
        "catch(e){success=true;}" +
        "if(!success)fail('Object.watch is accessible');</script>",
        ""
        );
  }

  /**
   * Tests that unreadable global properties are not readable by way of
   * Object.toSource().
   */
  public void testToSource() throws Exception {
    execGadget(
        "<script>var x;" +
        "try{x=toSource();}catch(e){}" +
        "if(x) fail('Global write-only values are readable.');</script>",
        ""
        );
  }

  /**
   * Empty styles should not cause parse failure.
   * <a href="http://code.google.com/p/google-caja/issues/detail?id=56">bug</a>
   */
  public void testEmptyStyle() throws Exception {
    execGadget("<style> </style>", "");
  }

  /**
   * Handlers should have their handlers rewritten.
   */
  public void testHandlerRewriting() throws Exception {
    execGadget(
        "<a onclick=\"foo(this)\">hi</a>",

        "assertEquals('<a onclick=\"return plugin_dispatchEvent___(" +
        "this, event || window.event, 0, \\'c_1___\\')\">hi</a>'," +
        " document.getElementById('test-test').innerHTML)"
        );
  }

  public void testECMAScript31Scoping() throws Exception {
    // TODO(stay): Once they decide on scoping & initialization rules, test
    // them here.
  }

  public void testExceptionsInScriptBlocks() throws Exception {
    execGadget(
        "<script>var a = 0, b = 0;</script>" +
        "<script>throw new Error(); a = 1;</script>" +
        "<script>b = 1;</script>\n" +
        "<script>\n" +
        "  assertEquals(0, a);" +
        "  assertEquals(1, b);" +
        "</script>",

        "");
  }

  public void testCustomOnErrorHandler() throws Exception {
    // TODO(ihab.awad): onerror handling is broken. The exception handling code
    // generated by our HTML compiler does not work with Valija at the moment.
    if (false) {
    execGadget(
        "<script>\n" +
        "  var a = 0, b = 0, messages = [];\n" +
        "  function onerror(message, source, lineNumber) {\n" +
        "    messages.push(source + ':' + lineNumber + ': ' + message);\n" +
        "  }\n" +
        "</script>\n" +
        "<script>throw new Error('panic'); a = 1;</script>\n" +        // line 7
        "<script>b = 1;</script>\n" +
        "<script>\n" +
        "  assertEquals(0, a);\n" +
        "  assertEquals(1, b);\n" +
        "  assertEquals(1, messages.length);\n" +
        "  assertEquals('testCustomOnErrorHandler:7: panic', messages[0]);\n" +
        "</script>",

        "");
    }
  }

  public void testPartialScript() throws Exception {
    PluginMeta meta = new PluginMeta();
    PluginCompiler compiler = new PluginCompiler(meta, mq);
    compiler.setMessageContext(mc);
    DomTree html = htmlFragment(fromString("<script>{</script>"));
    compiler.addInput(new AncestorChain<DomTree>(html));

    boolean passed = compiler.run();
    assertFalse(passed);

    assertMessage(
        MessageType.END_OF_FILE, MessageLevel.ERROR,
        FilePosition.instance(is, 1, 1, 9, 9, 1, 1, 10, 10));
  }

  private void execGadget(String gadgetSpec, String tests) throws Exception {
    PluginMeta meta = new PluginMeta();
    meta.setValijaMode(true);
    PluginCompiler compiler = new PluginCompiler(meta, mq);
    compiler.setMessageContext(mc);
    DomTree html = htmlFragment(fromString(gadgetSpec));
    if (html != null) { compiler.addInput(new AncestorChain<DomTree>(html)); }

    boolean failed = !compiler.run();

    if (failed) {
      fail();
    } else {
      Block jsTree = compiler.getJavascript();
      StringBuilder js = new StringBuilder();
      JsPrettyPrinter pp = new JsPrettyPrinter(js, null);
      RenderContext rc = new RenderContext(mc, pp);
      jsTree.render(rc);
      System.out.println("Compiled gadget: " + js);

      ParseTreeNode valijaOrigNode =
          js(fromResource("/com/google/caja/valija-cajita.js"));
      ParseTreeNode valijaCajoledNode =
          new CajitaRewriter(false).expand(valijaOrigNode, mq);
      String valijaCajoled = render(valijaCajoledNode);

      String htmlStubUrl = TestUtil.makeContentUrl(
          "<html><head/><body><div id=\"test-test\"/></body></html>");

      RhinoTestBed.Input[] inputs = new RhinoTestBed.Input[] {
          // Browser Stubs
          new RhinoTestBed.Input(getClass(), "/js/jqueryjs/runtest/env.js"),
          // Console Stubs
          new RhinoTestBed.Input(getClass(), "console-stubs.js"),
          // Initialize the DOM
          new RhinoTestBed.Input(
              // Document not defined until window.location set.
              "location = '" + htmlStubUrl + "';\n",
              "dom"),
          // Make the assertTrue, etc. functions available to javascript
          new RhinoTestBed.Input(getClass(), "asserts.js"),
          // Plugin Framework
          new RhinoTestBed.Input(getClass(), "../cajita.js"),
          new RhinoTestBed.Input(
              "___.setLogFunc(function(str, opt_stop) { console.log(str); });",
              "setLogFunc-setup"),
          new RhinoTestBed.Input(
              "var valijaMaker = {};\n" +
              "var testImports = ___.copy(___.sharedImports);\n" +
              "testImports.loader = {provide:___.simpleFunc(function(v){valijaMaker=v;})};\n" +
              "testImports.outers = ___.copy(___.sharedImports);\n" +
              "___.getNewModuleHandler().setImports(testImports);",
              getName() + "valija-setup"),
          new RhinoTestBed.Input(
              "___.loadModule(function (___, IMPORTS___) {" + valijaCajoled + "\n});",
              "valija-cajoled"),
          new RhinoTestBed.Input(getClass(), "html-emitter.js"),
          new RhinoTestBed.Input(getClass(), "container.js"),
          // The gadget
          new RhinoTestBed.Input(js.toString(), "gadget"),
          // The tests
          new RhinoTestBed.Input(tests, "tests"),
        };
      RhinoTestBed.runJs(inputs);
    }
  }
}
