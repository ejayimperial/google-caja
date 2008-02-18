package com.google.caja.parser.js;

import java.util.List;

/**
 * An Operation that executes some of its operands conditionally, depending on
 * the results of evaluating other operands.
 *
 * @author erights@gmail.com
 */
public final class ControlOperation extends Operation {

  public ControlOperation(Operator value, List<? extends Expression> children) {
    this(value, children.toArray(new Expression[children.size()]));
  }

  public ControlOperation(Operator op, Expression... params) {
    super(op, params);
  }
}
