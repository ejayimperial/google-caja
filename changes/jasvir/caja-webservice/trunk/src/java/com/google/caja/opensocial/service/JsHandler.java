package com.google.caja.opensocial.service;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.quasiliteral.DefaultCajaRewriter;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public class JsHandler implements ContentHandler {

  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
    return checker.check("text/javascript",contentType);
  }
  
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) {
    cajoleJs(uri, stream, response);
  }  
      
  private void cajoleJs(URI inputUri, Reader cajaInput, Appendable output) {
    InputSource is = new InputSource (inputUri);    
    CharProducer cp = CharProducer.Factory.create(cajaInput,is);
    MessageQueue mq = new SimpleMessageQueue();
    try {
      ParseTreeNode input;
      JsLexer lexer = new JsLexer(cp);
      JsTokenQueue tq = new JsTokenQueue(lexer, is);
      Parser p = new Parser(tq, mq);
      input = p.parse();
      tq.expectEmpty();

      DefaultCajaRewriter dcr = new DefaultCajaRewriter();
      output.append(dcr.format(dcr.expand(input, mq)));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
