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
 * @fileoverview decorates definitions from <tt>cajita.js</tt> to collect
 * debugging information at runtime.
 *
 * <h3>Usage</h3>
 * Currently a container loads cajita.js {@script <script src="cajita.js"/>}
 * to provide library support for cajoled code.
 * Containers will likely provide a sandbox to let developers to test their
 * gadgets.  This container can include both cajita.js and this file:<pre>
 *   <script src="cajita.js"/>
 *   <script src="cajita-debugmode.js"/>
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

  function logit(str) {
    Packages.java.lang.System.err.println(str);
  }

  var orig = cajita.copy(___);

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

  // Disable fast-tracking so that we receive notification on every operation
  // that modifies the stack.
  function noop(obj, name) {}
  function requireNotFrozen(obj, name) {
    if (orig.isFrozen(obj)) {
      cajita.fail("Can't set .", name, ' on frozen (', obj, ')');
    }
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

  var stackSealer = cajita.makeSealerUnsealerPair();
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
    return stackInvalid ? void 0 : stackSealer(cajita.freeze(stack.slice(0)));
  }

  function pushFrame(stackFrame) {
    stack.push(stackFrame);
    return stackFrame;
  }

  function popFrame(stackFrame) {
    // Check that pushed item is at the top, and if so pop, invalidate
    // otherwise.
    var top = stack.length - 1;
    if (stackFrame === stack[top]) {
      stack.length = top;
    } else {
      stackInvalid = true;
    }
  }

  function rethrowWith(ex, stackFrame) {
    pushFrame(stackFrame);
    attachCajaStack(ex);
    popFrame(stackFrame);
    throw ex;
  }

  function requireObject(obj, callerIdx) {
    switch (typeof obj) {
      case 'object':
        if (obj !== null) { return obj; }
        break;
      case 'function':
        return obj;
    }
    rethrowWith(new Error('Expected object not ' + obj),
                this.debugSymbols_[callerIdx]);
  }


  // Make sure that object accessors and mutators update the stack, and others
  // that can fail if obj is undefined or name is denied.

  /**
   * Associate the cajita call stack with an Error object if there is none there
   * already.
   */
  function attachCajaStack(error) {
    // Associate the current stack with ex if it is an Error.
    if (error instanceof Error && !error.cajitaStack___) {
      error.cajitaStack___ = getCallerStack();
    }
  }

  function errorDecorator(fn) {
    var arity = fn.length;
    return function (var_args) {
      try {
        return fn.apply(this, arguments);
      } catch (ex) {
        rethrowWith(ex, this.debugSymbols_[arguments[arity]]);
      }
    };
  }

  function callPub(obj, name, args, callerIdx) {
    var stackFrame = pushFrame(this.debugSymbols_[callerIdx]);
    try {
      try {
        return orig.callPub.apply(this, arguments);
      } catch (ex) {
        attachCajaStack(ex);
        throw ex;
      }
    } finally {
      popFrame(stackFrame);
    }
  }

  function asSimpleFunc(fun, callerIdx) {
    return makeWrapper(fun, undefined, 'asSimpleFunc', this.debugSymbols_[callerIdx]);
  }

  function simpleFunc(fun, name, callerIdx) {
    return makeWrapper(fun, name, 'simpleFunc', this.debugSymbols_[callerIdx]);
  }

  function simpleFrozenFunc(fun, name, callerIdx) {
    return makeWrapper(fun, name, 'simpleFrozenFunc', this.debugSymbols_[callerIdx]);
  }

  function construct(fun, args, callerIdx) {
    fun = fun.originalFunction___;

    var stackFrame = pushFrame(this.debugSymbols_[callerIdx]);
    try {
      try {
        return orig.construct(fun, args);
      } catch (ex) {
        attachCajaStack(ex);
        throw ex;
      }
    } finally {
      popFrame(stackFrame);
    }
  }

  /**
   * Return a function of the same kind (simple/method/ctor) as fun, but
   * making sure that any Error thrown because fun is not of the required kind
   * has a stack attached.
   *
   * @param {Function} fun
   * @param {string} conditionName name of the condition that checks
   *     that fun is of the right kind.  E.g. 'asSimpleFunc'
   * @param stackFrame of the call of fun in original source code.
   * @return {Function} applies fun, but attaches a cajita stack trace to any
   *     Error raised by fun.
   */
  function makeWrapper(fun, opt_name, conditionName, stackFrame) {
    try {
      fun = orig[conditionName](fun, opt_name);
      if (!fun) { return fun; }
    } catch (ex) {
      rethrowWith(ex, stackFrame);
    }
    function wrapper() {
      pushFrame(stackFrame);
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

    // fun might pass asCtor because it is simple.  Copy only the bits onto
    // wrapper that allow it to survive similar checks.
    if (fun.SIMPLEFUNC___) {
      wrapper.SIMPLEFUNC___ = true;
    } else if (fun.XO4A___) {
      wrapper.XO4A___ = true;
    }

    return orig.primFreeze(wrapper);
  }

  function tameException(ex) {
    var ex = orig.tameException(ex);
    // Make sure that tamed Errors propogate the cajitaStack___,
    // so that an exception can be rethrown.
    // We need to make sure tameException has the property that
    //     try { f(); } catch (ex) { throw ex; }
    // doesn't make ex unavailable to code with access to the unsealer.
    if (ex && (typeof ex) === 'object' && !ex.cajitaStack___) {
      ex.cajitaStack___ = getCallerStack();
    }
    return ex;
  }

  // Extend to output the source file position with the message.
  var origLog = cajita.log;
  function log(msg) {
    if (!stackInvalid && stack.length > 0) {
      msg = stack[stack.length - 1] + ': ' + msg;
    }
    return origLog(msg);
  }

  // Dump stack traces during loading to the console.
  function loadModule(module) {
    cajita.log('starting loadModule');
    try {
      orig.loadModule(module);
      cajita.log('done loadModule');
    } catch (ex) {
      if ('undefined' !== typeof console) {
        if (ex && ex.cajitaStack___) {
          var stack = unsealCallerStack(ex.cajitaStack___);
          if (stack) {
            console.group(
                ex.message + ' @ ' + ex.fileName + ':' + ex.lineNumber);
            console.error(stack.join('\n'));
            console.groupEnd();
          }
        } else {
          console.log(ex.stack.match(/@\S*:\d+(?:\n|$)/g).join('\n\n'));
        }
      }
      throw ex;
    }
  }

  /**
   * Receive debugSymbols during module initialization, and set up the debugging
   * hooks for this module's version of ___.
   */
  function useDebugSymbols(var_args) {
    var newDebugSymbols = arguments;
    cajita.log('using debug symbols');
    if (!cajita.isJSONContainer(this)) { cajita.fail('called on bad ___'); }
    if (this.debugSymbols_ !== void 0) {
      cajita.fail('___ reused with different debug symbols');
    }
    // Unpack the debugging symbols.

    // Per DebuggingSymbolsStage:
    //   The debugSymbols are a list of the form
    //       '[' <FilePosition> (',' <prefixLength> ',' <dFilePosition}>)* ']'
    //   where the dFilePositions are turned into FilePositions by
    //   prepending them with the first prefixLength characters of the
    //   preceding FilePosition.
    var debugSymbols = [];
    if (newDebugSymbols.length) {
      var last = newDebugSymbols[0];
      debugSymbols.push(last);
      for (var i = 1, n = newDebugSymbols.length; i < n; i += 2) {
        last = last.substring(0, newDebugSymbols[i]) + newDebugSymbols[i + 1];
        debugSymbols.push(last);
      }
    }
    this.debugSymbols_ = debugSymbols;

    // Disable fast-tracking
    override(
        this,
        [
         'grantRead', noop,
         'grantEnumOnly', noop,
         'grantCall', noop,
         'grantSet', requireNotFrozen,
         'grantDelete', requireNotFrozen
        ], 0);

    // Maintain stack through calls, and attach a stack when an operation fails.
    override(
        this,
        [
         'callPub', callPub,
         'asSimpleFunc', asSimpleFunc,
         'simpleFunc', simpleFunc,
         'simpleFrozenFunc', simpleFrozenFunc
        ], 1);
    override(
        this,
        [
         'canEnum', errorDecorator(orig.canEnum),
         'deletePub', errorDecorator(orig.deletePub),
         'readPub', errorDecorator(orig.readPub),
         'inPub', errorDecorator(orig.inPub),
         'setPub', errorDecorator(orig.setPub),
         'construct', construct
        ], null);
    // Make sure that tamed exceptions propagate stacktraces
    override(this, ['tameException', tameException], 0);
  }

  // Export useDebugSymbols and the rest of the debug API so that modules
  // compiled with debugging information can setup their ___, and so that
  // privileged exception handlers
  mixin(
      ___,
      [
       'getCallerStack', getCallerStack,
       'requireObject', requireObject,
       'startCallerStack', startCallerStack,
       'unsealCallerStack', unsealCallerStack,
       'useDebugSymbols', useDebugSymbols
      ]);

  // Include the top stack frame in log messages.
  override(cajita, ['log', log], 0);
  // Dump stack traces during loading to the console.
  override(___, ['loadModule', loadModule], 0);

  startCallerStack();
})();
