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

import static com.google.caja.parser.SyntheticNodes.s;
import static com.google.caja.parser.quasiliteral.QuasiBuilder.substV;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.caja.lexer.Keyword;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodeContainer;
import com.google.caja.parser.js.ArrayConstructor;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.BreakStmt;
import com.google.caja.parser.js.CaseStmt;
import com.google.caja.parser.js.Conditional;
import com.google.caja.parser.js.ContinueStmt;
import com.google.caja.parser.js.ControlOperation;
import com.google.caja.parser.js.DebuggerStmt;
import com.google.caja.parser.js.DefaultCaseStmt;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.FunctionDeclaration;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Literal;
import com.google.caja.parser.js.Loop;
import com.google.caja.parser.js.Noop;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.RegexpLiteral;
import com.google.caja.parser.js.ReturnStmt;
import com.google.caja.parser.js.SimpleOperation;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.parser.js.SwitchStmt;
import com.google.caja.parser.js.ThrowStmt;
import com.google.caja.reporting.MessageQueue;

/**
 * Rewrites a JavaScript parse tree to comply with default Valija rules.
 *
 * @author metaweta@gmail.com (Ihab Awad)
 */
@RulesetDescription(
    name="Caja Transformation Rules",
    synopsis="Default set of transformations used by Caja"
  )

public class DefaultValijaRewriter extends Rewriter {
  private int tempVarCount = 0;
  private final String tempVarPrefix = "$caja$";

  final public Rule[] valijaRules = {
    new Rule() {
      @Override
      @RuleDescription(
          name="module",
          synopsis="Assume an imported \"valija\" that knows our shared outers. " +
            "Name it $dis so top level uses of \"this\" in Valija work.",
          reason="",
          matches="@ss*;",
          substitutes="var $dis = valija.getOuters(); @startStmts*; @ss*;")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        if (node instanceof Block && scope == null) {
          Scope s2 = Scope.fromProgram((Block)node, mq);
          List<ParseTreeNode> expanded = new ArrayList<ParseTreeNode>();
          for (ParseTreeNode c : node.children()) {
            expanded.add(expand(c, s2, mq));
          }
          return substV(
              "var $dis = valija.getOuters(); @startStmts*; @expanded*;",
              "startStmts", new ParseTreeNodeContainer(s2.getStartStatements()),
              "expanded", new ParseTreeNodeContainer(expanded));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="block",
          synopsis="Initialize named functions at the beginning of their enclosing block.",
          reason="Nested named function declarations are illegal in ES3 but are universally " +
            "supported by all JavaScript implementations, though in different ways. " +
            "The compromise semantics currently supported by Caja is to hoist the " +
            "declaration of a variable with the function's name to the beginning of " +
            "the enclosing function body or module top level, and to initialize " +
            "this variable to a new anonymous function every time control re-enters " +
            "the enclosing block." +
            "\n" +
            "Note that ES3.1 and ES4 specify a better and safer semantics -- block " +
            "level lexical scoping -- that we'd like to adopt into Caja eventually. " +
            "However, it so challenging to implement this semantics by " +
            "translation to currently-implemented JavaScript that we provide " +
            "something quicker and dirtier for now.",
          matches="{@ss*;}",
          substitutes="@startStmts*; @ss*;")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        if (node instanceof Block) {
          List<ParseTreeNode> expanded = new ArrayList<ParseTreeNode>();
          Scope s2 = Scope.fromPlainBlock(scope, (Block)node);
          for (ParseTreeNode c : node.children()) {
            expanded.add(expand(c, s2, mq));
          }
          return substV(
              "@startStmts*; @ss*;",
              "startStmts", new ParseTreeNodeContainer(s2.getStartStatements()),
              "ss", new ParseTreeNodeContainer(expanded));
        }
        return NONE;
      }
    },

