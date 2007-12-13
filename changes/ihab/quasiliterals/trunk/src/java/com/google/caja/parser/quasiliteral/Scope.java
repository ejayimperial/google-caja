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
import com.google.caja.parser.Visitor;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.FunctionDeclaration;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Reference;
import com.google.caja.plugin.ReservedNames;

import java.util.HashMap;
import java.util.Map;

/**
 * A scope analysis of a {@link com.google.caja.parser.ParseTreeNode}.
 * 
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class Scope {
  private enum LocalType {
    FUNCTION,
    DATA
  }
  
  private Scope parent;
  private ParseTreeNode block;
  private boolean containsThis;
  private boolean containsArguments;
  private Map<String, LocalType> locals = new HashMap<String, LocalType>();

  public Scope(Scope parent, ParseTreeNode block) {
    this.parent = parent;
    this.block = block;
    walkBlock();
  }

  public Scope(ParseTreeNode block) {
    this(null, block);
  }
  
  /**
   * Is a given node in this scope evaluated for its value?
   *
   * @param n a ParseTreeNode
   * @return whether the node is used for its value.
   */
  public boolean isForValue(ParseTreeNode n) {
    // TODO(ihab.awad): Where does this go and how?
    return true;
  }

  /**
   * Does this scope mention "this" freely?
   * <p>
   * If "this" is only mentioned within a function definition within
   * this scope, then the result is <tt>false</tt>, since that "this"
   * isn't a free occurrence.
   *
   * @return whether this block has a free "this".
   */
  public boolean hasFreeThis() {
    return containsThis;
  }

  /**
   * Does this scope mention "arguments" freely?
   * <p>
   * If "arguments" is only mentioned within a function definition
   * within this scope, then the result is <tt>false</tt>, since that
   * "arguments" isn't a free occurrence.
   *
   * @return whether this block has a free "arguments".
   */
  public boolean hasFreeArguments() {
    return containsArguments;
  }

  /**
   * @see #isFunction(String)
   */
  public boolean isFunction(ParseTreeNode reference) {
    return isFunction(getNodeName(reference));
  }

  /**
   * @see #isDefined(String)
   */
  public boolean isDefined(ParseTreeNode reference) {
    return isDefined(getNodeName(reference));
  }

  /**
   * @see #isGlobal(String)
   */
  public boolean isGlobal(ParseTreeNode reference) {
    return isGlobal(getNodeName(reference));
  }

  /**
   * @see #isConstructor(String)
   */
  public boolean isConstructor(ParseTreeNode reference) {
    return isConstructor(getNodeName(reference));
  }

  /**
   * In this scope or some enclosing scope, is a given name
   * defined as a function?
   *
   * @param name an identifier.
   * @return whether 'name' is defined as a function within this
   * scope. If 'name' is not defined, return false.
   */
  public boolean isFunction(String name) {
    return getType(name) == LocalType.FUNCTION;
  }

  /**
   * Does this scope or some enclosing scope define a name?
   *
   * @param name an identifier.
   * @return whether 'name' is defined within this scope.
   */
  public boolean isDefined(String name) {
    return getType(name) != null;
  }

  /**
   * Is a given symbol global?
   *
   * @param name an identifier.
   * @return whether 'name' is a defined global variable.
   */
  public boolean isGlobal(String name) {
    return parent == null ?
        locals.containsKey(name) :
        parent.isGlobal(name);
  }

  public boolean isConstructor(String name) {
    // TODO(ihab.awad): Implement this
    return false;
  }

  private LocalType getType(String name) {
    return locals.containsKey(name) ?
        locals.get(name) :
        parent != null ? parent.getType(name) : null;
  }

  private String getNodeName(ParseTreeNode n) {
    if (n instanceof Identifier) {
      return ((Identifier)n).getValue();
    }
    if (n instanceof Reference) {
      return ((Reference)n).getIdentifierName();
    }
    throw new RuntimeException("Not a symbol node: " + n);
  }

  private void walkBlock() {
    // TODO(ihab.awad): parentify() fixes missing "next" pointer problems; revisit
    // when new parent-less rewrite of parse tree is synced.
    ((AbstractParseTreeNode)block).parentify();
    
    block.acceptPreOrder(new Visitor() {
      public boolean visit(ParseTreeNode node) {
        if (node instanceof FunctionConstructor) {
          return false;
        }
        if (node instanceof Declaration) {
          LocalType type = node instanceof FunctionDeclaration ?
              LocalType.FUNCTION : LocalType.DATA;
          String name = ((Declaration)node).getIdentifierName();
          if (locals.containsKey(name) && locals.get(name) != type) {
            throw new RuntimeException("Duplicate definition of local: " + name);
          }
          locals.put(name, type);
        }
        if (node instanceof Reference) {
          String name = ((Reference)node).getIdentifierName();
          if (ReservedNames.ARGUMENTS.equals(name)) { containsArguments = true; }
          if (ReservedNames.THIS.equals(name)) { containsThis = true; }
        }
        return true;
      }
    });
  }
}