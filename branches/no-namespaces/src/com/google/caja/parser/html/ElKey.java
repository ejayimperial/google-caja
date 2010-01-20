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

import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessagePart;
import com.google.caja.util.Strings;

import java.io.IOException;

import org.w3c.dom.Element;

/**
 * A normalized qualified element name used to lookup element definitions in
 * an {@link HtmlSchema HTML schema}.
 *
 * @author mikesamuel@gmail.com
 */
public final class ElKey implements MessagePart, Comparable<ElKey> {
  public final String qName;

  /**
   * @param qName normalized.
   */
  private ElKey(String qName) {
    assert qName != null;
    if (qName.indexOf(':') < 0) { qName = Strings.toLowerCase(qName); }
    this.qName = qName;
  }

  public static final ElKey WILDCARD = new ElKey("*");

  /** A key that describes all elements in the given namespace. */
  public static ElKey wildcard() { return WILDCARD; }

  public boolean isHtml() { return qName.indexOf(':') < 0; }

  public boolean is(Element el) {
    return ("*".equals(qName) || qName.equals(el.getNodeName()));
  }

  public static ElKey forHtmlElement(String localName) {
    assert localName.indexOf(':') < 0;
    return new ElKey(localName);
  }

  public static ElKey forElement(String qname) {
    return new ElKey(qname);
  }

  public static ElKey forElement(Element el) {
    return new ElKey(el.getNodeName());
  }

  public void format(MessageContext mc, Appendable out) throws IOException {
    out.append(qName);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ElKey)) { return false; }
    ElKey that = (ElKey) o;
    return this.qName.equals(that.qName);
  }

  @Override
  public int hashCode() {
    return qName.hashCode();
  }

  @Override
  public String toString() {
    return qName;
  }

  public int compareTo(ElKey that) {
    return this.qName.compareTo(that.qName);
  }
}
