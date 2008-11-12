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

package com.google.caja.demos.benchmarks;

import com.google.caja.parser.AncestorChain;
import com.google.caja.plugin.PluginCompiler;
import com.google.caja.plugin.PluginMeta;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.Pair;
import com.google.caja.util.RhinoTestBed;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

/**
 * Unit test which executes the V8 benchmark and collates the result for 
 * rendering with varz
 */
public class BenchmarkRunner extends CajaTestCase {
  public void testRichards() throws Exception { measureBenchmark("richards.js"); }
  public void testDeltaBlue() throws Exception { measureBenchmark("deltablue.js"); }
  public void testCrypto() throws Exception { measureBenchmark("crypto.js"); }
  public void testRayTrace() throws Exception { measureBenchmark("raytrace.js"); }
  public void testEarleyBoyer() throws Exception {
    runBenchmark("earley-boyer.js");
  }

  /**
   * Measures the size and performance characteristics of a given {@code jsFile}
   * @param jsFile  name of javascript file in benchmark
   */
  private void measureBenchmark(String jsFile) throws Exception {
    runBenchmark(jsFile);
    sizeBenchmark(jsFile);
  }

  
  
  /**
   * Computes the pre- and post- cajoled size of the given benchmark
   * Accumulates the result and formats it for consumption by varz
   * Format:
   * VarZ:benchmark.(benchmark name).(size).(language).(debug?).(compression)
   */
  private void sizeBenchmark(String filename) throws Exception {
    String uncajoled = uncajoledFile(filename);
    String cajoled = cajoledFile(filename);
    System.out.println("VarZ:benchmark." + getName() + 
        ".size.uncajoled.nodebug.plain=" + plainSize(uncajoled));
    System.out.println("VarZ:benchmark." + getName() + 
        ".size.uncajoled.nodebug.gzip=" + gzipSize(uncajoled));
    System.out.println("VarZ:benchmark." + getName() + 
        ".size.valija.nodebug.plain=" + plainSize(cajoled));
    System.out.println("VarZ:benchmark." + getName() + 
        ".size.valija.nodebug.gzip=" + gzipSize(cajoled));
  }

  private static int plainSize(String contents) throws UnsupportedEncodingException {
    return contents.getBytes("UTF-8").length;
  }

