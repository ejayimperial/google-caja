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
import java.util.Collections;
import java.util.List;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.CatchStmt;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.quasiliteral.Scope;
import com.google.caja.reporting.MessageQueue;

/**
 * A tree structure corresponding to the different {@link Scope}s in
 * a JavaScript program.
 *
 * @author mikesamuel@gmail.com
 */
public final class ScopeTree {
  private final AncestorChain<?> root;
  private final Scope scope;
  private final ScopeTree parent;
  private final List<ScopeTree> children = new ArrayList<ScopeTree>();

  public ScopeTree(AncestorChain<Block> scopeRoot, MessageQueue mq) {
    this(null, scopeRoot, Scope.fromProgram(scopeRoot.node, mq));
  }

  private ScopeTree(ScopeTree parent, AncestorChain<?> scopeRoot, Scope scope) {
    this.parent = parent;
    this.root = scopeRoot;
    this.scope = scope;
    walk(scopeRoot);
  }

  public AncestorChain<?> getRoot() { return root; }

  public Scope getScope() { return scope; }

  private void walk(AncestorChain<?> ac) {
    for (ParseTreeNode child : ac.node.children()) {
      AncestorChain<?> childAc = AncestorChain.instance(ac, child);
      if (child instanceof FunctionConstructor) {
        children.add(
            new ScopeTree(this, childAc, Scope.fromFunctionConstructor(
                this.scope, (FunctionConstructor) child)));
      } else if (child instanceof CatchStmt) {
        children.add(
            new ScopeTree(this, childAc, Scope.fromCatchStmt(
                this.scope, (CatchStmt) child)));
      } else {
        walk(childAc);
      }
    }
  }

  /**
   * All the uses of the identifier in the scope in which it is defined.
   * @param identifier an identifier defined in this scope or a parent scope.
   */
  public List<AncestorChain<Identifier>> usesOf(String identifier) {
    List<AncestorChain<Identifier>> uses
        = new ArrayList<AncestorChain<Identifier>>();
    ScopeTree t = this;
    while (t.parent != null && !t.scope.isLocallyDefined(identifier)) {
      t = t.parent;
    }
    t.findUses(identifier, t.root, uses);
    return uses;
  }

  private void findUses(
      String identifier, AncestorChain<?> ac,
      List<AncestorChain<Identifier>> out) {
    if (ac.node instanceof Identifier
        && identifier.equals(ac.node.getValue())) {
      out.add(ac.cast(Identifier.class));
    } else {
      for (ParseTreeNode child : ac.node.children()) {
        ScopeTree s2 = scopeForChild(child);
        if (s2 != this) {
          if (!s2.scope.isLocallyDefined(identifier)) {
            s2.findUses(identifier, AncestorChain.instance(ac, child), out);
          }
        } else {
          findUses(identifier, AncestorChain.instance(ac, child), out);
        }
      }
    }
  }

  /** @param n a node that is a child of a node in this.scope. */
  public ScopeTree scopeForChild(ParseTreeNode n) {
    if (n instanceof FunctionConstructor || n instanceof CatchStmt) {
      for (ScopeTree t : children) {
        if (t.root.node == n) { return t; }
      }
    }
    return this;
  }

  public List<ScopeTree> children() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toStringBuilder(0, sb);
    return sb.toString();
  }

  private void toStringBuilder(int depth, StringBuilder sb) {
    for (int d = depth; --d >= 0;) { sb.append("  "); }
    sb.append("(ScopeTree ");
    if (root.node instanceof FunctionConstructor) {
      sb.append("function");
      String name = root.cast(FunctionConstructor.class)
          .node.getIdentifierName();
      if (name != null) {
        sb.append(' ').append(name);
      }
    } else {
      String typeName = root.node.getClass().getSimpleName();
      typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
      sb.append(typeName);
    }
    for (ScopeTree child : children) {
      sb.append('\n');
      child.toStringBuilder(depth + 1, sb);
    }
    sb.append(')');
  }
}
