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

package com.google.caja.plugin.stages;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.MutableParseTreeNode;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.Visitor;
import com.google.caja.parser.js.ArrayConstructor;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.js.Reference;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.parser.js.UndefinedLiteral;
import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.Pair;
import com.google.caja.util.Pipeline;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Optimizes calls to {@code eval(Template("html&hellip;"))}.
 * TODO(mikesamuel): this could probably be more simply done as a
 * rewrite rule.
 *
 * @author mikesamuel@gmail.com
 */
public final class OpenTemplateStage implements Pipeline.Stage<Jobs> {
  public boolean apply(Jobs jobs) {
    for (Job job : jobs.getJobsByType(Job.JobType.JAVASCRIPT)) {
      optimizeOpenTemplate(job.getRoot(), jobs);
    }
    return jobs.hasNoFatalErrors();
  }

  private static void optimizeOpenTemplate(AncestorChain<?> chain, Jobs jobs) {
    ScopeChecker sc = new ScopeChecker();
    applyToScope(chain, sc);
    if (sc.variablesInScope.contains("eval")
        || sc.variablesInScope.contains("Template")) {
      return;
    }

    applyToScope(chain, new Optimizer(jobs));

    for (AncestorChain<FunctionConstructor> innerScope : sc.innerScopes) {
      optimizeOpenTemplate(innerScope, jobs);
    }
  }

  private static void applyToScope(AncestorChain<?> chain, Visitor v) {
    if (chain.node instanceof FunctionConstructor) {
      for (ParseTreeNode child : chain.node.children()) {
        child.acceptPreOrder(v, chain);
      }
    } else {
      chain.node.acceptPreOrder(v, chain.parent);
    }
  }

  private static class ScopeChecker implements Visitor {
    final Set<String> variablesInScope = new HashSet<String>();
    final List<AncestorChain<FunctionConstructor>> innerScopes
        = new ArrayList<AncestorChain<FunctionConstructor>>();

    public boolean visit(AncestorChain<?> chain) {
      if (chain.node instanceof FunctionConstructor) {
        innerScopes.add(chain.cast(FunctionConstructor.class));
        return false;
      }
      if (chain.node instanceof Declaration) {
        // Not correct because of throws blocks, but will be conservative.
        String name = ((Declaration) chain.node).getIdentifierName();
        variablesInScope.add(name);
      }
      return true;
    }
  }

  private static class Optimizer implements Visitor {
    Jobs jobs;

    Optimizer(Jobs jobs) {
      this.jobs = jobs;
    }
    
    public boolean visit(AncestorChain<?> chain) {
      if (chain.node instanceof FunctionConstructor) {
        return false;
      }
      if (!(chain.node instanceof Operation)) {
        return true;
      }

      // Look for
      //   Operation : FUNCTION_CALL   ; evalCall
      //     Reference : eval          ; evalRef
      //     Operation : FUNCTION)CALL ; tmplCall
      //       Reference : Template    ; tmplRef
      //       String concatenation    ; content

      Operation evalCall = (Operation) chain.node;
      if (evalCall.getOperator() != Operator.FUNCTION_CALL
          || evalCall.children().size() != 2) {
        return true;
      }

      Expression evalRef = evalCall.children().get(0);
      if (!(evalRef instanceof Reference
            && "eval".equals(((Reference) evalRef).getIdentifierName()))) {
        return true;
      }

      Expression rhs = evalCall.children().get(1);
      if (!(rhs instanceof Operation && rhs.children().size() == 2)) {
        return false;
      }

      Operation tmplCall = (Operation) rhs;
      if (tmplCall.getOperator() != Operator.FUNCTION_CALL) {
        return false;
      }

      Expression tmplRef = tmplCall.children().get(0);
      if (!(tmplRef instanceof Reference
            && "Template".equals(((Reference) tmplRef).getIdentifierName()))) {
        return false;
      }

      List<StringLiteral> stringLiterals
          = flattenStringConcatenation(tmplCall.children().get(1));
      if (stringLiterals == null) { return false; }

      Splitter splitter = new Splitter(stringLiterals, jobs.getMessageQueue());
      splitter.split();
      List<Expression> templateParts = splitter.parts;
      if (templateParts == null) { return false; }

      ((MutableParseTreeNode) chain.parent.node).replaceChild(
          new Operation(
              Operator.FUNCTION_CALL,
              new Operation(
                 Operator.CONSTRUCTOR,
                 new Reference(new Identifier("StringInterpolation"))),
              new ArrayConstructor(templateParts)),
          chain.node);

      return false;
    }
  }

