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

package com.google.caja.tools.jsdoc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.ObjectConstructor;
import com.google.caja.parser.js.Parser;
import com.google.caja.render.JsLinePreservingPrinter;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.MessageType;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.Executor;
import com.google.caja.util.Pair;

/**
 * Extracts documentation from JavaScript source code by rewriting the source
 * code to annotate values with doc strings, executes the code, and then walks
 * enumerable properties to extract the public API.
 *
 * The extracted API is exposed as JSON which can be formatted by the
 * <tt>./jsdoc_html_formatter.js</tt>.
 *
 * @author mikesamuel@gmail.com
 */
public class Jsdoc {
  private final MessageContext mc;
  private final MessageQueue mq;
  private final AnnotationHandlers handlers;
  private final List<ParseTreeNode> sources = new ArrayList<ParseTreeNode>();
  private final List<Pair<InputSource, Comment>> packageDocs
      = new ArrayList<Pair<InputSource, Comment>>();
  private final List<Pair<String, String>> initFiles
      = new ArrayList<Pair<String, String>>();

  Jsdoc(AnnotationHandlers handlers, MessageContext mc, MessageQueue mq) {
    this.mc = mc;
    this.mq = mq;
    this.handlers = handlers;
  }

  void addInitFile(String path, String content) {
    this.initFiles.add(Pair.pair(path, content));
  }
  void addSource(ParseTreeNode source) { this.sources.add(source); }
  void addPackage(InputSource pkg, Comment docs) {
    packageDocs.add(Pair.pair(pkg, docs));
  }

  /**
   * Produces documentation JSON from the {@link #addSource sources} and
   * {@link #addPackage packages} added prior.
   */
  ObjectConstructor extract() throws JsdocException {
    Executor.Input[] rewritten = sourceCodeWithDocHooks();
    try {
      Map<String, Object> bindings = new LinkedHashMap<String, Object>();
      bindings.put("stderr", System.err);
      bindings.put("jsdocMessageQueue__", mq);

      ObjectConstructor o = toJson(Executor.Factory.createJsExecutor(rewritten)
          .run(bindings, String.class), mq);
      System.err.flush();
      return o;
    } catch (Executor.AbnormalExitException ex) {
      Throwable th = ex;
      if (th.getCause() != null) { th = th.getCause(); }
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      th.printStackTrace(pw);
      pw.flush();
      String scriptTrace = sw.toString();
      throw new JsdocException(new Message(
          JsdocMessageType.SCRIPT_FAILED_AT_RUNTIME,
          MessagePart.Factory.valueOf(scriptTrace)), ex);
    } catch (Executor.MalformedSourceException ex) {
      throw new JsdocException(new Message(
          MessageType.INTERNAL_ERROR,
          MessagePart.Factory.valueOf("Script not parseable by Rhino")), ex);
    } catch (ParseException ex) {
      throw new JsdocException(new Message(
          MessageType.INTERNAL_ERROR,
          MessagePart.Factory.valueOf("Failed to parse JSON")), ex);
    }
  }

  /** Use the rewriter to add documentation hooks to JavaScript source. */
  private Executor.Input[] sourceCodeWithDocHooks() {
    JsdocRewriter rw = new JsdocRewriter(handlers, mc, mq);
    List<Executor.Input> hooked = new ArrayList<Executor.Input>();
    for (Pair<String, String> initFile : initFiles) {
      hooked.add(new Executor.Input(new StringReader(initFile.b), initFile.a));
    }
    try {
      // Add supporting js
      hooked.add(new Executor.Input(
          Jsdoc.class, "/com/google/caja/plugin/console-stubs.js"));
      hooked.add(new Executor.Input(Jsdoc.class, "jsdoc.js"));
    } catch (IOException ex) {
      throw new RuntimeException(ex);  // Fail if supporting JS missing
    }
    for (ParseTreeNode src : sources) {
      ParseTreeNode rewritten = rw.rewriteFile(src);
      String rewrittenJs = render(rewritten, src.getFilePosition().source());

      hooked.add(new Executor.Input(
          new StringReader(rewrittenJs),
          src.getFilePosition().source().getUri().toString()));
    }
    for (Pair<InputSource, Comment> doc : packageDocs) {
      ParseTreeNode pkgDocs = rw.rewritePackageDocs(doc.a, doc.b);
      if (pkgDocs == null) { continue; }
      String pkgDocsJs = render(pkgDocs);
      hooked.add(new Executor.Input(
          new StringReader(pkgDocsJs),
          doc.a.getUri().toString()));
    }
    // Add JS to emit the result.
    hooked.add(new Executor.Input(
        "jsdoc___.formatJson(jsdoc___.extractDocs())", "jsdoc"));
    return hooked.toArray(new Executor.Input[0]);
  }

  private static ObjectConstructor toJson(String json, MessageQueue mq)
      throws ParseException {
    InputSource is = new InputSource(URI.create("jsdoc:///output.json"));
    CharProducer cp = CharProducer.Factory.create(new StringReader(json), is);
    JsTokenQueue tq = new JsTokenQueue(new JsLexer(cp), is);
    Parser p = new Parser(tq, mq);
    ObjectConstructor jsonObj = (ObjectConstructor) p.parseExpression(true);
    tq.expectEmpty();

    return jsonObj;
  }

  private static String render(ParseTreeNode node, InputSource is) {
    StringBuilder out = new StringBuilder();
    RenderContext rc = new RenderContext(
        new MessageContext(), new JsLinePreservingPrinter(is, out, null));
    node.render(rc);
    rc.getOut().noMoreTokens();
    return out.toString();
  }

  private static String render(ParseTreeNode node) {
    StringBuilder out = new StringBuilder();
    RenderContext rc = new RenderContext(
        new MessageContext(), node.makeRenderer(out, null));
    node.render(rc);
    rc.getOut().noMoreTokens();
    return out.toString();
  }
}
