// Copyright (C) 2005 Google Inc.
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

package com.google.caja.parser.js;

import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mikesamuel@gmail.com
 */
public final class ReturnStmt extends AbstractStatement<Expression> {
  private Expression returnValue;

  public ReturnStmt(Void value, List<? extends Expression> children) {
    this(children.get(0));
  }

  public ReturnStmt(Expression returnValue) {
    if (null != returnValue) {
      appendChild(returnValue);
    }
  }

  @Override
  protected void childrenChanged() {
    super.childrenChanged();
    this.returnValue = children().isEmpty() ? null : children().get(0);
  }

  public Expression getReturnValue() { return returnValue; }

  @Override
  public Object getValue() { return null; }

  public void render(RenderContext rc) throws IOException {
    rc.out.append("return");
    if (null != returnValue) {
      rc.out.append(" ");
      returnValue.render(rc);
    }
  }
}