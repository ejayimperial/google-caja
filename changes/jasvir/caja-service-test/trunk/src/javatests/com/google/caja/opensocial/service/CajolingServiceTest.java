// Copyright (C) 2007 Google Inc.
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

import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNodes;
import com.google.caja.util.CajaTestCase;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Tests the running the cajoler as a webservice
 * 
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class CajolingServiceTest extends CajaTestCase {
  private CajolingService service;
  private TestingHttpHandler httpService;
  
  
  /**
   * Creates a webservice which returns the 
   */
  private class TestingHttpHandler implements HttpHandler {
  private String testInstance;
  private String contentType;
  
  public void setTest(String test, String contentType) {
    this.testInstance = test;
    this.contentType = contentType;
  }

   // returns 
   public void handle(HttpExchange ex) throws IOException {
     Reader request = new InputStreamReader(ex.getRequestBody());
     ex.getResponseHeaders().set("Content-Type", contentType);
     ex.sendResponseHeaders(HttpStatus.OK.value(), 0);
     Writer response = new OutputStreamWriter(ex.getResponseBody());
     response.write(testInstance);
     System.out.println(testInstance);
     response.close();
   }
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = new CajolingService();
    HttpServer server = HttpServer.create(new InetSocketAddress(18887),0);
    httpService = new TestingHttpHandler();
    HttpContext ctx = server.createContext("/cajaservicetest", httpService);
    server.setExecutor(null);
    server.start();
    service.start();
  }

  @Override
  public void tearDown() { 
    service.stop();
    service = null; 
    httpService = null;
  }

  public void testSimpleJs() throws Exception {
    checkJs(
        "{ var x = y; }",
        "{" + weldSetOuters("x", weldReadOuters("y")) + "}");
  }
  
  private void checkJs(String original, String cajoled) throws IOException, ParseException {
      httpService.setTest(original, "text/javascript");
      String localTestServer = "http://localhost:8887/cajaservice";
      String fetchUrl = "http://localhost:18887/cajaservicetest";
      String mimeType = "text/javascript";
      
      String request = localTestServer 
          + "?url=" + URLEncoder.encode(fetchUrl,"UTF-8")
          + "&mime-type=" + URLEncoder.encode(mimeType, "UTF-8");
      System.out.println(request);
      String response = getTextRequest(request);
      ParseTreeNodes.deepEquals(js(fromString(response)), js(fromString(cajoled)));
  }

  private String getTextRequest(String testServer) 
    throws IOException {
    URL serverUrl = new URL(testServer);
    HttpURLConnection connection = (HttpURLConnection)serverUrl.openConnection();
    InputStreamReader content = new InputStreamReader(connection.getInputStream());
    StringBuffer request = new StringBuffer();
    int in;
    while ((in = content.read()) != -1) { 
      request.append((char)in);
    }
    return request.toString();
  }
  
  // From DefaultCajaRewriterTest:
  
  /**
   * Welds together a string representing the repeated pattern of expected test output for
   * assigning to an outer variable.
   *
   * @author erights@gmail.com
   */
  private static String weldSetOuters(String varName, String expr) {
    return
        "(function() {" +
        "  var x___ = (" + expr + ");" +
        "  return (___OUTERS___." + varName + "_canSet___ ?" +
        "      (___OUTERS___." + varName + " = x___) :" +
        "      ___.setPub(___OUTERS___,'" + varName + "',x___));" +
        "})()";
  }

  /**
   * Welds together a string representing the repeated pattern of expected test output for
   * reading an outer variable.
   *
   * @author erights@gmail.com
   */
  private static String weldReadOuters(String varName) {
    return weldReadOuters(varName, true);
  }

  private static String weldReadOuters(String varName, boolean flag) {
    return
        "(___OUTERS___." + varName + "_canRead___ ?" +
        "    ___OUTERS___." + varName + ":" +
        "    ___.readPub(___OUTERS___, '" + varName + "'" + (flag ? ", true" : "") + "))";
  }  
}
