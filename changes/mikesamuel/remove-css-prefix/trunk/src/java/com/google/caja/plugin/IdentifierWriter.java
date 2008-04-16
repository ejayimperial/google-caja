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

package com.google.caja.plugin;

import com.google.caja.lexer.FilePosition;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.Operation;
import com.google.caja.parser.js.Operator;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites a CLASS, ID, or NAME attribute statically.
 * Makes sure individual values do not end in double underscore, and if suffix
 * is true, adds the gadget's id suffix.
 *
 * @author mikesamuel@gmail.com
 */
class IdentifierWriter {
  private final FilePosition pos;
  private final MessageQueue mq;
  private final boolean suffix;

  IdentifierWriter(FilePosition pos, MessageQueue mq, boolean suffix) {
    this.pos = pos;
    this.mq = mq;
    this.suffix = suffix;
  }

  interface Emitter {
    void emit(String s);
    void emit(Expression e);
  }

  static final class ConcatenationEmitter implements Emitter {
    private final List<Expression> parts = new ArrayList<Expression>();

    public void emit(Expression e) { parts.add(e); }

    public void emit(String s) {
      if (!parts.isEmpty()) {
        int lastIndex = parts.size() - 1;
        Expression last = parts.get(lastIndex);
        if (last instanceof StringLiteral) {
          parts.set(lastIndex,
                    TreeConstruction.stringLiteral(
                        ((StringLiteral) last).getUnquotedValue() + s));
          return;
        }
      }
      parts.add(TreeConstruction.stringLiteral(s));
    }

    public Expression getExpression() {
      return concatenation(parts);
    }
  }

  static final class AttribValueEmitter implements Emitter {
    private final List<String> tgtChain;
    private final Block out;
    AttribValueEmitter(List<String> tgtChain, Block out) {
      this.tgtChain = tgtChain;
      this.out = out;
    }
    public void emit(String s) {
      JsWriter.appendText(s, JsWriter.Esc.HTML_ATTRIB, tgtChain, out);
    }
    public void emit(Expression e) {
      JsWriter.append(e, tgtChain, out);
    }
  }

  void toJavascript(String nmTokens, Emitter out) {
    boolean wasSpace = true;
    boolean first = true;
    int pos = 0;
    for (int i = 0, n = nmTokens.length(); i < n; ++i) {
      char ch = nmTokens.charAt(i);
      boolean space = Character.isWhitespace(ch);
      if (space != wasSpace) {
        if (!wasSpace) {
          first = emitName(first, nmTokens.substring(pos, i), out);
        }
        pos = i;
        wasSpace = space;
      }
    }
    if (!wasSpace) {
      first = emitName(first, nmTokens.substring(pos), out);
    }
  }

  private boolean emitName(boolean first, String ident, Emitter out) {
    if (ident.endsWith("__")) {
      mq.addMessage(
          PluginMessageType.BAD_IDENTIFIER, pos,
          MessagePart.Factory.valueOf(ident));
      return first;
    }
    out.emit((first ? "" : " ") + ident + (suffix ? "-" : ""));
    if (suffix) {
      out.emit(TreeConstruction.call(
          TreeConstruction.memberAccess("___OUTERS___", "getIdClass___")));
    }
    return false;
  }

  private static Expression concatenation(List<Expression> operands) {
    Expression e = null;
    for (Expression operand : operands) {
      if (e == null) {
        e = operand;
      } else {
        e = Operation.create(Operator.ADDITION, e, operand);
      }
    }
    return e;
  }
}
