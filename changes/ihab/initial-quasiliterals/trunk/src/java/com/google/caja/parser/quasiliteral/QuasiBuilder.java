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

import com.google.caja.lexer.*;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.*;
import com.google.caja.reporting.DevNullMessageQueue;
import com.google.caja.reporting.Message;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a JavaScript {@link QuasiNode} tree given a JavaScript
 * {@link com.google.caja.parser.ParseTreeNode} tree.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class QuasiBuilder {

  /**
   * Given a quasiliteral pattern expressed as text, return a {@code QuasiNode}
   * representing the pattern.
   *
   * @param pattern a quasiliteral pattern.
   * @return the QuasiNode representation of the input.
   */
  public static QuasiNode parseQuasiNode(String pattern) throws ParseException {
    ParseTreeNode topLevelNode = parse(pattern);

    // The top-level node returned from the parser is always a Block.
    if (!(topLevelNode instanceof Block))
      throw new RuntimeException("Panic: top level is not a Block");

    // The top-level Block must always contain a single child, otherwise it is not a valid
    // pattern according to our rules.
    if (topLevelNode.children().size() != 1)
      throw new RuntimeException("Panic: top level Block does not contain exactly 1 child");

    // Promote the single child of the top-level Block to allow it to match anywhere.
    topLevelNode = topLevelNode.children().get(0);

    // If the top level is an ExpressionStmt, with one child, then "promote" its single child to
    // the top level to allow the contained expression to match anywhere.
    if (topLevelNode instanceof ExpressionStmt &&
        topLevelNode.children().size() == 1) {
      topLevelNode = topLevelNode.children().get(0);
    }

    // If the top level is now a Reference, "promote" its single Identifier child to the top
    // level to allow the pattern "match this identifier anywhere" to be expressed.
    if (topLevelNode instanceof Reference) {
      if (topLevelNode.children().size() != 1)
        throw new RuntimeException("Panic: Reference does not have exactly 1 child");
      if (!(topLevelNode.children().get(0) instanceof Identifier))
        throw new RuntimeException("Panic: Reference has non-Identifier child");
      topLevelNode = topLevelNode.children().get(0);
    }

    return build(topLevelNode);
  }
  
  private static QuasiNode build(ParseTreeNode n) {
    if (n instanceof ExpressionStmt &&
        n.children().size() == 1 &&
        n.children().get(0) instanceof Reference &&
        ((Reference)n.children().get(0)).getIdentifierName().startsWith("@")) {
      return buildMatchNode(Statement.class, ((Reference)n.children().get(0)).getIdentifierName());
    }

    if (n instanceof Reference &&
        ((Reference)n).getIdentifierName().startsWith("@")) {
      return buildMatchNode(Expression.class, ((Reference)n).getIdentifierName());
    }

    if (n instanceof Identifier &&
        ((Identifier)n).getValue().startsWith("@")) {
      return buildMatchNode(Identifier.class, ((Identifier)n).getValue());
    }
    
    return buildSimpleNode(n);
  }

  private static QuasiNode buildSimpleNode(ParseTreeNode n) {
    return new SimpleQuasiNode(
        n.getClass(),
        n.getValue(),
        buildChildrenOf(n));    
  }

  private static QuasiNode buildMatchNode(Class matchedClass, String quasiString) {
    assert(quasiString.startsWith("@"));
    if (quasiString.endsWith("*")) {
      return new MultipleQuasiMatchNode(
          matchedClass,
          quasiString.substring(1, quasiString.length() - 1));
    } else if (quasiString.endsWith("+")) {
      return new MultipleNonemptyQuasiMatchNode(
          matchedClass,
          quasiString.substring(1, quasiString.length() - 1));
    } else {
      return new SingleQuasiMatchNode(
          matchedClass,
          quasiString.substring(1, quasiString.length()));
    }
  }

  private static QuasiNode[] buildChildrenOf(ParseTreeNode n) {
    List<QuasiNode> children = new ArrayList<QuasiNode>();
    for (ParseTreeNode child : n.children()) children.add(build(child));
    return children.toArray(new QuasiNode[children.size()]);
  }

  private static ParseTreeNode parse(String sourceText) throws ParseException {
    InputSource inputSource;
    try {
      // TODO(ihab.awad): Come up with a cleaner "artificial" InputSource.
      inputSource = new InputSource(new URI("file:///artificial/input/source.js"));
    } catch (URISyntaxException e) {
      throw new Error(e);
    }

    Parser parser = new Parser(
        new JsTokenQueue(
            new JsLexer(
                CharProducer.Factory.create(new StringReader(sourceText),
                inputSource)), 
            inputSource,
            JsTokenQueue.NO_NON_DIRECTIVE_COMMENT),
        DevNullMessageQueue.singleton());

    Statement topLevelStatement = parser.parse();
    parser.getTokenQueue().expectEmpty();
    return topLevelStatement;
  }  
}
