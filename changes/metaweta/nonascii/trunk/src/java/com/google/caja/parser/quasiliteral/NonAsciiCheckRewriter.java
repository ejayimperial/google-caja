//Copyright (C) 2008 Google Inc.
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
import com.google.caja.parser.js.Identifier;
import com.google.caja.plugin.SyntheticNodes;
import com.google.caja.reporting.MessageQueue;

/**
 * Checks for non-{@link SyntheticNodes#SYNTHETIC synthetic} identifiers
 * containing non-ASCII characters.
 *
 * @author metaweta@gmail.com (Mike Stay)
 */
@RulesetDescription(
    name="NonAsciiCheckRewriter",
    synopsis="Check that all references containing non-ASCII characters are synthetic.")
public class NonAsciiCheckRewriter extends Rewriter {
  final public Rule[] cajaRules = {
    new Rule () {
      @Override
      @RuleDescription(
          name="identifierNonAscii",
          synopsis="Check that any identifier containing non-ASCII characters is synthetic.",
          reason="Non-ASCII characters can change the semantics depending on the interpreter.")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        if (node instanceof Identifier && !node.getAttributes().is(SyntheticNodes.SYNTHETIC)) {
          String name = ((Identifier)node).getValue();
          if (name != null && !name.matches("^[a-zA-Z_$][a-zA-Z0-9_$]*$")) {
            mq.addMessage(
                RewriterMessageType.ILLEGAL_IDENTIFIER_LEFT_OVER,
                node.getFilePosition(), node);
            return node;
          }
        }
        return NONE;
      }
    },

    new Rule () {
      @Override
      @RuleDescription(
          name="recurse",
          synopsis="Recurse into children",
          reason="")
      public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
        return expandAll(node, scope, mq);
      }
    },
  };

  public NonAsciiCheckRewriter() {
    this(true);
  }

  public NonAsciiCheckRewriter(boolean logging) {
    super(logging);
    addRules(cajaRules);
  }
}


