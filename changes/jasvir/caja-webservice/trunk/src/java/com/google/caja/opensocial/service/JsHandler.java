package com.google.caja.opensocial.service;

import com.google.caja.lang.css.CssSchema;
import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.opensocial.DefaultGadgetRewriter;
import com.google.caja.opensocial.UriCallback;
import com.google.caja.opensocial.UriCallbackOption;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.quasiliteral.DefaultCajaRewriter;
import com.google.caja.reporting.HtmlSnippetProducer;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.reporting.SnippetProducer;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

public class JsHandler extends ContentHandler {

  @Override
  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
    return checker.check("text/javascript",contentType);
  }
  
  @Override
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) {
    try {
      cajoleJs(uri, stream, response);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }  
      
  private void cajoleJs(URI inputUri, Reader cajaInput, Appendable output) throws URISyntaxException {
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

  private String messagesToString(
      Map<InputSource, ? extends CharSequence> originalSrc, MessageQueue mq) {
    MessageContext mc = new MessageContext();
    mc.inputSources = originalSrc.keySet();
    SnippetProducer sp = new HtmlSnippetProducer(originalSrc, mc);

    StringBuilder messageText = new StringBuilder();
    for (Message msg : mq.getMessages()) {
      if (MessageLevel.LINT.compareTo(msg.getMessageLevel()) > 0) { continue; }
      String snippet = sp.getSnippet(msg);
      
      messageText.append("<div class=\"message ")
          .append(msg.getMessageLevel().name()).append("\">")
          .append(msg.getMessageLevel().name()).append(' ')
          .append(msg.format(mc));
      if (!"".equals(snippet)) {
        messageText.append("<br />").append(snippet);
      }
      messageText.append("</div>");
    }
    return messageText.toString();
  }
}
