// Copyright (C) 2007 Google Inc.
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

package com.google.caja.parser.quasiliteral;

import com.google.caja.reporting.MessageTypeInt;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageContext;

import java.io.IOException;
import java.util.Formatter;

/**
 * The type of a {Message message} for the JavaScript quasiliteral rewriter.
 *
 * @author mikesamuel@gmail.com
 * @author ihab.awad@gmail.com
 */
public enum JsRewriterMessageType implements MessageTypeInt {

  VARIABLES_CANNOT_END_IN_DOUBLE_UNDERSCORE(
      "Variables cannot end in \"__\": %s, %s",
      MessageLevel.FATAL_ERROR),

  PROPERTIES_CANNOT_END_IN_DOUBLE_UNDERSCORE(
      "Properties cannot end in \"__\": %s, %s",
      MessageLevel.FATAL_ERROR),

  SELECTORS_CANNOT_END_IN_DOUBLE_UNDERSCORE(
      "Selectors cannot end in \"__\": %s, %s",
      MessageLevel.FATAL_ERROR),

  GLOBALS_CANNOT_END_IN_UNDERSCORE(
      "Globals cannot end in \"_\": %s, %s",
      MessageLevel.FATAL_ERROR),

  PUBLIC_PROPERTIES_CANNOT_END_IN_UNDERSCORE(
      "Public properties cannot end in \"_\": %s, %s",
      MessageLevel.FATAL_ERROR),

  PUBLIC_SELECTORS_CANNOT_END_IN_UNDERSCORE(
      "Public selectors cannot end in \"_\": %s, %s",
      MessageLevel.FATAL_ERROR),

  METHOD_IN_NON_METHOD_CONTEXT(
      "Method in non-method context: %s, %s",
      MessageLevel.FATAL_ERROR),

  CONSTRUCTOR_CANNOT_ESCAPE(
      "Constructor cannot escape: %s, %s",
      MessageLevel.FATAL_ERROR),

  INVOKED_INSTANCEOF_ON_NON_FUNCTION(
      "Invoked \"instanceof\" on non-function: %s, %s",
      MessageLevel.FATAL_ERROR),

  MAP_EXPRESSION_EXPECTED(
      "Map expression expected: %s, %s",
      MessageLevel.FATAL_ERROR),  
  ;

  private final String formatString;
  private final MessageLevel level;
  private final int paramCount;

  JsRewriterMessageType(String formatString, MessageLevel level) {
    this.formatString = formatString;
    this.level = level;
    this.paramCount = formatStringArity(formatString);
  }

  public void format(MessagePart[] parts, MessageContext context,
                     Appendable out) throws IOException {
    formatMessage(formatString, parts, context, out);
  }

  public MessageLevel getLevel() { return level; }

  public int getParamCount() {
    return paramCount;
  }

  public static void formatMessage(
      String formatString, MessagePart[] parts, MessageContext context,
      Appendable out)
      throws IOException {
    Object[] partStrings = new Object[parts.length];
    for (int i = 0; i < parts.length; ++i) {
      StringBuilder sb = new StringBuilder();
      parts[i].format(context, sb);
      partStrings[i] = sb.toString();
    }
    new Formatter(out).format(formatString, partStrings);
  }

  public static int formatStringArity(String formatString) {
    int count = 0;
    for (int i = 0, n = formatString.length(); i < n; ++i) {
      char ch = formatString.charAt(i);
      if ('%' == ch) {
        if (i + 1 < n && '%' != formatString.charAt(i + 1)) {
          ++count;
        } else {
          ++i;
        }
      }
    }
    return count;
  }
}
