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

package com.google.caja.util;

import com.google.caja.parser.MutableParseTreeNode;
import com.google.caja.parser.js.BooleanLiteral;
import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.IntegerLiteral;
import com.google.caja.parser.js.NullLiteral;
import com.google.caja.parser.js.RealLiteral;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.parser.json.JsonArray;
import com.google.caja.parser.json.JsonObject;

import java.util.Collections;

/**
 * Utility class for manipulating JSON objects
 *
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class Json {
  public static JsonObject formatAsJson(Object... members) {
    JsonObject o = newJsonObject();
    putJson(o, members);
    return o;
  }

  public static JsonObject newJsonObject() {
    return new JsonObject(
        Collections.<Pair<StringLiteral, Expression>>emptyList());
  }

  public static JsonArray newJsonArray() {
    return new JsonArray(Collections.<Expression>emptyList());
  }

  @SuppressWarnings("unchecked")
  public static void putJson(JsonObject o, Object... members) {
    MutableParseTreeNode.Mutation mut = o.createMutation();
    for (int i = 0, n = members.length; i < n; i += 2) {
      String name = (String) members[i];
      Expression value = toJsonValue(members[i + 1]);
      mut.appendChild(StringLiteral.valueOf(name)).appendChild(value);
    }
    mut.execute();
  }

  @SuppressWarnings("unchecked")
  public static void pushJson(JsonArray a, Object... members) {
    MutableParseTreeNode.Mutation mut = a.createMutation();
    for (Object member : members) {
      mut.appendChild(toJsonValue(member));
    }
    mut.execute();
  }

  public static Expression toJsonValue(Object value) {
    if (value == null) { return new NullLiteral(); }
    if (value instanceof Boolean) {
      return new BooleanLiteral((Boolean) value);
    }
    if (value instanceof Number) {
      if (value instanceof Integer || value instanceof Long
          || value instanceof Short || value instanceof Byte) {
        return new IntegerLiteral(((Number) value).longValue());
      } else {
        return new RealLiteral(((Number) value).doubleValue());
      }
    } else if (value instanceof Expression) { return (Expression) value; }

    return StringLiteral.valueOf(value.toString());
  }
}
