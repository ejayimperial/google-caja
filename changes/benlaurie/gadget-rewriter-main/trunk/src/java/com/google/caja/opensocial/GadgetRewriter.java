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

import java.io.IOException;
import java.net.URI;

/**
 * Caja rewriter for OpenSocial gadgets.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public interface GadgetRewriter {

  /**
   * Given a URI to an OpenSocial gadget specification, return a Caja parsed version of
   * the gadget specification.
   *
   * <p>The URIs in all {@code &lt;script src="..."&gt;} in the gadget will be resolved
   * using the supplied {@link UrlCallback} and possibly inlined into the returned text.
   *
   * <p>All URIs embedded in the gadget will be interpreted relative to {@code gadgetUri}
   * (unless, of course, they are absolute).
   *
   * @param gadgetUrl the URL of a gadget specification.
   * @param urlCallback a {@link UrlCallback} object for resolving URIs.
   * @param output an {@code Appendable} to which the rewriter will write the Caja parsed
   * gadget specification, as a literal string of content.
   * @exception UrlCallbackException if the {@code urlCallback} threw an exception.
   * @exception GadgetRewriteException if there was a problem parsing the gadget.
   * @exception IOException if there was an I/O problem.
   */
  void rewrite(URI gadgetUrl, UrlCallback urlCallback, Appendable output)
      throws UrlCallbackException, GadgetRewriteException, IOException;

  /**
   * Given the source text of an OpenSocial gadget specification, return a Caja parsed
   * version of the gadget specification.
   *
   * @param baseUrl a URL relative to which URIs embedded in the gadget specification
   * will be interpreted.
   * @param gadgetSpec the source text of the gadget specification.
   * @param urlCallback a {@link UrlCallback} object for resolving URIs.
   * @param output an {@code Appendable} to which the rewriter will write the Caja parsed
   * gadget specification, as a literal string of content.
   * @exception UrlCallbackException if the {@code urlCallback} threw an exception.
   * @exception GadgetRewriteException if there was a problem parsing the gadget.
   * @exception IOException if there was an I/O problem.
   * @see #rewrite(URI, UrlCallback, Appendable)
   */
  void rewrite(URI baseUrl, Readable gadgetSpec, UrlCallback urlCallback, Appendable output)
      throws UrlCallbackException, GadgetRewriteException, IOException;
}
