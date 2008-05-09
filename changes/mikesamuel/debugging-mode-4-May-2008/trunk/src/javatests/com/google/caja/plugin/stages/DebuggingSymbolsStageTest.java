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

  public void testCallNull() throws Exception {
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

        "testCallNull:5+3 - 4\n"
        + "testCallNull:3+25 - 26\n"
        + "testCallNull:2+28 - 31");
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
    assertTrue(pipeline.apply(jobs));

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
