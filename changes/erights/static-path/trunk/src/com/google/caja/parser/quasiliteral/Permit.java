package com.google.caja.parser.quasiliteral;

import java.util.HashMap;
import java.util.Map;

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Reference;

final class Permit {

  final private Map<String, Permit> myPermits;
  final private PermitTemplate myTemplate;

  Permit(PermitTemplate template) {
    myPermits = new HashMap<String, Permit>();
    myTemplate = template;
  }

  private Permit has(String name) {
    Permit result = myPermits.get(name);
    if (null != result) {
      return result;
    }
    PermitTemplate template = myTemplate.myTemplates.get(name);
    if (null != template) {
      result = new Permit(template);
      myPermits.put(name, result);
      return result;
    }
    return null;
  }

  Permit canRead(ParseTreeNode path) {
    if (path instanceof Reference) {
      return has(((Reference)path).getIdentifierName());
    }
    // TODO(erights): Add case for dotted path expression.
    return null;
  }

  Permit canCall(ParseTreeNode path) {
    Permit p = canRead(path);
    if (null == p) { return null; }
    return p.has("()");
  }

  public String toString() {
    StringBuilder myBuf = new StringBuilder();
    toString(myBuf, ",\n  ");
    return myBuf.toString();
  }

  private void toString(StringBuilder myBuf, String sep) {
    myBuf.append("{");
    boolean first = true;
    for (Map.Entry<String, Permit> assoc : myPermits.entrySet()) {
      if (!first) {
        myBuf.append(sep);
      }
      // TODO(erights): encode (or at least sanitize) key
      myBuf.append("\"").append(assoc.getKey()).append("\":");
      assoc.getValue().toString(myBuf, sep + "  ");
      first = false;
    }
    myBuf.append("}");
  }
}
