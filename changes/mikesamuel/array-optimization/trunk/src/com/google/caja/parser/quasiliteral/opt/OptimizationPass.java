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

package com.google.caja.parser.quasiliteral.opt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.caja.parser.AbstractParseTreeNode;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodes;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.quasiliteral.QuasiBuilder;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.util.Pair;

/**
 * An abstract parse tree transformation that can represent any transformation
 * that takes a parse tree, finds potential optimizations, filters out those
 * which are not semantics preserving, and then produces a parse tree with
 * those optimizations.
 *
 * @author mikesamuel@gmail.com
 */
public abstract class OptimizationPass {
  private final List<Pair<String, String>> optimizations;

  /**
   * @param optimizations paired QuasiBuilder match/subst patterns.
   *     The first element is a pattern recognized in the input, and the
   *     second is the optimized equivalent.
   */
  protected OptimizationPass(List<Pair<String, String>> optimizations) {
    this.optimizations = new ArrayList<Pair<String, String>>(optimizations);
  }

  /**
   * @param js a JavaScript or Caja parse tree.
   * @return js if no optimizations were performed, otherwise a parse tree that
   *     is semantically the same as js.
   */
  public ParseTreeNode optimize(Block js) {
    // Compute scopes using a throw-away SimpleMessageQueue to ignore errors
    // since this may run on JavaScript, not Caja.
    ScopeTree scopeTree = new ScopeTree(
        AncestorChain.instance(js), new SimpleMessageQueue());
    // Find all the sites we can apply an optimization too.
    Map<ScopeTree, List<AncestorChain<?>>> opts = findOptimizations(scopeTree);
    if (opts.isEmpty()) { return js; }
    // Construct a parse tree with appropriate optimizations.
    return optimize(js, opts);
  }

  protected Map<ScopeTree, List<AncestorChain<?>>>
      findOptimizations(ScopeTree scopeTree) {
    Map<ScopeTree, List<AncestorChain<?>>> out
        = new IdentityHashMap<ScopeTree, List<AncestorChain<?>>>();
    findOptimizationCandidates(scopeTree.getRoot(), scopeTree, out);
    return out;
  }

  /**
   * Walks the parse tree under ac, finding all nodes that match one of the
   * optimization patterns passed to the constructor.
   * @param ac a javascript parse tree chain.
   * @param scopeTree the scope in which ac appears.
   * @param out a multi-map from scopes to optimization candidates defined in
   *    that scope.  Modified in place.
   */
  protected void findOptimizationCandidates(
      AncestorChain<?> ac, ScopeTree scopeTree,
      Map<ScopeTree, List<AncestorChain<?>>> out) {
    Map<String, ParseTreeNode> bindings
        = new LinkedHashMap<String, ParseTreeNode>();
    for (Pair<String, String> optimization : this.optimizations) {
      bindings.clear();
      if (QuasiBuilder.match(optimization.a, ac.node, bindings)) {
        if (isOptimizationSemanticsPreserving(scopeTree, ac, bindings)) {
          List<AncestorChain<?>> forScope = out.get(scopeTree);
          if (forScope == null) {
            out.put(scopeTree, forScope = new ArrayList<AncestorChain<?>>());
          }
          forScope.add(ac);
        }
        break;
      }
    }
    for (ParseTreeNode child : ac.node.children()) {
      ScopeTree s2 = scopeTree.scopeForChild(child);
      findOptimizationCandidates(AncestorChain.instance(ac, child), s2, out);
    }
  }

  /**
   * Reject any candidate optimization that would change semantics.
   * @return true if semantics preserving.
   */
  protected abstract boolean isOptimizationSemanticsPreserving(
      ScopeTree scopeTree, AncestorChain<?> candidate,
      Map<String, ParseTreeNode> bindings);

  /**
   * @param js a JavaScript or Caja parse tree.
   * @param optimizations filtered.
   * @return js if no optimizations were performed, otherwise a parse tree that
   *     is semantically the same as js.
   */
  protected ParseTreeNode optimize(
      ParseTreeNode js, Map<ScopeTree, List<AncestorChain<?>>> optimizations) {
    Set<AncestorChain<?>> toRewrite = new HashSet<AncestorChain<?>>();
    for (List<AncestorChain<?>> acs : optimizations.values()) {
      toRewrite.addAll(acs);
    }
    return rewrite(AncestorChain.instance(js), toRewrite);
  }

  private ParseTreeNode rewrite(
      AncestorChain<?> ac, Set<AncestorChain<?>> toRewrite) {

    ParseTreeNode rewritten = ac.node;
    if (!ac.node.children().isEmpty()) {
      List<ParseTreeNode> children = new ArrayList<ParseTreeNode>();
      boolean differ = false;
      for (ParseTreeNode child : ac.node.children()) {
        ParseTreeNode rewrittenChild = rewrite(
            AncestorChain.instance(ac, child), toRewrite);
        children.add(rewrittenChild);
        if (child != rewrittenChild) { differ = true; }
      }
      if (differ) {
        // So that we can maintain optimize's @return guarantee.
        rewritten = ParseTreeNodes.newNodeInstance(
            ac.cast(ParseTreeNode.class).node.getClass(), ac.node.getValue(),
            children);
        rewritten.getAttributes().putAll(ac.node.getAttributes());
        ((AbstractParseTreeNode<?>) rewritten)
            .setFilePosition(ac.node.getFilePosition());
      }
    }

    if (toRewrite.contains(ac)) {
      Map<String, ParseTreeNode> bindings
          = new HashMap<String, ParseTreeNode>();
      for (Pair<String, String> optimization : optimizations) {
        bindings.clear();
        if (QuasiBuilder.match(optimization.a, rewritten, bindings)) {
          return QuasiBuilder.subst(optimization.b, bindings);
        }
      }
      // Control may reach here without matching if a nested optimization caused
      // no pattern to match.
    }
    return rewritten;
  }
}
