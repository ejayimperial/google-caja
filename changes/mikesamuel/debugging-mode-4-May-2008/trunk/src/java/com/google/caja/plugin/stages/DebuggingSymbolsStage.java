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

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.InputSource;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.ArrayConstructor;
import com.google.caja.parser.js.BooleanLiteral;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.IntegerLiteral;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.parser.quasiliteral.Rewriter;
import com.google.caja.parser.quasiliteral.Rule;
import com.google.caja.parser.quasiliteral.RuleDescription;
import com.google.caja.parser.quasiliteral.Scope;
import com.google.caja.parser.quasiliteral.QuasiBuilder;
import com.google.caja.plugin.Job;
import com.google.caja.plugin.Jobs;
import com.google.caja.plugin.PluginMessageType;
import com.google.caja.plugin.SyntheticNodes;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Adds debugging symbols to cajoled code.  This looks for calls into the TCB
 * object, {@code ___}, and adds an optional parameter to each call which is an
 * index into a table of file positions.
 * <p>
 * This stage first walks over cajoled code looking for patterns like
 * {@code ___.readPub(obj, 'key')}, and rewrites them to include an index into a
 * table of debugging symbols: {@code ___.readPub(obj, 'key', 123)}.
 * <p>
 * It then rewrites the module envelope to make a symbol table to checks that
 * may fail at runtime:
 * <pre>
 * ___.loadModule(
 *     function (___, IMPORTS___) {
 *       <b>___.useDebugSymbols(['foo.js:1+12-15',7,'2+4-18']);</b>
 *       ...
 *     })
 * </pre>
 * The debugSymbols are a list of the form
 * <code>'[' &lt;{@link FilePosition}&gt; (',' &lt;prefixLength&gt; ','
 *           &lt;&Delta;{@link FilePosition}&gt;)* ']'</code>
 * where the &Delta;{@code FilePosition}s are turned into {@code FilePosition}s
 * by prepending them with the first prefixLength characters of the preceding
 * {@code FilePosition}.
 * <p>
 * See also <tt>caja-debugmode.js</tt> for javascript which supports this stage.
 *
 * @author mikesamuel@gmail.com
 */
public final class DebuggingSymbolsStage implements Pipeline.Stage<Jobs> {
  private static final boolean DEBUG = false;

  public boolean apply(Jobs jobs) {
    if (jobs.getPluginMeta().debugMode()) {
      MessageQueue mq = jobs.getMessageQueue();
      for (ListIterator<Job> it = jobs.getJobs().listIterator();
           it.hasNext();) {
        Job job = it.next();
        if (job.getType() != Job.JobType.JAVASCRIPT) { continue; }

        if (DEBUG) {
          System.err.println(
              "\n\nPre\n===\n"
              + (job.getRoot().cast(Block.class).node.toStringDeep(1))
              + "\n\n");
        }

        DebuggingSymbols symbols = new DebuggingSymbols();
        Block js = addSymbols(job.getRoot().cast(Block.class), symbols, mq);
        if (!symbols.isEmpty()) {
          if (DEBUG) {
            System.err.println("\n\nPost\n===\n" + js.toStringDeep() + "\n\n");
          }
          it.set(
              new Job(AncestorChain.instance(attachSymbols(symbols, js, mq))));
        }
      }
    }
    return jobs.hasNoFatalErrors();
  }

  /**
   * Rewrites cajoled code to add position indices to caja operations.
   * @param js cajoled javascript.
   * @param symbols added to.
   * @param mq receives rewriting messages.
   * @return js rewritten.
   */
  private Block addSymbols(
      AncestorChain<Block> js, DebuggingSymbols symbols, MessageQueue mq) {
    return (Block) new CajaRuntimeRewriter(symbols).expand(js.node, mq);
  }

  /**
   * Adds a call to ___.useDebugSymbols to a ___.loadModule call.
   */
  private Block attachSymbols(
      DebuggingSymbols symbols, Block js, MessageQueue mq) {
    Map<String, ParseTreeNode> bindings
        = new LinkedHashMap<String, ParseTreeNode>();
    if (!QuasiBuilder.match(
            "{ ___.loadModule(function (___, IMPORTS___) { @body* }); }",
            js, bindings)) {
      mq.addMessage(PluginMessageType.MALFORMED_ENVELOPE, js.getFilePosition());
      return js;
    }
    return (Block) QuasiBuilder.substV(
        "{"
        + "___.loadModule("
        + "    function (___, IMPORTS___) {"
        + "      ___.useDebugSymbols(@symbols);"
        + "      @body"
        + "    });"
        + "}",

        "symbols", symbols.toJavascriptSideTable(),
        "body", bindings.get("body"));
  }
}

/**
 * Modifies cajoled code to remove fasttrack branches, and add debugging info
 * to the slow branches.
 */
