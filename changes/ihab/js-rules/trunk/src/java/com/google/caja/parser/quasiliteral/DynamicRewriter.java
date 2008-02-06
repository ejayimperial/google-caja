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

package com.google.caja.parser.quasiliteral;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;

/**
 * Rewriter implementation that executes a rewriter script written in JavaScript.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DynamicRewriter extends Rewriter {

  protected DynamicRewriter(String script) {
    this(true, script);
  }

  protected DynamicRewriter(boolean logging, String script) {
    super(logging);
    Context.enter();
    try {
      JsImporter importer = new JsImporter();
      ScriptableObject ruleset = importer.importModule(
          script,
          importer, this, Rule.NONE, System.err);
      NativeArray rules = (NativeArray)ruleset.get("rules", ruleset);
      for (int i = 0; i < rules.getLength(); i++) {
        NativeJavaObject ruleWrapper = (NativeJavaObject)rules.get(i, rules);
        addRule((Rule)ruleWrapper.unwrap());
      }
    } finally {
      Context.exit();
    }
  }  
}
