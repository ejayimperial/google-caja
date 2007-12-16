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

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodes;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.js.Statement;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.TestUtil;
import junit.framework.TestCase;

import java.io.StringReader;
import java.net.URI;

/**
 * Simple test harness for experimenting with quasiliteral rewrites. This
 * is not part of an automated test suite.
 *
 * @author ihab.awad@gmail.com
 */
public class DefaultRewriterTest extends TestCase {
  public void testSynthetic() throws Exception {
    // TODO(ihab.awad): Check that synthetic nodes are passed through
  }

  public void testWith0() throws Exception {
    // TODO(ihab.awad): Our parser does not even seem to recognize 'with'
    // as a control structure. Good on it, but so is this test unnecessary?
    // Confirm then decide what to do here.
    /*
    checkFails(
        "with(x) { y = 3; }",
        "\"with\" disallowed");
    */
  }

  public void testVariable0() throws Exception {
    checkSucceeds(
        "arguments;",
        "a___;");
  }

  public void testVariable1() throws Exception {
    checkSucceeds(
        "this;",
        "t___;");
  }

  public void testVariable2() throws Exception {
    checkFails(
        "foo__;",
        "Variables cannot end in \"__\"");
  }

  public void testVariable3() throws Exception {
    checkFails(
        "foo_;",
        "Globals cannot end in \"_\"");
  }

  public void testVariable4() throws Exception {
    checkFails(
        "function Ctor() { this.x = 1; }; var c = Ctor;",
        "Constructors are not first class");
  }

  public void testVariable5() throws Exception {
    checkSucceeds(
        "function foo() { }; var f = foo;",
        "function foo() { }; var f = ___.primFreeze(foo);");
  }

  public void testVariable6() throws Exception {
    checkSucceeds(
        "foo;",
        "___OUTERS___.foo;");
  }

  public void testVariable7() throws Exception {
    checkSucceeds(
        "function() { var x = 3; };",
        "function() { var x = 3; };");
  }

  public void testRead0() throws Exception {
    checkFails(
        "x.y__;",
        "Properties cannot end in \"__\"");
  }

  public void testRead1() throws Exception {
    checkSucceeds(
        "this.x;",
        "this.x_canRead__ ? this.x : ___.readProp(this, \'x\');");
  }

  public void testRead2() throws Exception {
    checkFails(
        "foo.bar_;",
        "Public properties cannot end in \"_\"");
  }

  public void testRead3() throws Exception {
    checkSucceeds(
        "y = foo.x;",
        "(function() {" +
        "  var x0___ = ___OUTERS___.x;" +
        "  y = x;" +
        "})();");
  }

  //////////////////////////////////////////////////////////////////////////////////
  //
  // END OF NEW-STYLE, SYSTEMATIC TESTS OF REWRITER
  // TESTS BELOW ARE _AD HOC_ OLD STUFF
  //
  //////////////////////////////////////////////////////////////////////////////////

  public void testThis() throws Exception {
    checkSucceeds(
        "this['a' + 'b'](1 + 2, 3 + 4)",
        "___.callProp(this, 'a' + 'b', [1 + 2, 3 + 4])");
  }

  public void testFunction() throws Exception {
    // TODO(ihab.awad): Apply some test conditions
    showTree("function.js");
  }

  public void testClickme() throws Exception {
    // TODO(ihab.awad): Apply some test conditions
    showTree("clickme.js");
  }

  public void testListfriends() throws Exception {
    // TODO(ihab.awad): Apply some test conditions
    showTree("listfriends.js");
  }

  private void checkFails(String input, String error) throws Exception {
    try {
      new DefaultRewriter().expand(parseText(input));
      fail("Should have failed on input: " + input);
    } catch (Exception e) {
      assertTrue(
          "Error does not contain \"" + error + "\": " + e.toString(),
          e.toString().contains(error));
    }
  }

  private void checkSucceeds(String input, String expectedResult) throws Exception {
    ParseTreeNode expectedResultNode = parseText(expectedResult);
    ParseTreeNode actualResultNode = new DefaultRewriter().expand(parseText(input));
    assertTrue(
        "Expected: " + format(expectedResultNode) + "\n" +
        "Actual: " + format(actualResultNode),
        ParseTreeNodes.deepEquals(expectedResultNode, actualResultNode));
  }

  private void showTree(String resource) throws Exception {
    ParseTreeNode program = parseResource(resource);
    ParseTreeNode rewritten = new DefaultRewriter().expand(program);
    System.out.println("program = " + format(program));
    System.out.println("rewritten = " + format(rewritten));
  }

  private static String format(ParseTreeNode n) throws Exception {
    StringBuilder output = new StringBuilder();
    // n.render(new RenderContext(new MessageContext(), output));
    n.format(new MessageContext(), output);
    return output.toString();
  }

  public ParseTreeNode parseText(String text) throws Exception {
    MessageContext mc = new MessageContext();
    MessageQueue mq = TestUtil.createTestMessageQueue(mc);
    InputSource is = new InputSource(new URI("file:///no/input/source"));
    CharProducer cp = CharProducer.Factory.create(new StringReader(text), is);
    JsLexer lexer = new JsLexer(cp);
    JsTokenQueue tq = new JsTokenQueue(lexer, is, JsTokenQueue.NO_NON_DIRECTIVE_COMMENT);
    Parser p = new Parser(tq, mq);
    Statement stmt = p.parse();
    p.getTokenQueue().expectEmpty();
    return stmt;

  }

  public ParseTreeNode parseResource(String resource) throws Exception {
    return parseText(TestUtil.readResource(getClass(), resource));    
  }
}