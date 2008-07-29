package com.google.caja.parser;

import com.google.caja.lexer.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public final class TokenList implements Iterable<Token<?>> {
  private final List<Token<?>> tokens;

  public TokenList(List<? extends Token<?>> tokens) {
    this.tokens = new ArrayList<Token<?>>(tokens);
  }

  public ListIterator<Token<?>> iterator() {
    return Collections.unmodifiableList(tokens).listIterator();
  }

  @Override
  public String toString() {
    return tokens.toString();
  }

  public static final TokenList EMPTY
      = new TokenList(Collections.<Token<?>>emptyList());
}
