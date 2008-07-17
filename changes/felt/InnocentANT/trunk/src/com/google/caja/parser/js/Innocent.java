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

package com.google.caja.parser.js;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.Pair;
import com.google.caja.parser.quasiliteral.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * An executable that directs the innocent code transformation of
 * input javascript.
 * 
 * Usage
 * <pre>
 * java com.google.caja.parser.js.Innocent file.js > transformed.js
 * </pre>
 *
 * <p>This parses and renders JS so guarantees valid output.</p>
 *
 * <p>It rewrites for-in loops and adds runtime checks to functions
 * that use the THIS keyword.</p>
 *
 * @author adriennefelt@gmail.com
 * @author mikesamuel@gmail.com
 */
public class Innocent {
  public static void main(String[] jsFilePaths) throws IOException {
    if (jsFilePaths.length > 1) {
      throw new IOException("The ICR only takes one input file.");
    }
    String jsFilePath = jsFilePaths[0];
    Pair<InputSource, File> input = checkInput(jsFilePath);
    boolean passed = innocent(input, new PrintWriter(System.out),
          new PrintWriter(System.err));

    System.exit(passed ? 0 : -1);
  }

  /** Called before opening file to check that input is readable. */
  private static Pair<InputSource, File> checkInput(String jsFile)
      throws IOException {
    Pair<InputSource, File> input;
    File f = new File(jsFile);
    if (!f.canRead()) { throw new IOException("Cannot read " + jsFile); }
    InputSource is = new InputSource(f.getAbsoluteFile().toURI());
    input = Pair.pair(is, f);
    return input;
  }

  public static boolean innocent(Pair<InputSource, File> input,
                               Writer out, PrintWriter err)
      throws IOException {
    
    MessageContext mc = new MessageContext();
    mc.inputSources = new ArrayList<InputSource>();
    mc.inputSources.add(input.a);
    
    final MessageQueue errs = new EchoingMessageQueue(
        err, mc, false);
    CharProducer cp = CharProducer.Factory.create(
        new InputStreamReader(new FileInputStream(input.b), "UTF-8"),
        input.a);
    
    JsLexer lexer = new JsLexer(cp);
    JsTokenQueue tq = new JsTokenQueue(lexer, input.a);
    Parser p = new Parser(tq, errs);
    String output = new String();
    
    try {    
      Block start = p.parse();      
      tq.expectEmpty();
      Rewriter icr = new InnocentCodeRewriter(true);
      output = DefaultCajaRewriter.format(icr.expand(start,errs));
      out.append(output);
    } catch (ParseException ex) {
      ex.toMessageQueue(errs);
    }

    out.flush();

    MessageLevel maxMessageLevel = MessageLevel.values()[0];
    for (Message msg : errs.getMessages()) {
      if (msg.getMessageLevel().compareTo(maxMessageLevel) >= 0) {
        maxMessageLevel = msg.getMessageLevel();
      }
    }
    return maxMessageLevel.compareTo(MessageLevel.ERROR) < 0; 
  }
}
