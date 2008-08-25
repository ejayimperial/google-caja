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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.CatchStmt;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.FormalParam;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.NumberLiteral;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.util.Pair;

/**
 * Remove runtime checks around array accesses where it is safe to do so.
 *
 * <h2>Assumptions</h2>
 * <ul>
 * <li>That an interpreter's primitive to string conversion cannot be affected
 *   by untrusted code.  {@code Number.prototype.toString = function () ...}
 *   does not work on targeted interpreters or cannot be effected by user code.
 * <li>That this method is only used to optimize when the object indexed is
 *   known to be an {@code Array} or that all publicly visible members of
 *   {@code Array}s are also visible on all other {@code Object}s.
 * <li>That the string form of all numeric values (as specified in ES3 section
 *   9.8.1); including negative, infinite, {@code NaN}, and non-integral values;
 *   are publicly visible members of {@code Array}.
 * <li>That "{@code undefined}" is a publicly visible member of {@code Array}s.
 * </ul>
 *
 * @author mikesamuel@gmail.com
 */
public final class ArrayIndexOptimization extends OptimizationPass {
  public ArrayIndexOptimization() {
    super(patterns());
  }

  private static List<Pair<String, String>> patterns() {
    List<Pair<String, String>> patterns = new ArrayList<Pair<String, String>>();
    patterns.add(Pair.pair(
        "@o.@key_canRead___ ? @o.@ident : ___.readPub(@o, @k)", "@o.@ident"));
    patterns.add(Pair.pair(
        "@o.@key_canRead___ ? @o.@ident : ___.readProp(@o, @k)", "@o.@ident"));
    patterns.add(Pair.pair("___.readPub(@o, @k)", "@o[@k]"));
    patterns.add(Pair.pair("___.readProp(@o, @k)", "@o[@k]"));
    return patterns;
  }

  @Override
  protected boolean isOptimizationSemanticsPreserving(
      ScopeTree scopeTree, AncestorChain<?> candidate,
      Map<String, ParseTreeNode> bindings) {
    Expression key = (Expression) bindings.get("k");
    if (key instanceof StringLiteral && !bindings.containsKey("ident")) {
      // HACK(mikesamuel): we don't want the narrower optimizations above to
      // interfere with the wider ones, so return false when the narrower one is
      // inside a wider one.
      // TODO(mikesamuel): solve this as a class of problems by changing
      // OptimizationPass.findOptimizations to recurse to bindings only on
      // a successful match.  This is really hard to do correctly though unless
      // there is a version of QuasiBuilder.match that binds to AncestorChains
      // instead of just nodes.
      return false;
    }
    return isArrayMemberExpr(key, scopeTree, new HashSet<String>());
  }

