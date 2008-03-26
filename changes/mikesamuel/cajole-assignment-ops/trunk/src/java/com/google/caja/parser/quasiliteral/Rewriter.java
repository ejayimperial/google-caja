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

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.Keyword;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.BooleanLiteral;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Literal;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.Reference;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import static com.google.caja.plugin.SyntheticNodes.s;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites a JavaScript parse tree.
 *  
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public abstract class Rewriter {
  private final Map<String, QuasiNode> patternCache = new HashMap<String, QuasiNode>();
  private final List<Rule> rules = new ArrayList<Rule>();
  private final Set<String> ruleNames = new HashSet<String>();
  private final boolean logging;

  /**
   * Creates a new Rewriter.
   *
   * @param logging whether this Rewriter should log the details of rule firings to
   * standard error.
   */
  public Rewriter(boolean logging) {
    this.logging = logging;
  }

  /**
   * Expands a parse tree node according to the rules of this rewriter, returning
   * the expanded result.
   *
   * @param node a top-level parse tree node to expand.
   * @param mq a message queue for compiler messages.
   * @return the expanded parse tree node.
   */
  public final ParseTreeNode expand(ParseTreeNode node, MessageQueue mq) {
    return expand(node, Scope.fromRootBlock((Block)node, mq), mq);
  }

  /**
   * Alternate form of {@link #expand(ParseTreeNode, MessageQueue)}.
   *
   * @param node a parse tree node to expand.
   * @param scope the scope in which 'node' is defined.
   * @param mq a message queue for compiler messages.
   * @return the exapnded parse tree node.
   */
  public final ParseTreeNode expand(ParseTreeNode node, Scope scope, MessageQueue mq) {
    for (Rule rule : rules) {

      ParseTreeNode result = null;
      RuntimeException ex = null;
      
      try {
        result = rule.fire(node, scope, mq);
      } catch (RuntimeException e) {
        ex = e;
      }
      
      if (result != Rule.NONE || ex != null) {
        if (logging) logResults(rule, node, result, ex);
        if (ex != null) throw ex;
        return result;
      }
    }

    throw new RuntimeException("Unimplemented case involving: " + node);
  }

  /**
   * Adds a rule to this rewriter. Rules are evaluated in the order in which they have
   * been added to the rewriter via this method. Rules may not be removed from the rewriter.
   * No two rules added to the rewriter may have the same
   * {@link com.google.caja.parser.quasiliteral.Rule#getName() name}.
   *
   * @param rule a rewriting rule.
   * @exception IllegalArgumentException if a rule with a duplicate name is added.
   */
  public void addRule(Rule rule) {
    // We keep 'ruleNames' as a guard against programming errors
    if (ruleNames.contains(rule.getName()))
      throw new IllegalArgumentException("Duplicate rule name: " + rule.getName());
    rules.add(rule);
    ruleNames.add(rule.getName());
  }

  /**
   * Obtains a quasiliteral node from a text pattern. Components of the rewriter should prefer
   * this methood over calling a {@link QuasiBuilder} directly since this method matains a
   * cache of pre-compiled patterns.
   *
   * @param patternText a quasiliteral pattern.
   * @return the quasiliteral node represented by the supplied pattern.
   */
  public final QuasiNode getPatternNode(String patternText) {
    if (!patternCache.containsKey(patternText)) {
      try {
        patternCache.put(
            patternText,
            QuasiBuilder.parseQuasiNode(patternText));
      } catch (ParseException e) {
        // Pattern programming error
        throw new RuntimeException(e);
      }
    }
    return patternCache.get(patternText);
  }

  private void logResults(
      Rule rule,
      ParseTreeNode input,
      ParseTreeNode result,
      Exception exception) {
    StringBuilder s = new StringBuilder();
    s.append("-----------------------------------------------------------------------\n");
    if (rule != null) {
      s.append("rule: ").append(rule.getName()).append("\n");
    }
    if (input != null) {
      s.append("input: (")
          .append(input.getClass().getSimpleName())
          .append(") ")
          .append(format(input))
          .append("\n");
    }
    if (result != null) {
      s.append("result: (")
          .append(result.getClass().getSimpleName())
          .append(") ")
          .append(format(result))
          .append("\n");
    }
    if (exception != null) {
      s.append("error: ")
          .append(exception.toString())
          .append("\n");
    }
    System.err.println(s.toString());
  }

  private String format(ParseTreeNode n) {
    try {
      StringBuilder output = new StringBuilder();
      n.render(new RenderContext(new MessageContext(), output));
      return output.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Split the target of a read/set operation into an lvalue, an rvalue, and
   * an ordered list of temporary variables needed to ensure proper order
   * of execution.
   * @param operand uncajoled expression that can be used as both an lvalue and
   *    an rvalue.
   * @return null if operand is not a valid lvalue, or its subexpressions do
   *    not cajole.
   */
  ReadAssignOperands deconstructReadAssignOperand(
      Expression operand, Scope scope, MessageQueue mq) {
    if (isLocalReference(operand, scope)) {
      final Expression expanded = (Expression) expand(operand, scope, mq);
      if (!expanded.isLeftHandSide()) {
        throw new IllegalStateException(expanded.toString());
      }
      if (expanded == Rule.NONE) { return null; }
      return new ReadAssignOperands(
          Collections.<Declaration>emptyList(), expanded) {
          @Override
          public Expression makeAssignment(Expression rvalue) {
            return Operation.create(Operator.ASSIGN, expanded, rvalue);
          }
        };
    }

    final boolean isProp;  // Is a property (as opposed to a public) reference
    final boolean isGlobal;  // Is a reference to a member of ___OUTERS___
    Expression left, right;
    Operator operator;
    if (operand instanceof Reference) {
      expand(operand, scope, mq);
      left = s(new Reference(s(new Identifier("___OUTERS___"))));
      ((Reference) left).setFilePosition(
          FilePosition.startOf(operand.getFilePosition()));
      right = operand;
      operator = Operator.MEMBER_ACCESS;
      isProp = false;
      isGlobal = true;  // Since left is OUTERS.
    } else if (operand instanceof Operation) {
      Operation op = (Operation) operand;
      operator = op.getOperator();

      Expression uncajoledObject = op.children().get(0);
      Expression uncajoledKey = op.children().get(1);
      isProp = uncajoledObject instanceof Reference
          && Keyword.THIS.toString().equals(
              ((Reference) uncajoledObject).getIdentifierName());

      ParseTreeNode lExp = expand(uncajoledObject, scope, mq);
      if (lExp == Rule.NONE) { return null; }
      ParseTreeNode rExp = expand(uncajoledKey, scope, mq);
      if (lExp == Rule.NONE) { return null; }

      left = (Expression) lExp;
      right = operator == Operator.MEMBER_ACCESS
          ? uncajoledKey  // Since the rhs of . is treated as a string literal.
          : (Expression) rExp;
      isGlobal = false;
    } else {
      throw new IllegalArgumentException("Not an lvalue : " + operand);
    }

    final Reference object;  // The object that contains the field to assign.
    final Expression key;  // Identifies the field to assign.
    List<Declaration> temporaries = new ArrayList<Declaration>();

    switch (operator) {
      case SQUARE_BRACKET:
        // a[b] += 2
        //   =>
        // var x___ = a;
        // var x0___ = b;

        // If the right is simple then we can assume it does not modify the
        // left, but otherwise the left has to be put into a temporary so that
        // it's evaluated before the right can muck with it.
        boolean isRightSimple = (right instanceof Literal
                                 || isLocalReference(right, scope));

        if (isLocalReference(left, scope) && isRightSimple) {
          object = (Reference) left;
        } else {
          Identifier tmpVar = s(new Identifier(scope.newTempVariable()));
          temporaries.add(s(new Declaration(tmpVar, left)));
          object = s(new Reference(tmpVar));
        }

        if (isRightSimple) {
          key = right;
        } else {
          Identifier tmpVar = s(new Identifier(scope.newTempVariable()));
          temporaries.add(s(new Declaration(tmpVar, right)));
          key = s(new Reference(tmpVar));
        }
        break;
      case MEMBER_ACCESS:
        if (isLocalReference(left, scope) || isOutersReference(left)) {
          // ___OUTERS___ is not treated as a local reference, but it is a
          // formal, so do not assign to a temporary.
          object = (Reference) left;
        } else {
          Identifier tmpVar = s(new Identifier(scope.newTempVariable()));
          temporaries.add(s(new Declaration(tmpVar, left)));
          object = s(new Reference(tmpVar));
        }

        key = toStringLiteral(right);
        break;
      default:
        throw new IllegalArgumentException("Not an lvalue : " + operand);
    }

    Expression rvalueCajoled = Operation.create(
            Operator.FUNCTION_CALL,
            Operation.create(
                Operator.MEMBER_ACCESS,
                new Reference(new Identifier("___")),
                new Reference(new Identifier(isProp ? "readProp" : "readPub"))),
            object,
            key);
    // Make sure exception thrown if global variable not defined.
    if (isGlobal) { rvalueCajoled.appendChild(new BooleanLiteral(true)); }

    return new ReadAssignOperands(temporaries, rvalueCajoled) {
        @Override
        public Expression makeAssignment(Expression rvalue) {
          return Operation.create(
              Operator.FUNCTION_CALL,
              Operation.create(
                  Operator.MEMBER_ACCESS,
                  new Reference(new Identifier("___")),
                  new Reference(new Identifier(isProp ? "setProp" : "setPub"))),
              object,
              key,
              rvalue);
        }
      };
  }

  /**
   * True iff e is a reference to a local in scope.
   * We distinguish local references in many places because members of
   * {@code ___OUTERS___} might be backed by getters/setters, and so
   * must be evaluated exactly once as an lvalue.
   */
  private static boolean isLocalReference(Expression e, Scope scope) {
    return e instanceof Reference 
        && !scope.isGlobal(((Reference) e).getIdentifierName());
  }

  /**
   * True iff e is a reference to the global object.
   */
  private static boolean isOutersReference(Expression e) {
    if (!(e instanceof Reference)) { return false; }
    return "___OUTERS___".equals(((Reference) e).getIdentifierName());
  }

  protected static final StringLiteral toStringLiteral(ParseTreeNode node) {
    Identifier ident;
    if (node instanceof Reference) {
      ident = ((Reference) node).getIdentifier();
    } else if (node instanceof Declaration) {
      ident = ((Declaration) node).getIdentifier();
    } else {
      ident = (Identifier) node;
    }
    StringLiteral sl = new StringLiteral(
        StringLiteral.toQuotedValue(ident.getName()));
    if (node.getFilePosition() != null) {
      sl.setFilePosition(node.getFilePosition());
    }
    return sl;
  }

  /**
   * The operands in a read/assign operation.
   * <p>
   * When we need to express a single read/assign operation such as {@code *=}
   * or {@code ++} as an operation that separates out the getting from the
   * setting.
   * <p>
   * This encapsulates any temporary variables created to prevent multiple
   * execution, and the cajoled lvalue and rvalue.
   */
  protected static abstract class ReadAssignOperands {
    private final List<Declaration> temporaries;
    private final Expression rvalue;

    ReadAssignOperands(
        List<Declaration> temporaries, Expression rvalue) {
      this.temporaries = temporaries;
      this.rvalue = rvalue;
    }

    /**
     * The temporaries required by lvalue and rvalue in order of
     * initialization.
     */
    public List<Declaration> getTemporaries() { return temporaries; }
    public ParseTreeNodeContainer getTemporariesAsContainer() {
      return new ParseTreeNodeContainer(temporaries);
    }
    /** Produce an assignment of the given rvalue to the cajoled lvalue. */
    public abstract Expression makeAssignment(Expression rvalue);
    /** The Cajoled RValue. */
    public Expression getRValue() { return rvalue; }
    /**
     * Can the assignment be performed using the rvalue as an lvalue without
     * the need for temporaries?
     */
    public boolean isSimpleLValue() {
      return temporaries.isEmpty() && rvalue.isLeftHandSide();
    }
  }
}