  private static int gzipSize(String contents) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    GZIPOutputStream out = new GZIPOutputStream(result);
    out.write(contents.getBytes());
    out.flush();
    return result.toByteArray().length;
  }
  
  private String uncajoledFile(String filename) throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(filename), "UTF-8"));
    StringBuffer buf = new StringBuffer();
    String line;
    while (null != (line = reader.readLine())) { buf.append(line); }
    return buf.toString();
  }

  private String cajoledFile(String filename) throws Exception {
    PluginMeta meta = new PluginMeta();
    meta.setValijaMode(true);
    PluginCompiler pc = new PluginCompiler(meta, mq);
    pc.addInput(AncestorChain.instance(js(fromResource(filename))));
    assertTrue(pc.run());
    return render(pc.getJavascript());
  }

  /**
   * Runs the given benchmark
   * Accumulates the result and formats it for consumption by varz
   * Format:
   * VarZ:benchmark.<benchmark name>.<speed|size>.<language>.<debug?>.<engine>.<primed?>
   */
  private void runBenchmark(String filename) throws Exception {
    Pair<Double,Long> scoreUncajoled = runUncajoled(filename);
    Pair<Double,Long> scoreCajoled = runCajoled(filename);
    System.out.println("VarZ:benchmark." + getName() 
        + ".speed.uncajoled.nodebug.rhino.cold=" + scoreUncajoled.a);
    System.out.println("VarZ:benchmark." + getName() 
        + ".speed.valija.nodebug.rhino.cold=" + scoreCajoled.a);
    System.out.println("VarZ:benchmark." + getName() 
        + ".speeddiff.valija.nodebug.rhino.cold=" + (scoreCajoled.a / scoreUncajoled.a));
    System.out.println("VarZ:benchmark." + getName() 
        + ".memory.uncajoled.nodebug.rhino.cold=" + scoreUncajoled.b);
    System.out.println("VarZ:benchmark." + getName() 
        + ".memory.valija.nodebug.rhino.cold=" + scoreCajoled.b);
    System.out.println("VarZ:benchmark." + getName() 
        + ".memorydiff.valija.nodebug.rhino.cold=" + (scoreCajoled.b / scoreUncajoled.b));
  }

  // Like run.js but outputs the result differently.
  // Cannot use ___.getNewModuleHandler.getLastValue() to get the result since
  // there is no ModuleEnvelope until We fix the compilation unit problem.
  // Instead, we attach the result to an outer object called benchmark.
  private static final String RUN_SCRIPT = (
      ""
      + "BenchmarkSuite.RunSuites({\n"
      + "      NotifyResult: function (n, r) {\n"
      + "        benchmark.name = n;\n"
      + "        benchmark.result = r;\n"
      + "      },\n"
      + "      NotifyScore: function (s) { benchmark.score = s; }\n"
      + "    });"
      );

  private Pair<Double,Long> runUncajoled(String filename) throws Exception {
    runGC();
    long memBefore = Runtime.getRuntime().freeMemory();
    Number score = (Number) RhinoTestBed.runJs(
        new RhinoTestBed.Input("var benchmark = {};", "setup"),
        new RhinoTestBed.Input(getClass(), "base.js"),
        new RhinoTestBed.Input(getClass(), filename),
        new RhinoTestBed.Input(RUN_SCRIPT, getName()),
        new RhinoTestBed.Input("benchmark.score", "score"));
    runGC();
    long memAfter = Runtime.getRuntime().freeMemory();
    return new Pair<Double, Long>(score.doubleValue(),memAfter-memBefore);
  }

  private Pair<Double, Long> runCajoled(String filename) throws Exception {
    PluginMeta meta = new PluginMeta();
    meta.setValijaMode(true);
    PluginCompiler pc = new PluginCompiler(meta, mq);
    pc.addInput(AncestorChain.instance(js(fromResource("base.js"))));
    pc.addInput(AncestorChain.instance(js(fromResource(filename))));
    pc.addInput(AncestorChain.instance(js(fromString(RUN_SCRIPT))));
    assertTrue(pc.run());
    String cajoledJs = render(pc.getJavascript());
    runGC();
    long memBefore = Runtime.getRuntime().freeMemory();
    Number score = (Number) RhinoTestBed.runJs(
        new RhinoTestBed.Input(getClass(), "../../cajita.js"),
        new RhinoTestBed.Input(
            ""
            + "var testImports = ___.copy(___.sharedImports);\n"
            + "testImports.loader = ___.freeze({\n"
            + "        provide: ___.simpleFrozenFunc(\n"
            + "            function(v){ valijaMaker = v; })\n"
            + "    });\n"
            + "testImports.outers = ___.copy(___.sharedImports);\n"
            + "___.getNewModuleHandler().setImports(testImports);",
            getName() + "valija-setup"),
        new RhinoTestBed.Input(getClass(), "../../plugin/valija.co.js"),
        new RhinoTestBed.Input(
            // Set up the imports environment.
            ""
            + "testImports = ___.copy(___.sharedImports);\n"
            + "testImports.benchmark = {};\n"
            + "testImports.$v = ___.asSimpleFunc(valijaMaker)(testImports);\n"
            + "___.getNewModuleHandler().setImports(testImports);",
            "benchmark-container"),
        new RhinoTestBed.Input(cajoledJs, getName()),
	new RhinoTestBed.Input(
            "testImports.benchmark.score",
            "score"));
    long memAfter = Runtime.getRuntime().freeMemory();    
    return new Pair<Double, Long>(score.doubleValue(),memAfter-memBefore);
  }
  
  /**
   * Try to flush the garbage collector
   */
  private void runGC() {
    System.gc();
    System.gc();
    System.gc();
  }
}
