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

package com.google.caja.tools;

import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.JsTokenType;
import com.google.caja.lexer.ParseException;
import com.google.caja.lexer.Token;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParserBase;
import com.google.caja.parser.Visitor;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.CatchStmt;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ExpressionStmt;
import com.google.caja.parser.js.ForEachLoop;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Loop;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.js.Reference;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.MessageType;
import com.google.caja.reporting.SimpleMessageQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A build task that performs sanity checks on JavaScript inputs, and if there
 * are no warnings or errors, outputs a time-stamp file to record the time at
 * which the linter passed.
 *
 * @author mikesamuel@gmail.com
 */
public class Linter implements BuildCommand {
  private static final Collection<String> ECMASCRIPT_BUILTINS
      = Collections.unmodifiableCollection(Arrays.asList(
          "Array",
          "Boolean",
          "Date",
          "decodeURI",
          "decodeURIComponent",
          "encodeURI",
          "encodeURIComponent",
          "Error",
          "EvalError",
          "Function",
          "Infinity",
          "isFinite",
          "isNaN",
          "Number",
          "Object",
          "parseFloat",
          "parseInt",
          "Math",
          "NaN",
          "RangeError",
          "ReferenceError",
          "RegExp",
          "String",
          "SyntaxError",
          "TypeError",
          "URIError",
          "undefined"));

  public void build(List<File> inputs, List<File> dependencies, File output)
      throws IOException {
    MessageContext mc = new MessageContext();
    mc.inputSources = new LinkedHashSet<InputSource>();
    MessageQueue mq = new SimpleMessageQueue();
    List<LintJob> lintJobs = parseInputs(inputs, mc, mq);
    lint(lintJobs, mq);
    if (reportErrors(mc, mq).compareTo(MessageLevel.WARNING) < 0) {
      // Touch the time-stamp file to make it clear that the inputs were
      // successfully linted.
      (new FileOutputStream(output)).close();
    }
  }

  private static List<LintJob> parseInputs(
      List<File> inputs, MessageContext mc, MessageQueue mq)
      throws IOException {
    List<LintJob> compUnits = new ArrayList<LintJob>();
    // Parse each input, and find annotations.
    for (File inp : inputs) {
      InputSource src = new InputSource(inp.toURI());
      mc.inputSources.add(src);
      JsTokenQueue tq = new JsTokenQueue(new JsLexer(
          new InputStreamReader(new FileInputStream(inp), "UTF-8"), src), src);
      try {
        if (tq.isEmpty()) { continue; }
        List<Token<JsTokenType>> tokens = tq.filteredTokens();
        Parser p = new Parser(tq, mq);
        compUnits.add(
            new LintJob(
                src,
                parseIdentifierListFromComment("requires", tokens, mq),
                parseIdentifierListFromComment("provides", tokens, mq),
                parseIdentifierListFromComment("overrides", tokens, mq),
                p.parse()));
      } catch (ParseException ex) {
        ex.toMessageQueue(mq);
      }
    }
    return compUnits;
  }

  private static void lint(List<LintJob> jobs, MessageQueue mq) {
    for (LintJob job : jobs) {
      lint(AncestorChain.instance(job.program),
           // Anything defined by this file can be read by this file.
           union(union(union(job.requires, job.provides), job.overrides),
                 ECMASCRIPT_BUILTINS),
           union(job.provides, job.overrides),
           Collections.<String, AncestorChain<?>>emptyMap(),
           mq);
    }
    // TODO: check that two files do not provide the same thing.
  }

