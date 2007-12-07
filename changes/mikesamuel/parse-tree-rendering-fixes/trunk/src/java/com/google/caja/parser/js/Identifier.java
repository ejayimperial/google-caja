// Copyright (C) 2007 Google Inc.
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
import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.util.List;

/**
 * An identifier used in JavaScript source.
 *
 * @author ihab.awad@gmail.com
 */
public final class Identifier extends AbstractExpression<ParseTreeNode> {
  private String value;

  public Identifier(Object value, List<? extends ParseTreeNode> children) {
    this((String)value);
  }

  public Identifier(String value) {
    this.value = value;
    childrenChanged();
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void clearValue() {
    value = null;
  }

  public void render(RenderContext r) throws IOException {
    r.out.append(value);
  }
}