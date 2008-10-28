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

import java.util.ArrayList;
import java.util.List;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.quasiliteral.opt.ArrayIndexOptimization;
import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;
import com.google.caja.plugin.Job.JobType;
import com.google.caja.util.Pipeline.Stage;

public final class OptimizeJavascriptStage implements Stage<Jobs> {
  public boolean apply(Jobs jobs) {
    if (!jobs.getPluginMeta().isDebugMode()) {
      ArrayIndexOptimization op = new ArrayIndexOptimization();
      List<Job> optimizedJobs = new ArrayList<Job>();
      for (Job job : jobs.getJobsByType(JobType.JAVASCRIPT)) {
        Block optimized = (Block) op.optimize(
            job.getRoot().cast(Block.class).node);
        optimizedJobs.add(new Job(AncestorChain.instance(optimized)));
      }
      jobs.getJobs().removeAll(jobs.getJobsByType(JobType.JAVASCRIPT));
      jobs.getJobs().addAll(optimizedJobs);
    }
    return jobs.hasNoFatalErrors();
  }
}
