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
 * Module providing utilities for the definition of Caja JavaScript rewriting rules.
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

  // TODO(ihab.awad): Refactor away the ambient access to Java classes which I
  // am now doing via the Rhino 'Packages.-' construct; (a) because it's bad security
  // practice; and (b) because we need to run this script in browser JS some day.

  /**
   * Returns the type of a node as a short String. Node types include
   * "Block", "Operation", etc.
   *
   * @param node a parse tree node.
   * @return the type of the node.
   */
  function getNodeType(node) {
    var s = String(node.getClass().getName()).split(/\./);
    return s[s.length - 1];
  }

  /**
   * Matches a node against a pattern.
   *
   * @param pattern a quasiliteral pattern.
   * @param node a parse tree node.
   * @return a map from binding names to binding values if the match
   *   succeeds; null otherwise.
   */
  function match(pattern, node) {
    return rewriter.getPatternNode(pattern).matchHere(node);
  }

  /**
   * Substitues a set of bindings into a pattern.
   *
   * @param pattern a quasiliteral pattern.
   * @param bindings a map of variable bindings.
   * @return a new parse tree node constructed from the inputs.
   */
  function subst(pattern, bindings) {
    var javaBindings = new Packages.java.util.HashMap();
    for (var k in bindings) javaBindings.put(k, bindings[k]);    
    return rewriter.getPatternNode(pattern).substituteHere(javaBindings);
  }

  /**
   * Creates a rule.
   *
   * @param name a unique name for the rule.
   * @fireFunction a function bound to the "fire" method. This function is of the
   *   form f(node, scope, mq) where 'node' is a parse tree node; 'scope' is the
   *   current scope; and 'mq' is a message queue for logging messages. The function
   *   should return the rewritten parse tree node, or NONE if it determines that this
   *   rule does not apply to the inputs.
   * @return a new rule.
   */
  function makeRule(name, fireFunction) {
    return new Packages.com.google.caja.parser.quasiliteral.Rule({
      getName: function() { return name; },
      format: function(mc, out) { out.append('Rule: "' + name + '"'); },
      fire: fireFunction
    });
  }

  /**
   * Creates a simple rule for the most common case of rewriting rules.
   *
   * @param name a unique name for the rule.
   * @nodeType the type of node that should match.
   * @quasiMatchExpr the quasiliteral expression that should match.
   * @quasiResultExpr the quasiliteral result of the output.
   * @rebindFunction a function to rebind quasi variables. This function is of the
   *   form f(node, scope, bindings, mq) where 'node' is a parse tree node; 'scope'
   *   is the current scope; 'bindings' is the current map of bindings from the original
   *   match expression; and 'mq' is a message queue. The function should return the new
   *   bindings as keys/values of a JavaScript object, or null if it determines that this
   *   rule does not apply to the inputs.
   * @return a new rule.
   */
  function makeSimpleRule(
      name,
      nodeType,
      quasiMatchExpr,
      quasiResultExpr,
      rebindFunction) {
    return makeRule(name, function(node, scope, mq) {
      if (!getNodeType(node) === nodeType) return NONE;
      var bindings = match(quasiMatchExpr, node);      
      if (!bindings) return NONE;
      if (rebindFunction) {
        bindings = rebindFunction(node, scope, bindings, mq);
        if (!bindings) return NONE;
      }
      return subst(quasiResultExpr, bindings);
    });
  }

  /**
   * Creates a new instance of a parse tree node.
   *
   * @param clazz the Java class of the desired node.
   * @param value the desired value of the node.
   * @param children a JavaScript array containing the children of the node.
   * @return a new node as requested.
   */
  function newNodeInstance(clazz, value, children) {
    return Packages.com.google.caja.parser.ParseTreeNodes.newNodeInstance(
        clazz,
        value,
        Packages.java.util.Arrays.asList(children));
  }

  /**
   * Expands each of the children of a supplied node and returns a new node just like
   * the one supplied but with the expanded children.
   *
   * @param node a node.
   * @param scope the current scope.
   * @param mq a mesage queue.
   * @return a new node with expanded children.
   */
  function expandAll(node, scope, mq) {
    var children = [];
    for (var i = 0; i < node.children().size(); i++) {
      children.push(rewriter.expand(node.children().get(i), scope, mq));
    }
    return newNodeInstance(node.getClass(), node.getValue(), children);
  }

  return {
    getNodeType: getNodeType,
    match: match,
    subst: subst,
    makeRule: makeRule,
    makeSimpleRule: makeSimpleRule,
    expandAll: expandAll,
    newNodeInstance: newNodeInstance
  };
});
