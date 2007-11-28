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

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.plugin.ReservedNames;
import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mikesamuel@gmail.com
 */
public class Reference extends AbstractExpression<Expression> {
  private Identifier identifier;

  public Reference(Object value, List<? extends ParseTreeNode> children) {
    this.children.add((Expression)children.get(0));
    childrenChanged();
  }

  public Reference(String identifierName) {
    children.add(new Identifier(identifierName));
    childrenChanged();
  }

  @Override
  protected void childrenChanged() {
    super.childrenChanged();
    if (children.size() != 1)
      throw new IllegalArgumentException("Reference must have exactly 1 child");
    identifier = (Identifier)children.get(0);
  }

  public String getIdentifierName() { return this.identifier.getValue(); }
  public void setIdentifierName(String identifierName) { this.identifier.setValue(identifierName); }

  @Override
  public Object getValue() { return null; }

  @Override
  public boolean isLeftHandSide() {
    return true;
  }

  public boolean isThis() {
    return
        getIdentifierName().equals("this") ||
        getIdentifierName().equals(ReservedNames.LOCAL_THIS);
  }

  public void render(RenderContext rc) throws IOException {
    rc.out.append(getIdentifierName());
  }

  public boolean isSuper() {
    return getIdentifierName().equals(ReservedNames.SUPER);
  }
}
