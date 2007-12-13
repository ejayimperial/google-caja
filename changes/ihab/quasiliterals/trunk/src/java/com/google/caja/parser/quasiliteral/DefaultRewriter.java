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
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.plugin.ReservedNames;
import com.google.caja.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
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

  private Pair<ParseTreeNode, ParseTreeNode> getCallArgumentAliases(
      ParseTreeNode arguments,
      Scope scope) {
    List<ParseTreeNode> refs = new ArrayList<ParseTreeNode>();
    List<ParseTreeNode> rhss = new ArrayList<ParseTreeNode>();

    for (int i = 0; i < arguments.children().size(); i++) {
      ParseTreeNode ref = new Reference(new Identifier("x" + i + "___"));
      refs.add(ref);
      rhss.add(substV(
          "ref", ref,
          "rhs", expand(arguments.children().get(i), scope),
          "var @ref = @rhs;"));
    }

    return new Pair<ParseTreeNode, ParseTreeNode>(
        new ParseTreeNodeContainer(refs),
        new ParseTreeNodeContainer(rhss));
  }

  private ParseTreeNode expandDef(ParseTreeNode symbol, ParseTreeNode value, Scope scope) {
    if (!(symbol instanceof Reference)) {
      throw new RuntimeException("expandDef on non-Reference: " + symbol);
    }
    return scope.isGlobal(symbol) ?
        substV(
            "s", symbol,
            "v", value,
            "___OUTERS___.@s = @v") :
        substV(
            "s", symbol,
            "v", value,
            "var @s = @v");
  }

  private ParseTreeNode expandMember(ParseTreeNode clazz, ParseTreeNode member, Scope scope) {
    return null;  // TODO(ihab.awad) -- what's to do here?
  }

  private ParseTreeNode expandMemberMap(ParseTreeNode clazz, ParseTreeNode member, Scope scope) {
    return null;  // TODO(ihab.awad) -- what's to do here?
  }

  public ParseTreeNode doExpand(ParseTreeNode node, Scope scope) {
    Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();

    // TODO(ihab.awad): BUG: Where we push a new scope, we are pushing the body of the
    // function, which misses the Declaration caused by the function itself, which comes
    // into scope within it. Look at all "new Scope(scope, <node>)" invocations and fix.

    // TODO(ihab.awad): BUG: Should throw some Caja exception, rather than just
    // RuntimeException, from the various functions, since these will eventually bubble
    // up as "compiler" error messages against user code.

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
      throw new RuntimeException("Variable cannot end with \"__\": " + node);
    }

    // variable_3
    if (match(node, bindings, "@x_")) {
      if (scope.isGlobal(bindings.get("x"))) {
        throw new RuntimeException("Globals cannot end in \"_\": " + node);
      }
    }

    if (match(node, bindings, "@x")) {
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
    if (match(node, bindings, "@fname.prototype.@p = @m")) {
      if (scope.isFunction(bindings.get("fname"))) {
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
    }

    // set_3
    if (match(node, bindings, "@x.@y_ = @z")) {
      throw new RuntimeException("Public properties cannot end in \"_\": " + node);
    }

    // set_4
    if (match(node, bindings, "@fname.prototype = @mm")) {
      if (scope.isFunction(bindings.get("fname"))) {
        // TODO(ihab.awad): ....
      }
    }

    // TODO(ihab.awad): ....

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
          getCallArgumentAliases(bindings.get("as"), scope);
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
    if (match(node, bindings, "caja.def(@fname, @base, @mm, @ss)") &&
        scope.isFunction(bindings.get("fname")) &&
        scope.isFunction(bindings.get("base"))) {
      Map<String, ParseTreeNode> b2 = new HashMap<String, ParseTreeNode>();
      if (match(bindings.get("mm"), b2, "{ @x* : @y* }")) {
        // TODO(ihab.awad): Make object literal expressions work!!!
      }
    }

    // call_5
    if (match(node, bindings, "@o.@m(@as*)")) {
      Pair<ParseTreeNode, ParseTreeNode> aliases =
          getCallArgumentAliases(bindings.get("as"), scope);
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
      Scope s2 = new Scope(scope, bindings.get("bs"));
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
      Scope s2 = new Scope(scope, bindings.get("bs"));
      if (!s2.hasFreeThis()) {
        return expandDef(
            bindings.get("f"),
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
      Scope s2 = new Scope(scope, bindings.get("bs"));
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
      Scope s2 = new Scope(scope, bindings.get("bs"));
      if (s2.hasFreeThis()) {
        throw new RuntimeException("Method in non-method context: " + node);
      }
    }
    
    // function_4
    // TODO(ihab.awad): Is there a way to avoid implementing isForValue() for this?
    if (match(node, bindings, "function @f(@ps*) { @bs*; }") && scope.isForValue(node)) {
      Scope s2 = new Scope(scope, bindings.get("bs"));
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
            bindings.get("f"),
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

    // TODO(ihab.awad): Implement when object literal quasis are ready

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
    // Expand the children of a Block or ParseTreeNodeContainer
    ////////////////////////////////////////////////////////////////////////

    if (node instanceof Block ||
        node instanceof ParseTreeNodeContainer) {
      return expandChildren(node, scope);
    }

    ////////////////////////////////////////////////////////////////////////
    // Our case analysis must cover the language
    ////////////////////////////////////////////////////////////////////////

    throw new RuntimeException("Unrecognized node: " + node);
  }
}
