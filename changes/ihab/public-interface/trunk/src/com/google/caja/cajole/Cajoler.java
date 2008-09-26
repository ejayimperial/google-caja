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
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.ParseException;

import java.net.URI;
import java.io.IOException;

/**
 * A <code>Cajoler</code> is the principal entry point into the Caja system
 * creating cajoled code. An instance of this class is a "one-shot" object
 * that cajoles a single piece of content producing a single Caja module.
 *
 * @author ihab.awad@gmail.com
 */
public interface Cajoler {

  /**
   * Assigns the module environment to be used for resolving external URIs
   * found in the input.
   *
   * @param env a module environment.
   */
  void setModuleEnvironment(ModuleEnvironment env);

  /**
   * Provides a message queue into which this <code>Cajoler</code> will
   * insert compilation messages.
   *
   * @param mq a message queue.
   */
  void setMessageQueue(MessageQueue mq);

  /**
   * Controls the level of debug support added to the output.
   *
   * @param debugLevel a debug level.
   */
  void setDebugLevel(DebugLevel debugLevel);

  /**
   * Sets the HTML schema used for cajoling modules of type
   * {@link com.google.caja.cajole.InputContentType#HTML_FRAGMENT}. 
   *
   * @param schema an HTML schema.
   */
  void setHtmlSchema(HtmlSchema schema);

  /**
   * Sets the CSS schema used for cajoling modules of type
   * {@link com.google.caja.cajole.InputContentType#HTML_FRAGMENT}.
   *
   * @param schema a CSS schema.
   */
  void setCssSchema(CssSchema schema);

  /**
   * Sets the mode in which the input is to be cajoled.
   *
   * @param mode a cajoling mode.
   */
  void setLanguageMode(CajolingMode mode);

  /**
   * Provide input for the cajoler. Only one setInput() method may be called,
   * only once.
   *
   * @param baseUri the base URI against which any URIs (if any) in the
   * input content will be resolved.
   * @param in the input node.
   */
  void setInput(URI baseUri, ParseTreeNode in);

  /**
   * Provide input for the cajoler. Only one setInput() method may be called,
   * only once.
   *
   * @param content the program text.
   * @param startPosition a position for the start of the text
   * @param contentType the type of content provided
   * @throws ParseException if a problem in parsing occurs.
   * @throws IOException if an I/O problem occurs.
   */
  void setInput(
      Readable content,
      FilePosition startPosition,
      InputContentType contentType)
      throws ParseException, IOException;

  /**
   * Run the cajoler given the supplied settings and inputs.
   *
   * @throws CajaException if a problem occurs.
   */
  void run() throws CajaException;

  /**
   * Obtain the output node. In the case of {@link CajolingMode#INNOCENT},
   * the node is a {@link com.google.caja.parser.js.Block}. In other cases,
   * the node is a {@link com.google.caja.parser.js.ModuleEnvelope}.
   *
   * @return the output node.
   */
  ParseTreeNode getOutputNode();
}