package com.google.caja.render;

final class JsRenderUtil {
  /**
   * True if lexical conventions allow a line break between the given two
   * tokens without inserting a semicolon.
   * @param before null or a non-comment, non-whitespace token.
   * @param after null or a non-comment, non-whitespace token.
   */
  static boolean canBreakBetween(String before, String after) {
    // According to semicolon insertion rules in ES262 Section 7.9.1
    //     When, as the program is parsed from left to right, a token
    //     is encountered that is allowed by some production of the
    //     grammar, but the production is a restricted production and
    //     the token would be the first token for a terminal or
    //     nonterminal immediately following the annotation "[no
    //     LineTerminator here]" within the restricted production (and
    //     therefore such a token is called a restricted token), and
    //     the restricted token is separated from the previous token
    //     by at least one LineTerminator, then a semicolon is
    //     automatically inserted before the restricted token.

    //     These are the only restricted productions in the grammar:
    //     PostfixExpression :
    //         LeftHandSideExpression [no LineTerminator here] ++
    //         LeftHandSideExpression [no LineTerminator here] --
    //     ContinueStatement :
    //         continue [no LineTerminator here] Identifieropt ;
    //     BreakStatement :
    //         break [no LineTerminator here] Identifieropt ;
    //     ReturnStatement :
    //         return [no LineTerminator here] Expressionopt ;
    //     ThrowStatement :
    //         throw [no LineTerminator here] Expression ;
    return !("++".equals(after)
             || "--".equals(after)
             || "continue".equals(before)
             || "break".equals(before)
             || "return".equals(before)
             || "throw".equals(before)
             // Though allowed by the spec, we also don't break before
             // division operators to prevent semicolon insertion from
             // causing the division operator from being interpreted
             // as the start of a regexp.
             || "/".equals(after)
             || "/=".equals(after)
             );
  }
}
