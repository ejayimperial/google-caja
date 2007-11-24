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

import java.util.*;

/**
 * A quasiliteral node that can match trees and substitute into trees of
 * {@link com.google.caja.parser.ParseTreeNode} objects, as parsed by the
 * Caja JavaScript {@link com.google.caja.parser.js.Parser}.
 *
 * <p>TODO(ihab.awad): This is a deliberately specific implementation for JavaScript
 * parse trees; decide later on whether this class hierarchy should be generalized,
 * and how.
 *
 * <p>TODO(ihab.awad): Support quasiliterals containing identifier string patterns, e.g.,
 * the pattern {@code @{f}_} matches an identifier in program text that ends with an underscore
 * and binds it to the variable {@code f}.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public abstract class QuasiNode {
  private final List<QuasiNode> children;

  protected QuasiNode(QuasiNode... children) {
    this.children = Collections.unmodifiableList(Arrays.asList(children));
  }

  public List<QuasiNode> getChildren() { return children; }

  public Map<String, ParseTreeNode> matchHere(ParseTreeNode specimen) {
    List<ParseTreeNode> specimens = new ArrayList<ParseTreeNode>();
    specimens.add(specimen);
    Map<String, ParseTreeNode> bindings = new HashMap<String, ParseTreeNode>();
    return consumeSpecimens(specimens, bindings) ? bindings : null;
  }

  public ParseTreeNode substituteHere(Map<String, ParseTreeNode> bindings) {
    List<ParseTreeNode> results = new ArrayList<ParseTreeNode>();
    return (createSubstitutes(results, bindings) && results.size() == 1) ? results.get(0) : null;
  }

  protected abstract boolean consumeSpecimens(
      List<ParseTreeNode> specimens,
      Map<String, ParseTreeNode> bindings);

  protected abstract boolean createSubstitutes(
      List<ParseTreeNode> substitutes,
      Map<String, ParseTreeNode> bindings);
  
  public String render() {
    return render(0);
  }

  private String render(int level) {
    String result = "";
    for (int i = 0; i < level; i++) result += "  ";
    result += this.toString() + "\n";
    for (QuasiNode child : getChildren()) result += child.render(level + 1);
    return result;
  }

  protected static boolean safeEquals(Object x, Object y) {
    return
        (x == null && y == null) ||
        (x != null && y != null && x.equals(y));
  }
}
