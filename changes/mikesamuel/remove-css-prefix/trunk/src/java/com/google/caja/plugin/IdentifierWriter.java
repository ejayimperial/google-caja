package com.google.caja.plugin;

import com.google.caja.lexer.FilePosition;
import com.google.caja.parser.js.Block;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;

import java.util.List;

/**
 * Rewrites a CLASS, ID, or NAME attribute statically.
 * Makes sure individual values do not end in double underscore, and if suffix
 * is true, adds the gadget's id suffix.
 */
class IdentifierWriter {
  private final FilePosition pos;
  private final MessageQueue mq;
  private final List<String> tgtChain;
  private final Block b;
  private final boolean suffix;

  IdentifierWriter(FilePosition pos, MessageQueue mq, boolean suffix,
                   List<String> tgtChain, Block b) {
    this.pos = pos;
    this.mq = mq;
    this.tgtChain = tgtChain;
    this.b = b;
    this.suffix = suffix;
  }

  void write(String nmTokens) {
    boolean wasSpace = true;
    boolean first = true;
    int pos = 0;
    for (int i = 0, n = nmTokens.length(); i < n; ++i) {
      char ch = nmTokens.charAt(i);
      boolean space = Character.isWhitespace(ch);
      if (space != wasSpace) {
        if (space) {
          emitName(first, nmTokens.substring(pos, i));
          first = false;
        }
        pos = i;
        wasSpace = space;
      }
    }
    if (!wasSpace) {
      emitName(first, nmTokens.substring(pos));
    }
  }

  private void emitName(boolean first, String ident) {
    if (ident.endsWith("__")) {
      mq.addMessage(
          PluginMessageType.BAD_IDENTIFIER, pos,
          MessagePart.Factory.valueOf(ident));
      return;
    }
    String chunk = (first ? "" : " ") + ident + (suffix ? "-" : "");
    JsWriter.appendString(JsWriter.htmlEscape(chunk), tgtChain, b);
    if (suffix) {
      JsWriter.append(
          TreeConstruction.call(
              TreeConstruction.memberAccess("___OUTERS___", "getIdClass___")),
          tgtChain, b);
    }
  }
}
