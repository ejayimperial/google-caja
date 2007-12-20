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

package com.google.caja.parser.html;

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.HtmlTokenType;
import com.google.caja.lexer.Token;
import com.google.caja.lexer.escaping.Escaping;
import com.google.caja.parser.MutableParseTreeNode;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.SyntheticAttributeKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.impl.TreeBuilder;

/**
 * A bridge between DomParser and html5lib which translates
 * {@code Token<HtmlTokenType>}s into SAX style events which are fed to the
 * TreeBuilder.  The TreeBuilder responds by issuing {@code createElement}
 * commands which are used to build a {@link DomTree}.
 *
 * @author mikesamuel@gmail.com
 */
public class Html5ElementStack implements OpenElementStack {
  private final CajaTreeBuilder builder = new CajaTreeBuilder();
  private final char[] charBuf = new char[1024];
  private final MessageQueue mq;
  private boolean isFragment;

  /** @param queue will receive error messages from html5lib. */
  Html5ElementStack(MessageQueue queue) {
    this.mq = queue;
  }

  /** @inheritDoc */
  public void open(boolean isFragment) {
    this.isFragment = isFragment;
    builder.setDoctypeExpectation(DoctypeExpectation.NO_DOCTYPE_ERRORS);
    try {
      builder.start(new Tokenizer(builder));
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
    builder.setErrorHandler(
        new ErrorHandler() {
          private FilePosition lastPos;
          private String lastMessage;
          
          public void error(SAXParseException ex) {
            // htmlparser is a bit strident, so we lower it's warnings to
            // MessageLevel.LINT.
            report(MessageLevel.LINT, ex);
          }
          public void fatalError(SAXParseException ex) {
            report(MessageLevel.FATAL_ERROR, ex);
          }
          public void warning(SAXParseException ex) {
            report(MessageLevel.LINT, ex);
          }

          private void report(MessageLevel level, SAXParseException ex) {
            String message = errorMessage(ex);
            FilePosition pos = builder.getErrorLocation();
            if (message.equals(lastMessage) && pos.equals(lastPos)) { return; }
            lastMessage = message;
            lastPos = pos;
            mq.getMessages().add(new Message(
                DomParserMessageType.GENERIC_SAX_ERROR, level, pos,
                MessagePart.Factory.valueOf(message)));
          }

          private String errorMessage(SAXParseException ex) {
            // Don't ask.
            return ex.getMessage()
                .replace('\u201c', '\'').replace('\u201d', '\'');
          }
        });
  }

  /** @inheritDoc */
  public void finish(FilePosition endOfFile) {
    builder.finish(endOfFile);
    builder.closeUnclosedNodes();
  }

  /** @inheritDoc */
  public String canonicalizeElementName(String elementName) {
    return canonicalElementName(elementName);
  }

  /** @inheritDoc */
  public String canonicalizeAttributeName(String attributeName) {
    return canonicalAttributeName(attributeName);
  }

  public static String canonicalElementName(String elementName) {
    // Locale.ENGLISH forces LANG=C like behavior.
    return elementName.toLowerCase(Locale.ENGLISH);
  }

  public static String canonicalAttributeName(String attributeName) {
    // Locale.ENGLISH forces LANG=C like behavior.
    return attributeName.toLowerCase(Locale.ENGLISH);
  }

  /** @inheritDoc */
  public DomTree.Fragment getRootElement() {
    // libHtmlParser always produces a document with html, head, and body tags
    // which we usually don't want, so unroll it.
    DomTree.Tag root = builder.getRootElement();
    DomTree.Fragment result = new DomTree.Fragment();
    result.setFilePosition(builder.getFragmentBounds());
    if (!isFragment) {
      result.appendChild(root);
      return result;
    }

    final List<? extends DomTree> children = root.children();

    // If disposing of the html, body, or head elements would lose info don't
    // do it, so look for attributes.
    boolean tagsBesidesHeadAndBody = false;
    boolean topLevelTagsWithAttributes = false;

    for (DomTree child : children) {
      if (child instanceof DomTree.Attrib) {
        topLevelTagsWithAttributes = true;
        break;
      } else if (child instanceof DomTree.Tag) {
        DomTree.Tag el = (DomTree.Tag) child;
        if (!("head".equals(el.getTagName())
              || "body".equals(el.getTagName()))) {
          tagsBesidesHeadAndBody = true;
          break;
        }
        if (!el.children().isEmpty()
            && el.children().get(0) instanceof DomTree.Attrib) {
          topLevelTagsWithAttributes = true;
          break;
        }
      }
    }

    if (tagsBesidesHeadAndBody || topLevelTagsWithAttributes) {
      // Merging the body and head would lose info.
      result.appendChild(root);
      return result;
    }

    // Merge the body and head into a fragment
    MutableParseTreeNode.Mutation mutation = result.createMutation();
    DomTree pending = null;
    for (DomTree child : children) {
      if (child instanceof DomTree.Tag) {
        // Shallow descent
        for (DomTree grandchild : child.children()) {
          pending = appendNormalized(pending, grandchild, mutation);
        }
      } else {
        pending = appendNormalized(pending, child, mutation);
      }
    }
    if (pending != null) { mutation.appendChild(pending); }

    mutation.execute();
    return result;
  }
  /**
   * Given one or two nodes, see if the two can be combined.
   * If two are passed in, they might be combined into one and returned, or
   * the first will be appended via mut, and the other returned.
   */
  private DomTree appendNormalized(
      DomTree pending, DomTree current, MutableParseTreeNode.Mutation mut) {
    if (pending == null) { return current; }
    Token<HtmlTokenType> pendingToken = pending.getToken();
    Token<HtmlTokenType> currentToken = current.getToken();
    if (!(HtmlTokenType.TEXT == pendingToken.type
          && HtmlTokenType.TEXT == currentToken.type)) {
      mut.appendChild(pending);
      return current;
    }
    return new DomTree.Text(
        Token.instance(
            pendingToken.text + currentToken.text, HtmlTokenType.TEXT,
            FilePosition.span(pendingToken.pos, currentToken.pos)));
  }

  /**
   * Records the fact that a tag has been seen, updating internal state
   *
   * @param start the token of the beginning of the tag, so {@code "<p"} for a
   *   paragraph start, {@code </p} for an end tag.
   * @param end the token of the beginning of the tag, so {@code ">"} for a
   *   paragraph start, {@code />} for an unary break tag.
   * @param attrs the attributes for the element.  This will be empty
   *   for end tags.
   */
  public void processTag(Token<HtmlTokenType> start, Token<HtmlTokenType> end,
                         List<DomTree.Attrib> attrs) {
    builder.setTokenContext(start, end);
    AttributesImpl attrsWrapped = new AttributesImpl(attrs);
    try {
      String tagName = CajaTreeBuilder.tagName(start.text);
      if (CajaTreeBuilder.isEndTag(start.text)) {
        builder.endTag(tagName, attrsWrapped);
      } else {
        builder.startTag(tagName, attrsWrapped);
      }
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Adds the given text node to the DOM.
   */
  public void processText(Token<HtmlTokenType> textToken) {
    builder.setTokenContext(textToken, textToken);
    String text = textToken.text;
    char[] chars;
    int n = text.length();
    if (n <= charBuf.length) {
      chars = charBuf;
      text.getChars(0, n, chars, 0);
    } else {
      chars = text.toCharArray();
    }
    try {
      builder.characters(chars, 0, n);
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
  }
}

/**
 * Bridges between html5lib's TreeBuilder which actually builds the DOM, and
 * HtmlLexer which produces tokens.  This does a bit of accounting to make sure
 * that file positions are preserved on all DOM, text, and attribute nodes.  
 *
 * @author mikesamuel@gmail.com
 */
final class CajaTreeBuilder extends TreeBuilder<DomTree> {
  /** Maintain parent chains while the DOM is being built. */
  private static final SyntheticAttributeKey<DomTree> PARENT
      = new SyntheticAttributeKey<DomTree>(DomTree.class, "parent");
  private static final boolean DEBUG = false;

  // Keep track of the tokens bounding the section we're processing so that
  // we can compute file positions for all added nodes.
  private Token<HtmlTokenType> startTok;
  private Token<HtmlTokenType> endTok;
  // The root html element.  TreeBuilder always creates a valid tree with
  // html, head, and body elements.
  private DomTree.Tag rootElement;
  // Used to compute the spanning file position on the overall document.  Since
  // nodes can move around we can't easily compute this without looking at all
  // descendants.
  private FilePosition fragmentBounds;

  CajaTreeBuilder() {
    super(
        // Allow loose parsing
        XmlViolationPolicy.ALLOW,
        // Don't coalesce text so that we can apply file positions.
        false);
    setIgnoringComments(true);
    setScriptingEnabled(true);  // Affects behavior of noscript
  }

  DomTree.Tag getRootElement() {
    return rootElement;
  }

  FilePosition getFragmentBounds() {
    return fragmentBounds;
  }

  FilePosition getErrorLocation() {
    return (startTok.pos != endTok.pos
            ? FilePosition.span(startTok.pos, endTok.pos)
            : startTok.pos);
  }

  void setTokenContext(Token<HtmlTokenType> start, Token<HtmlTokenType> end) {
    if (DEBUG) {
      System.err.println(
          "*** considering " + start.toString().replace("\n", "\\n"));
    }
    startTok = start;
    endTok = end;
    if (fragmentBounds == null) { fragmentBounds = start.pos; }
  }

  void finish(FilePosition pos) {
    Token<HtmlTokenType> eofToken = Token.instance(
        "", HtmlTokenType.IGNORABLE, pos);
    setTokenContext(eofToken, eofToken);
    fragmentBounds = FilePosition.span(fragmentBounds, pos);
    try {
      eof();  // Signal that we can close the html node now.
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected void appendCommentToDocument(char[] buf, int start, int length) {}

  @Override
  protected void appendComment(DomTree el, char[] buf, int start, int length) {}

  @Override
  protected void appendCharacters(
      DomTree el, char[] buf, int start, int length) {
    insertCharactersBefore(buf, start, length, null, el);
  }

  @Override
  protected void insertCharactersBefore(
      char[] buf, int start, int length, DomTree sibling, DomTree parent) {
    // Normalize text by adding to an existing text node.
    List<? extends DomTree> siblings = parent.children();

    int siblingIndex = siblings.indexOf(sibling);
    if (siblingIndex < 0) {
      siblingIndex = siblings.size();
    }
    if (siblingIndex > 0) {
      DomTree priorSibling = siblings.get(siblingIndex - 1);
      if (priorSibling instanceof DomTree.Text
          && priorSibling.getToken().type == HtmlTokenType.TEXT) {
        // Normalize the DOM by collapsing adjacent text nodes.
        Token<HtmlTokenType> previous = priorSibling.getToken();
        StringBuilder sb = new StringBuilder(previous.text);
        sb.append(buf, start, length);
        Token<HtmlTokenType> combined = Token.instance(
            sb.toString(), previous.type,
            FilePosition.span(previous.pos, endTok.pos));
        parent.replaceChild(p(new DomTree.Text(combined), parent),
                            up(priorSibling));
        return;
      }
    }

    Token<HtmlTokenType> tok = this.startTok;
    if (!bufferMatches(buf, start, length, tok.text)) {
      tok = Token.instance(String.valueOf(buf, start, length),
                           HtmlTokenType.TEXT, endTok.pos);
    }
    insertBefore(new DomTree.Text(tok), null, parent);
  }

  @Override
  protected void addAttributesToElement(DomTree el, Attributes attributes) {
    int n = attributes.getLength();
    if (n == 0) { return; }

    // This method is used to merge multiple body elements together.  Since
    // it's illegal to have multiple attributes with the same name, we need
    // to filter.  The spec says that firstcomers win.
    Set<String> have = new HashSet<String>();
    DomTree nodeAfterLastAttrib = null;
    for (DomTree child : el.children()) {
      if (!(child instanceof DomTree.Attrib)) {
        nodeAfterLastAttrib = child;
        break;
      }
      DomTree.Attrib attr = (DomTree.Attrib) child;
      have.add(Html5ElementStack.canonicalAttributeName(attr.getAttribName()));
    }

    MutableParseTreeNode.Mutation mut = el.createMutation();
    for (DomTree.Attrib attr : getAttributes(attributes)) {
      if (have.add(
              Html5ElementStack.canonicalAttributeName(attr.getAttribName()))) {
        mut.insertBefore(attr, nodeAfterLastAttrib);
      }
    }
    mut.execute();
  }

  @Override
  protected void insertBefore(DomTree child, DomTree sibling, DomTree parent) {
    parent.insertBefore(p(child, parent), sibling);
  }

  @Override
  protected DomTree parentElementFor(DomTree child) {
    return child.getAttributes().get(PARENT);
  }

  @Override
  protected void appendChildrenToNewParent(
      DomTree oldParent, DomTree newParent) {
    if (DEBUG) {
      System.err.println(
          "Appending children of " + oldParent + " to " + newParent);
    }
    List<? extends DomTree> children = oldParent.children();
    if (oldParent == newParent || oldParent.children().isEmpty()) { return; }
    MutableParseTreeNode.Mutation oldMut = oldParent.createMutation();
    MutableParseTreeNode.Mutation newMut = newParent.createMutation();
    for (DomTree child : children) {
      // Attributes not considered children by DOM2
      if (child instanceof DomTree.Attrib) { continue; }
      oldMut.removeChild(child);
      newMut.appendChild(p(child, newParent));
    }
    oldMut.execute();
    newMut.execute();
  }

  @Override
  protected void detachFromParentAndAppendToNewParent(
      DomTree child, DomTree newParent) {
    DomTree oldParent = parentElementFor(child);
    if (DEBUG) {
      System.err.println("detach " + child + " and append to " + newParent
                         + ", oldParent=" + oldParent);
    }
    if (oldParent != null) {
      oldParent.removeChild(child);
    }
    newParent.appendChild(p(child, newParent));
  }

  @Override
  protected DomTree shallowClone(DomTree el) {
    if (DEBUG) { System.err.println("cloning " + el); }
    if (el instanceof DomTree.Tag) {
      List<DomTree.Attrib> attribs = new ArrayList<DomTree.Attrib>();
      // Shallow clone includes attributes since they're not really children.
      for (DomTree child : el.children()) {
        if (!(child instanceof DomTree.Attrib)) { break; }
        attribs.add((DomTree.Attrib) child.clone());
      }
      return new DomTree.Tag(attribs, el.getToken(), el.getFilePosition());
    } else if (el instanceof DomTree.Fragment) {
      DomTree.Fragment clone = new DomTree.Fragment();
      clone.setFilePosition(el.getFilePosition());
      return clone;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  protected boolean hasChildren(DomTree element) {
    List<? extends DomTree> children = element.children();
    // If the last child is an attribute then we don't have any non-attribute
    // children.
    return !(children.isEmpty()
             || children.get(children.size() - 1) instanceof DomTree.Attrib);
  }

  @Override
  protected void detachFromParent(DomTree element) {
    if (DEBUG) { System.err.println("detach " + element + " from parent"); }
    parentElementFor(element).removeChild(element);
    up(element);
  }

  @Override
  protected DomTree createHtmlElementSetAsRoot(Attributes attributes) {
    DomTree.Tag documentElement = createElement("html", attributes);
    if (DEBUG) { System.err.println("Created root " + documentElement); }
    this.rootElement = documentElement;
    return documentElement;
  }

  @Override
  protected DomTree.Tag createElement(String name, Attributes attributes) {
    if (DEBUG) { System.err.println("Created element " + name); }
    Token<HtmlTokenType> elStartTok;
    FilePosition pos;
    name = Html5ElementStack.canonicalElementName(name);
    if (startTok == null) {
      elStartTok = Token.instance("<" + name, HtmlTokenType.TAGBEGIN, null);
      pos = null;
    } else if (startTok.type == HtmlTokenType.TAGBEGIN
               && tagMatchesElementName(tagName(startTok.text), name)) {
      elStartTok = startTok;
      pos = FilePosition.span(startTok.pos, endTok.pos);
    } else {
      pos = FilePosition.startOf(startTok.pos);
      elStartTok = Token.instance("<" + name, HtmlTokenType.TAGBEGIN, pos);
    }
    DomTree.Tag el
        = new DomTree.Tag(getAttributes(attributes), elStartTok, pos);
    el.setTagName(name);
    return el;
  }

  // Track unpopped elements since a </html> tag will not close tables and
  // other scoping elements.
  Set<DomTree> unpoppedElements = new HashSet<DomTree>();

  @Override
  protected void elementPopped(String name, DomTree node) {
    name = Html5ElementStack.canonicalElementName(name);
    if (DEBUG) { System.err.println("popped " + name + ", node=" + node); }
    FilePosition endPos;
    if (startTok.type == HtmlTokenType.TAGBEGIN
        && (isEndTag(startTok.text) || "select".equals(name))
        && tagCloses(tagName(startTok.text), name)) {
      endPos = endTok.pos;
    } else {
      // Implied ending.
      endPos = FilePosition.startOf(startTok.pos);
    }
    FilePosition startPos = node.getFilePosition();
    if (startPos == null) {
      startPos = node.children().isEmpty()
          ? endPos : node.children().get(0).getFilePosition();
    }
    if (endPos.endCharInFile() >= startPos.endCharInFile()) {
      node.setFilePosition(FilePosition.span(startPos, endPos));
    }
    unpoppedElements.remove(node);
  }

  // htmlparser does not generate elementPopped events for the html or body
  // elements, or for void elements.
  @Override
  protected void bodyClosed(DomTree body) {
    elementPopped("body", body);
  }

  @Override
  protected void htmlClosed(DomTree html) {
    elementPopped("html", html);        
  }

  @Override
  protected void elementPushed(String name, DomTree node) {
    if (DEBUG) { System.err.println("pushed " + name + ", node=" + node); }
    unpoppedElements.add(node);
  }

  // Make sure that the end file position is correct for elements still open
  // when EOF is reached.
  void closeUnclosedNodes() {
    for (DomTree node : unpoppedElements) {
      node.setFilePosition(
          FilePosition.span(node.getFilePosition(), endTok.pos));
    }
    unpoppedElements.clear();
  }

  private static boolean bufferMatches(
      char[] buf, int start, int len, String s) {
    if (len != s.length()) { return false; }
    for (int i = len; --i >= 0;) {
      if (s.charAt(i) != buf[start + i]) { return false; }
    }
    return true;
  }

  // Set up the parent chain so we can simulate org.w3c.dom.Node.getParentNode.
  private static <T extends DomTree> T p(T el, DomTree parent) {
    el.getAttributes().set(PARENT, parent);
    return el;
  }

  private static <T extends DomTree> T up(T el) {
    el.getAttributes().set(PARENT, null);
    return el;
  }

  // htmlparser passes around an org.xml.sax Attributes list which is a
  // String->String map, but I want to use DomTree.Attrib nodes since they
  // have position info.  htmlparser in some cases does create its own
  // Attributes instances, such as when it is expanding a tag to emulate
  // deprecated elements.
  private List<DomTree.Attrib> getAttributes(Attributes attributes) {
    if (attributes instanceof AttributesImpl) {
      return ((AttributesImpl) attributes).getAttributes();
    }
    // There might be attributes here, but only for emulated tags, such as the
    // mess that is "isindex"
    int n = attributes.getLength();
    if (n == 0) {
      return Collections.<DomTree.Attrib>emptyList();
    } else {
      List<DomTree.Attrib> fakeAttribs = new ArrayList<DomTree.Attrib>();
      FilePosition pos = FilePosition.startOf(startTok.pos);
      for (int i = 0; i < n; ++i) {
        String name = attributes.getLocalName(i);
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        Escaping.escapeXml(attributes.getValue(i), false, sb);
        sb.append('"');
        String encodedValue = sb.toString();
        fakeAttribs.add(
            new DomTree.Attrib(
                new DomTree.Value(
                    Token.instance(encodedValue, HtmlTokenType.ATTRVALUE, pos)),
                Token.instance(name, HtmlTokenType.ATTRNAME, pos), pos));
      }
      return fakeAttribs;
    }
  }

  // the start token text is either <name or </name for a tag
  static boolean isEndTag(String tokenText) {
    return tokenText.length() >= 2 && tokenText.charAt(1) == '/';
  }

  static String tagName(String tokenText) {
    String name = tokenText.substring(isEndTag(tokenText) ? 2 : 1);
    // Intern since the TreeBuilder likes to compare strings by reference.
    return Html5ElementStack.canonicalElementName(name).intern();
  }

  static boolean tagMatchesElementName(String tagName, String elementName) {
    return tagName.equals(elementName)
        || (tagName.equals("image") && elementName.equals("img"));
  }
  
  /**
   * true if a close tag with the given name closes an element with the
   * given name.
   */
  static boolean tagCloses(String tagName, String elementName) {
    return tagMatchesElementName(tagName, elementName)
        || (isHeading(tagName) && isHeading(elementName));
  }

  /** true for h1, h2, ... */
  static boolean isHeading(String tagName) {
    if (tagName.length() != 2 || 'h' != tagName.charAt(0)) { return false; }
    char ch1 = tagName.charAt(1);
    return ch1 >= '1' && ch1 <= '6';
  }
}

/**
 * An implementation of org.xml.sax that wraps {@code DomTree.Attrib}s.
 * This ignores all namespacing since HTML doesn't do namespacing.
 */
final class AttributesImpl implements Attributes {
  private final List<DomTree.Attrib> attribs;

  AttributesImpl(List<DomTree.Attrib> attribs) { this.attribs = attribs; }

  public int getIndex(String qName) {
    int index = 0;
    for (DomTree.Attrib attrib : attribs) {
      if (attrib.getAttribName().equals(qName)) { return index; }
      ++index;
    }
    return -1;
  }

  public int getIndex(String uri, String localName) {
    return getIndex(localName);
  }

  public int getLength() { return attribs.size(); }

  public String getLocalName(int index) {
    return attribs.get(index).getAttribName();
  }

  public String getQName(int index) { return getLocalName(index); }

  public String getType(int index) { return null; }

  public String getType(String qName) { return null; }

  public String getType(String uri, String localName) { return null; }

  public String getURI(int index) { return null; }

  public String getValue(int index) {
    return attribs.get(index).getAttribValue();
  }

  public String getValue(String qName) {
    int index = getIndex(qName);
    return index < 0 ? null : getValue(index);
  }

  public String getValue(String uri, String localName) {
    return getValue(localName);
  }

  public List<DomTree.Attrib> getAttributes() {
    return Collections.unmodifiableList(attribs);
  }
}
