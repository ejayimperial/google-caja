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

package com.google.caja.parser.quasiliteral;

import com.google.caja.lexer.ParseException;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.plugin.SyntheticNodes;

import java.io.IOException;

/**
 * @author ihab.awad@gmail.com
 */
public class RewriterTest extends RewriterTestCase {
  public void testIllegalRefs() throws Exception {
    checkAddsMessage(
        js(fromString("var x__;")),
        RewriterMessageType.ILLEGAL_IDENTIFIER_LEFT_OVER,
        MessageLevel.FATAL_ERROR);
    checkAddsMessage(
        js(fromString("function f__() { }")),
        RewriterMessageType.ILLEGAL_IDENTIFIER_LEFT_OVER,
        MessageLevel.FATAL_ERROR);
    checkAddsMessage(
        js(fromString("var x = function f__() { };")),
        RewriterMessageType.ILLEGAL_IDENTIFIER_LEFT_OVER,
        MessageLevel.FATAL_ERROR);
    checkAddsMessage(
        js(fromString("x__ = 3;")),
        RewriterMessageType.ILLEGAL_IDENTIFIER_LEFT_OVER,
        MessageLevel.FATAL_ERROR);
    checkSucceeds(
        js(fromString("x = y.__proto__;")),
        null);
  }

  protected Rewriter newRewriter() {
    return new Rewriter(true) {{
      addRule(new Rule () {
        @Override
        @RuleDescription(
            name="setTaint",
            synopsis="Ensures that the result is tainted",
            reason="Test that Rewriter tainting check works")
        public ParseTreeNode fire(ParseTreeNode node, Scope scope, MessageQueue mq) {
          node.getAttributes().set(SyntheticNodes.TAINTED, true);
          return node;
        }        
      });
    }};
  }

  protected Object executePlain(String program) throws IOException, ParseException {
    return null;
  }

  protected Object rewriteAndExecute(String program) throws IOException, ParseException {
    return executePlain(program);
  }
}
