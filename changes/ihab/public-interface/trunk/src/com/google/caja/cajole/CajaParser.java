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
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.ParseException;

import javax.activation.MimeType;
import java.io.IOException;

/**
 * Parses Caja-supported text content to a Caja parse tree.
 *
 * @author ihab.awad@gmail.com
 */
public interface CajaParser {

  /**
   * @return the module environment used for resolving external URIs
   * found in the input.
   */
  ModuleEnvironment getModuleEnvironment();

  /**
   * Parse some input content.
   *
   * @param content the input content.
   * @param startPosition a position for the start of the text
   * @param contentType the type of content provided
   * @param mq a {@code MessageQueue} into which messages will be placed.
   * @return a parse tree node
   * @throws com.google.caja.lexer.ParseException if a problem in parsing occurs.
   * @throws java.io.IOException if an I/O problem occurs.
   */
  ParseTreeNode parse(
      Readable content,
      FilePosition startPosition,
      MimeType contentType,
      MessageQueue mq)
      throws ParseException, IOException;
}