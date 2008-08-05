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

import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageType;
import java.io.IOException;

/**
 * Tokenizes JSON source.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class JsonLexer extends AbstractTokenStream<JsTokenType> {
  private final LookaheadCharProducer cp;
  private final CharProducer.MutableFilePosition pos
      = new CharProducer.MutableFilePosition();

  public JsonLexer(CharProducer cp) {
    this.cp = new LookaheadCharProducer(cp, 1);
  }

  @Override
  protected Token<JsTokenType> produce() throws ParseException {
    int ch;
    try {
      cp.getCurrentPosition(pos);
      while ((ch = cp.read()) >= 0 && isJsonSpace((char) ch)) {
        cp.getCurrentPosition(pos);
      }

      if (ch < 0) { return null; }

      FilePosition start = pos.toFilePosition();

      switch (ch) {
        case '[': case ']': case '{': case '}': case ':': case ',':
          return Token.instance(
              String.valueOf((char) ch), JsTokenType.PUNCTUATION,
              FilePosition.span(start, cp.getCurrentPosition()));
        case '"':
          return parseString(start, new StringBuilder().append((char) ch));
        case '.':
          return parseFraction(start, new StringBuilder().append((char) ch));
        case '0':
          return maybeParseFraction(
               start, new StringBuilder().append((char) ch));
        case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
          return parseIntegerTail(
              start, new StringBuilder().append((char) ch));
        case '+': case '-':
          return parseNumber(start, new StringBuilder().append((char) ch));
        case 'f': case 't': case 'n':
          return parseWord(start, new StringBuilder().append((char) ch));
        default:
          return badChar(ch);
      }
    } catch (IOException ex) {
      throw new ParseException(
          new Message(MessageType.PARSE_ERROR, cp.getCurrentPosition()), ex);
    }
  }

  private Token<JsTokenType> parseString(FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    boolean esc = false;
    while (true) {
      cp.getCurrentPosition(pos);
      int ch = cp.read();
      if (ch < 0) {
        throw new ParseException(
            new Message(MessageType.UNTERMINATED_STRING_TOKEN,
                        cp.getCurrentPosition()));
      }
      buf.append((char) ch);
      if (!esc) {
        if (ch == '"') {
          return Token.instance(
              buf.toString(), JsTokenType.STRING,
              FilePosition.span(start, cp.getCurrentPosition()));
        } else if (ch == '\\') {
          esc = true;
        } else {
          // unescaped = %x20-21 / %x23-5B / %x5D-10FFFF
          if (ch < 0x20) { badChar(ch); }
        }
      } else {
        // %x22 /          ; "    quotation mark  U+0022
        // %x5C /          ; \    reverse solidus U+005C
        // %x2F /          ; /    solidus         U+002F
        // %x62 /          ; b    backspace       U+0008
        // %x66 /          ; f    form feed       U+000C
        // %x6E /          ; n    line feed       U+000A
        // %x72 /          ; r    carriage return U+000D
        // %x74 /          ; t    tab             U+0009
        // %x75 4HEXDIG )  ; uXXXX                U+XXXX
        switch (ch) {
          case '"': case '\\': case '/': case 'b': case 'f':
          case 'n': case 'r': case 't': case 'u':
            esc = false;
            break;
          default:
            badChar(ch);
        }
      }
    }
  }

  private Token<JsTokenType> parseIntegerTail(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    for (int ch; isDigit(ch = cp.lookahead());) {
      buf.append((char) ch);
      cp.consume(1);
    }
    return maybeParseFraction(start, buf);
  }

  private Token<JsTokenType> parseNumber(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    cp.getCurrentPosition(pos);
    int ch = cp.read();
    buf.append((char) ch);
    if (ch == '.') {
      return parseFraction(start, buf);
    } else {
      if (!isDigit(ch)) { badChar(ch); }
      return parseIntegerTail(start, buf);
    }
  }

  private Token<JsTokenType> maybeParseFraction(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    int ch = cp.lookahead();
    if (ch == '.') {
      cp.consume(1);
      buf.append('.');
      return parseFractionTail(start, buf);
    } else {
      return maybeParseExponent(start, buf);
    }
  }

  private Token<JsTokenType> parseFraction(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    cp.getCurrentPosition(pos);
    int ch = cp.lookahead();

    if (!isDigit(ch)) { badChar(ch); }

    return parseFractionTail(start, buf);
  }

  private Token<JsTokenType> parseFractionTail(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {

    for (int ch; isDigit(ch = cp.lookahead());) {
      cp.consume(1);
      buf.append((char) ch);
      cp.getCurrentPosition(pos);
    }

    return maybeParseExponent(start, buf);
  }

  private Token<JsTokenType> maybeParseExponent(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    int ch = cp.lookahead();
    if (ch == 'e' || ch == 'E') {
      buf.append((char) ch);
      cp.consume(1);
      return parseExponent(start, buf);
    } else {
      return Token.instance(
          buf.toString(), JsTokenType.FLOAT,
          FilePosition.span(start, cp.getCurrentPosition()));
    }
  }

  private Token<JsTokenType> parseExponent(
      FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    cp.getCurrentPosition(pos);
    int ch = cp.lookahead();
    if (ch == '+' || ch == '-') {
      cp.consume(1);
      buf.append((char) ch);
      cp.getCurrentPosition(pos);
      ch = cp.lookahead();
    }

    if (!isDigit(ch)) {
      badChar(ch);
    }

    do {
      cp.consume(1);
      buf.append((char) ch);
      ch = cp.lookahead();
    } while (isDigit(ch));

    return Token.instance(
        buf.toString(), JsTokenType.FLOAT,
        FilePosition.span(start, cp.getCurrentPosition()));
  }

  private Token<JsTokenType> parseWord(FilePosition start, StringBuilder buf)
      throws IOException, ParseException {
    for (int ch; (ch = cp.lookahead()) >= 'a' && ch <= 'z';) {
      buf.append((char) ch);
      cp.consume(1);
    }
    String keyword = buf.toString();
    FilePosition tokenPos = FilePosition.span(start, cp.getCurrentPosition());
    if (!("null".equals(keyword) || "true".equals(keyword)
          || "false".equals(keyword))) {
      throw new ParseException(
          new Message(MessageType.UNEXPECTED_TOKEN, tokenPos,
                      MessagePart.Factory.valueOf(keyword)));
    }
    return Token.instance(keyword, JsTokenType.KEYWORD, tokenPos);
  }

  private static boolean isJsonSpace(int ch) {
    // Insignificant whitespace is allowed before or after any of the six
    // structural characters.

    // ws = *(
    //          %x20 /              ; Space
    //          %x09 /              ; Horizontal tab
    //          %x0A /              ; Line feed or New line
    //          %x0D                ; Carriage return
    //        )
    switch (ch) {
      case ' ': case '\t': case '\n': case '\r': return true;
      default: return false;
    }
  }

  private static boolean isDigit(int ch) { return ch >= '0' && ch <= '9'; }

  private Token<JsTokenType> badChar(int ch) throws ParseException{
    if (ch < 0) {
      throw new ParseException(
          new Message(MessageType.END_OF_FILE,
                      pos.toFilePosition().source()));
    } else {
      throw new ParseException(
          new Message(MessageType.UNEXPECTED_TOKEN, pos.toFilePosition(),
                      MessagePart.Factory.valueOf((char) ch)));
    }
  }
}