final class CajaRuntimeRewriter extends Rewriter {
  private final DebuggingSymbols symbols;

  CajaRuntimeRewriter(DebuggingSymbols symbols) {
    super(false);
    this.symbols = symbols;
  }

  /**
   * Rewrites a {@code ___} method call to add an extra parameter which is an
   * index into {@link DebuggingSymbols}.
   * The position is drawn from the <code>@posNode</code> binding if one is
   * present, or is otherwise drawn from the match as a whole.
   */
  abstract class AddPositionParamRule extends Rule {
    @Override
    public ParseTreeNode fire(
        ParseTreeNode node, Scope scope, MessageQueue mq) {
      Map<String, ParseTreeNode> bindings = match(node);
      if (bindings != null) {
        ParseTreeNode posNode = bindings.get("posNode");
        if (posNode == null) { posNode = node; }
        FilePosition pos = spanningPos(posNode);
        if (pos == null) { pos = FilePosition.UNKNOWN; }

        int index = symbols.indexForPosition(pos);
        rebind(bindings, scope, mq);
        bindings.put("debug", new IntegerLiteral(index));
        return subst(bindings);
      }
      return NONE;
    }

    protected void rebind(Map<String, ParseTreeNode> bindings,
                          Scope scope, MessageQueue mq) {
      expandEntries(bindings, scope, mq);
    }
  }

  /**
   * Simplifies cajoled code by stripping out the fasttrack check and
   * fasttrack path leaving only the slow, instrumented path.
   */
  abstract class SimplifyingRule extends Rule {
    @Override
    public ParseTreeNode fire(
        ParseTreeNode node, Scope scope, MessageQueue mq) {
      Map<String, ParseTreeNode> bindings = match(node);
      if (bindings != null) {
        expandEntries(bindings, scope, mq);
        return subst(bindings);
      }
      return NONE;
    }
  }

  {
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="callProp",
              synopsis="adds debug info to ___.callProp calls",
              reason="so we can debug callProp calls, silly!",
              matches="___.callProp(@obj, @name, @args)",
              substitutes="___.callProp(@obj, @name, @args, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="callPub",
              synopsis="adds debug info to ___.callPub calls",
              reason="",
              matches="___.callPub(@obj, @name, @args)",
              substitutes="___.callPub(@obj, @name, @args, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="deleteProp",
              synopsis="adds debug info to ___.deleteProp calls",
              reason="",
              matches="___.deleteProp(@obj, @name)",
              substitutes="___.deleteProp(@obj, @name, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="deletePub",
              synopsis="adds debug info to ___.deletePub calls",
              reason="",
              matches="___.deletePub(@obj, @name)",
              substitutes="___.deletePub(@obj, @name, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="readProp",
              synopsis="adds debug info to ___.readProp calls",
              reason="",
              matches="___.readProp(@obj, @name, @opt_shouldThrow?)",
              substitutes="___.readProp(@obj, @name, @opt_shouldThrow, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
          @Override
          protected void rebind(Map<String, ParseTreeNode> bindings,
                                Scope scope, MessageQueue mq) {
            if (bindings.get("opt_shouldThrow") == null) {
              bindings.put("opt_shouldThrow", new BooleanLiteral(false));
            }
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="readPub",
              synopsis="adds debug info to ___.readPub calls",
              reason="",
              matches="___.readPub(@obj, @name, @opt_shouldThrow?)",
              substitutes="___.readPub(@obj, @name, @opt_shouldThrow, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
          @Override
          protected void rebind(Map<String, ParseTreeNode> bindings,
                                Scope scope, MessageQueue mq) {
            if (bindings.get("opt_shouldThrow") == null) {
              bindings.put("opt_shouldThrow", new BooleanLiteral(false));
            }
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="setProp",
              synopsis="adds debug info to ___.setProp calls",
              reason="",
              matches="___.setProp(@obj, @name, @val)",
              substitutes="___.setProp(@obj, @name, @val, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="setPub",
              synopsis="adds debug info to ___.setPub calls",
              reason="",
              matches="___.setPub(@obj, @name, @val)",
              substitutes="___.setPub(@obj, @name, @val, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="asSimpleFunc",
              synopsis="adds debug info to ___.asSimpleFunc calls",
              reason="",
              matches="___.asSimpleFunc(@fun)",
              substitutes="___.asSimpleFunc(@fun, @debug)")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new SimplifyingRule() {
          @Override
          @RuleDescription(
              name="simplifyCall",
              synopsis="remove fasttrack so it can't dereference null",
              reason=("fasttrack useless in debug mode but it can still cause"
                      + "errors outside our stack"),
              matches="@obj.@x_canCall___ ? (@obj.@x(@actuals*)) : @operation",
              substitutes="@operation")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new SimplifyingRule() {
          @Override
          @RuleDescription(
              name="simplifyRead",
              synopsis="remove fasttrack so it can't dereference null",
              reason=("fasttrack useless in debug mode but it can still cause"
                      + "errors outside our stack"),
              matches="@obj.@key_canRead___ ? (@obj.@key) : @operation",
              substitutes="@operation")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new SimplifyingRule() {
          @Override
          @RuleDescription(
              name="simplifySet",
              synopsis="remove fasttrack so it can't dereference null",
              reason=("fasttrack useless in debug mode but it can still cause"
                      + "errors outside our stack"),
              matches="@obj.@key_canSet___ ? (@obj.@key = @y) : @operation",
              substitutes="@operation")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="forInLoop",
              synopsis="Add null check to forInLoop",
              reason="to identify the source of null pointer exceptions",
              matches=("{ @tmp___ = @posNode;"
                       + "for (@k___ in @tmp___) @body; }"),
              substitutes=("{ @tmp___ = ___.requireObject(@posNode, @debug);"
                           + "for (@k___ in @tmp___) @body; }"))
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new AddPositionParamRule() {
          @Override
          @RuleDescription(
              name="inOperator",
              synopsis="Add null check to rhs of 'in' operator",
              reason="to identify the source of null pointer exceptions",
              matches=("(@key in @posNode)"),
              substitutes=("(@key in ___.requireObject(@posNode, @debug))"))
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return super.fire(n, s, mq);
          }
        });
    addRule(new Rule() {
          @Override
          @RuleDescription(
              name="fallback",
              synopsis="handles cases not recognized by ",
              reason="because not everything is a runtime check")
          public ParseTreeNode fire(ParseTreeNode n, Scope s, MessageQueue mq) {
            return expandAll(n, s, mq);
          }
        });
  }

  private FilePosition spanningPos(ParseTreeNode node) {
    FilePosition pos = node.getFilePosition();
    if (!node.getAttributes().is(SyntheticNodes.SYNTHETIC)
        && !FilePosition.UNKNOWN.equals(pos)) {
      return pos;
    }

    FilePosition start = null, end = null;
    for (ParseTreeNode child : node.children()) {
      FilePosition childPos = spanningPos(child);
      if (childPos != null) {
        if (start == null) {
          start = end = childPos;
        } else if (start.source().equals(childPos.source())) {
          if (childPos.startCharInFile() < start.startCharInFile()) {
            start = childPos;
          }
          if (childPos.endCharInFile() > end.endCharInFile()) {
            end = childPos;
          }
        }
      }
    }
    if (start != null) { return FilePosition.span(start, end); }
    return null;
  }
}

/**
 * A module-specific collection of file positions of key pieces of cajoled code
 * that can be bundled with a cajoled module.
 */
final class DebuggingSymbols {
  private Map<FilePosition, Integer> positions
      = new LinkedHashMap<FilePosition, Integer>();