  private static List<StringLiteral> flattenStringConcatenation(Expression e) {
    return flattenStringsOnto(e, new ArrayList<StringLiteral>());
  }

  private static List<StringLiteral> flattenStringsOnto(
      Expression e, List<StringLiteral> out) {
    if (e instanceof StringLiteral) {
      out.add((StringLiteral) e);
      return out;
    }
    if (!(e instanceof Operation)) { return null; }
    Operation op = (Operation) e;
    if (op.getOperator() != Operator.ADDITION) { return null; }
    for (Expression child : op.children()) {
      out = flattenStringsOnto(child, out);
      if (out == null) { break; }
    }
    return out;
  }
}

final class Splitter {
  int startString = 0;
  int startOffset = 1;
  List<StringLiteral> literals = new ArrayList<StringLiteral>();
  List<Expression> parts = new ArrayList<Expression>();
  State state = State.LITERAL;
  int i, j;
  MessageQueue mq;

  Splitter(List<StringLiteral> literals, MessageQueue mq) {
    this.literals = literals;
    this.mq = mq;
  }

  private static enum State {
    LITERAL,
    SAW_DOLLAR,
    IN_REFERENCE,
    IN_BLOCK,
    ;
  }

  void split() {
    int n = literals.size();
    for (j = 0; j < n; ++j) {
      StringLiteral str = literals.get(j);
      String rawString = str.getValue();
      int m = rawString.length();
      for (i = 1; i < m; ++i) {
        char ch = rawString.charAt(i);
        switch (ch) {
          case '$':
            switch (state) {
              case LITERAL:
              case SAW_DOLLAR:
                state = State.SAW_DOLLAR;
                break;
              case IN_REFERENCE:
                finishReference(0);
                mark(0);
                state = State.SAW_DOLLAR;
                break;
            }
            break;
          case '{':
            switch (state) {
              case SAW_DOLLAR:
                finishLiteral(-1);
                mark(1);
                state = State.IN_BLOCK;
                break;
              case IN_REFERENCE:
                finishReference(0);
                mark(0);
                state = State.LITERAL;
                break;
            }
            break;
          case '}':
            switch (state) {
              case IN_REFERENCE:
                finishReference(0);
                mark(0);
                state = State.LITERAL;
                break;
              case SAW_DOLLAR:
                state = State.LITERAL;
                break;
              case IN_BLOCK:
                finishBlock(0);
                mark(1);
                state = State.LITERAL;
                break;
            }
            break;
          default:
            if (Character.isLetter(ch) || ch == '_') {
              switch (state) {
                case SAW_DOLLAR:
                  finishLiteral(-1);
                  mark(0);
                  state = State.IN_REFERENCE;
                  break;
              }
            } else {
              switch (state) {
                case IN_REFERENCE:
                  if (!Character.isDigit(ch)) {
                    finishReference(0);
                    mark(0);
                    state = State.LITERAL;
                  }
                  break;
                case SAW_DOLLAR:
                  state = State.LITERAL;
                  break;
              }
            }
            break;
        }
      }
    }
    switch (state) {
      case SAW_DOLLAR:
      case LITERAL:
        finishLiteral(0);
        break;
      case IN_REFERENCE:
        finishReference(0);
        break;
      case IN_BLOCK:
        // TODO: output to a message queue
        throw new RuntimeException("End of template inside brackets");
    }
  }

  private void mark(int delta) {
    startString = j;
    startOffset = i + delta;
  }

  private void finishLiteral(int delta) {
    FilePosition start = null, end = null;
    StringBuilder sb = new StringBuilder();
    for (Pair<String, FilePosition> p : upTo(delta)) {
      if (start == null) { start = p.b; }
      end = p.b;
      sb.append(p.a);
    }
    StringLiteral lit = new StringLiteral(StringLiteral.toQuotedValue(sb));
    lit.setFilePosition(FilePosition.span(start, end));
    parts.add(lit);
  }

