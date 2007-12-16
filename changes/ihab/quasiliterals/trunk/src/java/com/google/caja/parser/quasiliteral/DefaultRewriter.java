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

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Conditional;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Loop;
import com.google.caja.parser.js.Noop;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.ReturnStmt;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.plugin.ReservedNames;
import com.google.caja.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites a JavaScript parse tree to comply with Caja rules.
 *
 * <p>This class is separate from its superclass to (a) make the split between "rules" and
 * plumbing a bit clearer; and (b) allow us to experiment with alternative sets of rules if
 * we so choose.
 *
 * <p>TODO(ihab.awad): All exceptions must be CajaExceptions.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DefaultRewriter extends AbstractRewriter {
  private ParseTreeNode getFunctionHeadDeclarations(Scope scope) {
    List<ParseTreeNode> stmts = new ArrayList<ParseTreeNode>();

    if (scope.hasFreeArguments()) {
       stmts.add(substV(
           "var " + ReservedNames.LOCAL_ARGUMENTS + " = " + ReservedNames.ARGUMENTS));
    }
    if (scope.hasFreeThis()) {
      stmts.add(substV(
          "var " + ReservedNames.LOCAL_THIS + " = " + ReservedNames.THIS));
    }

    return new ParseTreeNodeContainer(stmts);
  }

  private Pair<ParseTreeNode, ParseTreeNode> reuse(
      String variableName,
      ParseTreeNode value,
      Scope scope) {
    ParseTreeNode ref = new Reference(new Identifier(variableName));
    return new Pair<ParseTreeNode, ParseTreeNode>(
        ref,
        substV(
            "ref", ref,
            "rhs", expand(value, scope),
            "var @ref = @rhs;"));    
  }

  private Pair<ParseTreeNode, ParseTreeNode> reuseAll(
      ParseTreeNode arguments,
      Scope scope) {
    List<ParseTreeNode> refs = new ArrayList<ParseTreeNode>();
    List<ParseTreeNode> rhss = new ArrayList<ParseTreeNode>();

    for (int i = 0; i < arguments.children().size(); i++) {
      Pair<ParseTreeNode, ParseTreeNode> p = reuse(
          "x" + i + "___",
          arguments.children().get(i),
          scope);
      refs.add(p.a);
      rhss.add(p.b);
    }

    return new Pair<ParseTreeNode, ParseTreeNode>(
        new ParseTreeNodeContainer(refs),
        new ParseTreeNodeContainer(rhss));
  }

  private ParseTreeNode expandDef(
      ParseTreeNode symbol,
      ParseTreeNode value,
      Scope scope) {
    if (!(symbol instanceof Reference)) {
      throw new RuntimeException("expandDef on non-Reference: " + symbol);
    }
    return scope.isGlobal(symbol) ?
        new ExpressionStmt((Expression)substV(
            "s", symbol,
            "v", value,
            "___OUTERS___.@s = @v")) :
        substV(
            "s", symbol.children().get(0),
            "v", value,
            "var @s = @v");
  }

  private ParseTreeNode expandMember(
      ParseTreeNode clazz,
      ParseTreeNode member,
      Scope scope) {
    return null;  // TODO(ihab.awad) -- what's to do here?
  }

  private ParseTreeNode expandMemberMap(
      ParseTreeNode clazz,
      ParseTreeNode rhsValue,
      Scope scope) {
    return null;  // TODO(ihab.awad) -- what's to do here?
  }

  private boolean isSynthetic(ParseTreeNode node) {
    return false;  // TODO(ihab.awad) -- what's to do here?
  }

  public ParseTreeNode doExpand(ParseTreeNode node, Scope scope) {
    Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();

    // TODO(ihab.awad): BUG: Throw CajaException since these will eventually bubble
    // up as "compiler" error messages against user code.

    ////////////////////////////////////////////////////////////////////////
    // Do nothing if the node is already the result of some translation
    ////////////////////////////////////////////////////////////////////////

    if (isSynthetic(node)) {
      return node.clone();
    }

    ////////////////////////////////////////////////////////////////////////
    // with - the evil "with" builtin
    ////////////////////////////////////////////////////////////////////////

    // with_0
    if (match(node, bindings, "with")) {
      throw new RuntimeException("\"with\" disallowed");
    }

    ////////////////////////////////////////////////////////////////////////
    // variable - variable name handling
    ////////////////////////////////////////////////////////////////////////

    // variable_0
    if (match(node, bindings, ReservedNames.ARGUMENTS)) {
      return subst(bindings, ReservedNames.LOCAL_ARGUMENTS);
    }

    // variable_1
    if (match(node, bindings, ReservedNames.THIS)) {
      return subst(bindings, ReservedNames.LOCAL_THIS);
    }

    // variable_2
    if (match(node, bindings, "@x__")) {
      throw new RuntimeException("Variables cannot end in \"__\": " + node);
    }

    // variable_3
    if (match(node, bindings, "@x_")) {
      String symbol = ((Identifier)bindings.get("x")).getValue() + "_";
      if (scope.isGlobal(symbol)) {
        throw new RuntimeException("Globals cannot end in \"_\": " + node);
      }
    }

    if (match(node, bindings, "@x") &&
        bindings.get("x") instanceof Reference) {
      // variable_4
      if (scope.isConstructor(bindings.get("x"))) {
        throw new RuntimeException("Constructors are not first class: " + node);
      }

      // variable_5
      if (scope.isFunction(bindings.get("x"))) {
        return subst(
            bindings,
            "___.primFreeze(@x)");
      }

      // variable_6
      if (scope.isGlobal(bindings.get("x"))) {
        return subst(
            bindings,
            "___OUTERS___.@x");
      }

      // variable_7
      return bindings.get("x");
    }

    ////////////////////////////////////////////////////////////////////////
    // read - reading values
    ////////////////////////////////////////////////////////////////////////

    // read_0
    if (match(node, bindings, "@x.@y__")) {
      throw new RuntimeException("Properties cannot end in \"__\": " + bindings.get("y"));
    }

    // read_1
    if (match(node, bindings, "this.@p")) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      return substV(
        "p",  bindings.get("p"),
        "fp", new Reference(new Identifier(propertyName + "_canRead___")),
        "rp", new StringLiteral(propertyName),
        "this.@fp ? this.@p : ___.readProp(this, @rp)");
    }

    // read_2
    if (match(node, bindings, "@x.@y_")) {
      throw new RuntimeException("Public properties cannot end in \"_\": " + bindings.get("y"));
    }

    // read_3
    if (match(node, bindings, "@o.@p")) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      return substV(
          "o",  expand(bindings.get("o"), scope),
          "p",  bindings.get("p"),
          "fp", new Reference(new Identifier(propertyName + "_canRead___")),
          "rp", new StringLiteral(propertyName),
          "(function() {" +
          "  var x___ = @o;" +
          "  @o.@fp ? @o.@p : ___.readPub(x___, @rp);" +
          "})()");
    }

    // read_4
    if (match(node, bindings, "this[@s]")) {
      return substV(
          "s", expand(bindings.get("s"), scope),
          "___.readProp(this, @s)");
    }

    // read_5
    if (match(node, bindings, "@o[@s]")) {
      return substV(
          "o", expand(bindings.get("o"), scope),
          "s", expand(bindings.get("s"), scope),          
          "___.readPub(@o, @s)");
    }
    
    ////////////////////////////////////////////////////////////////////////
    // set - assignments
    ////////////////////////////////////////////////////////////////////////

    // set_0
    if (match(node, bindings, "@x.@y__ = @z")) {
      throw new RuntimeException("Properties cannot end in \"__\": " + bindings.get("y"));
    }

    // set_1
    if (match(node, bindings, "this.@p = @r")) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      return substV(
          "r",  expand(bindings.get("r"), scope),
          "p",  bindings.get("p"),
          "fp", new Reference(new Identifier(propertyName + "_canSet___")),
          "rp", new StringLiteral(propertyName),
          "(function() {" +
          "  var x___ = @r;" +
          "  this.@fp ? this.@p = x___ : ___.setProp(x___, @rp, x___);" +
          "})()");
    }

    // set_2
    if (match(node, bindings, "@fname.prototype.@p = @m") &&
        scope.isFunction(bindings.get("fname"))) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      if (!"constructor".equals(propertyName)) {
        return substV(
            "m",  expandMember(bindings.get("fname"), bindings.get("m"), scope),
            "rp", new StringLiteral(propertyName),
            "(function() {" +
            "  var x___ = @m" +
            "  ___.setMember(@fname, @rp, x___)" +
            "})()");
      }
    }

    // set_3
    if (match(node, bindings, "@x.@y_ = @z")) {
      throw new RuntimeException("Public properties cannot end in \"_\": " + node);
    }

    // set_4
    if (match(node, bindings, "@fname.prototype = @mm") &&
        scope.isFunction(bindings.get("fname"))) {
      return substV(
          "fname", bindings.get("fname"),
          "mm", expandMemberMap(bindings.get("fname"), bindings.get("mm"), scope),
          "___.setMemberMap(@fname, @mm)");
    }

    // set_5
    if (match(node, bindings, "@fname.@p = @r") &&
        scope.isFunction(bindings.get("fname"))) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      if (!"Super".equals(propertyName)) {
        return substV(
            "fname", bindings.get("fname"),
            "rp", new StringLiteral(propertyName),
            "r", expand(bindings.get("r"), scope),
            "___.setPub(@fname, @rp, @r)");
      }
    }

    // set_6
    if (match(node, bindings, "@o.@p = @r")) {
      String propertyName = ((Reference)bindings.get("p")).getIdentifierName();
      Pair<ParseTreeNode, ParseTreeNode> po = reuse("x0___", bindings.get("o"), scope);
      Pair<ParseTreeNode, ParseTreeNode> pr = reuse("x1___", bindings.get("r"), scope);
      return substV(
          "pName", new StringLiteral(propertyName),
          "p", bindings.get("p"),
          "pCanSet", new Reference(new Identifier(propertyName + "_canSet___")),  
          "poa", po.a,
          "pob", po.b,
          "pra", pr.a,
          "prb", pr.b,
          "(function() {" +
          "  @pob;" +
          "  @prb;" +
          "  @poa.@pCanSet ? @poa.@p = @pra : ___.setPub(@poa, @pName, @pra)" +
          "})()");
    }

    // set_7
    if (match(node, bindings, "this[@s] = @r")) {
      return substV(
          "s", expand(bindings.get("s"), scope),
          "r", expand(bindings.get("r"), scope),
          "___.setProp(this, @s, @r)");
    }

    // set_8
    if (match(node, bindings, "@o[@s] = @r")) {
      return substV(
          "o", expand(bindings.get("o")),
          "s", expand(bindings.get("s")),
          "r", expand(bindings.get("r")),
          "___.setPub(@o, @s, @r)");
    }

    // set_9
    // TODO(ihab.awad): Why does MarkM have "&& isVar(v)" here?
    if (match(node, bindings, "@v = @r") &&
        !scope.isFunction(bindings.get("v"))) {
      return expandDef(
          bindings.get("v"),
          expand(bindings.get("r"), scope),
          scope);
    }

    // set_10
    // TODO(ihab.awad): Why does MarkM have "&& isVar(v)" here?
    if (match(node, bindings, "var @v = @r") &&
        !scope.isFunction(bindings.get("v"))) {
      return expandDef(
          new Reference((Identifier)bindings.get("v")),
          expand(bindings.get("r"), scope),
          scope);
    }

    // set_11
    // TODO(ihab.awad): Why does MarkM have "&& isVar(v)" here?
    if (match(node, bindings, "var @v") &&
        !scope.isFunction(bindings.get("v"))) {
      return expandDef(
          bindings.get("v"),
          new Reference(new Identifier("undefined")),
          scope);
    }

    ////////////////////////////////////////////////////////////////////////
    // new - new object creation
    ////////////////////////////////////////////////////////////////////////

    // new_0
    if (match(node, bindings, "new @ctor(@as*)") &&
        scope.isConstructor(bindings.get("ctor"))) {
      return substV(
          "ctor", bindings.get("ctor"),
          "as", expandAll(bindings.get("as"), scope),
          "(___.asCtor(@ctor))(@as*)");
    }

    // new_1
    if (match(node, bindings, "new @f(@as*)")) {
      return substV(
          "f", expand(bindings.get("f"), scope),
          "as", expandAll(bindings.get("as"), scope),
          "(___.asCtor(@f))(@as*)");
    }

    ////////////////////////////////////////////////////////////////////////
    // call - function calls
    ////////////////////////////////////////////////////////////////////////

    // call_0
    if (match(node, bindings, "@o.@s__(@as*)")) {
      throw new RuntimeException("Selectors cannot end in __: " + bindings.get("s"));
    }

    // call_1
    if (match(node, bindings, "this.@m(@as*)")) {
      Pair<ParseTreeNode, ParseTreeNode> aliases =
          reuseAll(bindings.get("as"), scope);
      String methodName = ((Reference)bindings.get("m")).getIdentifierName();      
      return substV(
          "o",  expand(bindings.get("o"), scope),
          "as", aliases.b,
          "vs", aliases.a,
          "m",  bindings.get("m"),
          "fm", new Reference(new Identifier(methodName + "_canCall___")),
          "rm", new StringLiteral(methodName),
          "(function() {" +
          "  @as*;" +
          "  this.@fm ? this.@m(@vs*) : ___.callProp(this, @rm, [@vs*]);" +
          "})()");
    }

    // call_2
    if (match(node, bindings, "@o.@s_(@as*)")) {
      throw new RuntimeException("Public selectors cannot end in _: " + bindings.get("s"));
    }

    // call_3
    if (match(node, bindings, "caja.def(@fname, @base)") &&
        scope.isFunction(bindings.get("fname")) &&
        scope.isFunction(bindings.get("base"))) {
      return subst(
          bindings,
          "caja.def(@fname, @base)");
    }

    // call_4
    // TODO(ihab.awad): Make object literal expressions work!!!
    // TODO(ihab.awad): Make "optional" quasis ("?" suffix) work!!!
    if (match(node, bindings, "caja.def(@fname, @base, @mm, @ss" + /* ? */ ")") &&
        scope.isFunction(bindings.get("fname")) &&
        scope.isFunction(bindings.get("base"))) {
      // TODO(ihab.awad): Need match(...) ignoring bindings so don't have to pass dummy value.
      Map<String, ParseTreeNode> b2 = new HashMap<String, ParseTreeNode>();
      if (match(bindings.get("mm"), b2, "{ @x* : @y* }")) {
        return substV(
            "fname", bindings.get("fname"),
            "base", bindings.get("base"),
            "mm", expandMemberMap(bindings.get("fname"), bindings.get("mm"), scope),
            "ss", expandAll(bindings.get("ss"), scope),
            "caja.def(@fname, @base, @mm, @ss" + /* ? */ "");
      }
    }

    // call_5
    if (match(node, bindings, "@o.@m(@as*)")) {
      Pair<ParseTreeNode, ParseTreeNode> aliases =
          reuseAll(bindings.get("as"), scope);
      String methodName = ((Reference)bindings.get("m")).getIdentifierName();
      return substV(
          "o",  expand(bindings.get("o"), scope),
          "as", aliases.b,
          "vs", aliases.a,
          "m",  bindings.get("m"),
          "fm", new Reference(new Identifier(methodName + "_canCall___")),
          "rm", new StringLiteral(methodName),          
          "(function() {" +
          "  var x___ = @o;" +
          "  @as*;" +
          "  x___.@fm ? x___.@m(@vs*) : ___.callPub(x___, @rm, [@vs*]);" +
          "})()");
    }

    // call_6
    if (match(node, bindings, "this[@s](@as*)")) {
      expandEntries(bindings, scope);
      return subst(
          bindings,
          "___.callProp(this, @s, [@as*])");
    }

    // call_7
    if (match(node, bindings, "@o[@s](@as*)")) {
      expandEntries(bindings, scope);
      return subst(
          bindings,
          "___.callPub(@o, @s, [@as*])");
    }

    // call_8
    if (match(node, bindings, "@f(@as*)")) {
      expandEntries(bindings, scope);
      return subst(
          bindings,
          "___.asSimpleFunc(@f)(@as*)");
    }

    ////////////////////////////////////////////////////////////////////////
    // function - function definitions
    ////////////////////////////////////////////////////////////////////////

    // function_0
    if (match(node, bindings, "function(@ps*) { @bs*; }")) {
      Scope s2 = new Scope(scope, node);
      if (!s2.hasFreeThis()) {
        return substV(
            "ps", bindings.get("ps"),
            "bs", expand(bindings.get("bs"), s2),
            "fh", getFunctionHeadDeclarations(s2),
            "___.primFreeze(" +
            "  ___.simpleFunction(" +
            "    function(@ps*) {" +
            "      @fh*;" +
            "      @bs*;" +
            "}))");
      }
    }

    // function_1
    // This is the case of a function *declaration* -- i.e., one where the function
    // constructor expression is not evaluated for its value.    
    if (match(node, bindings, "function @f(@ps*) { @bs*; }")) {
      Scope s2 = new Scope(scope, node);
      if (!s2.hasFreeThis()) {
        return expandDef(
            new Reference((Identifier)bindings.get("f")),
            substV(
                "ps", bindings.get("ps"),
                "bs", expand(bindings.get("bs"), s2),
                "fh", getFunctionHeadDeclarations(s2),
                "___.simpleFunction(" +
                "  function(@ps*) {" +
                "    @fh*;" +
                "    @bs*;" +
                "});"),
            scope);
      }
    }

    // function_2
    // Having fallen through case "function_1", we may now check for all other
    // function constructor expressions which *are* evaluated for their value.
    // The pattern we use builds a FunctionDeclaration object, then dereferences
    // to its contained FunctionConstructor
    if (match(node, bindings, getPatternNode("function @f(@ps*) { @bs* }").getChildren().get(1))) {
      Scope s2 = new Scope(scope, node);
      if (!s2.hasFreeThis()) {
        return expandDef(
            bindings.get("f"),
            substV(
                "ps", bindings.get("ps"),
                "fh", getFunctionHeadDeclarations(s2),
                "bs", expand(bindings.get("bs"), s2),
                "___.primFreeze(" +
                "  ___.simpleFunction(" +
                "    function(@ps*) {" +
                "      @fh*;" +
                "      @bs*;" +
                "}));"),
            scope);
      }
    }

    // function_3
    if (match(node, bindings, "function(@ps*) { @bs*; }")) {
      Scope s2 = new Scope(scope, node);
      if (s2.hasFreeThis()) {
        throw new RuntimeException("Method in non-method context: " + node);
      }
    }
    
    // function_4
    // TODO(ihab.awad): Is there a way to avoid implementing isForValue() for this?
    if (match(node, bindings, "function @f(@ps*) { @bs*; }") && scope.isForValue(node)) {
      Scope s2 = new Scope(scope, node);
      if (s2.hasFreeThis()) {
        throw new RuntimeException("Constructor cannot escape: " + node);
      }
    }

    // function_5
    if (match(node, bindings, "function @f(@ps*) { @sf.Super.call(this, @as*); @bs*; }")) {
      // The following test checks that Reference "@sf" has the same name as Identifier "@f": 
      if (bindings.get("sf").children().get(0).getValue().equals(bindings.get("f").getValue())) {
        if (!new Scope(scope, bindings.get("as")).hasFreeThis()) {
          Scope s2 = new Scope(scope, node);
          return expandDef(
              bindings.get("f"),
              substV(
                  "ps", expand(bindings.get("ps"), scope),
                  "fh", getFunctionHeadDeclarations(s2),
                  "sf", bindings.get("sf"),
                  "as", expand(bindings.get("as"), s2),
                  "bs", expand(bindings.get("bs"), s2),
                  "___.ctor(function(@ps*) {" +
                  "  @fh*;" +
                  "  @sf.Super.call(this, @as*);" +
                  "  @bs*;" +
                  "});"),
              scope);
        }
      }
    }

    // function_6
    if (match(node, bindings, "function @f(@ps*) { @bs*; }")) {
      if (new Scope(scope, bindings.get("bs")).hasFreeThis()) {
        Scope s2 = new Scope(scope, node);
        return expandDef(
            new Reference((Identifier)bindings.get("f")),
            substV(
                "ps", expand(bindings.get("ps"), scope),
                "fh", getFunctionHeadDeclarations(s2),
                "bs", expand(bindings.get("bs"), s2),
                "___.ctor(function(@ps*) {" +
                "  @fh*;" +
                "  @bs*;" +
                "});"),
            scope);
      }
    }
    
    ////////////////////////////////////////////////////////////////////////
    // map - object literals
    ////////////////////////////////////////////////////////////////////////

    // map_0
    // TODO(ihab.awad): What does "{}" parse to - a Block or an ObjectLiteral?
    if (match(node, bindings, "{}")) {      
      return node.clone();
    }

    // TODO(ihab.awad): What is the specific object literal match/subst syntax...?
    
    // map_1
    if (match(node, bindings, "@key_ : @val, @rests*")) {
      throw new RuntimeException("Key may not end in \"_\": " + bindings.get("key"));
    }

    // map_2
    if (match(node, bindings, "{ @key : @val, @rests* }") &&
        bindings.get("key") instanceof Identifier) {
      ParseTreeNode restsNode = expand(
          substV(
              "rests", bindings.get("rests"),
              "{ @rests* }"),
          scope);
      return substV(
          "key", bindings.get("key"),
          "val", expand(bindings.get("val"), scope),
          "rests", new ParseTreeNodeContainer(restsNode.children()),
          "{ @key : @val, @rests* }");
    }

    // map_3
    if (match(node, bindings, "{ @keyExpr : @val, @rests* }")) {
      throw new RuntimeException("Key expressions not yet supported: " + bindings.get("keyExpr"));
    }
    
    ////////////////////////////////////////////////////////////////////////
    // other - things not otherwise covered
    ////////////////////////////////////////////////////////////////////////

    // other_0
    if (match(node, bindings, "@o instanceof @f")) {
      if (scope.isFunction(bindings.get("f"))) {
        return substV(
            "o", expand(bindings.get("o"), scope),
            "f", bindings.get("f"),
            "@o instanceof @f");
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // Automatically recurse into some control structures
    ////////////////////////////////////////////////////////////////////////

    if (node instanceof ParseTreeNodeContainer ||
        node instanceof Block ||        
        node instanceof Conditional ||
        node instanceof ExpressionStmt ||
        node instanceof Loop ||
        node instanceof Noop ||
        node instanceof ReturnStmt) {
      return expandAll(node, scope);
    }

    ////////////////////////////////////////////////////////////////////////
    // Our case analysis must cover the language
    ////////////////////////////////////////////////////////////////////////

    throw new RuntimeException("Unrecognized node: " + node);
  }
}
