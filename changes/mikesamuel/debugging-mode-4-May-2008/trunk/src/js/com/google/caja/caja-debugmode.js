// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * @fileoverview decorates definitions from <tt>caja.js</tt> to collect
 * debugging information at runtime.
 *
 * <h3>Usage</h3>
 * Currently a container loads caja.js {@script <script src="caja.js"/>}
 * to provide library support for cajoled code.
 * Containers will likely provide a sandbox to let developers to test their
 * gadgets.  This container can include both caja.js and this file:<pre>
 *   <script src="caja.js"/>
 *   <script src="caja-debugmode.js"/>
 * </pre>.
 * TODO(mikesamuel): how to collect logging.
 *
 * <h3>Changes</h3>
 * This extends the {@code ___} object to maintain a stack of call sites.
 * It adds an operation, {@code ___.getCallerStack}, which returns the caller
 * stack, including special stack frames for delete/get/read/set operations.
 * <p>
 * In debug mode, the normal fasttracking is turned off, so all property
 * accessing/modifying operations go through {@code ___} methods.
 *
 * <h3>Interleaved operations</h3>
 * Interleaved operations, as from an XMLHTTPRequest's onReadyStateChange
 * handler or a cross-frame event, can corrupt the stack.  We try to detect
 * these on popping a stack frame, and mark the stack invalid.
 * <p>
 * The {@code ___.startCallerStack} can be invoked by container event/timeout
 * handling to initialize a stack.  This will throw out an invalid stack, and
 * start a new stack.  If the stack is not empty and not marked invalid, it will
 * be marked invalid.
 *
 * @author mikesamuel@gmail.com
 */

