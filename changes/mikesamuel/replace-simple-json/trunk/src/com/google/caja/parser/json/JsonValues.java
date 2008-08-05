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

package com.google.caja.parser.json;

import com.google.caja.parser.js.BooleanLiteral;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.Literal;
import com.google.caja.parser.js.NullLiteral;
import com.google.caja.parser.js.NumberLiteral;
import com.google.caja.parser.js.StringLiteral;

/**
 * Utilities for dealing with JSON.
 */
public final class JsonValues {
  /**
   * Returns true if the given expression is a valid JSON value, without
   * recursing to any children.  Since it does not recurse, this does not
   * check the JSON requirement that the object graph be a tree.
   */
  public static boolean isJsonValueShallow(Expression e) {
    if (e instanceof Literal) {
      return e instanceof BooleanLiteral
          || e instanceof NullLiteral
          || e instanceof NumberLiteral
          || (e instanceof StringLiteral
              && ((StringLiteral) e).getValue().charAt(0) == '"');
    } else {
      return e instanceof JsonArray || e instanceof JsonObject;
    }
  }

  private JsonValues() {}
}
