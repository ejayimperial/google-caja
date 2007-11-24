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
import com.google.caja.reporting.RenderContext;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mikesamuel@gmail.com
 */
public class DoWhileLoop extends Loop {
  private Expression condition;
  private Statement body;

  public DoWhileLoop(Object value, List<? extends ParseTreeNode> children) {
    this((String)value,
         (Expression)children.get(1),
         (Statement)children.get(0));
  }

  public DoWhileLoop(String label, Expression condition, Statement body) {
    super(label);
    children.add(body);
    children.add(condition);
    childrenChanged();
  }

  @Override
  protected void childrenChanged() {
    super.childrenChanged();
    this.condition = (Expression) children.get(1);
    this.body = (Statement) children.get(0);
  }

  @Override
  public Expression getCondition() { return condition; }
  @Override
  public Statement getBody() { return body; }
  @Override
  public boolean isDoLoop() { return true; }
  
  public void render(RenderContext rc) throws IOException {
    String label = getLabel();
    if (null != label && !"".equals(label)) {
      rc.out.append(label);
      rc.out.append(": ");
    }
    rc.out.append("do");
    body.renderBlock(rc, true, true, true);
    rc.out.append("while (");
    rc.indent += 2;
    condition.render(rc);
    rc.indent -= 2;
    rc.out.append(")");
  }
}