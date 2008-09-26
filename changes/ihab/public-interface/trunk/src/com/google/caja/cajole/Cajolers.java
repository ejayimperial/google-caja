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
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.ParseException;

import java.io.IOException;

/**
 * Entry point into the Caja system for external clients.
 *
 * @author ihab.awad@gmail.com
 */
public final class Cajolers {

  /**
   * Creates a new <code>Cajoler</code>.
   *
   * @return a new Cajoler.
   */
  public static Cajoler newCajoler() {
    return null;
  }

  /**
   * Parse some input program text.
   *
   * @param content the program text.
   * @param startPosition a position for the start of the text
   * @param contentType the type of content provided
   * @return a parse tree node
   * @throws ParseException if a problem in parsing occurs.
   * @throws IOException if an I/O problem occurs.
   */
  public static ParseTreeNode parse(
      Readable content,
      FilePosition startPosition,
      InputContentType contentType)
      throws ParseException, IOException {
    return null;
  }
}