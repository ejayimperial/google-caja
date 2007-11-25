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

import com.google.caja.lexer.ParseException;
import com.google.caja.parser.AbstractParseTreeNode;
import com.google.caja.parser.ParseTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites a JavaScript parse tree to comply with Caja rules.
 *
 * <p>TODO(ihab.awad): We will likely need various flavors of rewriting. This class is
 * deliberately specific to the immediate problem, but we'll need to refactor later.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class Rewriter {
  private final Map<String, QuasiNode> patternCache = new HashMap<String, QuasiNode>();      

  public ParseTreeNode rewrite(ParseTreeNode node) {
    Map<String, ParseTreeNode> bindings = new HashMap<String, ParseTreeNode>();

    // TODO(ihab.awad): The way this works, we *always* rewrite the contents of the bindings.
    // But some rewrite rules may define "synthetic" nodes which are passed into subst(...)
    // in the bindings map, but which should not be rewritten. Figure out a clean way to handle
    // the interleaving of both cases in the rewrite rules.

    // TODO(ihab.awad): These are dummy rules; implement the real ones.

    if (match(node, bindings, "@foo + @bar")) {
      return subst(bindings, "@foo - @bar");
    }

    return nullRewrite(node);
  }

  private boolean match(
      ParseTreeNode node,
      Map<String, ParseTreeNode> bindings,
      String patternText) {
    Map<String, ParseTreeNode> tempBindings = getPatternNode(patternText).matchHere(node);

    if (tempBindings != null) {
      bindings.putAll(tempBindings);
      return true;
    }
    return false;
  }

  private ParseTreeNode subst(
      Map<String, ParseTreeNode> bindings,
      String patternText) {
    ParseTreeNode result = getPatternNode(patternText).substituteHere(rewriteBindings(bindings));

    if (result == null) {
      // Pattern programming error
      // TODO(ihab.awad): Provide a detailed dump of the bindings in the exception
      throw new RuntimeException("Failed to substitute into: \"" + patternText + "\"");
    }

    return result;
  }

  private Map<String, ParseTreeNode> rewriteBindings(
      Map<String, ParseTreeNode> bindings) {
    Map<String, ParseTreeNode> result = new HashMap<String, ParseTreeNode>();
    for (String key : bindings.keySet()) {
      result.put(key, rewrite(bindings.get(key)));
    }
    return result;
  }

  private ParseTreeNode nullRewrite(ParseTreeNode node) {
    List<ParseTreeNode> rewrittenChildren = new ArrayList<ParseTreeNode>();
    for (ParseTreeNode child : node.children())
      rewrittenChildren.add(rewrite(child));
    return AbstractParseTreeNode.newNodeInstance(
        node.getClass(),
        node.getValue(),
        rewrittenChildren);
  }

  private QuasiNode getPatternNode(String patternText) {
    if (!patternCache.containsKey(patternText)) {
      try {
        patternCache.put(patternText, QuasiBuilder.parseQuasiNode(patternText));
      } catch (ParseException e) {
        // Pattern programming error
        throw new RuntimeException(e);
      }
    }
    return patternCache.get(patternText);
  }
}