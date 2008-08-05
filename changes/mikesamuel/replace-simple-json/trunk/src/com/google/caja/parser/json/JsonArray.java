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

import com.google.caja.parser.js.ArrayConstructor;
import com.google.caja.parser.js.Expression;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a {@link java.util.List} like interface to a JSON array.
 * A JSON array is like a JS ArrayConstructor, but only admits JSON values as
 * elements.
 *
 * @author mikesamuel@gmail.com
 */
public final class JsonArray extends ArrayConstructor
    implements Iterable<Expression> {

  public JsonArray(Void value, List<? extends Expression> children) {
    super(children);
  }

  public JsonArray(List<? extends Expression> elements) {
    super(elements);
  }

  @Override
  protected void childrenChanged() {
    super.childrenChanged();
    for (Expression el : children()) {
      if (!(JsonValues.isJsonValueShallow(el))) {
        throw new IllegalStateException(el.toString());
      }
    }
  }

  public Iterator<Expression> iterator() {
    List<Expression> exprs = Collections.unmodifiableList(children());
    return exprs.iterator();
  }

  public Expression get(int i) { return children().get(i); }
}
