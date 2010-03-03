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

package com.google.caja.plugin.stages;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.html.DomTree;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.FunctionDeclaration;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.Statement;
import com.google.caja.plugin.GxpCompiler;
import com.google.caja.plugin.HtmlCompiler;
import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;
import com.google.caja.util.Pipeline;
import static com.google.caja.plugin.SyntheticNodes.s;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Compile the html and add a method to the plugin for the
 * html and one for each extracted script tag and event handler.
 *
 * @author mikesamuel@gmail.com
 */
public class CompileHtmlStage implements Pipeline.Stage<Jobs> {
  public boolean apply(Jobs jobs) {
    HtmlCompiler htmlc = new HtmlCompiler(
        jobs.getMessageQueue(), jobs.getPluginMeta());

    List<Statement> renderedHtmlStatements = new ArrayList<Statement>();

    ListIterator<Job> it = jobs.getJobs().listIterator();
    while (it.hasNext()) {
      Job job = it.next();
      if (Job.JobType.HTML != job.getType()) { continue; }

      it.remove();
      try {
        renderedHtmlStatements.add(
            htmlc.compileDocument((DomTree) job.getRoot().node));
      } catch (GxpCompiler.BadContentException ex) {
        ex.toMessageQueue(jobs.getMessageQueue());
      }
    }

    for (FunctionDeclaration handler : htmlc.getEventHandlers()) {
      // function foo() { ... }
      // => ___OUTERS___.foo = function foo() { ... };
      // assuming meta.namespaceName is ___OUTERS___.
      Statement def = s(new ExpressionStmt(
          s(new Operation(
                Operator.ASSIGN,
                s(new Operation(
                      Operator.MEMBER_ACCESS,
                      s(new Reference(s(
                          new Identifier(
                              jobs.getPluginMeta().namespaceName)))),
                      s(new Reference(handler.getIdentifier())))),
                handler.getInitializer()))));
      jobs.getJobs().add(
          new Job(new AncestorChain<Block>(
                      new Block(Collections.singletonList(def)))));
    }

    if (!renderedHtmlStatements.isEmpty()) {
      Block htmlGeneration;
      if (renderedHtmlStatements.size() == 1
          && renderedHtmlStatements.get(0) instanceof Block) {
        htmlGeneration = (Block) renderedHtmlStatements.get(0);
      } else {
        htmlGeneration = new Block(renderedHtmlStatements);
      }
      jobs.getJobs().add(new Job(new AncestorChain<Block>(htmlGeneration)));
    }
    return jobs.hasNoFatalErrors();
  }
}