package com.google.caja.parser.js;

import java.util.List;

/**
 * The oddball Operations that don't fit into the other categories.
 *
 * @author erights@gmail.com
 */
public class SpecialOperation extends Operation {

  public SpecialOperation(Operator value, List<? extends Expression> children) {
    this(value, children.toArray(new Expression[children.size()]));
  }

  public SpecialOperation(Operator op, Expression... params) {
    super(op, params);
  }
}
