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

package com.google.caja.opensocial;

import com.google.caja.lexer.ParseException;
import com.google.caja.plugin.HtmlPluginCompiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

/**
 * A default implementation of the Caja/OpenSocial gadget rewriter.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DefaultGadgetRewriter implements GadgetRewriter {
  private static final String JAVASCRIPT_PREFIX = "___OUTERS___";
  private static final String CSS_PREFIX = "CSS_PREFIX";
  private static final String ROOT_DIV_ID = "ROOT_DIV_ID";

  public void rewrite(URL gadgetUrl, UrlCallback urlCallback, Appendable output)
      throws UrlCallbackException, GadgetRewriteException, IOException {
    rewrite(
        gadgetUrl,
        new InputStreamReader(urlCallback.retrieve(gadgetUrl, "text/xml")),
        urlCallback,
        output);
  }

  public void rewrite(URL baseUrl, Readable gadgetSpec, UrlCallback urlCallback, Appendable output)
      throws UrlCallbackException, GadgetRewriteException, IOException {
    GadgetParser parser = new GadgetParser();
    GadgetSpec spec = parser.parse(gadgetSpec);
    spec.setContent(rewriteContent(baseUrl, spec.getContent(), urlCallback));
    parser.render(spec, output);
 }

  private String rewriteContent(URL baseUrl, String content, UrlCallback callback)
      throws UrlCallbackException, GadgetRewriteException, IOException {
    HtmlPluginCompiler compiler =
        new HtmlPluginCompiler(content, JAVASCRIPT_PREFIX, CSS_PREFIX, ROOT_DIV_ID, true);
    // TODO(ihab.awad): Resolve embedded <script src="..."> tags (and any other embedded URIs,
    // for that matter) using the 'baseUri' and 'callback' args.
    try {
      if (!compiler.run()) {
        throw new GadgetRewriteException(compiler.getErrors());
      }
    } catch (ParseException e) {
      throw new GadgetRewriteException(e);
    }

    StringBuilder results = new StringBuilder();

    results.append("<style type=\"text/css\">\n");
    results.append(compiler.getOutputCss());
    results.append("</style>\n");

    results.append("<div id=\"" + ROOT_DIV_ID + "\"></div>\n");
    results.append("<script type=\"text/javascript\">\n");
    results.append(compiler.getOutputJs());
    results.append("</script>\n");

    return results.toString();
  }
}