  /**
   * @param ac the node to check.
   * @param canRead the set of variable names that can be read by expressions
   *   in ({@code ac.node})'s scope
   * @param canSet the set of variable names that can be modified by expressions
   *   in ({@code ac.node})'s scope.
   * @param mq receives messages about violations of canRead and canSet.
   */
  private static void lint(
      AncestorChain<?> ac, final Set<String> canRead, final Set<String> canSet,
      final Map<String, AncestorChain<?>> fnLocals, final MessageQueue mq) {
    ac.node.acceptPreOrder(new Visitor() {
      public boolean visit(AncestorChain<?> ac) {
        if (ac.node instanceof FunctionConstructor) {
          AncestorChain<FunctionConstructor> fc = ac.cast(
              FunctionConstructor.class);
          Map<String, AncestorChain<?>> locals = localsForFunctionBody(fc, mq);
          Set<String> localNames = locals.keySet();
          lint(AncestorChain.instance(fc, fc.node.getBody()),
               union(localNames, canRead), union(localNames, canSet), locals,
               mq);
          return false;
        } else if (ac.node instanceof Loop || ac.node instanceof ForEachLoop) {
          lintBlock(ac, fnLocals, canRead, canSet, mq);
          return false;
        } else if (ac.node instanceof ExpressionStmt) {
          AncestorChain<ExpressionStmt> es = ac.cast(ExpressionStmt.class);
          if (shouldBeEvaluatedForValue(es.node.getExpression())) {
            mq.addMessage(
                MessageType.NO_SIDE_EFFECT, ac.node.getFilePosition());
          }
        } else if (ac.node instanceof CatchStmt) {
          AncestorChain<CatchStmt> cs = ac.cast(CatchStmt.class);
          String exName = cs.node.getException().getIdentifierName();
          Map<String, AncestorChain<?>> catchLocals = fnLocals;
          if (fnLocals.containsKey(exName)) {  // Allow exception name to mask
            catchLocals = new LinkedHashMap<String, AncestorChain<?>>(fnLocals);
            catchLocals.remove(exName);
          }
          lintBlock(ac, catchLocals, canRead, canSet, mq);
          return false;
        } else if (ac.node instanceof Declaration) {
          AncestorChain<Declaration> d = ac.cast(Declaration.class);
          if (!canSet.contains(d.node.getIdentifierName())) {
            mq.addMessage(
                MessageType.INVALID_DECLARATION, d.node.getFilePosition(),
                MessagePart.Factory.valueOf(d.node.getIdentifierName()));
          }
        } else if (ac.node instanceof Operation) {
          Identifier lhs = leftHandSideOf(ac.cast(Operation.class));
          if (lhs != null && !canSet.contains(lhs.getName())) {
            mq.addMessage(
                MessageType.INVALID_ASSIGNMENT, lhs.getFilePosition(),
                MessagePart.Factory.valueOf(lhs.getName()));
          }
        } else if (ac.node instanceof Reference) {
          if (ac.parent != null
              && ac.parent.node instanceof Operation) {
            Operation parentOp = ac.parent.cast(Operation.class).node;
            if (parentOp.getOperator() == Operator.MEMBER_ACCESS
                && ac.node == parentOp.children().get(1)) {
              return false;
            }
          }
          AncestorChain<Reference> r = ac.cast(Reference.class);
          if (!canRead.contains(r.node.getIdentifierName())) {
            mq.addMessage(
                MessageType.UNDEFINED_SYMBOL, r.node.getFilePosition(),
                MessagePart.Factory.valueOf(r.node.getIdentifierName()));
          }
        }
        return true;
      }
    }, ac.parent);
  }

  private static void lintBlock(
      AncestorChain<?> ac, Map<String, AncestorChain<?>> fnLocals,
      Set<String> canRead, Set<String> canSet, MessageQueue mq) {
    Map<String, AncestorChain<?>> locals = localsForBlock(ac, fnLocals, mq);
    Set<String> localNames = locals.keySet();
    Set<String> newCanRead = union(localNames, canRead);
    Set<String> newCanSet = union(localNames, canSet);
    for (ParseTreeNode child : ac.node.children()) {
      lint(AncestorChain.instance(ac, child),
          newCanRead, newCanSet, locals, mq);
    }
  }

