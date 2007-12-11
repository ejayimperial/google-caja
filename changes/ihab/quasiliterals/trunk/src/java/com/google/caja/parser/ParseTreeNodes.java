// Copyright (C) 2005-2006 Google Inc.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * A utility class for common operations on {@link ParseTreeNode}s.
 *
 * @author ihab.awad@gmail.com
 */
public class ParseTreeNodes {

  /**
   * Construct a new {@code ParseTreeNode} via reflection assuming the existence of a
   * constructor having the following signature:
   *
   * <pre>ctor(T value, List&lt;? extends ParseTreeNode&gt; children)</pre>
   *
   * where {@code T} is the type of the values for the specific node class.
   *
   * @param clazz the concrete class of {@code ParseTreeNode} to instantiate.
   * @param value the value for the new node
   *        (see {@link com.google.caja.parser.ParseTreeNode#getValue()}).
   * @param children the children of the new node
   *        (see {@link com.google.caja.parser.ParseTreeNode#children()})).
   * @return the newly constructed {@code ParseTreeNode}.
   */
  public static ParseTreeNode newNodeInstance(
      Class<? extends ParseTreeNode> clazz,
      Object value,
      Iterable<? extends ParseTreeNode> children) {
    Constructor<? extends ParseTreeNode> ctor = findCloneCtor(clazz);
    try {
      return ctor.newInstance(value, children);
    } catch (InstantiationException e) {
      throw new RuntimeException(getCtorErrorMessage(ctor, value, children), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(getCtorErrorMessage(ctor, value, children), e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(getCtorErrorMessage(ctor, value, children), e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(getCtorErrorMessage(ctor, value, children), e);
    }
  }

  /**
   * Perform a deep equality test on a pair of {@code ParseTreeNode}s.
   *
   * @param x a {@code ParseTreeNode}.
   * @param y another {@code ParseTreeNode}.
   * @return whether the trees rooted at {@code this} and {@code n} are equal.
   */  
  public static boolean deepEquals(ParseTreeNode x, ParseTreeNode y) {
    if (x.getClass() == y.getClass()) {
      if ((x.getValue() == null && y.getValue() == null) ||
          (x.getValue() != null && x.getValue().equals(y.getValue()))) {
        if (x.children().size() == y.children().size()) {
          for (int i = 0; i < x.children().size(); i++) {
            if (!deepEquals(x.children().get(i), y.children().get(i)))
              return false;
          }
          return true;
        }
      }
    }
    return false;
  }

  private static Constructor<? extends ParseTreeNode> findCloneCtor(
      Class<? extends ParseTreeNode> clazz) {
    for (Constructor ctor : clazz.getDeclaredConstructors()) {
      if (ctor.getParameterTypes().length != 2) continue;
      if (ctor.getParameterTypes()[1].isAssignableFrom(List.class)) {
        return (Constructor<? extends ParseTreeNode>)ctor;
      }
    }
    throw new RuntimeException("Cannot find clone ctor for node " + clazz);
  }

  private static String getCtorErrorMessage(
      Constructor<? extends ParseTreeNode> ctor,
      Object value,
      Iterable<? extends ParseTreeNode> children) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error calling ctor ").append(ctor.toString());
    sb.append(" with value = ").append(value);
    sb.append(" (").append(value == null ? "" : value.getClass()).append(")");
    sb.append(" with children = ").append(children);
    sb.append(" (").append(children == null ? "" : children.getClass()).append(")");
    return sb.toString();
  }
}
