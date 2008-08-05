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

import com.google.caja.parser.js.Expression;
import com.google.caja.parser.js.ObjectConstructor;
import com.google.caja.parser.js.StringLiteral;
import com.google.caja.util.Pair;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a {@link java.util.Map} like interface to a JSON object.
 * A JSON object is like a JS ObjectConstructor, but only admits StringLiterals
 * as keys, and only admits JSON values as values.
 *
 * @author mikesamuel@gmail.com
 */
public final class JsonObject extends ObjectConstructor
    implements Iterable<Pair<String, Expression>> {

  public JsonObject(Void value, List<? extends Expression> children) {
    super(value, children);
  }

  @SuppressWarnings("unchecked")
  public JsonObject(List<Pair<StringLiteral, Expression>> properties) {
    super(List.class.cast(properties));
  }

  @Override
  protected void childrenChanged() {
    super.childrenChanged();
    List<? extends Expression> children = children();
    for (int i = 0, n = children.size(); i < n; i += 2) {
      if (!(children.get(i) instanceof StringLiteral)) {
        throw new IllegalStateException(children.get(i).toString());
      }
      if (!(JsonValues.isJsonValueShallow(children.get(i + 1)))) {
        throw new IllegalStateException(children.get(i + 1).toString());
      }
    }
  }

  public Iterator<Pair<String, Expression>> iterator() {
    return new Iterator<Pair<String, Expression>>() {
      int i = 0;
      public boolean hasNext() { return i < children().size(); }
      public Pair<String, Expression> next() {
        StringLiteral key = (StringLiteral) children().get(i++);
        Expression e = children().get(i++);
        return Pair.pair(key.getUnquotedValue(), e);
      }
      public void remove() { throw new UnsupportedOperationException(); }
    };
  }

  public Iterable<String> keys() {
    return new Iterable<String>() {
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          int i = 0;
          public boolean hasNext() { return i < children().size(); }
          public String next() {
            StringLiteral key = (StringLiteral) children().get(i);
            i += 2;
            return key.getUnquotedValue();
          }
          public void remove() { throw new UnsupportedOperationException(); }
        };
      }
    };
  }

  public boolean containsKey(String key) {
    return get(key) != null;
  }

  public Expression get(String key) {
    List<? extends Expression> children = children();
    for (int i = 0, n = children.size(); i < n; i += 2) {
      if (key.equals(((StringLiteral) children.get(i)).getUnquotedValue())) {
        return children.get(i + 1);
      }
    }
    return null;
  }

  public String getString(String key) {
    List<? extends Expression> children = children();
    for (int i = 0, n = children.size(); i < n; i += 2) {
      if (key.equals(((StringLiteral) children.get(i)).getUnquotedValue())) {
        return ((StringLiteral) children.get(i + 1)).getUnquotedValue();
      }
    }
    return null;
  }
}
