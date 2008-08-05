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

package com.google.caja.config;

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.ParseException;
import com.google.caja.reporting.Message;
import com.google.caja.util.CajaTestCase;
import com.google.caja.util.MoreAsserts;
import com.google.caja.util.TestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigUtilTest extends CajaTestCase {
  public void testEmptyConfigAllowNothing() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString("{}"), FilePosition.startOfFile(is), mq);
    assertTrue(w.allowedItems().isEmpty());
    assertMessages();
  }

  public void testAllowed() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString("{ \"allowed\": [ \"foo\", { \"key\" : \"bar\" } ] }"),
        FilePosition.startOfFile(is), mq);
    assertTrue(w.allowedItems().contains("foo"));
    assertTrue(w.allowedItems().contains("bar"));
    assertEquals(2, w.allowedItems().size());
    assertMessages();
  }

  public void testDenied() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"denied\": [ \"foo\", { \"key\" : \"bar\" } ],"
            + " \"allowed\": [ \"bar\", { \"key\" : \"boo\" } ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertTrue("boo", w.allowedItems().contains("boo"));
    assertFalse("bar", w.allowedItems().contains("bar"));
    assertFalse("foo", w.allowedItems().contains("foo"));
    assertEquals(1, w.allowedItems().size());
    assertMessages();
  }

  public void testMisspelledDenied() throws Exception {
    ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"denies\": [ \"foo\", { \"key\" : \"bar\" } ],"
            + " \"allowed\": [ \"bar\", { \"key\" : \"boo\" } ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertMessages(
        "WARNING: testMisspelledDenied:1+1: unrecognized key denies");
  }

  public void testAllowedOverridden() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"allowed\": [ \"foo\", \"bar\" ]"
                + "}"
                ) + "\"],"
            + " \"denied\": [ \"foo\" ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertTrue("bar", w.allowedItems().contains("bar"));
    assertFalse("foo", w.allowedItems().contains("foo"));
    assertEquals(1, w.allowedItems().size());
    assertMessages();
  }

  public void testDeniedOverridden() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"allowed\": [ \"foo\" ],"
                + " \"denied\": [ \"foo\", \"bar\" ]"
                + "}"
                ) + "\"],"
            + " \"allowed\": [ \"bar\" ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertTrue("bar", w.allowedItems().contains("bar"));
    assertFalse("foo", w.allowedItems().contains("foo"));
    assertEquals(1, w.allowedItems().size());
    assertMessages();
  }

  public void testDefinitionOverridden() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo\" },"
                + "     { \"key\": \"bar\", \"name\": \"Bar\" }"
                + "     ]"
                + "}"
                ) + "\"],"
            + " \"types\": ["
            + "     { \"key\": \"foo\", \"name\": \"FOO\" },"
            + "     { \"key\": \"baz\", \"name\": \"BAZ\" }"
            + "     ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertEquals("FOO", w.typeDefinitions().get("foo").get("name", null));
    assertEquals("Bar", w.typeDefinitions().get("bar").get("name", null));
    assertEquals("BAZ", w.typeDefinitions().get("baz").get("name", null));
    assertMessages();
  }

  public void testMissingUrl() throws Exception {
    try {
      ConfigUtil.loadWhiteListFromJson(
          fromString(
              "{"
              + " \"inherits\": [ {} ]"
              + "}"),
          FilePosition.startOfFile(is), mq);
      fail("parsing not aborted");
    } catch (ParseException ex) {
      assertMessages();
      ex.toMessageQueue(mq);
    }
    assertMessages(
        "FATAL_ERROR: testMissingUrl:1+1"
        + ": malformed config file: expected inherits src, not null");
  }

  public void testDuplicatedDefinitionsOk() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo\" }"
                + "     ]"
                + "}")
            + "\", \"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo\" }"
                + "     ]"
                + "}"
                ) + "\"]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertEquals("Foo", w.typeDefinitions().get("foo").get("name", null));
    assertMessages();
  }

  public void testOverriddenDuplicatedDefinitionsOk() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo\" }"
                + "     ]"
                + "}")
            + "\", \"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo!!\" }"
                + "     ]"
                + "}"
                ) + "\"],"
            + " \"types\": ["
            + "     { \"key\": \"foo\", \"name\": \"FOO\" }"
            + "     ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertEquals("FOO", w.typeDefinitions().get("foo").get("name", null));
    assertMessages();
  }

  public void testUnresolvedAmbiguousDefinition() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo\" }"
                + "     ]"
                + "}")
            + "\", \"" + TestUtil.makeContentUrl(
                "{"
                + " \"types\": ["
                + "     { \"key\": \"foo\", \"name\": \"Foo!!\" }"
                + "     ]"
                + "}"
                ) + "\"]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertEquals("Foo", w.typeDefinitions().get("foo").get("name", null));
    assertMessages("FATAL_ERROR: testUnresolvedAmbiguousDefinition:1+1:"
                   + " ambiguous type definition"
                   + " {\"key\":\"foo\",\"name\":\"Foo\"} !="
                   + " {\"key\":\"foo\",\"name\":\"Foo!!\"}");
  }

  public void testConflictsBetweenInheritedTypesResolved() throws Exception {
    WhiteList w = ConfigUtil.loadWhiteListFromJson(
        fromString(
            "{"
            + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                // This one has an ambiguous for foo.
                "{"
                + " \"inherits\": [\"" + TestUtil.makeContentUrl(
                    "{"
                    + " \"types\": ["
                    + "     { \"key\": \"foo\", \"name\": \"Foo-1\" }"
                    + "     ]"
                    + "}")
                + "\", \"" + TestUtil.makeContentUrl(
                    "{"
                    + " \"types\": ["
                    + "     { \"key\": \"foo\", \"name\": \"Foo-2\" }"
                    + "     ]"
                    + "}"
                    ) + "\"]"
                + "}"
                ) + "\"],"
            // But the ambiguity is resolved in a containing white-list.
            + " \"types\": ["
            + "     { \"key\": \"foo\", \"name\": \"Foo-3\" }"
            + "     ]"
            + "}"),
        FilePosition.startOfFile(is), mq);
    assertEquals("Foo-3", w.typeDefinitions().get("foo").get("name", null));
    assertMessages();
  }

  private void assertMessages(String... golden) {
    List<String> actual = new ArrayList<String>();
    for (Message msg : mq.getMessages()) {
      actual.add(msg.getMessageLevel() + ": " + msg);
    }
    MoreAsserts.assertListsEqual(Arrays.asList(golden), actual);
  }
}
