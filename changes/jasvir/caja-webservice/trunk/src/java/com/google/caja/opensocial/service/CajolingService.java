//Copyright (C) 2008 Google Inc.
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

package com.google.caja.opensocial.service;

import com.google.caja.lexer.InputSource;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A cajoling service which proxies connections:
 *      - cajole any javascript
 *      - cajoles any gadgets
 *      - checks requested and retrieved mime-types  
 *      
 * @author jasvir@gmail.com (Jasvir Nagra)
 */
public class CajolingService implements HttpHandler {
  private Map<InputSource, CharSequence> originalSources
    = new HashMap<InputSource, CharSequence>();
  private List<ContentHandler> handlers = new Vector<ContentHandler>();
  private ContentTypeCheck typeCheck = new LooseContentTypeCheck();
  private HttpServer server;
  
  public CajolingService() {
    registerHandlers();
  }
  
  public void start() {
    try{
      // TODO(jas): Use Config to config port
      server = HttpServer.create(new InetSocketAddress(8887),0);
      HttpContext ctx = server.createContext("/cajaservice",this);
      server.setExecutor(null);
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void stop() {
    server.stop(0);
  }

  private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();
    
    for (String param : params) {
      String[] result = param.split("=");
      String name = result[0];
      String value = URLDecoder.decode(result[1], "UTF-8");
      map.put(name, value);
    }
    return map;
  }
  
  /**
   * Read the remainder of the input request, send a BAD_REQUEST http status
   * to browser and close the connection
   * @param ex
   * @throws IOException 
   */
  private void closeBadRequest(HttpExchange ex) throws IOException {
    ex.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(),0);
    ex.getResponseBody().close();    
  }
  
  public void handle(HttpExchange ex) throws IOException {
    try {
      String requestMethod = ex.getRequestMethod();
      if (requestMethod.equalsIgnoreCase("GET")) {
        Map<String,String> urlMap = parseQuery(ex.getRequestURI().getQuery());

        String gadgetUrlString = urlMap.get("url");
        if (gadgetUrlString == null)
          throw new URISyntaxException(ex.getRequestURI().toString(), "Missing parameter \"url\" is required");
        URL gadgetUrl = new URL(gadgetUrlString);
        
        String expectedMimeType = urlMap.get("mime-type");
        if (expectedMimeType == null)
          throw new URISyntaxException(ex.getRequestURI().toString(), "Missing parameter \"mime-type\" is required");
        
        URLConnection urlConnect = gadgetUrl.openConnection();
        urlConnect.connect();
        Reader stream = new InputStreamReader(urlConnect.getInputStream());
        
        Headers responseHeaders = ex.getResponseHeaders();
        
        if (!typeCheck.check(expectedMimeType, urlConnect.getContentType())) {
          closeBadRequest(ex);
          return;
        }
        
        responseHeaders.set("Content-Type", expectedMimeType);
        ex.sendResponseHeaders(HttpStatus.ACCEPTED.value(), 0);
  
        Writer response = new OutputStreamWriter(ex.getResponseBody());
  
        applyHandler(gadgetUrl.toURI(), urlConnect.getContentType(), stream, response);
        
        response.close();
      }
    } catch (MalformedURLException e){
      closeBadRequest(ex);
      e.printStackTrace();
    } catch (URISyntaxException e) {
      closeBadRequest(ex);
      e.printStackTrace();
    } catch (UnsupportedContentTypeException e) {
      closeBadRequest(ex);
      e.printStackTrace();
    }
  }
   
  public void registerHandlers() {
    handlers.add(new JsHandler());
  }
  
  private void applyHandler(URI uri, String contentType, 
                            Reader stream, Writer response) 
      throws UnsupportedContentTypeException {
    for (ContentHandler handler : handlers) {
      if ( handler.canHandle(uri, contentType, typeCheck) ) {
        handler.apply(uri, contentType, stream, response);
        return;
      }
    }
    throw new UnsupportedContentTypeException();
  }
}
