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

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodeContainer;
import com.google.caja.parser.Visitor;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.CatchStmt;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.FunctionDeclaration;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.TryStmt;
import com.google.caja.parser.js.Declaration;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageType;
import com.google.caja.reporting.Message;
import com.google.caja.util.CajaTestCase;

import java.util.ArrayList;

/**
 *
 * @author ihab.awad@gmail.com
 */
public class ScopeTest extends CajaTestCase {
  public void testSimpleDeclaredFunction() throws Exception {
    Block n = js(fromString(
        "var x = 3;" +
        "function foo() {" +
        "  var y = 3;" +
        "  z = 4;" +
        "};"));
    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromFunctionConstructor(s0, findFunctionConstructor(n, "foo"));

    assertTrue(s0.isDefined("x"));
    assertFalse(s0.isImported("x"));

    assertTrue(s0.isDefined("foo"));
    assertFalse(s0.isImported("foo"));

    assertFalse(s0.isDefined("y"));
    assertFalse(s0.isImported("y"));

    assertFalse(s0.isDefined("z"));
    assertTrue(s0.isImported("z"));

    assertTrue(s1.isDefined("x"));
    assertFalse(s1.isImported("x"));

    assertTrue(s1.isDefined("foo"));
    assertFalse(s1.isImported("foo"));

    assertTrue(s1.isDefined("y"));
    assertFalse(s1.isImported("y"));

    assertFalse(s1.isDefined("z"));
    assertTrue(s1.isImported("z"));
  }

  public void testFreeVariablesDotted() throws Exception {
    assertFreeVariables("a;", "a", "");
    assertFreeVariables("a.b;", "a", "b");
    assertFreeVariables("a.b.c;", "a", "b,c");
    assertFreeVariables("a.b.c.d;", "a", "b,c,d");
  }

  public void testFreeVariablesIndexedChained() throws Exception {
    assertFreeVariables("a;", "a", "");
    assertFreeVariables("a[b];", "a,b", "");
    assertFreeVariables("a[b][c];", "a,b,c", "");
    assertFreeVariables("a[b][c][d];", "a,b,c,d", "");
  }

  public void testFreeVariablesIndexedRecursive() throws Exception {
    assertFreeVariables("a;", "a", "");
    assertFreeVariables("a[b];", "a,b", "");
    assertFreeVariables("a[b[c]];", "a,b,c", "");
    assertFreeVariables("a[b[c[d]]];", "a,b,c,d", "");
  }

  public void testFreeVariableFunction() throws Exception {
    assertFreeVariables("a();", "a", "");
  }

  public void testFreeVariableFunctionWithMember() throws Exception {
    assertFreeVariables("a();", "a", "");
    assertFreeVariables("a().b;", "a", "b");
    assertFreeVariables("a().b.c;", "a", "b,c");
    assertFreeVariables("a().b.c.d;", "a", "b,c,d");
  }

  public void testFreeVariableFunctionParams() throws Exception {
    assertFreeVariables("a(b, c, d);", "a,b,c,d", "");
  }

  public void testFreeVariableDeclaration() throws Exception {
    assertFreeVariables("var a = b, c = d;", "b,d", "a,c");
  }

  public void testFreeVariableCatchStmt() throws Exception {
    assertFreeVariables(
        "   try {"
        + "   a;"
        + " } catch (e) {"
        + "   b;"
        + "   e;"
        + " }",
        "a,b",
        "e");
    assertFreeVariables(
        "   try {"
        + "   a;"
        + " } catch (e0) {"
        + "   b;"
        + "   try {"
        + "     c;"
        + "   } catch (e1) {"
        + "     d;"
        + "     e0;"
        + "   }"
        + " }",
        "a,b,c,d",
        "e0,e1");
    assertFreeVariables(
        "   try {"
        + "   a;"
        + " } catch (e0) {"
        + "   b;"
        + "   try {"
        + "     c;"
        + "   } catch (e1) {"
        + "     d;"
        + "     e0;"
        + "   }"
        + "   e1;"
        + " }",
        "a,b,c,d,e1",
        "e0");
  }

  private void assertFreeVariables(String code,
                                   String freeVariables,
                                   String notFreeVariables)
      throws Exception {
    Block n = js(fromString(code));
    Scope s = Scope.fromProgram(n, mq);
    for (String v : freeVariables.split(",")) {
      assertTrue(
          "<" + v + "> should be a free variable in <" + code + ">",
          s.isImported(v));
    }
    for (String v : notFreeVariables.split(",")) {
      assertFalse(
          "<" + v + "> should not be a free variable in <" + code + ">",
          s.isImported(v));
    }
  }

  public void testAnonymousFunction() throws Exception {
    Block n = js(fromString("var x = function() {};"));
    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromFunctionConstructor(s0, findFunctionConstructor(n, null));

    assertTrue(s0.isDefined("x"));
    assertFalse(s0.isImported("x"));

    assertTrue(s1.isDefined("x"));
    assertFalse(s1.isImported("x"));
  }

