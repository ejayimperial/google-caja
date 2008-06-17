package com.google.caja.opensocial.service;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.quasiliteral.InnocentCodeRewriter;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: felt
 * Date: Jun 3, 2008
 * Time: 4:52:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class InnocentJSHandler implements ContentHandler {

  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
      return checker.check("text/javascript",contentType);
  }

  public Pair<String,String> apply(URI uri,
      String contentType, String contentEncoding, String contentCharset,
      InputStream stream, OutputStream response)
      throws UnsupportedContentTypeException {
    try {
      OutputStreamWriter writer = new OutputStreamWriter(response, "UTF-8");
      innocentTransform(uri, new InputStreamReader(stream, contentCharset), writer);
      writer.flush();
    } catch (IOException e) {
      throw new UnsupportedContentTypeException();
    }
    return new Pair<String, String>("text/javascript", "UTF-8");
  }

  private void innocentTransform(URI inputUri, Reader innocIn, Appendable output) throws UnsupportedContentTypeException {
    InputSource is = new InputSource (inputUri);
    CharProducer cp = CharProducer.Factory.create(innocIn,is);
    MessageQueue mq = new SimpleMessageQueue();
    try {
      ParseTreeNode input;
      JsLexer lexer = new JsLexer(cp);
      JsTokenQueue tq = new JsTokenQueue(lexer, is);
      Parser p = new Parser(tq, mq);
      input = p.parse();
      tq.expectEmpty();

      InnocentCodeRewriter icr = new InnocentCodeRewriter(true);
      output.append(icr.format(icr.expand(input, mq)));
    } catch (ParseException e) {
      throw new UnsupportedContentTypeException();
    } catch (IllegalArgumentException e) {
      throw new UnsupportedContentTypeException();
    } catch (IOException e) {
      throw new UnsupportedContentTypeException();
    }
  }

}



