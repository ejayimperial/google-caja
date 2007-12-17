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

import com.google.caja.lexer.ParseException;
import com.google.caja.util.RhinoTestBed;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;

/**
 * End-to-end tests that compile a gadget to javascript and run the
 * javascript under Rhino to test them.
 *
 * @author stay@google.com (Mike Stay)
 *
 */
public class HtmlCompiledPluginTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEmptyGadget() throws Exception {
    execGadget("", "");
  }

  public void testWrapperAccess() throws Exception {
    execGadget(
        "<script>x='test';</script>",
        "if (___.getNewModuleHandler().getOuters().x != 'test') {" +
          "fail('Cannot see inside the wrapper');" +
        "}"
        );
  }

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

  public void testEval() throws Exception {
    execGadget(
        "<script>x=eval;" + 
        "if(x)fail('Outer eval is accessible.')</script>",
        ""
        );
  }

  public void testObjectEval() throws Exception {
    execGadget(
        "<script>x=Object.eval;" +
        "if(x)fail('Object.eval is accessible.')</script>",
        ""
        );
  }

  public void testFunction() throws Exception {
    execGadget(
        "<script>x=Function;" +
        "if(x)fail('Outer eval is accessible.')</script>",
        ""
        );
  }

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

  public void testCatch() throws Exception {
    execGadget(
        "<script>" +
        "var e = 0;" +
        "try{ throw 1; } catch (e) {}" +
        "if (e) fail('Exception visible out of proper scope');" +
        "</script>",
        ""
        );
  }

  public void testVirtualGlobalThis() throws Exception {
    execGadget(
        "<script>x=this;</script>",
        ""
        );
  }
  
  public void testThisIsGlobalScope() throws Exception {
    execGadget(
        "<script>try{x=this;}catch(e){}</script>",
        "if (___.getNewModuleHandler().getOuters().x === this)" +
          "fail('Global scope is accessible');"
        );
  }

  public void testSetTimeout() throws Exception {
    execGadget(
        "<script>x=setTimeout;if(x)fail('setTimeout is accessible');</script>",
        ""
        );
  }

  public void testObjectWatch() throws Exception {
    execGadget(
        "<script>x={}; x=x.watch;" +
        "if(x)fail('Object.watch is accessible');</script>",
        ""
        );
  }

  public void testToSource() throws Exception {
    execGadget(
        "<script>try{x=toSource();}catch(e){}" +
        "if(x) fail('top level toSource is accessible');</script>",
        ""
    );
  }

  public void execGadget(String gadget_spec, String tests)
      throws IOException {
    HtmlPluginCompiler compiler = new HtmlPluginCompiler(
        gadget_spec, "test", "test", "test", true);
    boolean failed=false;
    try {
      if (!compiler.run()){
        failed = true;
      }
    } catch (ParseException e) {
      e.toMessageQueue(compiler.getMessageQueue());
      failed = true;
    }
    if (failed) {
      fail(compiler.getErrors());
    } else {
      String js = compiler.getOutputJs();
      System.out.println("Compiled gadget: " + js);
      RhinoTestBed.Input[] inputs = new RhinoTestBed.Input[] {
          // Make the assertTrue, etc. functions available to javascript
          new RhinoTestBed.Input(
              CompiledPluginTest.class,
              "browser-stubs.js"),
          new RhinoTestBed.Input(CompiledPluginTest.class, "asserts.js"),
          // Plugin Framework
          new RhinoTestBed.Input(CompiledPluginTest.class, "../caja.js"),
          new RhinoTestBed.Input(CompiledPluginTest.class, "container.js"),
//          new RhinoTestBed.Input(CompiledPluginTest.class, "html-sanitizer.js"),
          new RhinoTestBed.Input(
              new StringReader(
                  "var div = document.createElement('div');\n" +
                  "div.id = 'test-test';\n" +
                  "document.body.appendChild(div);\n"),
              "dom"),
          // The Gadget
          new RhinoTestBed.Input(
              new StringReader(js),
              "gadget"),
          // The tests
          new RhinoTestBed.Input(new StringReader(tests), "tests"),
        };
      RhinoTestBed.runJs(null, inputs);
    }
  }
}