  public void testNamedFunction() throws Exception {
    Block n = js(fromString("var x = function foo() {};"));
    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromFunctionConstructor(s0, findFunctionConstructor(n, "foo"));

    assertTrue(s0.isDefined("x"));
    assertFalse(s0.isImported("x"));

    assertFalse(s0.isDefined("foo"));
    assertFalse(s0.isImported("foo"));

    assertTrue(s1.isDefined("x"));
    assertFalse(s1.isImported("x"));

    assertTrue(s1.isDefined("foo"));
    assertFalse(s1.isImported("foo"));
  }

  public void testNamedFunctionSameName() throws Exception {
    Block n = js(fromString("var x = function x() {};"));
    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromFunctionConstructor(s0, findFunctionConstructor(n, "x"));

    assertTrue(s0.isDefined("x"));
    assertFalse(s0.isImported("x"));

    assertTrue(s1.isDefined("x"));
    assertFalse(s1.isImported("x"));
  }

  public void testFormalParams() throws Exception {
    Block n = js(fromString("function f(x) {};"));
    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromFunctionConstructor(s0, findFunctionConstructor(n, "f"));

    assertFalse(s0.isDefined("x"));
    assertTrue(s1.isDefined("x"));
  }

  public void testCatchBlocks() throws Exception {
    Block n = js(fromString("try { } catch (e) { var x; }"));

    TryStmt t = (TryStmt) n.children().get(0);
    CatchStmt c = (CatchStmt) t.children().get(1);

    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromCatchStmt(s0, c);

    // e only defined in catch scope
    assertFalse(s0.isDefined("e"));
    assertTrue(s1.isDefined("e"));
    assertTrue(s1.isException("e"));

    // Definition of x appears in main scope
    assertTrue(s0.isDefined("x"));
  }

  public void testBodyOfNamedFunction() throws Exception {
    Block n = js(fromString("function foo() { var x; }"));

    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromPlainBlock(s0);

    assertEquals(0, mq.getMessages().size());
    assertTrue(s0.isDefined("foo"));
  }

  public void testMaskedExceptionVariablesErrorA() throws Exception {
    Block n = js(fromString("var e; try { } catch (e) { var x; }"));

    TryStmt t = (TryStmt) n.children().get(1);
    CatchStmt c = (CatchStmt) t.children().get(1);

    Scope s0 = Scope.fromProgram(n, mq);
    Scope.fromCatchStmt(s0, c);

    assertMsgType(MessageType.MASKING_SYMBOL, mq.getMessages().get(0));
    assertMsgLevel(MessageLevel.ERROR, mq.getMessages().get(0));
  }

  public void testMaskedExceptionVariablesErrorB() throws Exception {
    Block n = js(fromString(
        "try { } catch (e) { function foo() { var e; } }"));

    TryStmt t = (TryStmt)n.children().get(0);
    CatchStmt c = (CatchStmt)t.children().get(1);
    Declaration d = findNodeWithIdentifier(n, Declaration.class, "foo");
    FunctionConstructor fc = (FunctionConstructor)d.getInitializer();

    Scope s0 = Scope.fromProgram(n, mq);
    Scope s1 = Scope.fromCatchStmt(s0, c);
    Scope.fromFunctionConstructor(s1, fc);

    assertEquals(1, mq.getMessages().size());
    assertMsgType(MessageType.MASKING_SYMBOL, mq.getMessages().get(0));
    assertMsgLevel(MessageLevel.ERROR, mq.getMessages().get(0));
  }

  public void testMaskedExceptionVariablesSame() throws Exception {
    Block outerBlock = js(fromString(
        "try { } catch (e) { try { } catch (e) { var x; } }"));

    TryStmt t0 = (TryStmt)outerBlock.children().get(0);
    CatchStmt c0 = t0.getCatchClause();
    Block b0 = (Block)c0.getBody();
    TryStmt t1 = (TryStmt)b0.children().get(0);
    CatchStmt c1 = t1.getCatchClause();

    Scope sn = Scope.fromProgram(outerBlock, mq);
    Scope sc0 = Scope.fromCatchStmt(sn, c0);
    Scope.fromCatchStmt(sc0, c1);

    assertEquals(0, mq.getMessages().size());
  }

  public void testStartStatementsForProgram() throws Exception {
    Scope s0 = Scope.fromProgram(js(fromString("{}")), mq);

    assertEquals(0, s0.getStartStatements().size());

    s0.addStartOfBlockStatement(js(fromString("{}")));
    assertEquals(1, s0.getStartStatements().size());

    s0.addStartOfScopeStatement(js(fromString("{}")));
    assertEquals(2, s0.getStartStatements().size());

    s0.declareStartOfScopeTempVariable();
    assertEquals(3, s0.getStartStatements().size());
  }

