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

import javax.mail.internet.ContentType;

public class ImageHandler extends ContentHandler {

  @Override
  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
    return checker.check("image/*", contentType);    
  }
  
  @Override
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) {
    try {
      int next;
      while ((next = stream.read()) != -1) {
        response.write(next);
      } 
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
