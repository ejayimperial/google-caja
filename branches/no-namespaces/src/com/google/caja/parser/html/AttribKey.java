// Copyright (C) 2009 Google Inc.
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

package com.google.caja.parser.html;

import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessagePart;
import com.google.caja.util.Strings;

import java.io.IOException;

import org.w3c.dom.Attr;

/**
 * Identifies an XML/HTML attribute as it appears on a particular kind of
 * XML/HTML element.
 *
 * @author mikesamuel@gmail.com
 */
public final class AttribKey implements MessagePart, Comparable<AttribKey> {
  /** The kind of element the attribute appears on. */
  public final ElKey el;
  /**
   * The normalized local name of the attribute.
   * For HTML, the normalized name is the lower case form of the name, but
   * for other namespaces, no normalization is done.
   */
  public final String qName;

  private AttribKey(ElKey el, String qName) {
    if (el == null || qName == null) {
      throw new NullPointerException();
    }
    this.el = el;
    this.qName = qName.indexOf(':') < 0
        ? Strings.toLowerCase(qName) : qName;
  }

  public boolean is(Attr a) {
    return qName.equals(a.getName())
        && el.is(a.getOwnerElement());
  }

  /**
   * Looks up an attribute key by element and local name.
   *
   * @param el not null.
   * @param localName unnormalized.
   */
  public static AttribKey forHtmlAttrib(ElKey el, String localName) {
    return new AttribKey(el, localName);
  }

  /**
   * Looks up an attribute key by qualified name.
   *
   * @param el not null.  The key of the element that the attribute appears on.
   * @param qname the qualified name of the attribute.
   * @return null if qname does not specify a namespace in scope.
   */
  public static AttribKey forAttribute(ElKey el, String qname) {
    return new AttribKey(el, qname);
  }

  /**
   * @param el an element key that describes the kind of element that attr
   *    does/will appear on.
   *    Normally, {@code el.equals(ElKey.forElement(attr.getOwnerElement())}.
   * @param attr not null.
   * @return a key describing attr.
   */
  public static AttribKey forAttribute(ElKey el, Attr attr) {
    return new AttribKey(el, attr.getName());
  }

  /**
   * Returns a key that describes this key, but appearing on any element in its
   * namespace.
   */
  public AttribKey onAnyElement() {
    return onElement(ElKey.wildcard());
  }

  /**
   * Returns the attribute key for this attribute but on a different element.
   */
  public AttribKey onElement(ElKey el) {
    if (el.equals(this.el)) { return this; }
    return new AttribKey(el, qName);
  }

  public void format(MessageContext mc, Appendable out) throws IOException {
    appendQName(qName, out);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AttribKey)) { return false; }
    AttribKey that = (AttribKey) o;
    return this.el.equals(that.el)
        && this.qName.equals(that.qName);
  }

  @Override
  public int hashCode() {
    return (qName.hashCode() + 31 * el.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    try {
      appendQName(el.qName, out);
      out.append("::");
      appendQName(qName, out);
    } catch (IOException ex) {
      throw new RuntimeException("Appending to StringBuilder", ex);
    }
    return out.toString();
  }

  private static void appendQName(String qName, Appendable out)
      throws IOException {
    out.append(qName);
  }

  public int compareTo(AttribKey that) {
    int delta = this.el.compareTo(that.el);
    if (delta == 0) {
      delta = this.qName.compareTo(that.qName);
    }
    return delta;
  }
}
