// Copyright 2007 Google Inc. All Rights Reserved.
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

package com.google.caja.opensocial.service;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.InputSource;
import com.google.caja.lexer.JsLexer;
import com.google.caja.lexer.JsTokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.opensocial.Callback;
import com.google.caja.opensocial.DefaultGadgetRewriter;
import com.google.caja.opensocial.GadgetRewriteException;
import com.google.caja.opensocial.UriCallback;
import com.google.caja.opensocial.UriCallbackException;
import com.google.caja.opensocial.UriCallbackOption;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Parser;
import com.google.caja.parser.quasiliteral.DefaultCajaRewriter;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.reporting.SnippetProducer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class GadgetHandler extends ContentHandler {

  @Override
  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
   System.out.println("Check GadgetHanlder canHandle " + uri + " (" + contentType + ")");
   return checker.check("application/xml",contentType);
  }

  @Override
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) {
    System.out.println("Applying GadgetHandlder on " + uri + " (" + contentType + ")");    
    try {
      cajoleGadget(uri, stream, response);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UriCallbackException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (GadgetRewriteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void cajoleGadget(URI inputUri, Reader cajaInput, Appendable output) 
    throws URISyntaxException, UriCallbackException, ParseException, 
           GadgetRewriteException, IOException {
    MessageQueue mq = new SimpleMessageQueue();
    DefaultGadgetRewriter rewriter = new DefaultGadgetRewriter(mq);

    UriCallback uriCallback = new UriCallback() {
      public UriCallbackOption getOption(
          ExternalReference extRef, String mimeType) {
        return UriCallbackOption.REWRITE;
      }
      public Reader retrieve(ExternalReference extref, String mimeType) {
        return null;
      }

      public URI rewrite(ExternalReference extref, String mimeType) {
        System.out.println("Rewriting " + extref);
        try {
          return URI.create(
              "http://localhost:8887/?url="
              + URLEncoder.encode(extref.getUri().toString(), "UTF-8")
              + "&mime-type=" + URLEncoder.encode(mimeType, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
          throw new RuntimeException("UTF-8 should be supported.", ex);
        }
      }
    };

    Reader r = uriCallback.retrieve(new ExternalReference(inputUri, null), null);
    CharProducer p = CharProducer.Factory.create(r, new InputSource(inputUri));
    rewriter.rewrite(inputUri, p, uriCallback, "view", output);
  }
}
