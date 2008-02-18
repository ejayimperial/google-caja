package com.google.caja.parser.js;

import java.util.List;

/**
 * An operation that assigns to its first child as an lValue.
 * <p>
 * All <tt>AssignOperation</tt>s except ASSIGN(=) also read from that
 * first child first, so those are read-modify-write operations.
 *
 * @author erights@gmail.com
 */
public final class AssignOperation extends Operation {

  public AssignOperation(Operator value, List<? extends Expression> children) {
    this(value, children.toArray(new Expression[children.size()]));
  }

  public AssignOperation(Operator op, Expression... params) {
    super(op, params);
  }
}
