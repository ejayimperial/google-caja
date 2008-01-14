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

import com.google.caja.lexer.FilePosition;
import com.google.caja.parser.AncestorChain;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.Visitor;
import com.google.caja.parser.js.CatchStmt;
import com.google.caja.parser.js.Declaration;
import com.google.caja.parser.js.FunctionConstructor;
import com.google.caja.parser.js.FunctionDeclaration;
import com.google.caja.parser.js.Identifier;
import com.google.caja.parser.js.Reference;
import com.google.caja.plugin.ReservedNames;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.MessageType;
import com.google.caja.util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A scope analysis of a {@link com.google.caja.parser.ParseTreeNode}.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class Scope {
  private enum LocalType {
    /**
     * A named function value, visible only within its own body.
     * Examples: "foo" in the following --
     *
     * <pre>
     * var y = function foo() { };
     * zip(function foo() { });
     * </pre>
     */
    FUNCTION(),

    /**
     * A function declaration, visible in its enclosing scope.
     * Example: "foo" in the following --
     *
     * <pre>
     * function foo() { }
     * </pre>
     */
    DECLARED_FUNCTION(FUNCTION),

    /**
     * A constructor declaration, i.e., one which mentions 'this' in its body,
     * Example: "foo" in the following --
     *
     * <pre>
     * function foo() { this.x = 3; }
     * </pre>
     */
    CONSTRUCTOR(DECLARED_FUNCTION),

    /**
     * A variable containing arbitrary data (including functions).
     * Examples: "x", "y", "z" and "t" in the following --
     *
     * <pre>
     * var x;
     * var y = 3;
     * var z = function() { };
     * var t = function foo() { };
     * </pre>
     */
    DATA(),

    /**
     * A variable defined in a catch block.
     * Example: "foo" in the following --
     *
     * <pre>
     * catch (foo) { this.x = 3; }
     * </pre>
     */
    CAUGHT_EXCEPTION,
    ;

    private final HashSet<LocalType> implications = new HashSet<LocalType>();

    private LocalType(LocalType... implications) {
      this.implications.add(this);
      for (LocalType implication : implications) {
        this.implications.addAll(implication.implications);
      }
    }

    public boolean implies(LocalType type) {
      return implications.contains(type);
    }
  }

  /** False if this is a pseudo scope for a catch block. */
  private final Scope parent;
  private final MessageQueue mq;
  private boolean isFullScope;
  private boolean containsThis;
  private boolean containsArguments;
  private final Map<String, Pair<LocalType, FilePosition>> locals
      = new HashMap<String, Pair<LocalType, FilePosition>>();

  // TODO(ihab.awad): Create scope from static methods.
  // TODO(ihab.awad): Take a message queue for adding error messages.

  /**
   * Create a root scope for a program.
   *
   * @param root the function constituting the nested scope.
   * @param mq a message queue that will receive messages about masked and
   *   redefined variables.
   */
  public Scope(ParseTreeNode root, MessageQueue mq) {
    assert mq != null;
    this.parent = null;
    this.mq = mq;
    processRoot(root);
  }

  /**
   * Create a nested scope for a function in a program.
   *
   * @param parent the parent scope of the function.
   * @param root the function constituting the nested scope.
   */
  public Scope(Scope parent, ParseTreeNode root) {
    this.parent = parent;
    this.mq = parent.mq;
    processRoot(root);
  }

  private void processRoot(ParseTreeNode root) {
    this.isFullScope = !(root instanceof CatchStmt);

    if (root instanceof FunctionConstructor) {
      FunctionConstructor ctor = (FunctionConstructor) root;

      // A function's name is bound to it in its body.
      // In
      //    var g = function f() { return f; };
      // the following is true
      //    typeof f === 'undefined' && g === g()
      String name = ctor.getIdentifierName();
      if (name != null) {
        // TODO(mikesamuel): the rewrite rules incorrectly drop the name of
        // function constructors, so we hack this to not add function names to
        // scope if its in the global scope so that isGlobal returns the
        // correct value.
        if (parent.getType(name) != Scope.LocalType.DECLARED_FUNCTION) {
          declare(ctor.getIdentifier(), Scope.LocalType.FUNCTION);
        }
      }

      for (ParseTreeNode n : ctor.getParams()) {
        walkBlock(n);
      }
      walkBlock(ctor.getBody());
    } else if (root instanceof CatchStmt) {
      CatchStmt catchStmt = (CatchStmt) root;
      declare(catchStmt.getException().getIdentifier(),
              LocalType.CAUGHT_EXCEPTION);

      // vars declared in the catch statements body should be declared in the
      // closest real scope.
      Scope realScope = this;
      while (!(realScope.parent == null || realScope.isFullScope)) {
        realScope = realScope.parent;
      }
      realScope.walkBlock(catchStmt.getBody());
    } else {
      walkBlock(root);
    }
  }

  /**
   * The parent of this scope.
   *
   * @return a {@code Scope} or {@code null}.
   */
  public Scope getParent() {
    return parent;
  }

  /**
   * Does this scope mention "this" freely?
   *
   * <p>If "this" is only mentioned within a function definition within
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
   *
   * <p>If "arguments" is only mentioned within a function definition
   * within this scope, then the result is <tt>false</tt>, since that
   * "arguments" isn't a free occurrence.
   *
   * @return whether this block has a free "arguments".
   */
  public boolean hasFreeArguments() {
    return containsArguments;
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
   * In this scope or some enclosing scope, is a given name
   * defined as a function?
   *
   * @param name an identifier.
   * @return whether 'name' is defined as a function within this
   *   scope. If 'name' is not defined, return false.
   */
  public boolean isFunction(String name) {
    return isDefined(name) && getType(name).implies(LocalType.FUNCTION);
  }

  /**
   * True if name is the name of the variable that a {@code catch} block's
   * exception is bound to.
   */
  public boolean isException(String name) {
    LocalType t = getType(name);
    return t == LocalType.CAUGHT_EXCEPTION;
  }

  /**
   * In this scope or some enclosing scope, is a given name
   * defined as a declared function?
   *
   * @param name an identifier.
   * @return whether 'name' is defined as a declared function within this
   *   scope. If 'name' is not defined, return false.
   */
  public boolean isDeclaredFunction(String name) {
    return isDefined(name) && getType(name).implies(LocalType.DECLARED_FUNCTION);
  }

  /**
   * In this scope or some enclosing scope, is a given name defined
   * as a constructor?
   *
   * @param name an identifier.
   * @return whether 'name' is defined as a constructor within this
   * scope. If 'name' is not defined, return false.
   */
  public boolean isConstructor(String name) {
    return isDefined(name) && getType(name).implies(LocalType.CONSTRUCTOR);
  }

  /**
   * Is a given symbol global?
   *
   * @param name an identifier.
   * @return whether 'name' is a defined global variable.
   */
  public boolean isGlobal(String name) {
    return
        parent == null ||
        (!locals.containsKey(name) && parent.isGlobal(name));
  }

  private LocalType getType(String name) {
    Scope current = this;
    do {
      Pair<LocalType, FilePosition> symbolDefinition = current.locals.get(name);
      if (symbolDefinition != null) { return symbolDefinition.a; }
      current = current.parent;
    } while (current != null);
    return null;      
  }

  private LocalType computeDeclarationType(Declaration decl) {
    if (decl instanceof FunctionDeclaration) {
      Scope s2 = new Scope(this, ((FunctionDeclaration)decl).getInitializer());
      return s2.hasFreeThis() ? LocalType.CONSTRUCTOR : LocalType.DECLARED_FUNCTION;
    }
    return LocalType.DATA;
  }

  private void walkBlock(ParseTreeNode root) {
    root.acceptPreOrder(new Visitor() {
      public boolean visit(AncestorChain<?> chain) {
        if (chain.node instanceof FunctionConstructor
            || chain.node instanceof CatchStmt) {
          return false;
        } else if (chain.node instanceof Declaration) {
          Declaration decl = (Declaration) chain.node;
          declare(decl.getIdentifier(), computeDeclarationType(decl));
        } else if (chain.node instanceof Reference) {
          String name = ((Reference)chain.node).getIdentifierName();
          if (ReservedNames.ARGUMENTS.equals(name)) {
            containsArguments = true;
          }
          if (ReservedNames.THIS.equals(name)) { containsThis = true; }
        }
        return true;
      }
    },
    null);
  }

  /**
   * Add a symbol to the symbol table for this scope with the given type.
   * If this symbol redefines another symbol with a different type, or masks
   * an exception, then an error will be added to this Scope's MessageQueue.
   */
  private void declare(Identifier ident, LocalType type) {
    String name = ident.getName();
    Pair<LocalType, FilePosition> oldDefinition = locals.get(name);
    if (oldDefinition != null) {
      LocalType oldType = oldDefinition.a;
      if (oldType != type) {
        // This is an error because redeclaring a function declaration as a
        // var because function declaration hoisting makes analysis hard.
        mq.getMessages().add(new Message(
            MessageType.SYMBOL_REDEFINED,
            MessageLevel.ERROR,
            ident.getFilePosition(),
            MessagePart.Factory.valueOf(name),
            oldDefinition.b));
      }
    }
    for (Scope ancestor = parent; ancestor != null;
         ancestor = ancestor.parent) {
      Pair<LocalType, FilePosition> maskedDefinition
          = ancestor.locals.get(name);
      if (maskedDefinition == null) { continue; }

      LocalType maskedType = maskedDefinition.a;
      if (maskedType != type
          // A function constructor's own name is visible, and that should mask
          // the declaration of the same name in the parent scope.
          && (maskedType != LocalType.DECLARED_FUNCTION
              || type != LocalType.FUNCTION
              || ancestor != parent)) {
        // Since different interpreters disagree about how exception
        // declarations affect local variable declarations, we need to
        // prevent exceptions masking locals and vice-versa.
        MessageLevel level = (
            (type == LocalType.CAUGHT_EXCEPTION
             || maskedType == LocalType.CAUGHT_EXCEPTION)
            ? MessageLevel.ERROR
            : MessageLevel.LINT);
        if (ident.getFilePosition() != null) {  // HACK(msamuel)
          mq.getMessages().add(new Message(
              MessageType.MASKING_SYMBOL,
              level,
              ident.getFilePosition(),
              MessagePart.Factory.valueOf(name),
              maskedDefinition.b));
        }
      }
      break;
    }

    locals.put(name, Pair.pair(type, ident.getFilePosition()));
  }
}
