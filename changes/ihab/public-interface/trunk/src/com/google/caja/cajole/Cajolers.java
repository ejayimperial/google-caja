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

package com.google.caja.cajole;

import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.lang.css.CssSchema;

/**
 * Entry point into the Caja system for external clients.
 *
 * @author ihab.awad@gmail.com
 */
public final class Cajolers {

  /**
   * Creates a new parser.
   *
   * @param moduleEnvironment {@link CajaParser#getModuleEnvironment()}.
   * @return a new parser.
   */
  public static CajaParser newParser(ModuleEnvironment moduleEnvironment) {
    return null;  // TODO(ihab.awad): Implement
  }

  /**
   * Creates a new cajoler.
   *
   * @param languageMode {@link CajaCajoler#getLanguageMode()}.
   * @param debugLevel {@link CajaCajoler#getDebugLevel()}.
   * @param htmlSchema {@link CajaCajoler#getHtmlSchema()}.
   * @param cssSchema {@link CajaCajoler#getCssSchema()}.
   * @return a new cajoler.
   */
  public static CajaCajoler newCajoler(
      CajolingMode languageMode,
      DebugLevel debugLevel,
      HtmlSchema htmlSchema,
      CssSchema cssSchema) {
    return null;  // TODO(ihab.awad): Implement
  }
}