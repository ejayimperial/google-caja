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

package com.google.caja.parser.quasiliteral;

import com.google.caja.parser.ParseTreeNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rewrites a JavaScript parse tree to comply with Caja rules.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class JsRewriter extends AbstractRewriter {
  public ParseTreeNode rewrite(ParseTreeNode node) {
    Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();

    // TODO(ihab.awad): Check whether arguments actually used before adding ___args___.

    if (match(node, bindings, "arguments")) {
      return subst(bindings, "___args___");
    }

    if (match(node, bindings, "function @f(@args*) { @body*; }")) {
      rewriteEntry(bindings, "args");
      rewriteEntry(bindings, "body");
      return subst(bindings, "function @f(@args*) { var ___args___ = arguments; @body*; }");
    }

    if (match(node, bindings, "function(@args*) { @body*; }")) {
      rewriteEntry(bindings, "args");
      rewriteEntry(bindings, "body");
      return subst(bindings, "function(@args*) { var ___args___ = arguments; @body*; }");
    }

    return nullRewrite(node);
  }
}