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

package com.google.caja.plugin.stages;

import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;

public class OptimizeJavascriptStageTest extends PipelineStageTestCase {
  public void testEmptyInput() throws Exception {
    assertPipeline(
        job(";", Job.JobType.JAVASCRIPT),
        job("{;\n}", Job.JobType.JAVASCRIPT));
  }

  public void testLoopCounter() throws Exception {
    assertPipeline(
        job(
            ""
            + "for (var i = 0; i < (arr.length_canRead___"
            + "                   ? arr.length"
            + "                   : ___.readPub(arr, 'length')); ++i) {"
            + "  f(___.readPub(arr, i));"
            + "}",
            Job.JobType.JAVASCRIPT),
        job(norm("for (var i = 0; i < arr.length; ++i) { f(arr[+i]); }"),
            Job.JobType.JAVASCRIPT));
  }

  public void testNotOptimizedInDebugMode() throws Exception {
    getMeta().setDebugMode(true);
    String js = (
        ""
        + "for (var i = 0; i < (arr.length_canRead___"
        + "                   ? arr.length"
        + "                   : ___.readPub(arr, 'length')); ++i) {"
        + "  f(___.readPub(arr, i));"
        + "}");
    assertPipeline(
        job(js, Job.JobType.JAVASCRIPT),
        job(norm(js), Job.JobType.JAVASCRIPT));
  }

  @Override
  protected boolean runPipeline(Jobs jobs) {
    return new OptimizeJavascriptStage().apply(jobs);
  }

  private String norm(String js) throws Exception {
    return render(js(fromString(js)));
  }
}
