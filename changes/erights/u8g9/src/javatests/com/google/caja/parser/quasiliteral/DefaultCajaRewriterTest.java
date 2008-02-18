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
import com.google.caja.parser.js.Block;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.TestUtil;
import com.google.caja.plugin.SyntheticNodes;
import junit.framework.TestCase;

import java.io.StringReader;
import java.net.URI;
import java.util.Collections;

/**
 * @author ihab.awad@gmail.com
 */
public class DefaultCajaRewriterTest extends TestCase {
  ////////////////////////////////////////////////////////////////////////
  // Handling of synthetic nodes
  ////////////////////////////////////////////////////////////////////////

  public void testSyntheticIsUntouched() throws Exception {
    ParseTreeNode input = parseText(
        "function foo() { this; arguments; }");
    setTreeSynthetic(input);
    checkSucceeds(input, input);
  }
  
  /**
   * Welds together a string representing the repeated pattern of expected test output for
   * assigning to an outer variable.
   *
   * @author erights@gmail.com
   */
  private static String weldSetOuters(String varName, String expr) {
	return "(function(){" +
	  	     "var x___ = (" + expr + ");" +
	  	     "return (___OUTERS___." + varName + "_canSet___ ?" +
	  				  "(___OUTERS___." + varName + " = x___) :" +
	  				  "___.setPub(___OUTERS___,'" + varName + "',x___));" +
	       "})()";
  }
  
  /**
   * Welds together a string representing the repeated pattern of expected test output for
   * reading an outer variable.
   *
   * @author erights@gmail.com
   */
  private static String weldReadOuters(String varName) {
    return "(___OUTERS___." + varName + "_canRead___ ? ___OUTERS___." + varName +
    " : ___.readPub(___OUTERS___, '" + varName + "', true))";
  }

  public void testNestedInsideSyntheticIsExpanded() throws Exception {
    ParseTreeNode innerInput = parseText("function foo() {}");
    ParseTreeNode input = ParseTreeNodes.newNodeInstance(
        Block.class,
        null,
        Collections.singletonList(innerInput));
    setSynthetic(input);
    ParseTreeNode expectedResult = parseText(
        "{" + weldSetOuters("foo", "___.simpleFunc(function foo() {})") + "}");
    checkSucceeds(input, expectedResult);
  }

  ////////////////////////////////////////////////////////////////////////
  // Handling of nested blocks
  ////////////////////////////////////////////////////////////////////////

  public void testNestedBlockWithFunction() throws Exception {
    checkSucceeds(
        "{ function foo() {} }",
        "{" + weldSetOuters("foo", "___.simpleFunc(function foo() {})") + "}");
  }

  public void testNestedBlockWithVariable() throws Exception {
    checkSucceeds(
        "{ var x = y; }",
        "{" + weldSetOuters("x", weldReadOuters("y")) + "}");
  }

  ////////////////////////////////////////////////////////////////////////
  // Specific rules
  ////////////////////////////////////////////////////////////////////////

  public void testWith() throws Exception {
    // Our parser does not recognize "with" at all.
  }

  public void testTryCatch() throws Exception {
    checkSucceeds(
        "try {" +
        "  e;" +
        "  x;" +
        "} catch (e) {" +
        "  e;" +
        "  y;" +
        "}",
        "try {" +
        weldReadOuters("e") + ";" +
        weldReadOuters("x") +
        "} catch (e) {" +
        "  e;" +
        weldReadOuters("y") +
        "}");
  }

  public void testTryCatchFinally() throws Exception {
    checkSucceeds(
        "try {" +
        "  e;" +
        "  x;" +
        "} catch (e) {" +
        "  e;" +
        "  y;" +
        "} finally {" +
        "  e;" +
        "  z;" +
        "}",
        "try {" +
        weldReadOuters("e") + ";" +
        weldReadOuters("x") +
        "} catch (e) {" +
        "  e;" +
        weldReadOuters("y") +
        "} finally {" +
        weldReadOuters("e") + ";" +
        weldReadOuters("z") +
        "}");
  }
  
