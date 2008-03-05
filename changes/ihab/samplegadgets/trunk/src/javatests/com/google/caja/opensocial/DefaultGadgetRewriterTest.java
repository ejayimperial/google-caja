// Copyright (C) 2007 Google Inc.
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

package com.google.caja.opensocial;

import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.InputSource;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.TestUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DefaultGadgetRewriterTest extends TestCase {

  private static final UriCallback uriCallback = new UriCallback() {
    public UriCallbackOption getOption(
        ExternalReference extref, String mimeType) {
      return UriCallbackOption.RETRIEVE;
    }

    public Reader retrieve(ExternalReference extref, String mimeType)
        throws UriCallbackException {
      if ("file".equals(extref.getUri().getScheme())) {
        if (extref.getUri().toString().startsWith(
                TestUtil.getResource(getClass(), "").toString())) {
          try {
            return new FileReader(extref.getUri().getPath());
          } catch (FileNotFoundException ex) {
            throw new UriCallbackException(extref, ex);
          }
        }
      }

      URL url;
      try {
        url = extref.getUri().toURL();        
      } catch (MalformedURLException e) {
        throw new UriCallbackException(extref, e);
      }

      try {
        return new BufferedReader(new InputStreamReader(url.openStream()));
      } catch (IOException e) {
        throw new UriCallbackException(extref, e);
      }
    }

    public URI rewrite(ExternalReference extref, String mimeType) {
      try {
        return URI.create(
            "http://url-proxy.test.google.com/"
            + "?url=" + URLEncoder.encode(extref.getUri().toString(), "UTF-8")
            + "&mime-type=" + URLEncoder.encode(mimeType, "UTF-8"));
      } catch (UnsupportedEncodingException ex) {
        // If we don't support UTF-8 we're in trouble
        throw new RuntimeException(ex);
      }
    }
  };

  private DefaultGadgetRewriter rewriter;

  @Override
  public void setUp() {
    rewriter = new DefaultGadgetRewriter(
        new EchoingMessageQueue(
            new PrintWriter(System.err), new MessageContext(), false)) {
          @Override
          protected RenderContext createRenderContext(
              Appendable out, MessageContext mc) {
            return new RenderContext(mc, out, false);
          }
        };
  }

  @Override
  public void tearDown() { rewriter = null; }

  // Test Gadget parsing
  public void testInlineGadget() throws Exception {
    assertRewritePasses("listfriends-inline.xml", MessageLevel.WARNING);
  }

  public void testSocialHelloWorld() throws Exception {
    assertRewritePasses("SocialHelloWorld.xml", MessageLevel.WARNING);
  }

  public void testParsing() throws Exception {
    assertRewritePasses("test-parsing.xml", MessageLevel.WARNING);
  }

  public void testSourcedGadget() throws Exception {
    assertRewritePasses("listfriends.xml", MessageLevel.WARNING);
  }

  // Test Gadget rewriting
  public void testExampleGadget() throws Exception {
    assertRewriteMatches("example.xml", "example-rewritten.xml",
                         MessageLevel.ERROR);
  }

  // Check that the validating and rewriting passes are hooked up.
  public void testTargetsDisallowed() throws Exception {
    assertRewriteFailsWithMessage(
        "<a target=\"_top\">Redirect window</a>",
        "attribute target cannot have value _top");
  }

  public void testMetaRefreshDisallowed() throws Exception {
    assertRewriteFailsWithMessage(
        "<meta http-equiv=\"refresh\" content=\"5;http://foo.com\"/>",
        "tag meta is not allowed");
  }

  public void testStylesSanitized() throws Exception {
    assertRewriteFailsWithMessage(
        "<p style=\"color: expression(foo)\">Bar</p>",
        "css property color has bad value: ==>expression(foo)<==");
  }

  public void testSampleGadget0000() throws Exception {
    assertRewritePasses(
        new URI("http://sea.ilike.com/brendan/gadget/ilike/orkut"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0001() throws Exception {
    assertRewritePasses(
        new URI("http://hosting.gmodules.com/ig/gadgets/file/104817968444070758456/chakpak-movies.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0002() throws Exception {
    assertRewritePasses(
        new URI("http://123.176.36.218:8008/devhangout/hangout_index.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0003() throws Exception {
    assertRewritePasses(
        new URI("http://static.ugenie.com/tbn_os/os-init.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0004() throws Exception {
    assertRewritePasses(
        new URI("http://www.rockyou.com/google_apps/emote_example/view/Emote_XML.php"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0005() throws Exception {
    assertRewritePasses(
        new URI("http://www.slide.com/ig_xml/ok/top8"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0006() throws Exception {
    assertRewritePasses(
        new URI("http://d94600a6.fb.joyent.us/specifications/latest"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0007() throws Exception {
    assertRewritePasses(
        new URI("http://orkut.actonme.com/photoattack/photoattack.php"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0008() throws Exception {
    assertRewritePasses(
        new URI("http://d94600a6.fb.joyent.us/specifications/latest"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0009() throws Exception {
    assertRewritePasses(
        new URI("http://www.indiadekha.com/orkut/app.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0010() throws Exception {
    assertRewritePasses(
        new URI("http://hangman.os.staging.c2w.com/containers/orkut/hangman.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0011() throws Exception {
    assertRewritePasses(
        new URI("http://orkutshelf.hungrymachine.com/gadget.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0012() throws Exception {
    assertRewritePasses(
        new URI("http://typeracer.com/orkut/gadget.xml"),
        MessageLevel.ERROR);
  }

  public void testSampleGadget0013() throws Exception {
    assertRewritePasses(
        new URI("http://www.flixster.com/orkut/application"),
        MessageLevel.ERROR);
  }

  private void assertRewritePasses(URI uri, MessageLevel failLevel)
      throws Exception {
    System.err.println("Rewriting URI: " + uri.toString());
    ExternalReference ref = new ExternalReference(
        uri,
        FilePosition.startOfFile(new InputSource(uri)));
    rewriter.rewrite(ref, uriCallback, System.out);
    checkMessages(failLevel);
  }

  private void assertRewritePasses(String file, MessageLevel failLevel)
      throws Exception {
    Reader input = new StringReader(TestUtil.readResource(getClass(), file));
    URI baseUri = TestUtil.getResource(getClass(), file);
    rewriter.rewrite(baseUri, input, uriCallback, System.out);

    checkMessages(failLevel);
  }

  private void assertRewriteMatches(
      String file, String goldenFile, MessageLevel failLevel)
      throws Exception {
    Reader input = new StringReader(TestUtil.readResource(getClass(), file));
    URI baseUri = TestUtil.getResource(getClass(), file);

    StringBuilder sb = new StringBuilder();
    rewriter.rewrite(baseUri, input, uriCallback, sb);
    String actual = normalXml(sb.toString()).trim();

    checkMessages(failLevel);

    String expected
        = normalXml(TestUtil.readResource(getClass(), goldenFile)).trim();
    if (!expected.equals(actual)) {
      System.err.println(actual);
      assertEquals(expected, actual);
    }
  }

  private static String normalXml(String xml) {
    return xml.replaceFirst("^<\\?xml[^>]*>", "");
  }

  private void assertRewriteFailsWithMessage(String htmlContent, String msg)
      throws Exception {
    Reader input = new StringReader(
       "<?xml version=\"1.0\"?>"
       + "<Module>"
       + "<ModulePrefs title=\"Example Gadget\">"
       + "<Require feature=\"opensocial-0.5\"/>"
       + "</ModulePrefs>"
       + "<Content type=\"html\">"
       + "<![CDATA[" + htmlContent + "]]>"
       + "</Content>"
       + "</Module>");
    URI baseUri = URI.create("http://unittest.google.com/foo/bar/");
    try {
      rewriter.rewrite(baseUri, input, uriCallback, System.out);
      fail("rewrite should have failed with message " + msg);
    } catch (GadgetRewriteException ex) {
      // pass
    }

    List<Message> errors = getMessagesExceedingLevel(MessageLevel.ERROR);

    assertFalse("Expected error msg: " + msg, errors.isEmpty());
    String actualMsg = errors.get(0).format(new MessageContext());
    // strip off the file position since that's tested in the modules that
    // generate the errors.
    actualMsg = actualMsg.substring(actualMsg.indexOf(": ") + 2).trim();
    assertEquals(msg, actualMsg);
  }

  private List<Message> getMessagesExceedingLevel(MessageLevel limit) {
    List<Message> matches = new ArrayList<Message>();
    for (Message msg : rewriter.getMessageQueue().getMessages()) {
      if (msg.getMessageLevel().compareTo(limit) >= 0) {
        matches.add(msg);
      }
    }
    return matches;
  }

  private void checkMessages(MessageLevel failLevel) {
    List<Message> failures = getMessagesExceedingLevel(failLevel);
    MessageContext mc = new MessageContext();
    if (!failures.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Message failure : failures) {
        sb.append(failure.getMessageLevel())
            .append(" : ")
            .append(failure.format(mc))
            .append('\n');
      }
      fail(sb.toString().trim());
    }
  }
}
