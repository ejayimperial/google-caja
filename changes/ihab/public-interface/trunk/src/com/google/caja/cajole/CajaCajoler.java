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

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.lang.css.CssSchema;
import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.CajaException;

/**
 * Given a parse tree node, rewrites it into its safe equivalent.
 *
 * @author ihab.awad@gmail.com
 */
public interface CajaCajoler {

  /**
   * @return the mode in which the input is to be cajoled.
   */
  CajolingMode getLanguageMode();

  /**
   * @return the level of debug support added to the output.
   */
  DebugLevel getDebugLevel();

  /**
   * @return the HTML schema used for cajoling modules containing HTML.
   */
  HtmlSchema getHtmlSchema();

  /**
   * @return the CSS schema used for cajoling modules containing CSS.
   */
  CssSchema getCssSchema();

  /**
   * Rewrite a program's parse tree.
   *
   * @param node the root node of the program's parse tree.
   * @param mq a {@code MessageQueue} into which messages will be placed.
   * @throws CajaException if a problem occurs.
   */
  void cajole(ParseTreeNode node, MessageQueue mq) throws CajaException;
}