  public void testTryFinally() throws Exception {
    checkSucceeds(
        "try {" +
        "  x;" +
        "} finally {" +
        "  z;" +
        "}",
        "try {" +
        weldReadOuters("x") +
        "} finally {" +
        weldReadOuters("z") +
        "}");
  }

  public void testVarArgs() throws Exception {
    checkSucceeds(
        "var foo = function() {" +
        "  p = arguments;" +
        "};",
        weldSetOuters(
            "foo", 
            "___.primFreeze(___.simpleFunc(function() {" +
            "  var a___ = ___.args(arguments);" +
               weldSetOuters("p", "a___") +
            "}))"));
  }

  public void testVarThis() throws Exception {
    checkSucceeds(
        "function foo() {" +
        "  p = this;" +
        "}",
        weldSetOuters("foo", "___.ctor(function foo() {" +
            "  var t___ = this;" +
            weldSetOuters("p", "t___") +
            "})"));
    checkSucceeds(
        "this;",
        "___OUTERS___;");
  }

  public void testVarBadSuffix() throws Exception {
    checkFails(
        "function() { foo__; };",
        "Variables cannot end in \"__\"");
    // Make sure *single* underscore is okay
    checkSucceeds(
        "function() { var foo_ = 3; }",
        "___.primFreeze(___.simpleFunc(function() { var foo_ = 3; }))");
  }

  public void testVarBadGlobalSuffix() throws Exception {
    checkFails(
        "foo_;",
        "Globals cannot end in \"_\"");
  }

  public void testVarBadCtorLeak() throws Exception {
    checkFails(
        "function Ctor() { this.x = 1; }; var c = Ctor;",
        "Constructors are not first class");
  }

  public void testVarFuncFreeze() throws Exception {
    checkSucceeds(
        "function() {"+
        "  function foo() {}" +
        "  var f = foo;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.simpleFunc(function foo() {});" +
        "  var f = ___.primFreeze(foo);" +
        "}));");
    checkSucceeds(
        "function foo() {}" +
        "var f = foo;",
        weldSetOuters("foo", "___.simpleFunc(function foo() {})") + ";" +
        weldSetOuters("f", "___.primFreeze(" + weldReadOuters("foo") + ")"));
  }

  public void testVarGlobal() throws Exception {
    checkSucceeds(
        "foo;",
        weldReadOuters("foo"));
    checkSucceeds(
        "function() {" +
        "  foo;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        weldReadOuters("foo") +
        "}));");
    checkSucceeds(
        "function() {" +
        "  var foo;" +
        "  foo;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo;" +
        "  foo;" +
        "}));");
  }

  public void testVarDefault() throws Exception {
    String unchanged =
        "var x = 3;" +
        "if (x) { }" +
        "x + 3;" +
        "var y = undefined;";
    checkSucceeds(
        "function() {" +
        unchanged +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        unchanged +
        "}));");
  }

  public void testReadBadSuffix() throws Exception {
    checkFails(
        "x.y__;",
        "Properties cannot end in \"__\"");
  }

  public void testReadInternal() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {" +
        "    p = this.x;" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo() {" +
        "    var t___ = this;" +
        weldSetOuters("p", "t___.x_canRead___ ? t___.x : ___.readProp(t___, 'x')") +
        "  });" +
        "}));");
  }

  public void testReadBadInternal() throws Exception {
    checkFails(
        "foo.bar_;",
        "Public properties cannot end in \"_\"");
  }

  public void testReadPublic() throws Exception {
    checkSucceeds(
        "p = foo.p;",
        weldSetOuters("p", "(function() {" +
            "  var x___ = " + weldReadOuters("foo") + ";" +
            "  return x___.p_canRead___ ? x___.p : ___.readPub(x___, 'p');" +
            "})()"));
  }

  public void testReadIndexInternal() throws Exception {
    checkSucceeds(
        "function foo() { p = this[3]; }",
        weldSetOuters("foo", "___.ctor(" +
        "  function foo() {" +
        "    var t___ = this;" +
			   "    " + weldSetOuters("p", "___.readProp(t___, 3)") +
        "  }" +
	")"));
  }
  