  /**
   * True if all uses of the given reference as a RightHandSideExpression in
   * scopeTree are guaranteed to result in a valid {@code Array} member.
   */
  static boolean doesVarReferenceArrayMember(
      Reference r, ScopeTree scopeTree, Set<String> identifiersExpanding) {
    if (!scopeTree.getScope().isDefined(r.getIdentifierName())) {
      return false;
    }
    for (AncestorChain<Identifier> use
         : scopeTree.usesOf(r.getIdentifierName())) {
      if (use.parent.node instanceof Reference) {
        AncestorChain<?> gp = use.parent.parent;
        // If it's the LHS of an assignment, check the RHS
        if (gp.node instanceof Operation
            && use.parent.node == gp.node.children().get(0)) {
          Operation operation = gp.cast(Operation.class).node;
          Operator op = operation.getOperator();
          if (op == Operator.ASSIGN) {
            if (!isArrayMemberExpr(
                operation.children().get(1), scopeTree, identifiersExpanding)) {
              return false;
            }
          } else if (op.getAssignmentDelegate() != null) {
            if (!isNumberOrUndefOperator(op.getAssignmentDelegate())) {
              return false;
            }
          }
        }
      } else if (use.parent.node instanceof Declaration) {
        Declaration d = (Declaration) use.parent.node;
        // If it's initialized as a result of some non-local value such as
        // a function call or thrown exception, assume it's not numeric.
        if (d instanceof FormalParam) { return false; }
        if (use.parent.parent.node instanceof CatchStmt) { return false; }
        // Otherwise the initializer had better not be a non-numeric value.
        if (d.getInitializer() != null) {
          Expression init = d.getInitializer();
          if (!isArrayMemberExpr(init, scopeTree, identifiersExpanding)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * True if the expression is guaranteed to return a value {@code v} s.t.
   * {@code ('' + v)} is publicly readable {@code Array} member.
   *
   * <h2>Shortcomings of the current implementation</h2>
   * This method is a heuristic.  It will return true for many expressions that
   * are guaranteed to return a publicly visible {@code Array} member but not
   * all.
   * <p>
   * Specifically, it will not handle concatenations such as {@code ('1' + '2')}
   * and will not attempt to predict function binding or evaluation.
   * <p>
   * It will also ignore any variables that are co-assigners, as in
   * {@code
   *   var i = 0, j = 0, tmp;
   *   ...
   *   j = (tmp = i, i = j, tmp);
   * }
   * and the simpler but equivalent
   * {@code
   *   var i = 0, j = 0, tmp;
   *   ...
   *   tmp = i;
   *   i = j;
   *   j = i;
   * }
   * <p>It will return false if the {@code +} operator is used to produce a
   * value assigned to the referenced variable.  This is true even if both
   * operands are provably numeric.
   *
   * @param e an expression evaluated in the context of scopeTree.
   * @param scopeTree the scope in which e is evaluated.
   * @param identifiersExpanding a set of identifiers in e that are being
   *     checked.  This is used to prevent infinite recursion when identifiers
   *     are co-assigned to one another.
   * @return true if e is guaranteed to either not halt or to return a value
   *     whose string form is a publicly visible member of {@code Array}s.
   */
  static boolean isArrayMemberExpr(
      Expression e, ScopeTree scopeTree, Set<String> identifiersExpanding) {
    if (e instanceof NumberLiteral) { return true; }
    if (e instanceof StringLiteral) {
      String s = ((StringLiteral) e).getUnquotedValue();
      return "length".equals(s);
    }
    if (e instanceof Operation) {
      Operation op = (Operation) e;
      switch (op.getOperator()) {
        case COMMA:
          Expression last = op.children().get(1);
          return isArrayMemberExpr(last, scopeTree, identifiersExpanding);
        case LOGICAL_OR: case LOGICAL_AND:
          return isArrayMemberExpr(
              op.children().get(0), scopeTree, identifiersExpanding)
              && isArrayMemberExpr(
              op.children().get(1), scopeTree, identifiersExpanding);
        case TERNARY:
          return isArrayMemberExpr(
              op.children().get(1), scopeTree, identifiersExpanding)
              && isArrayMemberExpr(
              op.children().get(2), scopeTree, identifiersExpanding);
        default:
          if (isNumberOrUndefOperator(op.getOperator())) {
            return true;
          }
          break;
      }
    } else if (e instanceof Reference) {
      Reference r = (Reference) e;
      String name = r.getIdentifierName();
      if (!identifiersExpanding.contains(name)) {
        identifiersExpanding.add(name);
        return doesVarReferenceArrayMember(r, scopeTree, identifiersExpanding);
      }
    }
    return false;
  }

  /**
   * True if the given operator always produces a numeric or undefined result.
   * This is independent of whether the operator also has a side effect.
   * This is a heuristic in the same way as {@link #isArrayMemberExpr}.
   */
  static boolean isNumberOrUndefOperator(Operator o) {
    if (o.getAssignmentDelegate() != null) {
      o = o.getAssignmentDelegate();
    }
    // If we decided that 'true' and 'false' were always readable members, we
    // could white-list NOT, IN, and all the comparison operators, but there is
    // no compelling reason to do so as of this writing.
    switch (o) {
      // ADDITION is excluded since it can return a string result.
      case BITWISE_AND: case BITWISE_OR: case BITWISE_XOR:
      case DIVISION: case IDENTITY: case INVERSE:
      case LSHIFT: case MODULUS: case MULTIPLICATION: case NEGATION:
      case POST_DECREMENT: case POST_INCREMENT:
      case PRE_DECREMENT: case PRE_INCREMENT:
      case RSHIFT: case RUSHIFT: case SUBTRACTION:
      case VOID:  // Returns undefined
        return true;
      default:
        return false;
    }
    // The pass-through operators (',', '||', '&&', and '?:') are handled
    // elsewhere.
  }
}