  /** Returns an index into the debugging symbols table. */
  public int indexForPosition(FilePosition pos) {
    Integer index = positions.get(pos);
    if (index == null) {
      positions.put(pos, index = positions.size());
    }
    return index;
  }

  /**
   * Produces a javascript array that can be consumed by
   * {@code ___.useDebugSymbols} from caja-debugmode.js.
   */
  public ArrayConstructor toJavascriptSideTable() {
    MessageContext mc = new MessageContext();
    mc.inputSources = allInputSources();
    List<Expression> debugTable = new ArrayList<Expression>(
        positions.size() * 2  - 1);
    String last = null;
    for (FilePosition p : positions.keySet()) {
      String posStr = formatPos(p, mc);
      int prefixLen = 0;
      if (last != null) {
        prefixLen = commonPrefixLength(posStr, last);
        debugTable.add(new IntegerLiteral(prefixLen));
      }
      debugTable.add(new StringLiteral(
          StringLiteral.toQuotedValue(posStr.substring(prefixLen))));
      last = posStr;
    }
    return new ArrayConstructor(debugTable);
  }

  public boolean isEmpty() { return positions.isEmpty(); }

  private Set<InputSource> allInputSources() {
    Set<InputSource> sources = new HashSet<InputSource>();
    for (FilePosition p : positions.keySet()) {
      sources.add(p.source());
    }
    return sources;
  }

  /** Length of the longest string that is a prefix of both. */
  private static int commonPrefixLength(String a, String b) {
    int n = Math.min(a.length(), b.length());
    int prefixLen = 0;
    while (prefixLen < n && a.charAt(prefixLen) == b.charAt(prefixLen)) {
      ++prefixLen;
    }
    return prefixLen;
  }

  private static String formatPos(FilePosition pos, MessageContext mc) {
    StringBuilder sb = new StringBuilder();
    try {
      pos.format(mc, sb);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return sb.toString();
  }
}
