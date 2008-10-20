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

package com.google.caja.parser.quasiliteral.opt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.OperatorType;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.Statement;
import com.google.caja.parser.quasiliteral.QuasiBuilder;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.RhinoTestBed;

public class ArrayIndexOptimizationTest extends CajaTestCase {
  public void testIsNumberOrUndefOperator() throws IOException {
    List<Statement> stmts = new ArrayList<Statement>();
    for (Operator op : Operator.values()) {
      if (!ArrayIndexOptimization.isNumberOrUndefOperator(op)) { continue; }
      Reference[] operands = new Reference[ARITY.get(op.getType())];
      fillOperands(op, 0, operands, stmts);
    }
    runArrayOptOperatorTest(stmts);
  }

  public void testTestFramework() throws IOException {
    List<Statement> stmts = new ArrayList<Statement>();
    fillOperands(Operator.ADDITION, 0, new Reference[2], stmts);
    try {
      runArrayOptOperatorTest(stmts);
    } catch (AssertionFailedError e) {
      return;
    }
    fail("Expected failure on foo+foo");
  }

  public void testDoesVarReferenceArrayMember() throws Exception {
    ScopeTree global = new ScopeTree(AncestorChain.instance(js(fromString(
        ""
        + "var n = 3;"
        + "return function (arr) {"
        + "  var i = 0, j = 0, k, l = 0;"
        + "  for (var i = 0; i < arr.length; ++i) { arr[i] += j; }"
        + "  j = arr[0].toString();"
        + "  k = arr[1] ? i * 2 : i;"
        + "  (function () {"
        + "     l += x;"
        + "   })();"
        + "  return arr[i][j][k];"
        + "};"))),
        mq);
    ScopeTree inner = global.children().get(0);
    assertTrue(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("i")), inner, new HashSet<String>()));
    // Can't determine what arr[0].toString() is.
    assertFalse(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("j")), inner, new HashSet<String>()));
    // i is, and k is defined in terms of numeric operations on i.
    // As long as this works, single use temporary variables will not prevent
    // this optimization from working.
    assertTrue(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("k")), inner, new HashSet<String>()));
    // l is modified in a closure using a value that is not provably numeric.
    assertFalse(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("l")), inner, new HashSet<String>()));
    // n is defined in an outer scope, but all uses of it are numeric.
    assertTrue(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("n")), inner, new HashSet<String>()));
    // Initialization of arr is out of the control of the code sampled.
    assertFalse(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("arr")), inner, new HashSet<String>()));
    // x is not defined in this scope, so must be suspect
    assertFalse(ArrayIndexOptimization.doesVarReferenceArrayMember(
        new Reference(new Identifier("x")), inner, new HashSet<String>()));
  }

  private static Map<OperatorType, Integer> ARITY
      = new EnumMap<OperatorType, Integer>(OperatorType.class);
  static {
    ARITY.put(OperatorType.BRACKET, 2);
    ARITY.put(OperatorType.INFIX, 2);
    ARITY.put(OperatorType.POSTFIX, 1);
    ARITY.put(OperatorType.PREFIX, 1);
    ARITY.put(OperatorType.TERNARY, 3);
  }

  /** Correspond to the global vars defined in array-opt-operator-test.js */
  private static Reference[] REFERENCES = {
    new Reference(new Identifier("undefined")),
    new Reference(new Identifier("nullValue")),
    new Reference(new Identifier("zero")),
    new Reference(new Identifier("negOne")),
    new Reference(new Identifier("posOne")),
    new Reference(new Identifier("truthy")),
    new Reference(new Identifier("untruthy")),
    new Reference(new Identifier("emptyString")),
    new Reference(new Identifier("numberLikeString")),
    new Reference(new Identifier("fooString")),
    new Reference(new Identifier("lengthString")),
    new Reference(new Identifier("anObj")),
    new Reference(new Identifier("sneaky")),
    new Reference(new Identifier("f")),
  };

  private void runArrayOptOperatorTest(List<Statement> stmts)
      throws IOException {
    RhinoTestBed.runJs(
        new RhinoTestBed.Input(
            getClass(), "/com/google/caja/plugin/asserts.js"),
        new RhinoTestBed.Input(
            getClass(), "array-opt-operator-test.js"),
        new RhinoTestBed.Input(
            render(new Block(stmts)), getName()));
  }

  private static void fillOperands(
      Operator op, int operandIdx, Reference[] operands, List<Statement> out) {
    if (operandIdx == operands.length) {
      out.add(new ExpressionStmt((Expression) QuasiBuilder.substV(
          "requireArrayMember(function () { return @e; });",
          "e", Operation.create(op, operands))));
      return;
    }
    for (Reference r : REFERENCES) {
      operands[operandIdx] = r;
      fillOperands(op, operandIdx + 1, operands, out);
    }
  }
}
