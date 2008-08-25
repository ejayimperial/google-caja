package com.google.caja.parser.js;

import java.util.List;

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.reporting.RenderContext;

public final class QuotedExpression extends AbstractExpression<ParseTreeNode>{
  public QuotedExpression(Expression e) {
    createMutation().appendChild(e).execute();
  }
  public QuotedExpression(Void value, List<? extends Expression> children) {
    this(children.get(0));
  }
  @Override
  public Object getValue() { return null; }
  public void render(RenderContext r) { }
  public Expression unquote() { return (Expression)children().get(0); }
}
