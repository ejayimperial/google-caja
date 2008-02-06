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

/**
 * Default Caja rewriter rules.
 *
 * @param importer an importer for resolving module dependencies.
 * @param rewriter a rewriter in which the rewriting rules are hosted.
 * @param NONE a distinguished parse tree node that a rule may return as the result of rewriting
 *   an input node to indicate that it does not apply to the supplied node.
 * @param stderr a standard error stream for messages.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
(function(importer, rewriter, NONE, stderr) {

  var crs = importer.importModule(
      'com/google/caja/parser/quasiliteral/cajaRuleSupport.js',
      importer, rewriter, NONE, stderr);

  var rules = [];

  rules.push(crs.makeSimpleRule(
      "varArgs",
      "Reference",
      "arguments",
      "a___",
      function(node, scope, bindings, mq) {
        stderr.println("bindings = " + bindings);
        stderr.println("bindings.getClass() = " + bindings.getClass());
        stderr.println("bindings.keySet() = " + bindings.keySet());
        return bindings;
      }));

  rules.push(crs.makeSimpleRule(
      "testOperationSchmivit",
      "Operation",
      "@x + @y",
      "@a - @b",
      function(node, scope, bindings, mq) {
        return {
          a: bindings.get("y"),
          b: bindings.get("x")
        };
      }));



  /*
  addRule(new LegacyRule("varFuncFreeze", this) {
    public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
      Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
      if (match("@x", node, bindings) &&
          bindings.get("x") instanceof Reference) {
        String name = getReferenceName(bindings.get("x"));
        if (scope.isFunction(name)) {
          return substV(
              "___.primFreeze(@x)",
              "x", expandReferenceToOuters(bindings.get("x"), scope, mq));
        }
      }
      return NONE;
    }
  });
  */

  rules.push(crs.makeRule(
      "debugPassThrough",
      function(node, scope, mq) {
        return crs.expandAll(node, scope, mq);
      }));

  return { rules: rules };
});