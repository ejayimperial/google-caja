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

import com.google.caja.util.TestUtil;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

/**
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DefaultGadgetRewriterTest extends TestCase {

  private static final UrlCallback urlCallback = new UrlCallback() {
    public UrlCallbackOption getOption(URL url) {
      return UrlCallbackOption.RETRIEVE;
    }

    public String retrieve(URL url) throws UrlCallbackException {
      try {
        return (String)url.getContent();
      } catch (IOException e) {
        throw new UrlCallbackException(e);
      }
    }

    public URL rewrite(URL url) throws UrlCallbackException {
      throw new UrlCallbackException(url, "Rewrite unsupported");
    }
  };

  private GadgetRewriter rewriter;

  public void setUp() { rewriter = new DefaultGadgetRewriter(); }

  public void tearDown() { rewriter = null; }
  
  public void testBasic() throws Exception {
    String data = TestUtil.readResource(getClass(), "listfriends-inline.xml");
    System.out.println(rewriter.rewrite(null, data, urlCallback));
  }
}