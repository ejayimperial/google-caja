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

import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.CajaTestCase;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class ExpressionSanitizerTest extends CajaTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testFoo() throws Exception {
    runTest(
        "x",
        "___OUTERS___.x_canRead___ ? ___OUTERS___.x : ___.readPub(___OUTERS___, 'x', true);");
  }

  private void runTest(String input, String golden)
      throws Exception {
    MessageContext mc = new MessageContext();
    MessageQueue mq = new EchoingMessageQueue(
        new PrintWriter(new OutputStreamWriter(System.err)), mc);
    PluginMeta meta = new PluginMeta();

    Block inputNode = js(fromString(input));
    assertTrue(new ExpressionSanitizerCaja(mq, meta).sanitize(ac(inputNode)));
    String inputCmp = render(inputNode);

    String goldenCmp = render(js(fromString(golden)));

    assertEquals(goldenCmp, inputCmp);
  }

  private static <T extends ParseTreeNode> AncestorChain<T> ac(T node) {
    return new AncestorChain<T>(node);
  }
}