  public void testReadIndexPublic() throws Exception {
    checkSucceeds(
        "function() { var foo; p = foo[3]; };",
        "___.primFreeze(___.simpleFunc(" +
        "  function() {" +
        "    var foo;" +
        "    " + weldSetOuters("p", "___.readPub(foo, 3)") +
        "}));");
  }

  public void testSetBadSuffix() throws Exception {
    checkFails(
        "x.y__ = z;",
        "Properties cannot end in \"__\"");
  }

  public void testSetInternal() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() { this.p = x; }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo() {" +
        "    var t___ = this;" +
        "    (function() {" +
        "      var x___ = " + weldReadOuters("x") + ";" +
        "      return t___.p_canSet___ ? (t___.p = x___) : " +
        "                                ___.setProp(t___, 'p', x___);" +
        "    })();" +
        "  });" +
        "}));");
  }

  public void testSetMember() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {}" +
        "  foo.prototype.p = x;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.simpleFunc(function foo() {});" +
        "  (function() {" +
        "    var x___ = " + weldReadOuters("x") + ";" +
        "    ___.setMember(foo, 'p', x___);" +
        "  })();" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  function foo() {}" +
        "  foo.prototype.p = function(a, b) { this; }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +            
        "  var foo = ___.simpleFunc(function foo() {});" +
        "  (function() {" +
        "    var x___ = ___.method(foo, function(a, b) {" +
        "      var t___ = this;" +
        "      t___;" +
        "    });" +
        "    ___.setMember(foo, 'p', x___);" +
        "  })();" +            
        "}));");
  }

  public void testSetBadInternal() throws Exception {
    checkFails(
        "x.y_;",
        "Public properties cannot end in \"_\"");
  }

  public void testSetMemberMap() throws Exception {    
    checkFails(
        "function foo() {}" +
        "foo.prototype = x;",  
        "Map expression expected");
    checkFails(
        "function foo() {}" +
        "foo.prototype = function() {};",
        "Map expression expected");
    checkSucceeds(
        "function foo() {}" +
        "foo.prototype = { k0: v0, k1: function() { this.p = 3; } };",
        weldSetOuters("foo", "___.simpleFunc(function foo() {})") + ";" +
        "___.setMemberMap(" +
        "    " + weldReadOuters("foo") + ", {" +
        "    k0: " + weldReadOuters("v0") + "," +
        "    k1: ___.method(foo, function() { " +
        "      var t___ = this;" +
        "      (function() {" +
        "        var x___ = 3;" +
        "        return t___.p_canSet___ ? (t___.p = x___) : " +
        "                                  ___.setProp(t___, 'p', x___);" +
        "      })();" +
        "    })" +
        "});");
  }

  public void testSetStatic() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {}" +
        "  foo.p = x;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.simpleFunc(function foo() {});" +
        "  ___.setPub(foo, 'p', " + weldReadOuters("x") + ");" +
        "}));");
  }

  public void testSetPublic() throws Exception {
    checkSucceeds(
        "function() {" +
        "  var x = undefined;" +
        "  x.p = y;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var x = undefined;" +
        "  (function() {" +
        "    var x___ = x;" +
        "    var x0___ = " + weldReadOuters("y") + ";" +
        "    return x___.p_canSet___ ? (x___.p = x0___) : " +
        "                              ___.setPub(x___, 'p', x0___);" +
        "  })();" +
        "}));");
  }

  public void testSetIndexInternal() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {" +
        "    this[x] = y;" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo() {" +
        "    var t___ = this;" +
        "    ___.setProp(t___, " + weldReadOuters("x") + ", " + weldReadOuters("y") + ");" +
        "  });" +
        "}));");
  }

  public void testSetIndexPublic() throws Exception {
    checkSucceeds(
        "function() {" +
        "  var o = undefined;" +
        "  o[x] = y;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var o = undefined;" +
        "  ___.setPub(o, " + weldReadOuters("x") + ", " + weldReadOuters("y") + ");" +
        "}));");
  }

  public  void testSetInitialize() throws Exception {
    checkSucceeds(
        "function() {" +
        "  var v = x;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var v = " + weldReadOuters("x") + ";" +
        "}));");
    checkSucceeds(
        "var v = x",
        weldSetOuters("v", weldReadOuters("x")));
  }  
  
  public  void testSetDeclare() throws Exception {
    checkSucceeds(
        "function() {" +
        "  var v;" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var v;" +
        "}));");
    checkSucceeds(
        "var v;",
        "___.setPub(___OUTERS___, 'v', ___.readPub(___OUTERS___, 'v'));");
  }

  public void testNewCtor() throws Exception {
    checkSucceeds(
        "function foo() { this.p = 3; }" +
        "new foo(x, y);",
        weldSetOuters("foo", 
            "___.ctor(function foo() {" +
            "  var t___ = this;" +
            "  (function() {" +
            "    var x___ = 3;" +
            "    return t___.p_canSet___ ? (t___.p = x___) : " +
            "                              ___.setProp(t___, 'p', x___);" +
            "  })();" +
            "})") + ";" +
        "new (___.asCtor(" + weldReadOuters("foo") + "))(" + weldReadOuters("x") + ", " + weldReadOuters("y") + ");");
    checkSucceeds(
        "function foo() {}" +
        "new foo(x, y);",
        weldSetOuters("foo", "___.simpleFunc(function foo() {})") + ";" +
        "new (___.asCtor(" + weldReadOuters("foo") + "))(" + weldReadOuters("x") + ", " + weldReadOuters("y") + ");");
  }

  public void testNewBadCtor() throws Exception {
    checkFails(
        "new foo.bar();",
        "Cannot invoke \"new\" on an arbitrary expression");
    checkFails(
        "new 3();",
        "Cannot invoke \"new\" on an arbitrary expression");
    checkFails(
        "new (x + y)();",
        "Cannot invoke \"new\" on an arbitrary expression");
  }

  public void testNewFunc() throws Exception {
    checkSucceeds(
        "function() {" +
        "  new x(y, z);" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  new (___.asCtor(" + weldReadOuters("x") + "))(" + weldReadOuters("y") + ", " + weldReadOuters("z") + ");" +
        "}));");
  }

  public void testCallBadSuffix() throws Exception {
    checkFails(
        "x.p__(3, 4);",
        "Selectors cannot end in \"__\"");
  }
  
  public void testCallInternal() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {" +
        "    this.f(x, y);" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo() {" +
        "    var t___ = this;" +
        "    (function() {" +
        "      var x0___ = " + weldReadOuters("x") + ";" +
        "      var x1___ = " + weldReadOuters("y") + ";" +
        "      return t___.f_canCall___ ?" +
        "          this.f(x0___, x1___) :" +
        "          ___.callProp(t___, 'f', [x0___, x1___]);" +
        "    })();" +
        "  });" +
        "}));");
  }

  public void testCallBadInternal() throws Exception {
    checkFails(
        "o.p_();",
        "Public selectors cannot end in \"_\"");
  }

  public void testCallCajaDef2() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function Point() {}" +
        "  caja.def(Point, Object);" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point);" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var Point = ___.simpleFunc(function Point() {});" +
        "  caja.def(Point, Object);" +
        "  var WigglyPoint = ___.simpleFunc(function WigglyPoint() {});" +
        "  caja.def(WigglyPoint, Point);" +
        "}));");
  }

  public void testCallCajaDef2Bad() throws Exception {
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  caja.def(Point, Array);" +
        "};",
        "caja.def called with non-constructor");
    checkFails(
        "function() {" +
        "  var Point = 3;" +
        "  caja.def(Point, Object);" +
        "};",
        "caja.def called with non-constructor");
  }

  public void testCallCajaDef3Plus() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function Point() {}" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point, { m0: x, m1: function() { this.p = 3; } });" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var Point = ___.simpleFunc(function Point() {});" +
        "  var WigglyPoint = ___.simpleFunc(function WigglyPoint() {});" +
        "  caja.def(WigglyPoint, Point, {" +
        "      m0: " + weldReadOuters("x") + "," +
        "      m1: ___.method(WigglyPoint, function() {" +
        "        var t___ = this;" +
        "        (function() {" +
        "          var x___ = 3;" +
        "          return t___.p_canSet___ ? (t___.p = x___) : " +
        "                                    ___.setProp(t___, 'p', x___);" +
        "        })();" +            
        "      })" +
        "  });" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  function Point() {}" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point," +
        "      { m0: x, m1: function() { this.p = 3; } }," +
        "      { s0: y, s1: function() { return 3; } });" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var Point = ___.simpleFunc(function Point() {});" +
        "  var WigglyPoint = ___.simpleFunc(function WigglyPoint() {});" +
        "  caja.def(WigglyPoint, Point, {" +
        "      m0: " + weldReadOuters("x") + "," +
        "      m1: ___.method(WigglyPoint, function() {" +
        "        var t___ = this;" +
        "        (function() {" +
        "          var x___ = 3;" +
        "          return t___.p_canSet___ ? (t___.p = x___) : " +
        "                                    ___.setProp(t___, 'p', x___);" +
        "        })();" +
        "      })" +
        "  }, {" +
        "      s0: " + weldReadOuters("y") + "," +
        "      s1: ___.primFreeze(___.simpleFunc(function() { return 3; }))" +
        "  });" +            
        "}));");
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point, x);" +
        "};",
        "Map expression expected");
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point, { foo: x }, x);" +
        "};",
        "Map expression expected");
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  function WigglyPoint() {}" +
        "  caja.def(WigglyPoint, Point, { foo: x }, { bar: function() { this.x = 3; } });" +
        "};",
        "Method in non-method context");
  }

  public void testCallCajaDef3PlusBad() throws Exception {
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  caja.def(Point, Array, {});" +
        "};",
        "caja.def called with non-constructor");
    checkFails(
        "function() {" +
        "  var Point = 3;" +
        "  caja.def(Point, Object, {});" +
        "};",
        "caja.def called with non-constructor");
    checkFails(
        "function() {" +
        "  function Point() {}" +
        "  caja.def(Point, Array, {}, {});" +
        "};",
        "caja.def called with non-constructor");
    checkFails(
        "function() {" +
        "  var Point = 3;" +
        "  caja.def(Point, Object, {}, {});" +
        "};",
        "caja.def called with non-constructor");
  }

  public void testCallPublic() throws Exception {
    checkSucceeds(
        "o.m(x, y);",
        "(function() {" +
        "  var x___ = " + weldReadOuters("o") + ";" +
        "  var x0___ = " + weldReadOuters("x") + ";" +
        "  var x1___ = " + weldReadOuters("y") + ";" +
        "  return x___.m_canCall___ ?" +
        "    x___.m(x0___, x1___) :" +
        "    ___.callPub(x___, 'm', [x0___, x1___]);" +
        "})();");
  }

  public void testCallIndexInternal() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo() {" +
        "    this[x](y, z);" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo() {" +
        "    var t___ = this;" +
        "    ___.callProp(t___, " + weldReadOuters("x") + ", [" + weldReadOuters("y") + ", " + weldReadOuters("z") + "]);" +
        "  });" +
        "}));");
  }

  public void testCallIndexPublic() throws Exception {
    checkSucceeds(
        "x[y](z, t);",
        "___.callPub(" + weldReadOuters("x") + ", " + weldReadOuters("y") + ", [" + weldReadOuters("z") + ", " + weldReadOuters("t") + "]);");
  }

  public void testCallFunc() throws Exception {
    checkSucceeds(
        "f(x, y);",
        "___.asSimpleFunc(" + weldReadOuters("f") + ")(" + weldReadOuters("x") + ", " + weldReadOuters("y") + ");");
  }

  public void testFuncAnonSimple() throws Exception {
    checkSucceeds(
        "function(x, y) { x = arguments; y = z; };",
        "___.primFreeze(___.simpleFunc(function(x, y) {" +
        "  var a___ = ___.args(arguments);" +
        "  x = a___;" +
        "  y = " + weldReadOuters("z") + ";" +
        "}));");
  }

  public void testFuncNamedSimpleDecl() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo(x, y) {" +
        "    x = arguments;" +
        "    y = z;" +
        "    return foo(x - 1, y - 1);" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.simpleFunc(function foo(x, y) {" +
        "      var a___ = ___.args(arguments);" +
        "      x = a___;" +
        "      y = " + weldReadOuters("z") + ";" +
        "      return ___.asSimpleFunc(___.primFreeze(foo))(x - 1, y - 1);" +
        "  });"+
        "}));");
    checkSucceeds(
        "function foo(x, y ) {" +
        "  return foo(x - 1, y - 1);" +
        "}",
        weldSetOuters("foo", "___.simpleFunc(function foo(x, y) {" +
        "  return ___.asSimpleFunc(___.primFreeze(foo))(x - 1, y - 1);" +
			   "})"));
  }

  public void testFuncNamedSimpleValue() throws Exception {
    checkSucceeds(
        "function() {" +
        "  var f = function foo(x, y) {" +
        "    x = arguments;" +
        "    y = z;" +
        "    return foo(x - 1, y - 1);" +
        "  };" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var f = ___.primFreeze(___.simpleFunc(" +
        "    function foo(x, y) {" +
        "      var a___ = ___.args(arguments);" +
        "      x = a___;" +
        "      y = " + weldReadOuters("z") + ";" +
        "      return ___.asSimpleFunc(___.primFreeze(foo))(x - 1, y - 1);" +            
        "  }));"+
        "}));");
    checkSucceeds(
        "var foo = function foo(x, y ) {" +
        "  return foo(x - 1, y - 1);" +
        "}",
        weldSetOuters("foo", "___.primFreeze(___.simpleFunc(function foo(x, y) {" +
        "  return ___.asSimpleFunc(___.primFreeze(foo))(x - 1, y - 1);" +
			   "}))"));
  }

  public void testFuncBadMethod() throws Exception {
    checkFails(
        "function(x) { x = this; };",
        "Method in non-method context");
  }

  public void testFuncBadCtor() throws Exception {
    checkFails(
        "var f = function foo(x) { x = this; };",
        "Constructor cannot escape");
  }

  public void testFuncDerivedCtorDecl() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo(x, y) {" +
        "    foo.Super.call(this, x + y);" +
        "    y = z;" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo(x, y) {" +
        "    var t___ = this;" +
        "    foo.Super.call(t___, x + y);" +
        "    y = " + weldReadOuters("z") + ";" +
        "  });" +
        "}));");
  }

  public void testFuncCtorDecl() throws Exception {
    checkSucceeds(
        "function() {" +
        "  function foo(x, y) {" +
        "    x = this;" +
        "    y = z;" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var foo = ___.ctor(function foo(x, y) {" +
        "    var t___ = this;" +
        "    x = t___;" +
        "    y = " + weldReadOuters("z") + ";" +            
        "  });" +
        "}));");
  }

  public void testMapEmpty() throws Exception {
    checkSucceeds(
        "f = {};",
        weldSetOuters("f", "{}"));
  }

  public void testMapBadKeySuffix() throws Exception {
    checkFails(
        "var o = { x_: 3 };",
        "Key may not end in \"_\"");
  }

  public void testMapNonEmpty() throws Exception {
    checkSucceeds(
        "var o = { k0: x, k1: y };",
        weldSetOuters("o", "{ k0: " + weldReadOuters("x") + ", k1: " + weldReadOuters("y") + " }"));
  }

  public void testOtherInstanceof() throws Exception {
    checkSucceeds(
        "function foo() {}" +
        "x instanceof foo;",      
        weldSetOuters("foo", "___.simpleFunc(function foo() {})") + ";" +
        weldReadOuters("x") + " instanceof ___.primFreeze(" + weldReadOuters("foo") + ");");
  }

  public void testOtherBadInstanceof() throws Exception {
    checkFails(
        "var x = 3; y instanceof x;",
        "Invoked \"instanceof\" on non-function");
  }

  public void testMultiDeclaration() throws Exception {
    // 'var' in global scope, part of a block
    checkSucceeds(
        "var x, y;",
        "___.setPub(___OUTERS___, 'x', ___.readPub(___OUTERS___, 'x')), " +
        "___.setPub(___OUTERS___, 'y', ___.readPub(___OUTERS___, 'y'));");
    checkSucceeds(
        "var x = foo, y = bar;",
        weldSetOuters("x", weldReadOuters("foo")) + ", " + 
        weldSetOuters("y", weldReadOuters("bar")) + ";");
    checkSucceeds(
        "var x, y = bar;",
        "___.setPub(___OUTERS___, 'x', ___.readPub(___OUTERS___, 'x')), " + 
        weldSetOuters("y", weldReadOuters("bar")) + ";");
    // 'var' in global scope, 'for' statement
    checkSucceeds(
        "for (var x, y; ; ) {}",
        "for (___.setPub(___OUTERS___, 'x', ___.readPub(___OUTERS___, 'x')), " +
        "___.setPub(___OUTERS___, 'y', ___.readPub(___OUTERS___, 'y')); ; ) {}");
    checkSucceeds(
        "for (var x = foo, y = bar; ; ) {}",
        "for (" + weldSetOuters("x", weldReadOuters("foo")) + ", " + 
        weldSetOuters("y", weldReadOuters("bar")) + "; ; ) {}");
    checkSucceeds(
        "for (var x, y = bar; ; ) {}",
        "for (___.setPub(___OUTERS___, 'x', ___.readPub(___OUTERS___, 'x')), " +
        weldSetOuters("y", weldReadOuters("bar")) + "; ; ) {}");
    // 'var' in global scope, part of a block
    checkSucceeds(
        "function() {" +
        "  var x, y;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var x, y;" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  var x = foo, y = bar;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var x = " + weldReadOuters("foo") + ", y = " + weldReadOuters("bar") + ";" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  var x, y = bar;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var x, y = " + weldReadOuters("bar") + ";" +
        "}));");
    // 'var' in global scope, 'for' statement        
    checkSucceeds(
        "function() {" +
        "  for (var x, y; ; ) {}" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  for (var x, y; ; ) {}" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  for (var x = foo, y = bar; ; ) {}" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  for (var x = " + weldReadOuters("foo") + ", y = " + weldReadOuters("bar") + "; ; ) {}" +
        "}));");
    checkSucceeds(
        "function() {" +
        "  for (var x, y = bar; ; ) {}" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  for (var x, y = " + weldReadOuters("bar") + "; ; ) {}" +
        "}));");
  }

  public void testRecurseParseTreeNodeContainer() throws Exception {
    // Tested implicitly by other cases
  }

  public void testRecurseBlock() throws Exception {
    // Tested implicitly by other cases
  }

  public void testRecurseConditional() throws Exception {
    checkSucceeds(
        "if (x === y) {" +
        "  z;" +
        "} else if (z === y) {" +
        "  x;" +
        "} else {" +
        "  y;" +
        "}",
        "if (" + weldReadOuters("x") + " === " + weldReadOuters("y") + ") {" +
        weldReadOuters("z") +
        "} else if (" + weldReadOuters("z") + " === " + weldReadOuters("y") + ") {" +
        weldReadOuters("x") +
        "} else {" +
        weldReadOuters("y") +
        "}");
  }

  public void testRecurseExpressionStmt() throws Exception {
    // Tested implicitly by other cases
  }

  public void testRecurseIdentifier() throws Exception {
    // Tested implicitly by other cases
  }

  public void testRecurseLiteral() throws Exception {
    checkSucceeds(
        "3;",
        "3;");
  }

  public void testRecurseLoop() throws Exception {
    checkSucceeds(
        "for (var k = 0; k < 3; k++) {" +
        "  x;" +
        "}",
        "for (" + weldSetOuters("k", "0") + "; " + 
        weldReadOuters("k") + " < 3;" +
        " (function () {" +
        "  var x___ = Number(___.readPub(___OUTERS___, 'k', true));" +
        "  ___.setPub(___OUTERS___, 'k', x___ + 1);" +
        "  return x___;" +
        "})()) {" +
        weldReadOuters("x") +
        "}");
    checkSucceeds(
        "function() {" +
        "  for (var k = 0; k < 3; k++) {" +
        "    x;" +
        "  }" +
        "};",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  for (var k = 0; k < 3; k++) {" +
        "    " + weldReadOuters("x") + ";" +
        "  }" +
        "}));");
  }

  public void testRecurseNoop() throws Exception {
    checkSucceeds(
        ";",
        ";");
  }

  public void testRecurseOperation() throws Exception {
    checkSucceeds(
        "x + y;",
        weldReadOuters("x") + " + " + weldReadOuters("y"));
  }

  public void testRecurseReturnStmt() throws Exception {
    checkSucceeds(
        "return x;",
        "return " + weldReadOuters("x") + ";");
  }

  public void testRecurseThrowStmt() throws Exception {
    checkSucceeds(
        "throw x;",
        "throw " + weldReadOuters("x") + ";");
    checkSucceeds(
        "function() {" +
        "  var x;" +
        "  throw x;" +
        "}",
        "___.primFreeze(___.simpleFunc(function() {" +
        "  var x;" +
        "  throw x;" +
        "}));");
  }

  public void testSpecimenClickme() throws Exception {
    checkSucceeds(readResource("clickme.js"));
  }

  public void testSpecimenListfriends() throws Exception {
    checkSucceeds(readResource("listfriends.js"));
  }

  private void setSynthetic(ParseTreeNode n) {
    n.getAttributes().set(SyntheticNodes.SYNTHETIC, true);
  }

  private void setTreeSynthetic(ParseTreeNode n) {
    setSynthetic(n);
    for (ParseTreeNode child : n.children()) {
      setTreeSynthetic(child);
    }
  }

  private void checkFails(String input, String error) throws Exception {
    MessageContext mc = new MessageContext();
    MessageQueue mq = TestUtil.createTestMessageQueue(mc);
    new DefaultCajaRewriter(true).expand(parseText(input), mq);

    StringBuilder messageText = new StringBuilder();
    for (Message m : mq.getMessages()) {
      m.format(mc, messageText);
      messageText.append("\n");
    }
    assertTrue(
        "Messages do not contain \"" + error + "\": " + messageText.toString(),
        messageText.toString().contains(error));
  }

  private void checkSucceeds(
      ParseTreeNode inputNode,
      ParseTreeNode expectedResultNode)
      throws Exception{
    MessageQueue mq = TestUtil.createTestMessageQueue(new MessageContext());
    ParseTreeNode actualResultNode = new DefaultCajaRewriter().expand(inputNode, mq);
    for (Message m : mq.getMessages()) {
      if (m.getMessageLevel().compareTo(MessageLevel.WARNING) >= 0) {
        fail(m.toString());
      }
    }
    if (expectedResultNode != null) {
      assertEquals(
          format(expectedResultNode),
          format(actualResultNode));
    }
  }

  private void checkSucceeds(String input, String expectedResult) throws Exception {
    checkSucceeds(parseText(input), parseText(expectedResult));
  }

  private void checkSucceeds(String input) throws Exception {
    checkSucceeds(parseText(input), null);
  }

  private static String format(ParseTreeNode n) throws Exception {
    StringBuilder output = new StringBuilder();
    n.render(new RenderContext(new MessageContext(), output));
    // Alternative, to get "S-expression" tree representation for debug:
    //   n.format(new MessageContext(), output);
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

  private String readResource(String resource) throws Exception {
    return TestUtil.readResource(getClass(), resource);    
  }
}
