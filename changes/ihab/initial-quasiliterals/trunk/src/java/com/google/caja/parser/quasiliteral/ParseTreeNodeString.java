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

package com.google.caja.parser.quasiliteral;

import com.google.caja.parser.AbstractParseTreeNode;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.reporting.RenderContext;

import java.io.IOException;

/**
 * Convenience wrapper allowing a quasiliteral pattern to return a simple String
 * result in a type-safe manner.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class ParseTreeNodeString extends AbstractParseTreeNode<ParseTreeNode> {
  private final String value;

  public ParseTreeNodeString(String value) {
    this.value = value;
  }

  public String getValue() { return value; }

  public void render(RenderContext rc) throws IOException {
    rc.out.append("ParseTreeNodeString: " + value);
  }
}