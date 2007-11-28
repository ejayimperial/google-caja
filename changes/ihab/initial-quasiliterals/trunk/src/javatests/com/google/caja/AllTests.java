// Copyright (C) 2005 Google Inc.
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

package com.google.caja;

import com.google.caja.lexer.*;
import com.google.caja.parser.ParseTreeNodeTest;
import com.google.caja.parser.css.Css2Test;
import com.google.caja.parser.css.CssParserTest;
import com.google.caja.parser.css.CssTreeTest;
import com.google.caja.parser.html.DomParserTest;
import com.google.caja.parser.js.ParserTest;
import com.google.caja.parser.js.StringLiteralTest;
import com.google.caja.plugin.*;
import com.google.caja.plugin.caps.CapabilityRewriterTest;
import com.google.caja.quasiliteral.MatchTest;
import com.google.caja.quasiliteral.QuasiBuilderTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.regex.Pattern;

/**
 * @author mikesamuel@gmail.com
 */
public class AllTests {

  @SuppressWarnings("unchecked")
  public static Test suite() {
    TestSuite suite = new TestSuite("Caja Tests");
    Class<? extends TestCase>[] testClasses = new Class[] {
          CapabilityRewriterTest.class,
          CharProducerTest.class,
          CommentLexerTest.class,
          CompiledPluginTest.class,
          Css2Test.class,
          CssLexerTest.class,
          CssParserTest.class,
          CssRewriterTest.class,
          CssTreeTest.class,
          CssValidatorTest.class,
          DomParserTest.class,
          ExpressionSanitizerTest.class,
          GxpCompilerTest.class,
          GxpValidatorTest.class,
          HtmlLexerTest.class,
          HtmlWhitelistTest.class,
          JsLexerTest.class,
          LookaheadCharProducerTest.class,
          ParseTreeNodeTest.class,
          ParserTest.class,
          PluginCompilerTest.class,
          PunctuationTrieTest.class,
          StringLiteralTest.class,
          UrlUtilTest.class,
          QuasiBuilderTest.class,
          MatchTest.class,
        };
    Pattern testFilter = Pattern.compile(System.getProperty("test.filter", ""));
    for (Class<? extends TestCase> testClass : testClasses) {
      if (testFilter.matcher(testClass.getName()).find()) {
        suite.addTestSuite(testClass);
      }
    }
    return suite;
  }
}