  private static Map<String, AncestorChain<?>> localsForFunctionBody(
      AncestorChain<FunctionConstructor> ac, MessageQueue mq) {
    Map<String, AncestorChain<?>> locals
        = new LinkedHashMap<String, AncestorChain<?>>();
    if (ac.node.getIdentifierName() != null) {
      locals.put(
          ac.node.getIdentifierName(),
          AncestorChain.instance(ac, ac.node.getIdentifier()));
    }
    locals.put("arguments", ac);
    locals.put("this", ac);
    for (ParseTreeNode child : ac.node.children()) {
      findLocals(AncestorChain.instance(ac, child), locals, mq);
    }
    return locals;
  }

  private static Map<String, AncestorChain<?>> localsForBlock(
      AncestorChain<?> ac, Map<String, AncestorChain<?>> fnLocals,
      MessageQueue mq) {
    Map<String, AncestorChain<?>> blockLocals
        = new LinkedHashMap<String, AncestorChain<?>>(fnLocals);
    for (ParseTreeNode child : ac.node.children()) {
      findLocals(AncestorChain.instance(ac, child), blockLocals, mq);
    }
    return blockLocals;
  }

  private static void findLocals(
      AncestorChain<?> ac, final Map<String, AncestorChain<?>> locals,
      final MessageQueue mq) {
    ac.node.acceptPreOrder(new Visitor() {
      public boolean visit(AncestorChain<?> ac) {
        if (ac.node instanceof FunctionConstructor
            || ac.node instanceof CatchStmt
            || ac.node instanceof Loop
            || ac.node instanceof ForEachLoop) {
          return false;
        } else if (ac.node instanceof Declaration) {
          String name = ac.cast(Declaration.class).node.getIdentifierName();
          AncestorChain<?> orig = locals.get(name);
          if (orig != null) {
            mq.addMessage(
                MessageType.SYMBOL_REDEFINED,
                ac.node.getFilePosition(),
                MessagePart.Factory.valueOf(name),
                orig.node.getFilePosition());
          } else {
            locals.put(name, ac);
          }
        }
        return true;
      }
    }, ac.parent);
  }

  /** Dumps error messages to STDERR. */
  private static MessageLevel reportErrors(MessageContext mc, MessageQueue mq) {
    MessageLevel max = MessageLevel.values()[0];
    for (Message msg : mq.getMessages()) {
      MessageLevel level = msg.getMessageLevel();
      if (level.compareTo(max) > 0) { max = level; }
      System.err.println(level.name() + " : " + msg.format(mc));
    }
    return max;
  }

  /** Encapsulates information about a single input to the linter. */
  private static final class LintJob {
    final InputSource src;
    final Set<String> requires, provides, overrides;
    final Block program;

    LintJob(InputSource src, Set<String> requires, Set<String> provides,
            Set<String> overrides, Block program) {
      this.src = src;
      this.requires = requires;
      this.provides = provides;
      this.overrides = overrides;
      this.program = program;
    }
  }

