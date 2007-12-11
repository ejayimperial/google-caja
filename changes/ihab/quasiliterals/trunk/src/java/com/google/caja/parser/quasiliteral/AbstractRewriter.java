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
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.ParseTreeNodes;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites a parse tree.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public abstract class AbstractRewriter {
  private final Map<String, QuasiNode> patternCache = new HashMap<String, QuasiNode>();      

  public abstract ParseTreeNode rewrite(ParseTreeNode node);

  protected boolean match(
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

  protected ParseTreeNode subst(
      Map<String, ParseTreeNode> bindings,
      String patternText) {
    ParseTreeNode result = getPatternNode(patternText).substituteHere(bindings);

    if (result == null) {
      // Pattern programming error
      // TODO(ihab.awad): Provide a detailed dump of the bindings in the exception
      throw new RuntimeException("Failed to substitute into: \"" + patternText + "\"");
    }

    return result;
  }

  protected void rewriteEntry(Map<String, ParseTreeNode> bindings, String key) {
    bindings.put(key, rewrite(bindings.get(key)));
  }

  protected ParseTreeNode nullRewrite(ParseTreeNode node) {
    List<ParseTreeNode> rewrittenChildren = new ArrayList<ParseTreeNode>();
    for (ParseTreeNode child : node.children()) {
      rewrittenChildren.add(rewrite(child));
    }
    return ParseTreeNodes.newNodeInstance(
        node.getClass(),
        node.getValue(),
        rewrittenChildren);
  }

  private QuasiNode getPatternNode(String patternText) {
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
}