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

package com.google.caja.parser.json;

import com.google.caja.lexer.JsTokenType;
import com.google.caja.lexer.JsonLexer;
import com.google.caja.lexer.TokenQueue;
import com.google.caja.lexer.ParseException;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.json.JsonParser;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.MoreAsserts;

import java.io.IOException;
import java.util.Arrays;

public final class JsonParserTest extends CajaTestCase {

  public void testLooseValues() throws Exception {
    // Json only allows arrays or objects at the top level
    assertParseFails("null", "Expected [ not null");
  }

  public void testArrayLooseCommas() throws Exception {
    assertParseFails("[,]", "2 - 3: Unexpected token ,");
    assertParseFails("[0,]", "4 - 5: Unexpected token ]");
    assertParseFails("[0,,1]", "4 - 5: Unexpected token ,");
  }

  public void testObjectLooseCommas() throws Exception {
    assertParseFails("{,}", "Expected STRING not ,");
    assertParseFails("{\"foo\":0,}", "Expected STRING not }");
  }

  public void testObjectBareKeys() throws Exception {
    assertParseFails("{foo:0}", "Unexpected token foo");
  }

  public void testObjectMissingColon() throws Exception {
    assertParseFails("{\"foo\", 0}", "7 - 8: Expected : not ,");
  }

  public void testDisallowedKeywords() throws Exception {
    assertParseFails("[undefined]", "Unexpected token 'u'");
  }

  public void testUnclosed() throws Exception {
    assertParseFails("[\"foo", "Unclosed string");
    assertParseFails("[[]", "1+4: Expected ] not EOF");
    assertParseFails("{\"a\": []", "Expected } not EOF");
    assertParseFails("[", "Unexpected end of input in testUnclosed");
    assertParseFails("{", "Unexpected end of input in testUnclosed");
  }

  public void testObjects() throws Exception {
    assertParse("{}", "JsonObject");
    assertParse("{ \"a\": 0 }",
                "JsonObject",
                "  StringLiteral : \"a\"",
                "  RealLiteral : 0.0");
    assertParse("{ \"a\": false, \n   \"b\": true,\n   \"\": null }",
                "JsonObject",
                "  StringLiteral : \"a\"",
                "  BooleanLiteral : false",
                "  StringLiteral : \"b\"",
                "  BooleanLiteral : true",
                "  StringLiteral : \"\"",
                "  NullLiteral : null");
  }

  public void testArrays() throws Exception {
    assertParse("[]", "JsonArray");
    assertParse("[0]",
                "JsonArray",
                "  RealLiteral : 0.0");
    assertParse("[0,+1, -2.5]",
                "JsonArray",
                "  RealLiteral : 0.0",
                "  RealLiteral : 1.0",
                "  RealLiteral : -2.5");
    assertParse("[0,[+1], \"\", {\"a\": -2.5}, null]",
                "JsonArray",
                "  RealLiteral : 0.0",
                "  JsonArray",
                "    RealLiteral : 1.0",
                "  StringLiteral : \"\"",
                "  JsonObject",
                "    StringLiteral : \"a\"",
                "    RealLiteral : -2.5",
                "  NullLiteral : null");
  }

  public void testNumbers() throws Exception {
    assertParse("[0, +0.1, -.3, 1.5, -1e2, +1.5e-2, -.5e+3]",
                "JsonArray",
                "  RealLiteral : 0.0",
                "  RealLiteral : 0.1",
                "  RealLiteral : -0.3",
                "  RealLiteral : 1.5",
                "  RealLiteral : -100.0",
                "  RealLiteral : 0.015",
                "  RealLiteral : -500.0");
  }

  public void testStrings() throws Exception {
    assertParse("[\"\", \"a\", \"\\n\", \"\\u0123\", \"\\\\\", \"\\\"\"]",
                "JsonArray",
                "  StringLiteral : \"\"",
                "  StringLiteral : \"a\"",
                "  StringLiteral : \"\\n\"",
                "  StringLiteral : \"\\u0123\"",
                "  StringLiteral : \"\\\\\"",
                "  StringLiteral : \"\\\"\"");
  }

  private void assertParse(String json, String... parseTree)
      throws IOException, ParseException {
    JsonLexer lexer = new JsonLexer(fromString(json));
    TokenQueue<JsTokenType> tq = new TokenQueue<JsTokenType>(lexer, is);
    JsonParser parser = new JsonParser(tq);
    Expression e = parser.parseJson();
    tq.expectEmpty();

    StringBuilder sb = new StringBuilder();
    e.formatTree(mc, 0, sb);

    MoreAsserts.assertListsEqual(
        Arrays.asList(parseTree),
        Arrays.asList(sb.toString().split("\n")));
  }

  private void assertParseFails(String json, String message) {
    String actualMessage = null;
    try {
      JsonLexer lexer = new JsonLexer(fromString(json));
      TokenQueue<JsTokenType> tq = new TokenQueue<JsTokenType>(lexer, is);
      JsonParser parser = new JsonParser(tq);
      parser.parseJson();
      tq.expectEmpty();
      fail("Parse did not fail: " + json);
    } catch (ParseException ex) {
      actualMessage = ex.getCajaMessage().toString();
    }

    assertTrue(actualMessage, actualMessage.endsWith(message));
  }
}
