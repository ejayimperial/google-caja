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

package com.google.caja.lexer;

import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageType;
import com.google.caja.util.MoreAsserts;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class JsonLexerTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEmptyJson() throws Exception {
    assertLexed("");
  }

  public void testObject() throws Exception {
    assertLexed(
        "{}",
        t("{", JsTokenType.PUNCTUATION),
        t("}", JsTokenType.PUNCTUATION)
        );
  }

  public void testObjectMembers() throws Exception {
    assertLexed(
        "{"
        + "  \"number\": 123,"
        + "  \"string\": \"foo\\\"\\uabcd\\b\\f\\n\\t\\r\\/\\\\\","
        + "  \"boolean\": true, \"boolean\": false,"
        + "  \"<null>\": null,"
        + "  \"array\": [1, 2, 3],"
        + "}",
        t("{", JsTokenType.PUNCTUATION),
        t("\"number\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("123", JsTokenType.FLOAT),
        t(",", JsTokenType.PUNCTUATION),
        t("\"string\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("\"foo\\\"\\uabcd\\b\\f\\n\\t\\r\\/\\\\\"", JsTokenType.STRING),
        t(",", JsTokenType.PUNCTUATION),
        t("\"boolean\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("true", JsTokenType.KEYWORD),
        t(",", JsTokenType.PUNCTUATION),
        t("\"boolean\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("false", JsTokenType.KEYWORD),
        t(",", JsTokenType.PUNCTUATION),
        t("\"<null>\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("null", JsTokenType.KEYWORD),
        t(",", JsTokenType.PUNCTUATION),
        t("\"array\"", JsTokenType.STRING),
        t(":", JsTokenType.PUNCTUATION),
        t("[", JsTokenType.PUNCTUATION),
        t("1", JsTokenType.FLOAT),
        t(",", JsTokenType.PUNCTUATION),
        t("2", JsTokenType.FLOAT),
        t(",", JsTokenType.PUNCTUATION),
        t("3", JsTokenType.FLOAT),
        t("]", JsTokenType.PUNCTUATION),
        t(",", JsTokenType.PUNCTUATION),
        t("}", JsTokenType.PUNCTUATION)
        );
  }

  public void testUnterminatedString() throws Exception {
    assertLexFails("\"foo", MessageType.UNTERMINATED_STRING_TOKEN);
    assertLexFails("\"foo\\\"", MessageType.UNTERMINATED_STRING_TOKEN);
    assertLexFails("\"foo\\", MessageType.UNTERMINATED_STRING_TOKEN);
  }

  public void testLineTerminatorsInString() throws Exception {
    assertLexFails("\"foo\nbar\"", MessageType.UNEXPECTED_TOKEN);
    assertLexFails("\"foo\rbar\"", MessageType.UNEXPECTED_TOKEN);
  }

  public void testNumbers() throws Exception {
    assertLexed("0", t("0", JsTokenType.FLOAT));
    assertLexFails("00", MessageType.UNEXPECTED_TOKEN);
    assertLexFails("01", MessageType.UNEXPECTED_TOKEN);
    assertLexFails("0x1", MessageType.UNEXPECTED_TOKEN);
    assertLexed("1", t("1", JsTokenType.FLOAT));
    assertLexed("123", t("123", JsTokenType.FLOAT));
    assertLexed("0.", t("0.", JsTokenType.FLOAT));
    assertLexed("123.", t("123.", JsTokenType.FLOAT));
    assertLexed("123.456", t("123.456", JsTokenType.FLOAT));
    assertLexed(".456", t(".456", JsTokenType.FLOAT));
    assertLexed("0.e0", t("0.e0", JsTokenType.FLOAT));
    assertLexed("123.E1", t("123.E1", JsTokenType.FLOAT));
    assertLexed("123.456E-1", t("123.456E-1", JsTokenType.FLOAT));
    assertLexed(".456e+2", t(".456e+2", JsTokenType.FLOAT));
    assertLexed("789e+3", t("789e+3", JsTokenType.FLOAT));
    assertLexFails("123e", MessageType.END_OF_FILE);
    assertLexFails("123e+", MessageType.END_OF_FILE);
    assertLexFails("123e-", MessageType.END_OF_FILE);
    assertLexed("+1", t("+1", JsTokenType.FLOAT));
    assertLexed("-23", t("-23", JsTokenType.FLOAT));
    assertLexed("+.5", t("+.5", JsTokenType.FLOAT));
    assertLexed("-.67", t("-.67", JsTokenType.FLOAT));
    assertLexFails("--1", MessageType.UNEXPECTED_TOKEN);
    assertLexFails(".", MessageType.END_OF_FILE);
    assertLexFails("-.", MessageType.END_OF_FILE);
  }

  public void assertLexed(String json, TokenStub... golden) throws Exception {
    List<TokenStub> actual = new ArrayList<TokenStub>();
    JsonLexer lexer = new JsonLexer(
        CharProducer.Factory.create(
            new StringReader(json),
            new InputSource(URI.create("test:///" + getName()))));
    while (lexer.hasNext()) {
      Token<JsTokenType> tok = lexer.next();
      actual.add(t(tok.text, tok.type));
    }
    MoreAsserts.assertListsEqual(Arrays.asList(golden), actual);
  }

  public void assertLexFails(String json, MessageType golden) throws Exception {
    JsonLexer lexer = new JsonLexer(
        CharProducer.Factory.create(
            new StringReader(json),
            new InputSource(URI.create("test:///" + getName()))));
    try {
      while (lexer.hasNext()) { lexer.next(); }
    } catch (ParseException ex) {
      assertEquals(ex.getCajaMessage().format(new MessageContext()),
                   golden, ex.getCajaMessage().getMessageType());
    }
  }

  private static TokenStub t(String text, JsTokenType type) {
    return new TokenStub(text, type);
  }
  
  private static class TokenStub {
    private final String text;
    private final JsTokenType type;

    TokenStub(String text, JsTokenType type) {
      if (text == null || type == null) { throw new NullPointerException(); }
      this.text = text;
      this.type = type;
    }

    @Override
    public String toString() {
      return "[TokenStub " + text + " : " + type + "]";
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof TokenStub)) { return false; }
      TokenStub that = (TokenStub) o;
      return this.text.equals(that.text) && this.type == that.type;
    }

    @Override
    public int hashCode() {
      return text.hashCode() + 31 * type.hashCode();
    }
  }
}
