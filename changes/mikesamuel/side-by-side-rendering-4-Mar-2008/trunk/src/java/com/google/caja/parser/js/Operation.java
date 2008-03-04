// Copyright (C) 2005 Google Inc.
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

package com.google.caja.parser.js;

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.TokenConsumer;
import com.google.caja.reporting.RenderContext;

import java.util.Arrays;
import java.util.List;

/**
 * An expression that applies an {@link Operator} to a number of operands.
 *
 * @author mikesamuel@gmail.com
 */
public class Operation extends AbstractExpression<Expression> {
  private Operator op;

  public Operation(Operator value, List<? extends Expression> children) {
    this(value, children.toArray(new Expression[children.size()]));
  }

  public Operation(Operator op, Expression... params) {
    this.op = op;
    if (null == op) { throw new NullPointerException(); }
    createMutation().appendChildren(Arrays.asList(params)).execute();
  }

  @Override
  public Object getValue() { return op; }

  public Operator getOperator() { return op; }

  @Override
  public boolean isLeftHandSide() {
    switch (op) {
      case MEMBER_ACCESS:
      case SQUARE_BRACKET:
        return true;
      default:
        return false;
    }
  }

  public void render(RenderContext rc) {
    TokenConsumer out = rc.getOut();
    out.mark(getFilePosition());
    switch (op.getType()) {
      case PREFIX:
        out.consume(op.getSymbol());
        renderParam(0, rc);
        break;
      case POSTFIX:
        renderParam(0, rc);
        out.mark(FilePosition.endOfOrNull(getFilePosition()));
        out.consume(op.getSymbol());
        break;
      case INFIX:
        renderParam(0, rc);
        switch (getOperator()) {
          default:
            // These spaces are necessary for security.
            // If they are not present, then rendered javascript might include
            // the strings ]]> or </script> which would prevent it from being
            // safely embedded in HTML or XML.
            out.consume(" ");
            out.consume(op.getSymbol());
            out.consume(" ");
            break;
          case MEMBER_ACCESS:
          case COMMA:
            out.consume(op.getSymbol());
            break;
        }
        renderParam(1, rc);
        break;
      case BRACKET:
        renderParam(0, rc);
        out.consume(op.getOpeningSymbol());
        boolean seen = false;
        for (Expression e : children().subList(1, children().size())) {
          if (seen) {
            out.consume(",");
          } else {
            seen = true;
          }
          // make sure that comma operators are properly escaped
          if (!parenthesize(Operator.COMMA, false, e)) {
            e.render(rc);
          } else {
            out.consume("(");
            e.render(rc);
            out.mark(FilePosition.endOfOrNull(e.getFilePosition()));
            out.consume(")");
          }
        }
        out.mark(FilePosition.endOfOrNull(getFilePosition()));
        out.consume(op.getClosingSymbol());
        break;
      case TERNARY:
        renderParam(0, rc);
        out.consume(op.getOpeningSymbol());
        out.consume(" ");
        renderParam(1, rc);
        out.consume(op.getClosingSymbol());
        out.consume(" ");
        renderParam(2, rc);
        break;
    }
  }

  private void renderParam(int i, RenderContext rc) {
    TokenConsumer out = rc.getOut();
    Expression e = children().get(i);
    out.mark(e.getFilePosition());
    if (!parenthesize(op, 0 == i, e)) {
      e.render(rc);
    } else {
      out.consume("(");
      e.render(rc);
      out.mark(FilePosition.endOfOrNull(getFilePosition()));
      out.consume(")");
    }
  }

  private static boolean parenthesize(
      Operator op, boolean firstOp, Expression child) {
    // Parenthesize blocklike expressions
    if (child instanceof FunctionConstructor
        || child instanceof ObjectConstructor) {
      // Parenthesize constructors if they're the first op.
      // They can be the right hand of assignments, but they won't parse
      // unparenthesized if used as the first operand in a call, followed by a
      // postfix op, or as part of the condition in a ternary op.
      return firstOp;
    }

    if (child instanceof NumberLiteral) {
      if (firstOp && op == Operator.MEMBER_ACCESS) {
        // Parenthesize numbers and booleans when they're the left hand side of
        // an operator.
        // 3.toString() is not valid, but (3).toString() is.
        return true;
      }

      if (OperatorType.PREFIX == op.getType()) {
        // make sure that -(-3) is not written out as --3, and that -(3) is not
        // written out as the literal node -3.
        return ((NumberLiteral) child).getValue().doubleValue() < 0;
      }
    }

    if (!(child instanceof Operation)) { return false; }

    // Parenthesize based on associativity and precedence
    Operator childOp = ((Operation) child).getOperator();
    int delta = op.getPrecedence() - childOp.getPrecedence();
    if (delta < 0) {
      // e.g. this is * and child is +
      return true;
    } else if (delta == 0) {
      // LEFT: a + b + c -> (a + b) + c
      // So we need to parenthesize right in a + (b + c)
      // RIGHT: a = b = c -> a = (b = c)
      // And we'd need to parenthesize left in (a = b) = c if that were legal

      // -(-a) is right associative so it is parenthesized

      // ?: is right associative, so in a ? b : c, a would be parenthesized were
      // it a trinary op
      return (childOp.getAssociativity() == Associativity.LEFT) != firstOp;
    } else {
      return false;
    }
  }
}
