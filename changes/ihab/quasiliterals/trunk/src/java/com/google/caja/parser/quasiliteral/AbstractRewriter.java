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

import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.AbstractParseTreeNode;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodes;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites a parse tree.
 *
 * <p>TODO(ihab.awad): Refactor to a more general quasiliteral library for
 * {@code ParseTreeNode}s and a set of specific implementations for various
 * languages (JS, HTML, CSS, ...).
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public abstract class AbstractRewriter {
  private final Map<String, QuasiNode> patternCache = new HashMap<String, QuasiNode>();      

  public final ParseTreeNode expand(ParseTreeNode module) {
    return expand(module, new Scope(module));
  }

  protected final ParseTreeNode expand(ParseTreeNode node, Scope scope) {
    // The subclass overrides doExpand but recurses by calling expand, so we have
    // a place to wrap each step in logging, debugging, or other boilerplate.
    return doExpand(node, scope);
  }

  protected final boolean match(
      ParseTreeNode node,
      Map<String, ParseTreeNode> bindings,
      QuasiNode pattern) {
    Map<String, ParseTreeNode> tempBindings = pattern.matchHere(node);

    if (tempBindings != null) {
      bindings.putAll(tempBindings);
      return true;
    }
    return false;
  }

  protected final boolean match(
      ParseTreeNode node,
      Map<String, ParseTreeNode> bindings,
      String patternText) {
    return match(node, bindings, getPatternNode(patternText));
  }

  protected final ParseTreeNode subst(
      Map<String, ParseTreeNode> bindings,
      String patternText) {
    return subst(bindings, getPatternNode(patternText));
  }

  protected final ParseTreeNode subst(
      Map<String, ParseTreeNode> bindings,
      QuasiNode pattern) {
    ParseTreeNode result = pattern.substituteHere(bindings);

    if (result == null) {
      // Pattern programming error
      // TODO(ihab.awad): Provide a detailed dump of the bindings in the exception
      throw new RuntimeException("Failed to substitute into: \"" + pattern + "\"");
    }

    return result;
  }

  protected final ParseTreeNode substV(Object... args) {
    if (args.length %2 == 0) throw new RuntimeException("Wrong # of args for subst()");
    Map<String, ParseTreeNode> bindings = new HashMap<String, ParseTreeNode>();
    for (int i = 0; i < args.length - 1; ){
      bindings.put(
          (String)args[i++],
          (ParseTreeNode)args[i++]);
    }
    return subst(bindings, (String)args[args.length - 1]);    
  }

  protected final void expandEntry(
      Map<String, ParseTreeNode> bindings,
      String key,
      Scope scope) {
    bindings.put(key, expand(bindings.get(key), scope));
  }

  protected final void expandEntries(
      Map<String, ParseTreeNode> bindings,
      Scope scope) {
    for (String key : bindings.keySet()) {
      expandEntry(bindings, key, scope);
    }
  }

  protected final ParseTreeNode expandChildren(ParseTreeNode node, Scope scope) {
    // TODO(ihab.awad): Remove parentify(): not needed with new parse tree classes
    ((AbstractParseTreeNode)node).parentify();

    List<ParseTreeNode> rewrittenChildren = new ArrayList<ParseTreeNode>();
    for (ParseTreeNode child : node.children()) {
      rewrittenChildren.add(expand(child, scope));
    }
    return ParseTreeNodes.newNodeInstance(
        node.getClass(),
        node.getValue(),
        rewrittenChildren);
  }

  protected final QuasiNode getPatternNode(String patternText) {
    if (!patternCache.containsKey(patternText)) {
      try {
        patternCache.put(
            patternText,
            QuasiBuilder.parseQuasiNode(
                new InputSource(URI.create("built-in:///js-quasi-literals")),
                patternText));
      } catch (ParseException e) {
        // Pattern programming error
        throw new RuntimeException(e);
      }
    }
    return patternCache.get(patternText);
  }

  protected final String stringJoin(String delim, String[] values) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      sb.append(values[i]);
      if (i < values.length - 1) sb.append(delim);
    }
    return sb.toString();
  }

  protected String format(ParseTreeNode n) {
    try {
      StringBuilder output = new StringBuilder();
      n.render(new RenderContext(new MessageContext(), output));
      return output.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }  

  protected abstract ParseTreeNode doExpand(ParseTreeNode node, Scope scope);
}