    new Rule () {
      @Override
      @RuleDescription(
          name="foreachExpr",
          synopsis="Get the keys, then iterate over them.",
          reason="",
          matches="<approx>for (@k in @o) @ss;",
          substitutes="var @t1 = valija.keys(@o);" +
                      "for (var @t2 = 0; @t2 < @t1.length; @t2++) {" +
                      "  @k = @t1[@t2];" +
                      "  @ss;" +
                      "}")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("for (@k in @o) @ss;", node, bindings) &&
            bindings.get("k") instanceof ExpressionStmt) {
          ExpressionStmt es = (ExpressionStmt)bindings.get("k");
          bindings.put("k", es.getExpression());

          Identifier t1 = new Identifier(tempVarPrefix + tempVarCount++);
          scope.declareStartOfScopeVariable(t1);
          Reference rt1 = s(new Reference(t1));

          Identifier t2 = new Identifier(tempVarPrefix + tempVarCount++);
          scope.declareStartOfScopeVariable(t2);
          Reference rt2 = s(new Reference(t2));

          return substV(
              "@t1 = valija.keys(@o);" +
              "for (@t2 = 0; @t2 < @t1.length; ++@t2) {" +
              "  @k = @t1[@t2];" +
              "  @ss;" +
              "}",
              "t1", rt1,
              "o", expand(bindings.get("o"), scope, mq),
              "t2", rt2,
              "k", expand(bindings.get("k"), scope, mq),
              "ss", expand(bindings.get("ss"), scope, mq));
        } else {
          return NONE;
        }
      }
    },

    new Rule () {
      @Override
      @RuleDescription(
          name="foreach",
          synopsis="Get the keys, then iterate over them.",
          reason="",
          matches="<approx>for (@k in @o) @ss;",
          substitutes="var @t1 = valija.keys(@o);" +
                      "for (var @t2 = 0; @t2 < @t1.length; @t2++) {" +
                      "  @k = @t1[@t2];" +
                      "  @ss;" +
                      "}")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("for (var @k in @o) @ss;", node, bindings)) {
          Identifier t1 = scope.declareStartOfScopeTempVariable();
          Identifier t2 = scope.declareStartOfScopeTempVariable();
          Reference rt1 = s(new Reference(t1));
          Reference rt2 = s(new Reference(t2));
          return substV(
              "@t1 = valija.keys(@o);" +
              "for (@t2 = 0; @t2 < @t1.length; ++@t2) {" +
              "  @k = @t1[@t2];" +
              "  @ss;" +
              "}",
              "t1", rt1,
              "o", expand(bindings.get("o"), scope, mq),
              "t2", rt2,
              "k", new Reference((Identifier)bindings.get("k")),
              "ss", expand(bindings.get("ss"), scope, mq));
        } else {
          return NONE;
        }
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="this",
          synopsis="Replace all occurrences of \"this\" with $dis.",
          reason="",
          matches="this",
          substitutes="$dis")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match(Keyword.THIS.toString(), node, bindings)) {
          return newReference(ReservedNames.DIS);
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="initGlobalVar",
          synopsis="",
          reason="",
          matches="<in outer scope>var @v = @r",
          substitutes="<approx>valija.setOuter(@'v', @r)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("var @v = @r", node, bindings) &&
            scope.getParent() == null) {
          return substV(
              "valija.setOuter(@rv, @r)",
              "rv", toStringLiteral(bindings.get("v")),
              "r", expand(bindings.get("r"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="setGlobalVar",
          synopsis="",
          reason="",
          matches="<declared in outer scope>@v = @r",
          substitutes="<approx>valija.setOuter(@'v', @r)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@v = @r", node, bindings) &&
            scope.isImported(getReferenceName(bindings.get("v")))) {
          return substV(
              "valija.setOuter(@rv, @r)",
              "rv", toStringLiteral(bindings.get("v")),
              "r", expand(bindings.get("r"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="declGlobalVar",
          synopsis="",
          reason="",
          matches="<in outer scope>var @v",
          substitutes="<approx>valija.initOuter(@'v')")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("var @v", node, bindings) &&
            scope.isImported(getReferenceName(bindings.get("v")))) {
          return substV(
              "valija.initOuter(@rv)",
              "rv", toStringLiteral(bindings.get("v")));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="readGlobalVar",
          synopsis="",
          reason="",
          matches="<declared in outer scope>@v",
          substitutes="<approx>valija.readOuter(@'v')")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@v", node, bindings) &&
            scope.isImported(getReferenceName(bindings.get("v")))) {
          return substV(
              "valija.readOuter(@rv)",
              "rv", toStringLiteral(bindings.get("v")));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="readPublic",
          synopsis="Read @'p' from @o or @o's POE table",
          reason="",
          matches="@o.@p",
          substitutes="<approx> valija.read(@o, @'p')")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o.@p", node, bindings)) {
          Reference p = (Reference) bindings.get("p");
          return substV(
              "valija.read(@o, @rp)",
              "o", expand(bindings.get("o"), scope, mq),
              "rp", toStringLiteral(p));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="readIndexPublic",
          synopsis="Read @p from @o or @o's POE table",
          reason="",
          matches="@o[@p]",
          substitutes="valija.read(@o, @p)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o[@p]", node, bindings)) {
          return substV(
              "valija.read(@o, @p)",
              "o", expand(bindings.get("o"), scope, mq),
              "p", expand(bindings.get("p"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="setPublic",
          synopsis="Set @'p' on @o or @o's POE table",
          reason="",
          matches="@o.@p = @r",
          substitutes="<approx> valija.set(@o, @'p', @r)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o.@p = @r", node, bindings)) {
          Reference p = (Reference) bindings.get("p");
          return substV(
              "valija.set(@o, @rp, @r)",
              "o", expand(bindings.get("o"), scope, mq),
              "rp", toStringLiteral(p),
              "r", expand(bindings.get("r"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="setIndexPublic",
          synopsis="Set @p on @o or @o's POE table",
          reason="",
          matches="@o[@p] = @r",
          substitutes="valija.set(@o, @p, @r)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o[@p] = @r", node, bindings)) {
          return substV(
              "valija.set(@o, @p, @r)",
              "o", expand(bindings.get("o"), scope, mq),
              "p", expand(bindings.get("p"), scope, mq),
              "r", expand(bindings.get("r"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="constructNoArgs",
          synopsis="Construct a new object and supply the missing empty argument list.",
          reason="",
          matches="new @c",
          substitutes="valija.construct(@c, [])")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("new @c", node, bindings)) {
          return substV(
              "valija.construct(@c, [])",
              "c", expand(bindings.get("c"), scope, mq));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="construct",
          synopsis="Construct a new object.",
          reason="",
          matches="new @c(@as*)",
          substitutes="valija.construct(@c, [@as*])")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("new @c(@as*)", node, bindings)) {
          expandEntries(bindings, scope, mq);
          return QuasiBuilder.subst(
              "valija.construct(@c, [@as*])",
              bindings);
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="callNamed",
          synopsis="Call a property with a statically known name.",
          reason="",
          matches="@o.@p(@as*)",
          substitutes="<approx> valija.callMethod(@o, @'p', [@as*])")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o.@p(@as*)", node, bindings)) {
          Reference p = (Reference) bindings.get("p");
          List<ParseTreeNode> expanded = new ArrayList<ParseTreeNode>();
          ParseTreeNodeContainer args = (ParseTreeNodeContainer)bindings.get("as");
          for (ParseTreeNode c : args.children()) {
            expanded.add(expand(c, scope, mq));
          }
          return substV(
              "valija.callMethod(@o, @rp, [@as*])",
              "o", expand(bindings.get("o"), scope, mq),
              "rp", toStringLiteral(p),
              "as", new ParseTreeNodeContainer(expanded));
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="callMethod",
          synopsis="Call a property with a computed name.",
          reason="",
          matches="@o[@p](@as*)",
          substitutes="valija.callMethod(@o, @p, [@as*])")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o[@p](@as*)", node, bindings)) {
          List<ParseTreeNode> expanded = new ArrayList<ParseTreeNode>();
          ParseTreeNodeContainer args = (ParseTreeNodeContainer)bindings.get("as");
          for (ParseTreeNode c : args.children()) {
            expanded.add(expand(c, scope, mq));
          }
          return substV(
              "valija.callMethod(@o, @p, [@as*])",
              "o", expand(bindings.get("o"), scope, mq),
              "p", expand(bindings.get("p"), scope, mq),
              "as", new ParseTreeNodeContainer(expanded));
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="callFunc",
          synopsis="Call a function.",
          reason="",
          matches="@f(@as*)",
          substitutes="valija.callFunc(@f, [@as*])")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@f(@as*)", node, bindings)) {
          List<ParseTreeNode> expanded = new ArrayList<ParseTreeNode>();
          ParseTreeNodeContainer args = (ParseTreeNodeContainer)bindings.get("as");
          for (ParseTreeNode c : args.children()) {
            expanded.add(expand(c, scope, mq));
          }
          return substV(
              "valija.callFunc(@f, [@as*])",
              "f", expand(bindings.get("f"), scope, mq),
              "as", new ParseTreeNodeContainer(expanded));
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="disfuncAnon",
          synopsis="Transmutes functions into disfunctions.",
          reason="",
          matches="function (@ps*) {@bs*;}",
          substitutes="valija.dis(function ($dis, @ps*) {@fh*; @stmts*; @bs*;})")
      public ParseTreeNode fire(
          ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("function (@ps*) {@bs*;}", node, bindings)) {
          Scope s2 = Scope.fromFunctionConstructor(scope, (FunctionConstructor)node);
          return substV(
              "valija.dis(function ($dis, @ps*) {@fh*; @stmts*; @bs*;})",
              "ps", bindings.get("ps"),
              // It's important to expand bs before computing fh and stmts.
              "bs", expand(bindings.get("bs"), s2, mq),
              "fh", getFunctionHeadDeclarations(this, s2, mq),
              "stmts", new ParseTreeNodeContainer(s2.getStartStatements()));
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="disfuncNamedDecl",
          synopsis="Transmutes functions into disfunctions.",
          reason="",
          matches="function @fname(@ps*) {@bs*;}",
          substitutes="<approx>var @fname = valija.dis(" +
                                   "function($dis, @ps*) {" +
                                   "  @fh*;" +
                                   "  @stmts*;" +
                                   "  @bs*;" +
                                   "}, " +
                                   "@'fname');")
          public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        // Named simple function declaration
        if (node instanceof FunctionDeclaration &&
            QuasiBuilder.match(
                "function @fname(@ps*) { @bs*; }",
                node.children().get(1), bindings)) {
          Scope s2 = Scope.fromFunctionConstructor(
              scope,
              (FunctionConstructor)node.children().get(1));
          // TODO(metaweta): remove the check for ending underscores
          //                 and change valija.* to mangle them instead
          checkFormals(bindings.get("ps"), mq);
          Identifier fname = (Identifier)bindings.get("fname");
          scope.declareStartOfScopeVariable(fname);
          Expression expr = (Expression)substV(
              "@fname = valija.dis(" +
              "  function($dis, @ps*) {" +
              "    @fh*;" +
              "    @stmts*;" +
              "    @bs*;" +
              "}, @rf);",
              "fname", s(new Reference(fname)),
              "rf", toStringLiteral(fname),
              "ps", bindings.get("ps"),
              // It's important to expand bs before computing fh and stmts.
              "bs", expand(bindings.get("bs"), s2, mq),
              "fh", getFunctionHeadDeclarations(this, s2, mq),
              "stmts", new ParseTreeNodeContainer(s2.getStartStatements()));
          scope.addStartOfBlockStatement(new ExpressionStmt(expr));
          return substV(";");
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="disfuncNamedValue",
          synopsis="",
          reason="",
          matches="function @fname(@ps*) { @bs*; }",
          substitutes=
            "<approx>" +
            "(function() {" +
            "  var @fname = valija.dis(function ($dis, @ps*) {" +
            "    @fh*;" +
            "    @stmts*;" +
            "    @bs*;" +
            "  }," +
            "  @'fname')" +
            "  return @fname;" +
            "})();")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        // Named simple function expression
        if (QuasiBuilder.match("function @fname(@ps*) { @bs*; }", node, bindings)) {
          Scope s2 = Scope.fromFunctionConstructor(
              scope,
              (FunctionConstructor)node);
          // TODO(metaweta): remove the check for ending underscores
          //                 and change valija.* to mangle them instead
          checkFormals(bindings.get("ps"), mq);
          Identifier fname = (Identifier)bindings.get("fname");
          Reference fRef = new Reference(fname);
          return substV(
              "(function() {" +
              "  var @fRef = valija.dis(function ($dis, @ps*) {" +
              "    @fh*;" +
              "    @stmts*;" +
              "    @bs*;" +
              "  }," +
              "  @rf);" +
              "  return @fRef;" +
              "})();",
              "fname", fname,
              "fRef", fRef,
              "rf", toStringLiteral(fname),
              "ps", bindings.get("ps"),
              // It's important to expand bs before computing fh and stmts.
              "bs", expand(bindings.get("bs"), s2, mq),
              "fh", getFunctionHeadDeclarations(this, s2, mq),
              "stmts", new ParseTreeNodeContainer(s2.getStartStatements()));
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="metaDisfunc",
          synopsis="Shadow the constructor Function().",
          reason="",
          matches="Function",
          substitutes="valija.Disfunction")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("Function", node, bindings)) {
          return substV("valija.Disfunction");
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="otherTypeof",
          synopsis="Rewrites typeof.",
          reason="Both typeof function and typeof disfunction need to return \"function\".",
          matches="typeof @f",
          substitutes="valija.typeOf(@f)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("typeof @f", node, bindings)) {
          return substV(
              "valija.typeOf(@f)",
              "f", expand(bindings.get("f"), scope, mq));
        }
        return NONE;
      }
    },
    
    new Rule() {
      @Override
      @RuleDescription(
          name="otherInstanceof",
          synopsis="Rewrites instanceof.",
          reason="Need to check both the shadow prototype chain and the real one.",
          matches="@o instanceof @f",
          substitutes="valija.instanceOf(@o, @f)")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        Map<String, ParseTreeNode> bindings = new LinkedHashMap<String, ParseTreeNode>();
        if (QuasiBuilder.match("@o instanceof @f", node, bindings)) {
          return substV(
              "valija.instanceOf(@o, @f)",
              "o", expand(bindings.get("o"), scope, mq),
              "f", expand(bindings.get("f"), scope, mq));
        }
        return NONE;
      }
    },
  
    new Rule() {
      @Override
      @RuleDescription(
          name="regexLiteral",
          synopsis="Use the regular expression constructor",
          reason="So that every use of a regex literal creates a new instance"
               + " to prevent state from leaking via interned literals.  This"
               + " is consistent with the way ES4 treates regex literals.",
          substitutes="new ___.RegExp(@pattern, @modifiers?)")
      public ParseTreeNode fire(
          ParseTreeNode node, Scope scope, MessageQueue mq) {
        if (node instanceof RegexpLiteral) {
          RegexpLiteral re = (RegexpLiteral) node;
          StringLiteral pattern = StringLiteral.valueOf(re.getMatchText());
          StringLiteral modifiers = !"".equals(re.getModifiers())
              ? StringLiteral.valueOf(re.getModifiers())
              : null;
          return QuasiBuilder.substV(
              "new RegExp(@pattern, @modifiers?)",
              "pattern", pattern,
              "modifiers", modifiers);
        }
        return NONE;
      }
    },

    new Rule() {
      @Override
      @RuleDescription(
          name="recurse",
          synopsis="Automatically recurse into some structures",
          reason="")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        if (node instanceof ParseTreeNodeContainer ||
            node instanceof ArrayConstructor ||
            node instanceof BreakStmt ||
            node instanceof CaseStmt ||
            node instanceof Conditional ||
            node instanceof ContinueStmt ||
            node instanceof DebuggerStmt ||
            node instanceof DefaultCaseStmt ||
            node instanceof ExpressionStmt ||
            node instanceof Identifier ||
            node instanceof Literal ||
            node instanceof Loop ||
            node instanceof Noop ||
            node instanceof SimpleOperation ||
            node instanceof ControlOperation ||
            node instanceof ReturnStmt ||
            node instanceof SwitchStmt ||
            node instanceof ThrowStmt) {
          return expandAll(node, scope, mq);
        }
        return NONE;
      }
    }
  };

  /**
   * Creates a default valija rewriter with logging on.
   */
  public DefaultValijaRewriter() {
    this(true);
  }

  public DefaultValijaRewriter(boolean logging) {
    super(logging);
    addRules(valijaRules);
  }

}
