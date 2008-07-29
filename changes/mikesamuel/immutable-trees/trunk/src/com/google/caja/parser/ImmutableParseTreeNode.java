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

package com.google.caja.parser;

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.TokenConsumer;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.RenderContext;
import com.google.caja.util.Callback;
import com.google.caja.util.ImmutableList;
import com.google.caja.util.SyntheticAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Base class for immutable parse tree nodes.
 *
 * @param <T> a lower bound on the type of children.
 *
 * @author mikesamuel@gmail.com
 */
public abstract class ImmutableParseTreeNode<T extends ParseTreeNode>
// TODO(mikesamuel): type parameter T should extend ImmutableParseTreeNode
// to make parse trees deeply immutable.
    implements ParseTreeNode {
  private final ImmutableList<T> children;
  private SyntheticAttributes attributes;

  protected ImmutableParseTreeNode(ImmutableList<T> children) {
    this.children = children;
  }

  public final boolean acceptPostOrder(Visitor v, AncestorChain<?> ancestors) {
    AncestorChain<? extends ParseTreeNode> ac
        = AncestorChain.instance(ancestors, this);
    for (ParseTreeNode child : children()) {
      child.acceptPostOrder(v, ac);
    }
    return v.visit(ac);
  }

  public final boolean acceptPreOrder(Visitor v, AncestorChain<?> ancestors) {
    AncestorChain<? extends ParseTreeNode> ac
        = AncestorChain.instance(ancestors, this);
    if (!v.visit(ac)) { return false; }
    for (ParseTreeNode child : children()) {
      child.acceptPreOrder(v, ac);
    }
    return true;
  }

  public final List<T> children() { return children; }

  public final Class<T> getChildType() { return children.getElementType(); }

  public void format(MessageContext context, Appendable out)
      throws IOException {
    formatTree(context, out);
  }

  protected void formatSelf(MessageContext context, Appendable out)
      throws IOException {
    String cn = this.getClass().getName();
    cn = cn.substring(cn.lastIndexOf(".") + 1);
    cn = cn.substring(cn.lastIndexOf("$") + 1);
    out.append(cn);
    Object value = getValue();
    if (null != value) {
      out.append(" : ");
      if (value instanceof MessagePart) {
        ((MessagePart) value).format(context, out);
      } else {
        out.append(value.toString());
      }
    }
    if (!context.relevantKeys.isEmpty() && null != attributes) {
      for (SyntheticAttributes.Key<?> k : context.relevantKeys) {
        Object attribValue = attributes.get(k);
        if (attribValue != k.getDefaultValue()) {
          out.append(" ; ").append(k.getName()).append('=');
          if (attribValue instanceof MessagePart) {
            ((MessagePart) attribValue).format(context, out);
          } else {
            out.append(String.valueOf(attribValue));
          }
        }
      }
    }
  }
  public final void formatTree(MessageContext context, Appendable out)
      throws IOException {
    formatTree(context, 0, out);
  }

  public final void formatTree(
      MessageContext context, int depth, Appendable out)
      throws IOException {
    for (int d = depth; --d >= 0;) { out.append("  "); }
    formatSelf(context, out);
    for (ParseTreeNode child : children()) {
      out.append("\n");
      child.formatTree(context, depth + 1, out);
    }
  }

  public final SyntheticAttributes getAttributes() {
    if (attributes == null) { attributes = new SyntheticAttributes(); }
    return attributes;
  }

  public final FilePosition getFilePosition() {
    return getAttributes().get(FILE_POSITION);
  }

  public final void setFilePosition(FilePosition pos) {
    getAttributes().set(FILE_POSITION, pos);
  }

  public Object getValue() { return null; }

  public abstract TokenConsumer makeRenderer(
      Appendable out, Callback<IOException> handler);

  public abstract void render(RenderContext r);

  @SuppressWarnings("unchecked")
  @Override
  public final ImmutableParseTreeNode<T> clone() {
    Class<T> contentType = children.getElementType();
    // This cast does not account for the type parameter but it is correct since
    // the child list has type contentType.
    ImmutableParseTreeNode<T> cloned = getClass().cast(
        ParseTreeNodes.newNodeInstance(
            contentType, getValue(), cloneChildren(contentType, children)));
    cloned.setFilePosition(getFilePosition());
    if (attributes != null) {
      cloned.attributes = new SyntheticAttributes(attributes);
      cloned.attributes.remove(TAINTED);
    }
    return cloned;
  }

  private static <E extends ParseTreeNode>
  ImmutableList<? extends E> cloneChildren(
      Class<E> contentType, ImmutableList<E> children) {
    // TODO: produce an ImmutableList efficiently
    ImmutableList.Builder<E> clonedChildren
        = ImmutableList.Builder.instance(contentType, children.size());
    for (E child : children) {
      clonedChildren.add(
          contentType.cast(child.getClass().cast(child.clone())));
    }
    return clonedChildren.toImmutableList();
  }
}
