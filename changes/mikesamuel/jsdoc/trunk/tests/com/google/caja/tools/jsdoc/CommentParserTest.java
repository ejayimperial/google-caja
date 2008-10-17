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

package com.google.caja.tools.jsdoc;

import com.google.caja.lexer.CharProducer;
import com.google.caja.lexer.ParseException;
import com.google.caja.util.CajaTestCase;

/**
 * @author mikesamuel@gmail.com
 */
public class CommentParserTest extends CajaTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEmptyComment() throws Exception {
    assertComment("(Comment)", "/***/");
    assertComment("(Comment)", "/****/");
    assertComment("(Comment)", "/*******************/");
  }

  public void testTextOnly() throws Exception {
    assertComment("(Comment (TextAnnotation 'Hello World '))",
                  "/** Hello World */");
  }

  public void testLineBreaking() throws Exception {
    assertComment("(Comment (TextAnnotation 'Hello\nWorld\n'))",
                  "/** Hello\n * World\n */");
    assertComment("(Comment (TextAnnotation 'Hello\nWorld\n'))",
                  "/** Hello\r\n * World\r */");
    assertComment("(Comment (TextAnnotation 'Hello\nWorld\n'))",
                  ""
                  + "/** Hello\r\n"
                  + " ** World\r\n"
                  + " **/");
    assertComment("(Comment (TextAnnotation 'Hello\nWorld'))",
                  ""
                  + "/**Hello\r\n"
                  + "World**/");
  }

  public void testInlineAnnotationInBody() throws Exception {
    assertComment(
        "(Comment (TextAnnotation 'Foo ')"
        + " (InlineAnnotation 'code' (TextAnnotation '{{Bar{Baz}}}'))"
        + " (TextAnnotation ' Boo '))",
        "/** Foo {@code {{Bar{Baz}}}} Boo */");
  }

  public void testBlockAnnotations1() throws Exception {
    assertComment("(Comment (TextAnnotation 'Oh ')"
                  + " (BlockAnnotation 'see' (TextAnnotation 'can you say ')))",
                  "/** Oh @see can you say */");
  }

  public void testBlockAnnotations() throws Exception {
    assertComment("(Comment (TextAnnotation 'Oh ')"
                  + " (BlockAnnotation 'param' (TextAnnotation 'a foo '))"
                  + " (BlockAnnotation 'param' (TextAnnotation 'b bar ')))",
                  "/** Oh @param a foo @param b bar */");
  }

  public void testMixedAnnotations() throws Exception {
    assertComment(
        "(Comment (TextAnnotation 'Oh ')"
        + " (InlineAnnotation 'link' (TextAnnotation 'zoicks'))"
        + " (TextAnnotation ' ')"
        + " (BlockAnnotation 'param' (TextAnnotation 'a foo ')"
          + " (InlineAnnotation 'code' (TextAnnotation '{ baz: boo }'))"
        + " (TextAnnotation ' bar ')))",
        "/** Oh {@link zoicks} @param a foo {@code { baz: boo }} bar */");
  }

  public void testComplexComment() throws Exception {
    assertComment(
        "(Comment"
        + " (TextAnnotation '\n')"
        + " (BlockAnnotation 'param'"
          + " (TextAnnotation '{type} p blah blah.\n'))"
        + " (BlockAnnotation 'return'"
          + " (TextAnnotation '{Array.<type>} blah-dy blah\n  ')"
          + " (InlineAnnotation 'code'"
            + " (TextAnnotation '{x: \"}\", y: 3}'))"
          + " (TextAnnotation '\n  given the input\n  ')"
          + " (InlineAnnotation 'code'"
            + " (TextAnnotation ''{\\'x=}&y=3\\'}''))"
          + " (TextAnnotation '\n')))",

        "/**\n"
        + " * @param {type} p blah blah.\n"
        + " * @return {Array.<type>} blah-dy blah\n"
        + " *   {@code {x: \"}\", y: 3}}\n"
        + " *   given the input\n"
        + " *   {@code '{\\'x=}&y=3\\'}'}\n"
        + " */");
  }

  public void testSpecialCharsOutsideAnnotataion() throws Exception {
    assertComment("(Comment (TextAnnotation '@'))", "/*@*/");
    assertComment("(Comment (TextAnnotation '{@'))", "/*{@*/");
    assertComment("(Comment (TextAnnotation '{@}'))", "/*{@}*/");
    assertComment("(Comment (TextAnnotation '@') (BlockAnnotation 'a'))",
                  "/*@@a*/");
  }

  public void testEmailAddresses() throws Exception {
    assertComment(
        "(Comment (BlockAnnotation 'author' (TextAnnotation 'foo@bar.com ')))",
        "/* @author foo@bar.com */");
  }

  private void assertComment(String golden, String commentText)
      throws ParseException {
    CharProducer cp = fromString(commentText);
    String actual = CommentParser.parseStructuredComment(cp).toString();
    assertEquals(
        actual.replace("\r", "\\r").replace("\n", "\\n"), golden, "" + actual);
  }
}