  /**
   * Find identifier lists in documentation comments.
   * Annotations in documentation comments start with a '&#64;' symbol followed
   * by annotationName.
   * The following content ends at the next '@' symbol, and is parsed as an
   * identifier list separated by spaces and/or commas.
   */
  private static final Set<String> parseIdentifierListFromComment(
      String annotationName, List<Token<JsTokenType>> comments,
      MessageQueue mq) {
    // TODO(mikesamuel): replace with jsdoc comment parser
    Set<String> idents = new LinkedHashSet<String>();
    for (Token<JsTokenType> comment : comments) {
      // Remove line prefixes so they're not interpreted as significant in the
      // middle of an identifier list.
      // And remove trailing content that is not whitespace or commas
      String body = comment.text
          .replaceAll("\\*+/$", "")
          .replaceAll("[\r\n]+[ \t]*\\*+[ \t]?", " ");
      String annotPrefix = "@" + annotationName;
      for (int annotStart = -1;
           (annotStart = body.indexOf(annotPrefix, annotStart + 1)) >= 0;) {
        int annotBodyStart = annotStart + annotPrefix.length();
        int annotBodyEnd = body.indexOf('@', annotBodyStart);
        if (annotBodyEnd < 0) { annotBodyEnd = body.length(); }
        String annotBody = body.substring(annotBodyStart, annotBodyEnd).trim();
        if ("".equals(annotBody)) { continue; }
        // annotBody is the content of an annotation.
        for (String ident : annotBody.split("[\\s,]+")) {
          if (!ParserBase.isJavascriptIdentifier(ident)) {
            mq.addMessage(
                MessageType.INVALID_IDENTIFIER, comment.pos,
                MessagePart.Factory.valueOf(ident));
          } else {
            idents.add(ident);
          }
        }
      }
    }
    return Collections.unmodifiableSet(idents);
  }

  private static <T> Set<T> union(
      Collection<? extends T> a, Collection<? extends T> b) {
    Set<T> u = new LinkedHashSet<T>(a);
    u.addAll(b);
    return u;
  }

  /**
   * Given an expression, checks the top-level identifier which is modified.
   */
  private static Identifier leftHandSideOf(AncestorChain<Operation> op) {
    Expression lhs = op.node.children().get(0);
    switch (op.node.getOperator()) {
      case ASSIGN:
      case POST_DECREMENT: case POST_INCREMENT:
      case PRE_DECREMENT: case PRE_INCREMENT:
        break;
      default:
        if (op.node.getOperator().getAssignmentDelegate() == null) {
          return null;
        }
        break;
    }
    while (lhs instanceof Operation
           && (((Operation) lhs).getOperator() == Operator.MEMBER_ACCESS
               || ((Operation) lhs).getOperator() == Operator.SQUARE_BRACKET)) {
      lhs = ((Operation) lhs).children().get(0);
    }
    return lhs instanceof Reference ? ((Reference) lhs).getIdentifier() : null;
  }

  /**
   * A heuristic that identifies expressions that should not appear in a place
   * where their value cannot be used.  This identifies expressions that don't
   * have a side effect, or that are overly complicated.
   *
   * <p>
   * E.g. the expression {@code [1, 2, 3]} has no side effect and so should not
   * appear where its value would be ignored.
   *
   * <p>
   * The expression {@code +f()} might have a side effect, but the {@code +}
   * operator is redundant, and so the expression should not be ignored.
   *
   * <p>
   * Expressions like function calls and assignments are considered side effects
   * and can reasonably appear where their value is not used.
   *
   * <p>
   * Member access operations {@code a.b} could have a useful side-effect, but
   * are unlikely to be used that way.
   *
   * <p>
   * To convince this method that an operations value is being purposely ignored
   * use the {@code void} operator.
   *
   * @return true for any expression that is likely to be used for its value.
   */
  private static boolean shouldBeEvaluatedForValue(Expression e) {
    // A literal or value constructor
    if (!(e instanceof Operation)) { return false; }
    Operator op = ((Operation) e).getOperator();
    switch (op) {
      case ASSIGN:
      case DELETE:
      case FUNCTION_CALL:
      case POST_DECREMENT: case POST_INCREMENT:
      case PRE_DECREMENT: case PRE_INCREMENT:
      case VOID: // indicates value purposely ignored
        return false;
      default: return op.getAssignmentDelegate() == null;
    }
  }

  public static void main(String[] args) throws IOException {
    List<File> inputs = new ArrayList<File>();
    for (String arg : args) { inputs.add(new File(arg)); }
    List<File> deps = new ArrayList<File>();
    File out = File.createTempFile(Linter.class.getSimpleName(), ".stamp");
    (new Linter()).build(inputs, deps, out);
  }
}