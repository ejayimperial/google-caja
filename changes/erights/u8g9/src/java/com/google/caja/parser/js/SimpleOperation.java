package com.google.caja.parser.js;

import java.util.List;

/**
 * An Operation that simply evaluates all its children in order as rValues and 
 * then returns a result computed from the resulting values.
 *
 * @author erights@gmail.com
 */
public final class SimpleOperation extends Operation {

  public SimpleOperation(Operator value, List<? extends Expression> children) {
    this(value, children.toArray(new Expression[children.size()]));
  }

  public SimpleOperation(Operator op, Expression... params) {
    super(op, params);
  }
}
