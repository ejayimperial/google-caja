// Copyright (C) 2008 Google Inc.
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

import static com.google.caja.parser.quasiliteral.QuasiBuilder.substV;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.SyntheticNodes;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Statement;
import com.google.caja.util.RhinoTestBed;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author adrienne.felt@gmail.com
 * @author ihab.awad@gmail.com
 */
public class InnocentCodeRewriterTest extends RewriterTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  // Tests block-level forEach statements
  public void testForEachPlain() throws Exception {
    checkSucceeds(
        "var x = new Object;" +
        "x.y = 3;" +
        "for (var k in x) { k; }",

        "var x0___;" +
        "var x = new Object;" +
        "x.y = 3;" +
        "for (x0___ in x) {" +
        "  if (x0___.match(/___(\\.|[.*])/) || x0___.match(/___$/)) {" +
        "    continue;" +
        "  }" +
        "  k = x0___;" +
        "  k;" +
        "}");

    // Checks that test.number is only incremented once when
    // the for loop is executed
    rewriteAndExecute(
        "var test = new Object;" +
        "test.number = 0;" +
        "test.other = 450;" +
        "test.__defineGetter__('num', " +
        "    function() { this.number++; return this.number; });" +
        "test.__defineSetter__('num', " +
        "    function(y) { return this.number = y; });",
        "for (k in test) {" +
        "  var p = test[k];" +
        "}",
        "assertEquals(test.number, 1);");

    // Checks that the hidden properties are not visited when
    // the for loop is executed
    rewriteAndExecute(
        "var test = new Object;" +
        "test.visible = 0;" +
        "test.hidden___ = 5;" +
        "test.hiddenObj___ = new Object;" +
        "var counter = 0;",
        "for (k in test) {" +
        "  counter++;" +
        "}",
        "assertEquals(counter,1);");
  }

  // Checks that var x0 is added *inside* the function scope
  public void testForEachFunc() throws Exception {
    checkSucceeds(
        "function add(a,b) {" +
        "  var x = new Object;" +
        "  x.y = 3;" +
        "  for (var k in x) { k; }" +
        "}",

        "function add(a,b) {" +
        "  var x0___;" +
        "  var x = new Object;" +
        "  x.y = 3;" +
        "  for (x0___ in x) {" +
        "    if (x0___.match(/___(\\.|[.*])/) || x0___.match(/___$/)) {" +
        "      continue;" +
        "    }" +
        "    k = x0___;" +
        "    k;" +
        "  }" +
        "}");

      rewriteAndExecute(
        "var a = 3;",
        "function () {" +
        "  var test = new Object;" +
        "  test.visible = 0;" +
        "  var counter = 0;" +
        "  for (k in test) {" +
        "    counter++;" +
        "  }" +
        "}",
        "var q = 0;" +
        "if (typeof x0___ == 'undefined') {" +
        "  q = 1;" +
        "}"+
        "assertEquals(q,1);");
  }

  // Checks calls to THIS
  public void testThis() throws Exception {
    checkSucceeds(
        "this.fun();" +
        "var a = 3;",
        "if (this.___) {" +
        "  throw ReferenceError;" +
        "}" +
        "this.fun();" +
        "var a = 3;");
    checkSucceeds(
        "var a = this[0];",
        "if (this.___) {" +
        "  throw ReferenceError;" +
        "}" +
        "var a = this[0];");
    checkSucceeds(
        "var a = this.b;",
        "if (this.___) {" +
        "  throw ReferenceError;" +
        "}" +
        "var a = this.b;");
    checkSucceeds(
        "return this;",
        "if (this.___) {" +
        "  throw ReferenceError;" +
        "}" +
        "return this;");

    rewriteAndExecute(
      ";",
      "function a() {" +
      "  this.fun();" +
      "}",
      "assertThrows(a);");
    rewriteAndExecute(
      ";",
      "function a() {" +
      "  var q = this[0];" +
      "}",
      "assertThrows(a);");
    rewriteAndExecute(
      ";",
      "function a() {" +
      "  var k = this.q;" +
      "}",
      "assertThrows(a);");
    rewriteAndExecute(
      ";",
      "function a() {" +
      "  return this;" +
      "}",
      "assertThrows(a);");
  }

  @Override
  protected Object executePlain(String caja) throws IOException {
    mq.getMessages().clear();
    // Make sure the tree assigns the result to the unittestResult___ var.
    return RhinoTestBed.runJs(
        null,
        new RhinoTestBed.Input(getClass(), "/com/google/caja/caja.js"),
        new RhinoTestBed.Input(getClass(), "../../plugin/asserts.js"),
        new RhinoTestBed.Input(caja, getName() + "-uncajoled"));
  }

  @Override
  protected Object rewriteAndExecute(String pre, String trans, String post)
      throws IOException, ParseException {
    mq.getMessages().clear();

    Statement cajaTree = replaceLastStatementWithEmit(
        js(fromString(trans, is)), "unittestResult___;");
    String transJs = render(
        cajole(js(fromResource("../../plugin/asserts.js")), cajaTree));

    assertNoErrors();

    Object result = RhinoTestBed.runJs(
        null,
        new RhinoTestBed.Input(
            getClass(), "/com/google/caja/plugin/console-stubs.js"),
        new RhinoTestBed.Input(getClass(), "/com/google/caja/caja.js"),
        new RhinoTestBed.Input(pre, getName()),
        new RhinoTestBed.Input(transJs, getName()),
        new RhinoTestBed.Input(post, getName()));

    assertNoErrors();
    return result;
  }

  private <T extends ParseTreeNode> T replaceLastStatementWithEmit(
      T node, String lValueExprString) throws ParseException {
    if (node instanceof ExpressionStmt) {
      ParseTreeNode lValueExpr =
          js(fromString(lValueExprString))  // a Block
          .children().get(0)                // an ExpressionStmt
          .children().get(0);               // an Expression
      ExpressionStmt es = (ExpressionStmt) node;
      Expression e = es.getExpression();
      Operation emitter = (Operation)substV(
          "@lValueExpr = @e;",
          "lValueExpr", syntheticTree(lValueExpr),
          "e", e);
      es.replaceChild(emitter, e);
    } else {
      List<? extends ParseTreeNode> children = node.children();
      if (!children.isEmpty()) {
        replaceLastStatementWithEmit(
            children.get(children.size() - 1), lValueExprString);
      }
    }
    return node;
  }

  private <T extends ParseTreeNode> T syntheticTree(T node) {
    for (ParseTreeNode c : node.children()) { setTreeSynthetic(c); }
    return SyntheticNodes.s(node);
  }

  private ParseTreeNode cajole(Statement... nodes) {
    return newRewriter().expand(new Block(Arrays.asList(nodes)), mq);
  }

  @Override
  protected Rewriter newRewriter() {
    return new InnocentCodeRewriter(true);
  }
}