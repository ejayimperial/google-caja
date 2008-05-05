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

import java.io.IOException;

/**
 * @author ihab.awad@gmail.com
 */
public class IllegalReferenceCheckRewriterTest extends RewriterTestCase {
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
  }

  protected Rewriter newRewriter() {
    return new IllegalReferenceCheckRewriter(true);
  }

  protected Object executePlain(String program) throws IOException, ParseException {
    return null;
  }

  protected Object rewriteAndExecute(String program) throws IOException, ParseException {
    return executePlain(program);
  }
}
