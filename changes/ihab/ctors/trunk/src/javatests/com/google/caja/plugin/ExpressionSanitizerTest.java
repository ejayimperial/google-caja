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

package com.google.caja.plugin;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Parser;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.TestUtil;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;

import junit.framework.TestCase;

/**
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class ExpressionSanitizerTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /*

  // TURN INTO FUNCTIONAL TESTS

  }

  */

  private void runTest(String input, String golden, boolean sanitary)
      throws Exception {
    MessageContext mc = new MessageContext();
    MessageQueue mq = new EchoingMessageQueue(
        new PrintWriter(new OutputStreamWriter(System.err)), mc);
    PluginMeta meta = new PluginMeta("pre");

    InputSource is = new InputSource(new URI("test:///" + getName()));
    CharProducer cp = CharProducer.Factory.create(
        new StringReader(input), is);
    Block jsBlock;
    {
      JsLexer lexer = new JsLexer(cp);
      JsTokenQueue tq = new JsTokenQueue(
          lexer, is, JsTokenQueue.NO_NON_DIRECTIVE_COMMENT);
      Parser p = new Parser(tq, mq);
      jsBlock = p.parse();
      p.getTokenQueue().expectEmpty();
    }

    boolean reallyFailed = false;

    boolean actualSanitary = new ExpressionSanitizerCaja(mq, meta)
        .sanitize(ac(jsBlock));
    if (actualSanitary) {
      for (Message msg : mq.getMessages()) {
        if (MessageLevel.ERROR.compareTo(msg.getMessageLevel()) <= 0) {
          reallyFailed = true;
          if (sanitary) {
            fail(msg.toString());
          }
          break;
        }
      }
    }

    if (!sanitary) {
      assertTrue(reallyFailed);
    }

    StringBuilder actualBuf = new StringBuilder();
    RenderContext rc = new RenderContext(mc, actualBuf);

    jsBlock.render(rc);

    String actual = actualBuf.toString();

    if (sanitary) {
      // TODO(mikesamuel): replace with a reparse and structural comparison.
      assertEquals(actual, golden.trim(), actual.trim());
      assertEquals(input, sanitary, actualSanitary);
    }
  }

  /*


  */

  private static <T extends ParseTreeNode> AncestorChain<T> ac(T node) {
    return new AncestorChain<T>(node);
  }
}
