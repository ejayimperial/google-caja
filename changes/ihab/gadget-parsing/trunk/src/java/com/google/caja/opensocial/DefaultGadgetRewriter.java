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
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.plugin.HtmlPluginCompiler;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.net.URL;

/**
 * A default implementation of the Caja/OpenSocial gadget rewriter.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class DefaultGadgetRewriter implements GadgetRewriter {
  private static final String JAVASCRIPT_PREFIX = "JAVASCRIPT_PREFIX";
  private static final String CSS_PREFIX = "JAVASCRIPT_PREFIX";
  private static final String ROOT_DIV_ID = "ROOT_DIV_ID";

  public String rewrite(URL gadgetUrl, UrlCallback urlCallback)
      throws UrlCallbackException, GadgetRewriteException {
    return rewrite(gadgetUrl, urlCallback.retrieve(gadgetUrl), urlCallback);
  }

  public String rewrite(URL baseUrl, String gadgetSpec, UrlCallback urlCallback)
      throws UrlCallbackException, GadgetRewriteException {
    GadgetParser parser = new GadgetParser();
    GadgetSpec spec = parser.parse(gadgetSpec);
    spec.setContent(rewriteContent(baseUrl, spec.getContent(), urlCallback));
    return parser.render(spec);
 }

  private String rewriteContent(URL baseUrl, String content, UrlCallback callback)
      throws UrlCallbackException, GadgetRewriteException {
    HtmlPluginCompiler compiler =
        new HtmlPluginCompiler(content, JAVASCRIPT_PREFIX, CSS_PREFIX, ROOT_DIV_ID, false);
    // TODO(ihab.awad): Resolve embedded <script src="..."> tags (and any other embedded URLs,
    // for that matter) using the 'baseUrl' and 'callback' args.
    try {
      if (!compiler.run()) {
        throw new GadgetRewriteException(compiler.getErrors());
      }
    } catch (ParseException e) {
      throw new GadgetRewriteException(e);
    }

    StringBuffer results = new StringBuffer();

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
