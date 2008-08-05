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

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.JsTokenType;
import com.google.caja.lexer.Keyword;
import com.google.caja.lexer.ParseException;
import com.google.caja.lexer.Token;
import com.google.caja.lexer.TokenQueue;
import com.google.caja.parser.AbstractParseTreeNode;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.BooleanLiteral;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.Literal;
import com.google.caja.parser.js.NullLiteral;
import com.google.caja.parser.js.RealLiteral;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageType;
import com.google.caja.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parses JSON returning a javascript parse tree where object constructors
 * and lists are queryable.  This interface mimics Simple JSON's, but the
 * tree nodes have file position info, and render keys in the order of
 * entry model adopted by EcmaScript 3.1.
 *
 * @author mikesamuel@gmail.com
 */
public final class JsonParser {
  private final TokenQueue<JsTokenType> tq;

  public JsonParser(TokenQueue<JsTokenType> tq) {
    this.tq = tq;
  }

  public Expression parseJson() throws ParseException {
    Token<JsTokenType> t = tq.peek();
    if ("{".equals(t.text)) {
      return parseObject();
    } else {
      return parseArray();
    }
  }

  private Expression parseJsonValue() throws ParseException {
    Token<JsTokenType> t = tq.peek();
    switch (t.type) {
      case PUNCTUATION:
        if ("{".equals(t.text)) {
          return parseObject();
        } else if ("[".equals(t.text)) {
          return parseArray();
        }
        break;
      case STRING:
        return parseString();
      case FLOAT:
        return parseNumber();
      default:
        return parseKeyword();
    }
    throw new ParseException(new Message(
        MessageType.UNEXPECTED_TOKEN,
        t.pos, MessagePart.Factory.valueOf(t.text)));
  }

  private JsonArray parseArray() throws ParseException {
    FilePosition pos = tq.currentPosition();
    tq.expectToken("[");
    ArrayList<Expression> values = new ArrayList<Expression>();
    if (!tq.checkToken("]")) {
      do {
        values.add(parseJsonValue());
      } while (tq.checkToken(","));
      tq.expectToken("]");
    }
    return finish(new JsonArray(values), pos);
  }

  private JsonObject parseObject() throws ParseException {
    FilePosition pos = tq.currentPosition();
    tq.expectToken("{");
    ArrayList<Pair<StringLiteral, Expression>> values
        = new ArrayList<Pair<StringLiteral, Expression>>();
    if (!tq.checkToken("}")) {
      do {
        StringLiteral key = parseString();
        tq.expectToken(":");
        Expression value = parseJsonValue();
        values.add(Pair.pair(key, value));
      } while (tq.checkToken(","));
      tq.expectToken("}");
    }
    return finish(new JsonObject(values), pos);
  }

  private Literal parseKeyword() throws ParseException {
    Token<JsTokenType> t = tq.expectTokenOfType(JsTokenType.KEYWORD);
    switch (Keyword.fromString(t.text)) {
      case NULL:
        return finish(new NullLiteral(), t.pos);
      case FALSE:
        return finish(new BooleanLiteral(false), t.pos);
      case TRUE:
        return finish(new BooleanLiteral(true), t.pos);
      default:
        throw new ParseException(
            new Message(MessageType.UNEXPECTED_TOKEN, t.pos,
                        MessagePart.Factory.valueOf(t.text)));
    }
  }

  private RealLiteral parseNumber() throws ParseException {
    Token<JsTokenType> t = tq.expectTokenOfType(JsTokenType.FLOAT);
    return finish(
        new RealLiteral(new BigDecimal(t.text),
                        Collections.<ParseTreeNode>emptyList()),
        t.pos);
  }

  private StringLiteral parseString() throws ParseException {
    Token<JsTokenType> t = tq.expectTokenOfType(JsTokenType.STRING);
    return finish(new StringLiteral(t.text), t.pos);
  }

  private <T extends AbstractParseTreeNode<?>>
  T finish(T e, FilePosition start) {
    e.setFilePosition(FilePosition.span(start, tq.lastPosition()));
    return e;
  }
}
