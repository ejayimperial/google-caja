// Copyright 2008 Google Inc. All Rights Reserved.
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

package com.google.caja.plugin.stages;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.js.Block;
import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;
import com.google.caja.plugin.PluginMeta;
import com.google.caja.reporting.Message;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.Pipeline;
import com.google.caja.util.RhinoTestBed;

/**
 * @author msamuel@google.com (Mike Samuel)
 */
public class DebuggingSymbolsStageTest extends CajaTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testDereferenceNull() throws Exception {
    assertStackTrace(
        "var x = null;\n"
        + "var xDotFoo = (x).foo;",
        //                   ^^^ 2+19-22

        "testDereferenceNull:2+19 - 22");
  }

  public void testCallOnNullObject() throws Exception {
    assertStackTrace(
        "{\n"
        + "  function f(x) { return x.foo(); }\n"
        //                            ^^^ 2+28-31
        + "  function g() { return f(null); }\n"
        //                         ^ 3+25-26
        + "\n"
        + "  g();\n"
        //   ^ 5+3-4
        + "}",

        "testCallOnNullObject:5+3 - 4\n"
        + "testCallOnNullObject:3+25 - 26\n"
        + "testCallOnNullObject:2+28 - 31");
  }

  public void testCallUndefinedMethod() throws Exception {
    assertStackTrace(
        "{\n"
        + "  function f(x) { return x.noSuchMethod(); }\n"
        //                            ^^^^^^^^^^^^ 2+28-40
        + "  function g() { return f(new Date); }\n"
        //                         ^ 3+25-26
        + "\n"
        + "  g();\n"
        //   ^ 5+3-4
        + "}",

        "testCallUndefinedMethod:5+3 - 4\n"
        + "testCallUndefinedMethod:3+25 - 26\n"
        + "testCallUndefinedMethod:2+28 - 40");
  }

  public void testInaccessibleProperty() throws Exception {
    assertStackTrace(
        "{\n"
        + "  function f(x, k) { return x['foo_'] = 0; }\n"
        //                             ^^^^^^^^^^^^^ 2+29-42
        + "  f(new Date);\n"
        //   ^ 3+3-4
        + "}",

        "testInaccessibleProperty:3+3 - 4\n"
        + "testInaccessibleProperty:2+29 - 42");
  }

  public void testSetOfNullObject() throws Exception {
    assertStackTrace(
        "(null).x = 0;",
        //      ^ 1+8-9
        "testSetOfNullObject:1+8 - 9");
  }

  public void testDeleteOfNullObject() throws Exception {
    assertStackTrace(
        "delete (null).x;",
        //       ^^^^^^^ 1+9-16
        "testDeleteOfNullObject:1+9 - 16");
  }

  public void testMethodCalling() throws Exception {
    assertStackTrace(
        "{\n"
        + "function f(x) { return x.foo; }\n"
        //                          ^^^ 2+26-29
        + "\n"
        + "function Clazz() { this; }\n"
        + "caja.def(\n"
        + "    Clazz, Object,\n"
        + "    { method: function () { return f(this.foo_); } });\n"
        //                                    ^ 7+36-37
        + "\n"
        + "(new Clazz).method(null);\n"
        //             ^^^^^^ 9+13-19
        + "}",

        "testMethodCalling:9+13 - 19\n"
        + "testMethodCalling:7+36 - 37\n"
        + "testMethodCalling:2+26 - 29");
  }

  public void testEnumerateOfNull() throws Exception {
    assertStackTrace(
        "(function () {\n"
        + "  var myObj = null;\n"
        + "  for (var k in myObj) {\n"
        //                 ^^^^^ 3+17-22
        + "    ;\n"
        + "  }\n"
        + "})();",

        "testEnumerateOfNull:2+7 - 4+6\n"
        + "testEnumerateOfNull:3+17 - 22");
  }

  public void testPropertyInNull() throws Exception {
    assertStackTrace(
        "(function (x) {\n"
        + "  return 'k' in x;\n"
        //                 ^ 2+17-18
        + "})(null);",

        "testPropertyInNull:1+12 - 2+18\n"
        + "testPropertyInNull:2+17 - 18");
  }

  public void testIllegalAccessInsideHoistedFunction() throws Exception {
    assertStackTrace(
        "var x = true;\n"
        + "if (x) {\n"
        + "  var y = 5;\n"
        + "  function f() {\n"
        + "    var x = 'y___';\n"
        + "    this[x] = 1;\n"
        //          ^^^^^^
        + "  }\n"
        + "}\n"
        + "new f();",
        //     ^

        "testIllegalAccessInsideHoistedFunction:9+5 - 6\n"
        + "testIllegalAccessInsideHoistedFunction:6+10 - 16");
  }

  private void assertStackTrace(String js, String golden) throws Exception {
    Block block = js(fromString(js));
    System.err.println("\n\nblock\n=====\n" + block.toStringDeep(1));

    PluginMeta meta = new PluginMeta();
    meta.setDebugMode(true);

    Jobs jobs = new Jobs(mc, mq, meta);
    jobs.getJobs().add(new Job(AncestorChain.instance(block)));

    Pipeline<Jobs> pipeline = new Pipeline<Jobs>();
    pipeline.getStages().add(new ConsolidateCodeStage());
    pipeline.getStages().add(new ValidateJavascriptStage());
    pipeline.getStages().add(new InferFilePositionsStage());
    pipeline.getStages().add(new DebuggingSymbolsStage());
    if (!pipeline.apply(jobs)) {
      StringBuilder sb = new StringBuilder();
      for (Message msg : mq.getMessages()) {
        if (0 == sb.length()) { sb.append('\n'); }
        sb.append(msg.getMessageLevel()).append(": ");
        msg.format(mc, sb);
      }
      fail(sb.toString());
    }

    block = jobs.getJobs().get(0).getRoot().cast(Block.class).node;

    try {
      Object stack = RhinoTestBed.runJs(
          null,
          new RhinoTestBed.Input(getClass(), "/com/google/caja/caja.js"),
          new RhinoTestBed.Input(
              getClass(), "/com/google/caja/caja-debugmode.js"),
          new RhinoTestBed.Input(getClass(), "../console-stubs.js"),
          new RhinoTestBed.Input(
              "var stack = '<no-stack>';                              "
              + "try {                                                "
              +    render(block)
              + "} catch (e) {                                        "
              + "  stack = e.cajaStack___;                            "
              + "  if (!stack) { throw e; }                           "
              + "  stack = ___.unsealCallerStack(stack).join('\\n');  "
              + "}                                                    "
              + "stack                                                ",
              getName()));

      System.err.println("==========================");
      System.err.println(mc.inputSources);

      assertEquals(golden, stack);
    } catch (Exception ex) {
      System.err.println(render(block));
      throw ex;
    } catch (Error ex) {
      System.err.println(render(block));
      throw ex;
    }
  }
}
