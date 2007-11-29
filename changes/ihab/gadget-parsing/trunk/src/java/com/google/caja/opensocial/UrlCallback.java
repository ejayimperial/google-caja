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

import java.net.URL;

/**
 * A callback that retrieves or rewrites URLs on behalf of a {@link GadgetRewriter}.
 * 
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public interface UrlCallback {

  /**
   * Inform the caller what to do with a content URL.
   *
   * @param url the URL.
   *
   * @return a {@code URLCallbackOption} indicating how to proceed.
   */
  UrlCallbackOption getOption(URL url);

  /**
   * Retrieve the literal content of the specified URL.
   *
   * @param url an absolute URL.
   * @return the content returned by a GET on that URL.
   * @exception UrlCallbackException if the URL could (or should) not be retrieved.
   */
  String retrieve(URL url) throws UrlCallbackException;

  /**
   * Rewrites a URL, perhaps to point to a proxy.
   *
   * @param url an absolute URL.
   * @return a rewritten form of the URL.
   * @exception UrlCallbackException if the URL could (or should) not be rewritten.
   */
  URL rewrite(URL url) throws UrlCallbackException;
}
