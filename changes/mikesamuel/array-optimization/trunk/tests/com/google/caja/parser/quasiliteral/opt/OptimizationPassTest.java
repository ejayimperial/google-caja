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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.quasiliteral.opt.OptimizationPass;
import com.google.caja.parser.quasiliteral.opt.ScopeTree;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.Pair;

public class OptimizationPassTest extends CajaTestCase {
  public void testMultipleOptimizations() throws Exception {
    List<Pair<String, String>> opts = new ArrayList<Pair<String, String>>();
    opts.add(Pair.pair("(@x)++", "++@x"));
    opts.add(Pair.pair("(@x)--", "--@x"));
    OptimizationPass p = new OptimizationPass(opts) {
      @Override
      protected boolean isOptimizationSemanticsPreserving(
          ScopeTree scopeTree, AncestorChain<?> candidate,
          Map<String, ParseTreeNode> bindings) {
        if (!(candidate.node instanceof Operation)) {
          return false;
        }
        AncestorChain<?> p = candidate;
        while (p.parent.node instanceof Operation
               && (Operator.COMMA == p.parent.node.getValue()
                   || Operator.VOID == p.parent.node.getValue())) {
          if (p.node == p.parent.node.children().get(0)) { return true; }
          p = p.parent;
        }
        if (!(p.parent.node instanceof ExpressionStmt)) {
          return false;
        }
        return true;
      }
    };

    ParseTreeNode actual = p.optimize(js(fromString(
        ""
        + "var a = 0, b = a++;\n"
        + "a++;\n"
        + "for (var i = 0; i < b; i++, b++) {\n"
        + "  if (b === 10) {\n"
        + "    b = (i--, a++);\n"
        + "  } else {\n"
        + "    a += b++;\n"
        + "  }\n"
        + "}")));

    assertEquals(
        render(js(fromString(
            ""
            + "var a = 0, b = a++;\n"  // Used
            + "++a;\n"  // Not used
            + "for (var i = 0; i < b; ++i, ++b) {\n"  // Neither used
            + "  if (b === 10) {\n"
            + "    b = (--i, a++);\n"  // Second used
            + "  } else {\n"
            + "    a += b++;\n"  // Used
            + "  }\n"
            + "}"))),
        render(actual));
  }

  public void testScopeTrees() throws Exception {
    OptimizationPass p = new ArrayIndexOptimization();

    ParseTreeNode actual = p.optimize(js(fromString(
        ""
        + "(function (o) {\n"
        + "  var factor = 1;\n"
        + "  for (var i = 0; i < o.length; ++i) {\n"
        + "    factor *= f(___.readPub(o, i));\n"
        + "  }\n"
        + "})();\n"
        + "(function (o, j) {\n"
        + "  var i = 'hello';\n"
        + "  var k = 0;\n"
        + "  function f() { k += j; }\n"
        + "  while (j < 10) {\n"
        + "    g(___.readPub(o, i), ___.readPub(o, j), ___.readPub(o, k));\n"
        + "    j++;\n"
        + "  }\n"
        + "})();\n"
        + "(function () {\n"
        + "  var a = 0, b = 0;\n"
        + "  a += 'hello__';\n"
        + "  return function (o) {\n"
        + "    return [___.readPub(o, a), ___.readPub(o, b)];\n"
        + "  };\n"
        + "})();\n"
        + "function f(o) {\n"
        + "  return ___.readPub(o, 1) + ___.readPub(o, 'foo')\n"
        + "      + ___.readPub(o, ++o) + ___.readPub(o, o + 1)\n"
        + "      + ___.readPub(o, g(o));\n"
        + "}\n"
        + "function g(o) {\n"
        + "  var t0___, t1___;\n"
        + "  return t0___ = o, t1___ = ++n, ___.readPub(t0___, t1___);\n"
        + "}")));

    assertEquals(
        render(js(fromString(
            ""
            + "(function (o) {\n"
            + "  var factor = 1;\n"
            + "  for (var i = 0; i < o.length; ++i) {\n"
            + "    factor *= f(o[+i]);\n"
            + "  }\n"
            + "})();\n"
            + "(function (o, j) {\n"
            + "  var i = 'hello';\n"
            + "  var k = 0;\n"
            + "  function f() { k += j; }\n"
            + "  while (j < 10) {\n"
            + "    g(___.readPub(o, i), ___.readPub(o, j),\n"
            + "      ___.readPub(o, k));\n"
            + "    j++;\n"
            + "  }\n"
            + "})();\n"
            + "(function () {\n"
            + "  var a = 0, b = 0;\n"
            + "  a += 'hello__';\n"
            + "  return function (o) {\n"
            + "    return [___.readPub(o, a), o[+b]];\n"
            + "  };\n"
            + "})();\n"
            + "function f(o) {\n"
            + "  return o[1] + ___.readPub(o, 'foo')\n"
            + "      + o[++o] + ___.readPub(o, o + 1)\n"
            + "      + ___.readPub(o, g(o));\n"
            + "}\n"
            + "function g(o) {\n"
            + "  var t0___, t1___;\n"
            + "  return t0___ = o, t1___ = ++n, t0___[+t1___];\n"
            + "}"))),
        render(actual));
  }
}
