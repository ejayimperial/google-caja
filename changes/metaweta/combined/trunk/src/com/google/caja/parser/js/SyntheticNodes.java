 // Copyright (C) 2006 Google Inc.
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

package com.google.caja.parser.js;

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.util.SyntheticAttributeKey;

/**
 * Defines a Synthetic Attribute that marks a node as having been generated
 * as part of the compilation process.
 * <p>
 * Such nodes can be exempted from scrutiny by other nodes.  For example,
 * javascript identifiers in the {@code *__} namespace are reserved for
 * use by Caja runtime checks.
 * {@link com.google.caja.parser.js.Reference}s and
 * {@link com.google.caja.parser.js.Declaration}s
 * generated by the Caja compile with names like that should be marked synthetic
 * so that they won't be rejected by the validator.  But since user code is
 * not synthetic, they will be rejected by the validator.
 *
 * @author mikesamuel@gmail.com
 */
public final class SyntheticNodes {
  /**
   * Indicates that a node breaks the caja rules in some way, but that this
   * is OK since it comes from a privileged source, not user code.
   */
  public static final SyntheticAttributeKey<Boolean> SYNTHETIC
      = new SyntheticAttributeKey<Boolean>(Boolean.class, "synthetic");

  public static boolean isSynthesizable(ParseTreeNode node) {
    return (node instanceof Identifier && node.getValue() != null)
        || node instanceof FunctionConstructor;
  }

  /**
   * A convenience function used to mark {@link #isSynthesizable synthesizable}
   * nodes created during source->javascript translation as
   * {@link #SYNTHETIC synthetic}.  Nodes corresponsing to javascript
   * embedded in the original will not be synthetic.
   * <p>
   * This is meant to be imported statically.
   */
  public static <T extends ParseTreeNode> T s(T t) {
    if (isSynthesizable(t)) {
      t.getAttributes().set(SYNTHETIC, Boolean.TRUE);
    }
    return t;
  }

  /** A synthetic identifier may occupy the <code>*__</code> namespace. */
  public static Identifier s(Identifier t) {
    t.getAttributes().set(SYNTHETIC, Boolean.TRUE);
    return t;
  }

  /**
   * Temporaries created as a result of rewriting operations inside a synthetic
   * function are actually declared in the closest non-synthetic enclosing
   * function.
   */
  public static FunctionConstructor s(FunctionConstructor t) {
    t.getAttributes().set(SYNTHETIC, Boolean.TRUE);
    return t;
  }

  private SyntheticNodes() {}
}