  private void finishReference(int delta) {
    FilePosition start = null, end = null;
    StringBuilder sb = new StringBuilder();
    for (Pair<String, FilePosition> p : upTo(delta)) {
      if (start == null) { start = p.b; }
      end = p.b;
      sb.append(p.a);
    }
    Identifier ident = new Identifier(sb.toString());
    Reference ref = new Reference(ident);
    FilePosition pos = FilePosition.span(start, end);
    ident.setFilePosition(pos);
    ref.setFilePosition(pos);
    parts.add(ref);
  }

  private void finishBlock(int delta) {
    FilePosition start = null, end = null;
    List<CharProducer> producers = new ArrayList<CharProducer>();
    for (Pair<String, FilePosition> p : upTo(delta)) {
      if (start == null) { start = p.b; }
      end = p.b;
      producers.add(
          CharProducer.Factory.create(new StringReader(p.a), p.b));
    }
    CharProducer joined;
    if (producers.size() == 1) {
      joined = producers.get(0);
    } else {
      joined = CharProducer.Factory.chain(
          producers.toArray(new CharProducer[0]));
    }
    CharProducer exprText = CharProducer.Factory.fromJsString(joined);
    JsLexer lexer = new JsLexer(exprText);
    JsTokenQueue tq = new JsTokenQueue(
        lexer, start.source(), JsTokenQueue.NO_COMMENT);
    Parser p = new Parser(tq, mq);
    Expression result;
    try {
      result = p.parseExpression(true);
      tq.expectEmpty();
    } catch (ParseException ex) {
      ex.toMessageQueue(mq);
      UndefinedLiteral placeholder = new UndefinedLiteral();
      placeholder.setFilePosition(FilePosition.span(start, end));
      result = placeholder;
    }
    parts.add(result);
  }

  private List<Pair<String, FilePosition>> upTo(int delta) {
    int currentString = startString;
    int currentOffset = startOffset;

    while (literals.size() > startString
           && startOffset >= literals.get(startString).getValue().length()-1) {
      startOffset = 1 + startOffset
          - (literals.get(startString).getValue().length() - 1);
      ++startString;
    }

    int endString = j;
    int endOffset = i + delta;

    while (endOffset < 1 && endString > 0) {
      --endString;
      endOffset += literals.get(endString).getValue().length() - 1;
    }

    List<Pair<String, FilePosition>> parts
        = new ArrayList<Pair<String, FilePosition>>();
    while (currentString < endString) {
      StringLiteral lit = literals.get(currentString);
      String literalText = lit.getValue();
      int end = literalText.length() - 1;
      parts.add(
          Pair.pair(
              literalText.substring(currentOffset, end),
              clippedPos(lit.getFilePosition(), currentOffset, end)));
      currentOffset = 1;
      ++currentString;
    }
    if (currentString == endString && currentOffset <= endOffset
        && currentString < literals.size()) {
      StringLiteral lit = literals.get(currentString);
      String literalText = lit.getValue();
      parts.add(
          Pair.pair(
              literalText.substring(currentOffset, endOffset),
              clippedPos(lit.getFilePosition(), currentOffset, endOffset)));
    }
    if (parts.isEmpty()) {
      StringLiteral lastLit = literals.get(literals.size() - 1);
      parts.add(Pair.pair("", FilePosition.endOf(lastLit.getFilePosition())));
    }
    return parts;
  }

  private static FilePosition clippedPos(FilePosition p, int start, int end) {
    if (end <= 0) {
      return FilePosition.startOf(p);
    }
    if (p.endCharInFile() - p.startCharInFile() <= start) {
      return FilePosition.endOf(p);
    }
    if (end < start) { end = start; }
    return FilePosition.instance(
        p.source(),
        p.startLineNo(), p.startLogicalLineNo(),
        p.startCharInFile() + start, p.startCharInLine() + start,
        p.startLineNo(), p.startLogicalLineNo(),
        p.startCharInFile() + end, p.startCharInLine() + end);
  }
}