  public void testStartStatementsForPlainBlock() throws Exception {
    Scope s0 = Scope.fromProgram(js(fromString("{}")), mq);
    Scope s1 = Scope.fromPlainBlock(s0);

    assertEquals(0, s0.getStartStatements().size());
    assertEquals(0, s1.getStartStatements().size());

    s1.addStartOfBlockStatement(js(fromString("{}")));
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.addStartOfScopeStatement(js(fromString("{}")));
    assertEquals(1, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.declareStartOfScopeTempVariable();
    assertEquals(2, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());
  }

  public void testStartStatementsForCatchStmt() throws Exception {
    Scope s0 = Scope.fromProgram(js(fromString("{}")), mq);
    Block block = js(fromString("try {} catch (e) {}"));
    TryStmt t = (TryStmt)block.children().get(0);
    Scope s1 = Scope.fromCatchStmt(s0, t.getCatchClause());

    assertEquals(0, s0.getStartStatements().size());
    assertEquals(0, s1.getStartStatements().size());

    s1.addStartOfBlockStatement(js(fromString("{}")));
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.addStartOfScopeStatement(js(fromString("{}")));
    assertEquals(1, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.declareStartOfScopeTempVariable();
    assertEquals(2, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());
  }

  public void testStartStatementsForFunctionConstructor() throws Exception {
    Scope s0 = Scope.fromProgram(js(fromString("{}")), mq);
    Block block = js(fromString("function() {};"));
    FunctionConstructor fc = (FunctionConstructor)block.children().get(0).children().get(0);
    Scope s1 = Scope.fromFunctionConstructor(s0, fc);

    assertEquals(0, s0.getStartStatements().size());
    assertEquals(0, s1.getStartStatements().size());

    s1.addStartOfBlockStatement(js(fromString("{}")));
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.addStartOfScopeStatement(js(fromString("{}")));
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(2, s1.getStartStatements().size());

    s1.declareStartOfScopeTempVariable();
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(3, s1.getStartStatements().size());
  }

  public void testStartStatementsForParseTreeNodeContainer() throws Exception {
    Scope s0 = Scope.fromProgram(js(fromString("{}")), mq);
    Scope s1 = Scope.fromParseTreeNodeContainer(
        s0,
        new ParseTreeNodeContainer(new ArrayList<ParseTreeNode>()));

    assertEquals(0, s0.getStartStatements().size());
    assertEquals(0, s1.getStartStatements().size());

    s1.addStartOfBlockStatement(js(fromString("{}")));
    assertEquals(0, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.addStartOfScopeStatement(js(fromString("{}")));
    assertEquals(1, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());

    s1.declareStartOfScopeTempVariable();
    assertEquals(2, s0.getStartStatements().size());
    assertEquals(1, s1.getStartStatements().size());
  }

  public void testUnmaskableIdentifiersInCatch() throws Exception {
    Block b = js(fromString("try {} catch (Object) {}"));
    TryStmt tryStmt = (TryStmt) b.children().get(0);
    Scope top = Scope.fromProgram(b, mq);
    Scope.fromCatchStmt(top, tryStmt.getCatchClause());
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("Object"));
  }

  public void testUnmaskableIdentifiersInDeclarations() throws Exception {
    Block b = js(fromString("var Array, undefined;"));
    Scope.fromProgram(b, mq);
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("Array"));
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("undefined"));
  }

  public void testUnmaskableFormals() throws Exception {
    Block b = js(fromString("function NaN(Infinity, arguments) {}"));
    Scope top = Scope.fromProgram(b, mq);
    FunctionDeclaration fn = ((FunctionDeclaration) b.children().get(0));
    Scope.fromFunctionConstructor(top, fn.getInitializer());
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("NaN"));
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("Infinity"));
    assertMessage(
        RewriterMessageType.CANNOT_MASK_IDENTIFIER, MessageLevel.FATAL_ERROR,
        MessagePart.Factory.valueOf("arguments"));
  }

  private FunctionConstructor findFunctionConstructor(ParseTreeNode root, String name) {
    return findNodeWithIdentifier(root, FunctionConstructor.class, name);
  }

  private static class Holder<T> { T value; }

  @SuppressWarnings("unchecked")
  private <T extends ParseTreeNode> T findNodeWithIdentifier(
      ParseTreeNode root,
      final Class<T> clazz,
      final String identifierValue) {
    final Holder<T> result = new Holder<T>();

    root.acceptPreOrder(new Visitor() {
      public boolean visit(AncestorChain<?> chain) {
        if (clazz.isAssignableFrom(chain.node.getClass()) &&
            chain.node.children().size() > 0 &&
            chain.node.children().get(0) instanceof Identifier) {
          Identifier id = (Identifier)chain.node.children().get(0);
          if ((identifierValue == null && id.getValue() == null) ||
              (identifierValue != null && identifierValue.equals(id.getValue()))) {
            assertNull(result.value);
            result.value = (T)chain.node;
            return false;
          }
        }
        return true;
      }
    },
    null);

    assertNotNull(result.value);
    return result.value;
  }

  private void assertMsgType(MessageType type, Message message) {
    assertEquals(type, message.getMessageType());
  }

  private void assertMsgLevel(MessageLevel level, Message message) {
    assertTrue(level.compareTo(message.getMessageLevel()) <= 0);
  }
}