(function () {
  var orig = caja.copy(___);

  function mixin(obj, members) {
    for (var i = 0, n = members.length; i < n; i += 2) {
      var k = members[i], v = members[i + 1];
      if (k in obj) { throw new Error('overriding ' + k + ' in ' + v); }
      obj[k] = v;
    }
  }

  function override(obj, members, extraOptionalParams) {
    for (var i = 0, n = members.length; i < n; i += 2) {
      var k = members[i], v = members[i + 1];
      if (!(k in obj)) { throw new Error('can\'t override ' + k + ' in ' + v); }
      if (extraOptionalParams !== null
          && obj[k].length + extraOptionalParams !== v.length) {
        throw new Error('overriding ' + k + ' with a different signature');
      }
      obj[k] = v;
    }
  }

  // Disabled fast-tracking so that we receive notification on every operation
  // that modifies the stack.
  function noop(obj, name) {}
  function requireNotFrozen(obj, name) {
    if (orig.isFrozen(obj)) {
      caja.fail("Can't set .", name, ' on frozen (', obj, ')');
    }
  }
  override(
      ___,
      [
        'allowRead', noop,
        'allowEnum', noop,
        'allowCall', noop,
        'allowSet', requireNotFrozen,
        'allowDelete', requireNotFrozen
      ], 0);


  // Receive debugSymbols during module initialization.
  var debugSymbols = null;
  function useDebugSymbols(newDebugSymbols) {
    if (debugSymbols !== null) {
      caja.fail('___ reused with different debug symbols');
    }
    // Per DebuggingSymbolsStage:
    //   The debugSymbols are a list of the form
    //       '[' <FilePosition> (',' <prefixLength> ',' <dFilePosition}>)* ']'
    //   where the dFilePositions are turned into FilePositions by
    //   prepending them with the first prefixLength characters of the
    //   preceding FilePosition.
    debugSymbols = [];
    if (newDebugSymbols.length) {
      var last = newDebugSymbols[0];
      debugSymbols.push(last);
      for (var i = 1, n = newDebugSymbols.length; i < n; i += 2) {
        last = last.substring(0, newDebugSymbols[i]) + newDebugSymbols[i + 1];
        debugSymbols.push(last);
      }
    }
    console.log('using symbols ' + debugSymbols);
  }

  // Define the stack, and accessors
  var stack;
  var stackInvalid;

  /**
   * Resets the caller stack to an empty state.
   * Called by event handler wrapper to bring the stack into a known good state
   * and similarly by {@code setTimeout}/{@code setInterval} wrappers
   * to associate the call stack at the time the timeout or interval was
   * registered for execution.
   * @param opt_precedingCallStack a sealed call stack.
   */
  function startCallerStack(opt_precedingCallStack) {
    stack = [];
    stackInvalid = false;
  }

  var stackSealer = caja.makeSealerUnsealerPair();
  /**
   * Unseals a sealed call stack.  Not available to cajoled code.
   */
  var unsealCallerStack = stackSealer.unseal;
  stackSealer = stackSealer.seal;

  /**
   * Returns a sealed call stack, that will not be mutated by subsequent
   * changes.
   */
  function getCallerStack() {
    return stackInvalid ? undefined : stackSealer(caja.freeze(stack.slice(0)));
  }

  function pushFrame(callerIdx) {
    var stackFrame = debugSymbols[callerIdx];
    console.log('pushing ' + stackFrame + ' for idx ' + callerIdx);
    stack.push(stackFrame);
    return stackFrame;
  }

  function popFrame(stackFrame) {
    // Check that pushed item is at the top, and if so pop, invalidate
    // otherwise.
    var top = stack.length - 1;
    if (stackFrame === stack[top]) {
      stack.length = top;
      console.log('popped ' + stackFrame);
    } else {
      stackInvalid = true;
      console.warn('stack is invalid');
    }
  }

  function rethrowWith(ex, callerIdx) {
    var stackFrame = pushFrame(callerIdx);
    attachCajaStack(ex);
    popFrame(stackFrame);
    throw ex;
  }

  mixin(
      ___,
      [
        'getCallerStack', getCallerStack,
        'startCallerStack', startCallerStack,
        'unsealCallerStack', unsealCallerStack,
        'useDebugSymbols', useDebugSymbols
      ]);



  // Make sure that object accessors and mutators update the stack, and others
  // that can fail if obj is undefined or name is denied.

  /**
   * Associate the caja call stack with an Error object if there is none there
   * already.
   */
  function attachCajaStack(error) {
    // Associate the current stack with ex if it is an Error.
    if (error instanceof Error && !error.cajaStack___) {
      error.cajaStack___ = getCallerStack();
    }
  }

  function errorDecorator(fn) {
    var arity = fn.length;
    return function (var_args) {
      try {
        return fn.apply(this, arguments);
      } catch (ex) {
        rethrowWith(ex, arguments[arity]);
      }
    };
  }

  function callProp(obj, name, args, callerIdx) {
    var stackFrame = pushFrame(callerIdx);
    try {
      try {
        return orig.callProp.apply(this, arguments);
      } catch (ex) {
        attachCajaStack(ex);
        throw ex;
      }
    } finally {
      popFrame(stackFrame);
    }
  }

  function callPub(obj, name, args, callerIdx) {
    var stackFrame = pushFrame(callerIdx);
    try {
      try {
        return orig.callProp.apply(this, arguments);
      } catch (ex) {
        attachCajaStack(ex);
        throw ex;
      }
    } finally {
      popFrame(stackFrame);
    }
  }

  function asSimpleFunc(fun, callerIdx) {
    try {
      fun = orig.asSimpleFunc(fun);
    } catch (ex) {
      rethrowWith(ex, callerIdx);
    }
    function wrapper() {
      var stackFrame = pushFrame(callerIdx);
      try {
        try {
          return fun.apply(this, arguments);
        } catch (ex) {
          attachCajaStack(ex);
          throw ex;
        }
      } finally {
        popFrame(stackFrame);
      }
    }
    return orig.simpleFunc(wrapper);
  }

  
  function tameException(ex) {
    var ex = orig.tameException(ex);
    // Make sure that tamed Errors propogate the cajaStack___,
    // so that an exception can be rethrown.
    // We need to make sure tameException has the property that
    //     try { f(); } catch (ex) { throw ex; }
    // doesn't make ex unavailable to code with access to the unsealer.
    if (ex && (typeof ex) === 'object' && !ex.cajaStack___) {
      ex.cajaStack___ = getCallerStack();
    }
    return ex;
  }

  override(___, ['callPub', callPub, 'callProp', callProp, 'asSimpleFunc', asSimpleFunc], 1);
  override(___, ['tameException', tameException], 0);

  override(
      ___,
      [
        'canEnum', errorDecorator(orig.canEnum),
        'deletePub', errorDecorator(orig.deletePub),
        'deleteProp', errorDecorator(orig.deleteProp),
        'readPub', errorDecorator(orig.readPub),
        'readProp', errorDecorator(orig.readProp),
        'setPub', errorDecorator(orig.setPub),
        'setProp', errorDecorator(orig.setProp)
      ], null);

  startCallerStack();
})();
