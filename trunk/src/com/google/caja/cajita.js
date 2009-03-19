// Copyright (C) 2007 Google Inc.
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
 * @fileoverview the Cajita runtime library.
 * It is written in Javascript, not Cajita, and would be rejected by the Cajita
 * translator. This module exports two globals:<ol>
 * <li>"___" for use by the output of the Cajita translator and by some
 *     other untranslated Javascript code.
 * <li>"cajita" providing some common services to the Cajita programmer.
 * </ol>
 * @author erights@gmail.com
 * @requires console, this
 * @provides ___, arraySlice, cajita, dateToISOString, funcBind
 * @overrides Array, Boolean, Date, Function, Number, Object, RegExp, String
 * @overrides Error, EvalError, RangeError, ReferenceError, SyntaxError,
 *   TypeError, URIError
 */

// TODO(erights): All code text in comments should be enclosed in
// {@code ...}.

// TODO(ihab.awad): Missing tests in CajitaTest.java for the functionality
// in this file.


// Add a tag to whitelisted builtin constructors so we can check the class
// cross-frame. Note that Function is not on the list.

Array.typeTag___ = 'Array';
Object.typeTag___ = 'Object';
String.typeTag___ = 'String';
Boolean.typeTag___ = 'Boolean';
Number.typeTag___ = 'Number';
Date.typeTag___ = 'Date';
RegExp.typeTag___ = 'RegExp';
Error.typeTag___ = 'Error';
EvalError.typeTag___ = 'EvalError';
RangeError.typeTag___ = 'RangeError';
ReferenceError.typeTag___ = 'ReferenceError';
SyntaxError.typeTag___ = 'SyntaxError';
TypeError.typeTag___ = 'TypeError';
URIError.typeTag___ = 'URIError';

Object.prototype.proto___ = null;

////////////////////////////////////////////////////////////////////////
// Cajita adds the following common Javascript extensions to ES3
// TODO(erights): Move such extensions to a separate extensions.js.
////////////////////////////////////////////////////////////////////////

/** In anticipation of ES3.1 */
function dateToISOString() {
  function f(n) {
    return n < 10 ? '0' + n : n;
  }
  return (this.getUTCFullYear()     + '-' +
          f(this.getUTCMonth() + 1) + '-' +
          f(this.getUTCDate())      + 'T' +
          f(this.getUTCHours())     + ':' +
          f(this.getUTCMinutes())   + ':' +
          f(this.getUTCSeconds())   + 'Z');
}
if (Date.prototype.toISOString === void 0) {
  Date.prototype.toISOString = dateToISOString;
}

if (Date.prototype.toJSON === void 0) {
  /** In anticipation of ES3.1 */
  Date.prototype.toJSON = Date.prototype.toISOString;
}

/** In anticipation of ES4, and because it's useful. */
function arraySlice(self, start, end) {
  return Array.prototype.slice.call(self, start || 0, end || self.length);
}
if (Array.slice === void 0) {
  Array.slice = arraySlice;
}


/**
 * In anticipation of ES3.1.
 * <p>
 * Bind this function to <tt>self</tt>, which will serve
 * as the value of <tt>this</tt> during invocation. Curry on a
 * partial set of arguments in <tt>var_args</tt>. Return the curried
 * result as a new function object.
 * <p>
 * Note: Like the built-in Function.prototype.call and
 * Function.prototype.apply, this one is not whitelisted. Instead,
 * it is provided as it would be in future JavaScripts without
 * special knowledge of Caja. This allows Caja code using bind() to
 * work today uncajoled as well. It also suppresses the overriding
 * of 'bind' by the 'in' test in setStatic().
 * <p>
 * Note that this is distinct from the tamed form of bind() made
 * available to Cajita code.
 */
function funcBind(self, var_args) {
  var thisFunc = this;
  var leftArgs = Array.slice(arguments, 1);
  function funcBound(var_args) {
    var args = leftArgs.concat(Array.slice(arguments, 0));
    return thisFunc.apply(self, args);
  };
  return funcBound;
}
if (Function.prototype.bind === void 0) {
  Function.prototype.bind = funcBind;
}

/**
 * In anticipation of ES 3.1.
 *
 * Parses a string of well-formed JSON text. This is a snapshot
 * of http://code.google.com/p/json-sans-eval/.
 *
 * If the input is not well-formed, then behavior is undefined, but it is
 * deterministic and is guaranteed not to modify any object other than its
 * return value.
 *
 * @param {string} json per RFC 4627
 * @return {Object|Array}
 * @author Mike Samuel <mikesamuel@gmail.com>
 */
var jsonParse = (function () {
  var number
      = '(?:-?\\b(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?\\b)';
  var oneChar = '(?:[^\\0-\\x08\\x0a-\\x1f\"\\\\]'
      + '|\\\\(?:[\"/\\\\bfnrt]|u[0-9A-Fa-f]{4}))';
  var string = '(?:\"' + oneChar + '*\")';

  // Will match a value in a well-formed JSON file.
  // If the input is not well-formed, may match strangely, but not in an unsafe
  // way.
  // Since this only matches value tokens, it does not match whitespace, colons,
  // or commas.
  var jsonToken = new RegExp(
      '(?:false|true|null|[\\{\\}\\[\\]]'
      + '|' + number
      + '|' + string
      + ')', 'g');

  // Matches escape sequences in a string literal
  var escapeSequence = new RegExp('\\\\(?:([^u])|u(.{4}))', 'g');

  // Decodes escape sequences in object literals
  var escapes = {
    '"': '"',
    '/': '/',
    '\\': '\\',
    'b': '\b',
    'f': '\f',
    'n': '\n',
    'r': '\r',
    't': '\t'
  };
  function unescapeOne(_, ch, hex) {
    return ch ? escapes[ch] : String.fromCharCode(parseInt(hex, 16));
  }

  // A non-falsy value that coerces to the empty string when used as a key.
  var EMPTY_STRING = new String('');
  var SLASH = '\\';

  // Constructor to use based on an open token.
  var firstTokenCtors = { '{': Object, '[': Array };

  return function (json) {
    // Split into tokens
    var toks = json.match(jsonToken);
    // Construct the object to return
    var result;
    var tok = toks[0];
    if ('{' === tok) {
      result = {};
    } else if ('[' === tok) {
      result = [];
    } else {
      throw new Error(tok);
    }

    // If undefined, the key in an object key/value record to use for the next
    // value parsed.
    var key;
    // Loop over remaining tokens maintaining a stack of uncompleted objects and
    // arrays.
    var stack = [result];
    for (var i = 1, n = toks.length; i < n; ++i) {
      tok = toks[i];

      var cont;
      switch (tok.charCodeAt(0)) {
        default:  // sign or digit
          cont = stack[0];
          cont[key || cont.length] = +(tok);
          key = void 0;
          break;
        case 0x22:  // '"'
          tok = tok.substring(1, tok.length - 1);
          if (tok.indexOf(SLASH) !== -1) {
            tok = tok.replace(escapeSequence, unescapeOne);
          }
          cont = stack[0];
          if (!key) {
            if (cont instanceof Array) {
              key = cont.length;
            } else { 
              // Use as key for next value seen.
              if (tok) {
                // Check that tok is a key name that can be set on a JSON
                // object.
                // The below is equivalent to calling canSetPub but we already
                // know that the object is unfrozen, and is a JSON container.

                // FIXME: this is a temporary hack until we can do this in a way
                // that allows us to use native JSON parsing.  See bug 978.
                if (tok.substring(tok.length - 2) === '__'
                    || tok === 'valueOf' || tok === 'toString') {
                  throw new Error('Invalid key ' + tok);
                }
                key = tok;
              } else {
                key = EMPTY_STRING;
              }
              break;
            }
          }
          cont[key] = tok;
          key = void 0;
          break;
        case 0x5b:  // '['
          cont = stack[0];
          stack.unshift(cont[key || cont.length] = []);
          key = void 0;
          break;
        case 0x5d:  // ']'
          stack.shift();
          break;
        case 0x66:  // 'f'
          cont = stack[0];
          cont[key || cont.length] = false;
          key = void 0;
          break;
        case 0x6e:  // 'n'
          cont = stack[0];
          cont[key || cont.length] = null;
          key = void 0;
          break;
        case 0x74:  // 't'
          cont = stack[0];
          cont[key || cont.length] = true;
          key = void 0;
          break;
        case 0x7b:  // '{'
          cont = stack[0];
          stack.unshift(cont[key || cont.length] = {});
          key = void 0;
          break;
        case 0x7d:  // '}'
          stack.shift();
          break;
      }
    }
    // Fail if we've got an uncompleted object.
    if (stack.length) { throw new Error(); }
    return result;
  };
})();

// The following may or may not exist in the browser-supplied
// global scope; declare as a 'var' to avoid errors when we
// check for its existence later.

var escape;

// cajita.js exports the following names to the Javascript global
// namespace. Cajita code can only use the "cajita" object. The "___"
// object is for use by code generated by the Cajita translator, and by
// Javascript code (such as a powerbox) in the embedding application.

var cajita;
var ___;

// Explicitly passing in the actual global object to avoid
// ReferenceErrors when referring to potentially nonexistent objects
// like HTMLDivElement.

(function(global) {

  function ToInt32(alleged_int) {
    return alleged_int >> 0;
  }

  function ToUInt32(alleged_int) {
    return alleged_int >>> 0;
  }

  /**
   * Returns the first index at which the specimen is found (by
   * "identical()") or -1 if none, starting at offset i, if present.
   * If i < 0, the offset is relative to the end of the array.
   */
  function arrayIndexOf(specimen, i) {
    var len = ToUInt32(this.length);
    i = ToInt32(i);
    if (i < 0) {
      if ((i += len) < 0) {
        i = 0;
      }
    }
    for (; i < len; ++i) {
      if (i in this && identical(this[i], specimen)) {
        return i;
      }
    }
    return -1;
  }
  Array.prototype.indexOf = arrayIndexOf;

  /**
   * Returns the last index at which the specimen is found (by
   * "identical()") or -1 if none, starting at offset i, if present.
   * If i < 0, the offset is relative to the end of the array.
   */
  function arrayLastIndexOf(specimen, i) {
    var len = ToUInt32(this.length);

    if (isNaN(i)) {
      i = len - 1;
    } else {
      i = ToInt32(i);
      if (i < 0) {
        i += len;
        if (i < 0) {
          return -1;
        }
      } else if (i >= len) {
        i = len - 1;
      }
    }

    for (; i >= 0 ; --i) {
      if (i in this && identical(this[i], specimen)) {
        return i;
      }
    }
    return -1;
  }
  Array.prototype.lastIndexOf = arrayLastIndexOf;

  ////////////////////////////////////////////////////////////////////////
  // Some regular expressions checking for specific suffixes.
  ////////////////////////////////////////////////////////////////////////

  var endsWith_canDelete___ = /_canDelete___$/;
  var endsWith_canRead___ = /_canRead___$/;
  var endsWith_canSet___ = /_canSet___$/;
  var endsWith___ = /___$/;
  var endsWith__ = /__$/;

  ////////////////////////////////////////////////////////////////////////
  // Some very basic primordial methods
  ////////////////////////////////////////////////////////////////////////

  /**
   * A reliable typeof for use by Cajita code, and by uncajoled code
   * (like parts of cajita.js) that require a reliable typeof.
   * <p>
   * Cajita specifies that <tt>typeof new RegExp("x")</tt>
   * evaluate to <tt>'object'</tt>. Unfortunately, on some of Cajita's
   * current target platforms (including at least Safari 3 and Rhino),
   * it returns <tt>'function'</tt> instead. Since the distinction
   * between functions and non-functions is crucial to Cajita, we
   * translate the Cajita <tt>typeof</tt> operator into calls to this
   * <tt>typeOf</tt> function.
   */
  function typeOf(obj) {
    var result = typeof obj;
    if (result !== 'function') { return result; }
    var ctor = obj.constructor;
    if (typeof ctor === 'function' && 
        ctor.typeTag___ === 'RegExp' && 
        obj instanceof ctor) {
      return 'object';
    }
    return 'function';
  }

  var myOriginalHOP = Object.prototype.hasOwnProperty;

  /**
   * <tt>hasOwnProp(obj, name)</tt> means what
   * <tt>obj.hasOwnProperty(name)</tt> would normally mean in an
   * unmodified Javascript system.
   */
  function hasOwnProp(obj, name) { 
    var t = typeof obj;
    if (t !== 'object' && t !== 'function') {
      // If obj is a primitive, Object(obj) still has no own properties.
      return false; 
    }
    return myOriginalHOP.call(obj, name);
  }
  
  /**
   * Are x and y not observably distinguishable?
   */
  function identical(x, y) {
    if (x === y) {
      // 0 === -0, but they are not identical
      return x !== 0 || 1/x === 1/y;
    } else {
      // NaN !== NaN, but they are identical.
      // NaNs are the only non-reflexive value, i.e., if x !== x,
      // then x is a NaN.
      return x !== x && y !== y;
    }
  }

  /**
   * Inherited by non-frozen simple functions, which freezes them and
   * installs an overriding fastpath CALL___ to themselves.
   */
  function callFault(var_args) {
    return asFunc(this).apply(USELESS, arguments);
  }
  Object.prototype.CALL___ = callFault;

  ////////////////////////////////////////////////////////////////////////
  // Diagnostics and condition enforcement
  ////////////////////////////////////////////////////////////////////////

  /**
   * The initial default logging function does nothing.
   * <p>
   * Note: JavaScript has no macros, so even in the "does nothing"
   * case, remember that the arguments are still evaluated.
   */
  function defaultLogger(str, opt_stop) {}
  var myLogFunc = frozenFunc(defaultLogger);

  /**
   * Gets the currently registered logging function.
   */
  function getLogFunc() { return myLogFunc; }

  /**
   * Register newLogFunc as the current logging function, to be called
   * by <tt>___.log(str)</tt> and <tt>___.fail(...)</tt>.
   * <p>
   * A logging function is assumed to have the signature
   * <tt>(str, opt_stop)</tt>, where<ul>
   * <li><tt>str</tt> is the diagnostic string to be logged, and
   * <li><tt>opt_stop</tt>, if present and <tt>true</tt>, indicates
   *     that normal flow control is about to be terminated by a
   *     throw. This provides the logging function the opportunity to
   *     terminate normal control flow in its own way, such as by
   *     invoking an undefined method, in order to trigger a Firebug
   *     stacktrace.
   * </ul>
   */
  function setLogFunc(newLogFunc) { myLogFunc = newLogFunc; }

  /**
   * Calls the currently registered logging function.
   */
  function log(str) { myLogFunc(String(str)); }


  /**
   * Throw, and optionally log, an error whose message is the
   * concatenation of the arguments.
   * <p>
   * The arguments are converted to strings (presumably by an
   * implicit call to ".toString()") and appended together to make
   * the message of the Error that's thrown.
   */
  function fail(var_args) {
    var message = Array.slice(arguments, 0).join('');
    myLogFunc(message, true);
    throw new Error(message);
  }

  /**
   * Like an assert that can't be turned off.
   * <p>
   * Either returns true (on success) or throws (on failure). The
   * arguments starting with <tt>var_args</tt> are converted to
   * strings and appended together to make the message of the Error
   * that's thrown.
   * <p>
   * TODO(erights) We may deprecate this in favor of <pre>
   *     test || fail(var_args...)
   * </pre> or <pre>
   *     if (!test) { fail(var_args...); }
   * </pre>
   */
  function enforce(test, var_args) {
    return test || fail.apply({}, Array.slice(arguments, 1));
  }

  /**
   * Enforces <tt>typeOf(specimen) === typename</tt>, in which case
   * specimen is returned.
   * <p>
   * If not, throws an informative TypeError
   * <p>
   * opt_name, if provided, should be a name or description of the
   * specimen used only to generate friendlier error messages.
   */
  function enforceType(specimen, typename, opt_name) {
    if (typeOf(specimen) !== typename) {
      fail('expected ', typename, ' instead of ', typeOf(specimen),
           ': ', (opt_name || specimen));
    }
    return specimen;
  }

  /**
   * Enforces that specimen is a non-negative integer within the range
   * of exactly representable consecutive integers, in which case
   * specimen is returned.
   * <p>
   * "Nat" is short for "Natural number".
   */
  function enforceNat(specimen) {
    enforceType(specimen, 'number');
    if (Math.floor(specimen) !== specimen) {
      fail('Must be integral: ', specimen);
    }
    if (specimen < 0) {
      fail('Must not be negative: ', specimen);
    }
    // Could pre-compute precision limit, but probably not faster
    // enough to be worth it.
    if (Math.floor(specimen - 1) !== specimen - 1) {
      fail('Beyond precision limit: ', specimen);
    }
    if (Math.floor(specimen - 1) >= specimen) {
      fail('Must not be infinite: ', specimen);
    }
    return specimen;
  }

  ////////////////////////////////////////////////////////////////////////
  // Privileged fault handlers
  ////////////////////////////////////////////////////////////////////////

  function debugReference(obj) {
    switch (typeOf(obj)) {
      case 'object': {
        if (obj === null) { return '<null>'; }
        var constr = directConstructor(obj);
        return '[' + ((constr && constr.name) || 'Object') + ']';
      }
      default: {
        return '(' + obj + ':' + typeOf(obj) + ')';
      }
    }
  }

  var myKeeper = {

    toString: function toString() { return '<Logging Keeper>'; },

    handleRead: function handleRead(obj, name) {
      //log('Not readable: (' + debugReference(obj) + ').' + name);
      return void 0;
    },

    handleCall: function handleCall(obj, name, args) {
      fail('Not callable: (', debugReference(obj), ').', name);
    },

    handleSet: function handleSet(obj, name, val) {
      fail('Not settable: (', debugReference(obj), ').', name);
    },

    handleDelete: function handleDelete(obj, name) {
      fail('Not deletable: (', debugReference(obj), ').', name);
    }
  };

  Object.prototype.handleRead___ = function handleRead___(name) {
    var handlerName = name + '_getter___';
    if (this[handlerName]) {
      return this[handlerName]();
    }
    return myKeeper.handleRead(this, name);
  };

  Object.prototype.handleCall___ = function handleCall___(name, args) {
    var handlerName = name + '_handler___';
    if (this[handlerName]) {
      return this[handlerName].call(this, args);
    }
    return myKeeper.handleCall(this, name, args);
  };

  Object.prototype.handleSet___ = function handleSet___(name, val) {
    var handlerName = name + '_setter___';
    if (this[handlerName]) {
      return this[handlerName](val);
    }
    return myKeeper.handleSet(this, name, val);
  };

  Object.prototype.handleDelete___ = function handleDelete___(name) {
    var handlerName = name + '_deleter___';
    if (this[handlerName]) {
      return this[handlerName]();
    }
    return myKeeper.handleDelete(this, name);
  };

  ////////////////////////////////////////////////////////////////////////
  // walking prototype chain, checking JSON containers
  ////////////////////////////////////////////////////////////////////////

  /**
   * Does str end with suffix?
   */
  function endsWith(str, suffix) {
    enforceType(str, 'string');
    enforceType(suffix, 'string');
    var d = str.length - suffix.length;
    return d >= 0 && str.lastIndexOf(suffix) === d;
  }

  /**
   * Returns the 'constructor' property of obj's prototype.
   * <p>
   * SECURITY TODO(erights): Analyze the security implications
   * of exposing this as a property of the cajita object.
   * <p>
   * By "obj's prototype", we mean the prototypical object that obj
   * most directly inherits from, not the value of its 'prototype'
   * property. We memoize the apparent prototype into 'proto___' to
   * speed up future queries. 
   * <p>
   * If obj is a function or not an object, return undefined.
   */
  function directConstructor(obj) {
    if (obj === null) { return void 0; }
    if (obj === void 0) { return void 0; }
    if (typeOf(obj) === 'function') {
      // Since functions return undefined,
      // directConstructor() doesn't provide access to the
      // forbidden Function constructor.
      return void 0;
    }
    obj = Object(obj);
    var result;
    if (myOriginalHOP.call(obj, 'proto___')) {
      var proto = obj.proto___;
      // At this point we know that (typeOf(proto) === 'object')
      if (proto === null) { return void 0; }
      result = proto.constructor;
      // rest of: if (!isPrototypical(result))
      if (result.prototype !== proto || typeOf(result) !== 'function') {
        result = directConstructor(proto);
      }
    } else {
      if (!myOriginalHOP.call(obj, 'constructor')) {
        // TODO(erights): Detect whether this is a valid constructor
        // property in the sense that result is a proper answer. If
        // not, at least give a sensible error, which will be hard to
        // phrase. 
        result = obj.constructor;
      } else {
        var oldConstr = obj.constructor;
        if (delete obj.constructor) {
          result = obj.constructor;
          obj.constructor = oldConstr;
        } else if (isPrototypical(obj)) {
          // A difficult case. In Safari, and perhaps according to
          // ES3, the prototypical object created for the default
          // value of a function's 'prototype' property has a
          // non-deletable 'constructor' property. If this is what we
          // have, then we assume it inherits directly from
          // Object.prototype, so the result should be Object.
          log('Guessing the directConstructor of : ' + obj);
          result = Object;
        } else {
          fail('Discovery of direct constructors unsupported when the ',
               'constructor property is not deletable: ',
               oldConstr); 
        }
      }
      if (typeOf(result) !== 'function' || !(obj instanceof result)) {
        fail('Discovery of direct constructors for foreign begotten ',
             'objects not implemented on this platform.\n');
      }
      if (result.prototype.constructor === result) {
        // Memoize, so it'll be faster next time.
        obj.proto___ = result.prototype;
      }
    }
    return result;
  }

  /**
   * The function category of the whitelisted global constructors
   * defined in ES is the string name of the constructor, allowing
   * isInstanceOf() to work cross-frame. Otherwise, the function
   * category of a function is just the function itself.  
   */
  function getFuncCategory(fun) {
    enforceType(fun, 'function');
    if (fun.typeTag___) { 
      return fun.typeTag___; 
    } else {
      return fun;
    }
  }

  /**
   * Is <tt>obj</tt> a direct instance of a function whose category is
   * the same as the category of <tt>ctor</tt>? 
   */
  function isDirectInstanceOf(obj, ctor) {
    var constr = directConstructor(obj);
    if (constr === void 0) { return false; }
    return getFuncCategory(constr) === getFuncCategory(ctor);
  }

  /**
   * Is <tt>obj</tt> an instance of a function whose category is
   * the same as the category of <tt>ctor</tt>? 
   */
  function isInstanceOf(obj, ctor) {
    if (obj instanceof ctor) { return true; }
    if (isDirectInstanceOf(obj, ctor)) { return true; }
    // BUG TODO(erights): walk prototype chain.
    // In the meantime, this will fail should it encounter a
    // cross-frame instance of a "subclass" of ctor.
    return false;
  }
  
  /**
   * A Record is an object whose direct constructor is Object.
   * <p>
   * These are the kinds of objects that can be expressed as 
   * an object literal ("<tt>{...}</tt>") in the JSON language.
   */
  function isRecord(obj) {
    return isDirectInstanceOf(obj, Object);
  }
  
  /**
   * An Array is an object whose direct constructor is Array.
   * <p>
   * These are the kinds of objects that can be expressed as 
   * an array literal ("<tt>[...]</tt>") in the JSON language.
   */
  function isArray(obj) {
    return isDirectInstanceOf(obj, Array);
  }

  /**
   * A JSON container is a non-prototypical object whose direct
   * constructor is Object or Array.
   * <p>
   * These are the kinds of non-primitive objects that can be
   * expressed in the JSON language.
   */
  function isJSONContainer(obj) {
    var constr = directConstructor(obj);
    if (constr === void 0) { return false; }
    var typeTag = constr.typeTag___;
    if (typeTag !== 'Object' && typeTag !== 'Array') { return false; }
    return !isPrototypical(obj);
  }

  /**
   * If obj is frozen, Cajita code cannot directly assign to
   * own properties of obj, nor directly add or delete own properties to
   * obj.
   * <p>
   * The status of being frozen is not inherited. If A inherits from
   * B (i.e., if A's prototype is B), A and B each may or may not be
   * frozen independently. (Though if B is prototypical, then it must
   * be frozen.) 
   * <p>
   * If <tt>typeof obj</tt> is neither 'object' nor 'function', then
   * it's currently considered frozen.
   */
  function isFrozen(obj) {
    if (!obj) { return true; }
    // TODO(erights): Object(<primitive>) wrappers should also be
    // considered frozen.
    if (obj.FROZEN___ === obj) { return true; }
    var t = typeof obj;
    return t !== 'object' && t !== 'function';
  }

  /**
   * Mark obj as frozen so that Cajita code cannot directly assign to its
   * own properties.
   * <p>
   * If obj is a function, also freeze obj.prototype.
   * <p>
   * This appears as <tt>___.primFreeze(obj)</tt> and is wrapped by
   * <tt>cajita.freeze(obj)</tt>, which applies only to JSON containers.
   * It does a shallow freeze, i.e., if record y inherits from record x,
   * ___.primFreeze(y) will not freeze x.
   */
  function primFreeze(obj) {
    // Fail silently on undefined, since
    //   (function(){
    //     var f = Foo;
    //     if (true) { function Foo() {} }
    //   })();
    // gets translated to (roughly)
    //   (function(){
    //     var Foo;
    //     var f = ___.primFreeze(Foo);
    //     if (true) { Foo = function Foo() {}; }
    //   })();
    if (isFrozen(obj)) { return obj; }

    // badFlags are names of properties we need to turn off.
    // We accumulate these first, so that we're not in the midst of a
    // for/in loop on obj while we're deleting properties from obj.
    var badFlags = [];
    for (var k in obj) {
      if (endsWith_canSet___.test(k) || endsWith_canDelete___.test(k)) {
        if (obj[k]) {
          badFlags.push(k);
        }
      }
    }
    for (var i = 0; i < badFlags.length; i++) {
      var flag = badFlags[i];
      if (myOriginalHOP.call(obj, flag)) {
        if (!(delete obj[flag])) {
          fail('internal: failed delete: ', debugReference(obj), '.', flag);
        }
      }
      if (obj[flag]) {
        obj[flag] = false;
      }
    }
    obj.FROZEN___ = obj;
    if (typeOf(obj) === 'function') {
      if (isFunc(obj)) { 
        grantCall(obj, 'call');
        grantCall(obj, 'apply');
        obj.CALL___ = obj;
      }
      // Do last to avoid possible infinite recursion.
      if (obj.prototype) { primFreeze(obj.prototype); }
    }
    return obj;
  }

  /**
   * Like primFreeze(obj), but applicable only to JSON containers and
   * (pointlessly but harmlessly) to functions.
   */
  function freeze(obj) {
    if (!isJSONContainer(obj)) {
      if (typeOf(obj) === 'function') {
        enforce(isFrozen(obj), 'Internal: non-frozen function: ' + obj);
        return obj;
      }
      fail('cajita.freeze(obj) applies only to JSON Containers: ',
           debugReference(obj));
    }
    return primFreeze(obj);
  }

  /**
   * Makes a mutable copy of a JSON container.
   * <p>
   * Even if the original is frozen, the copy will still be mutable.
   * It does a shallow copy, i.e., if record y inherits from record x,
   * ___.copy(y) will also inherit from x.
   */
  function copy(obj) {
    if (!isJSONContainer(obj)) {
      fail('cajita.copy(obj) applies only to JSON Containers: ',
           debugReference(obj));
    }
    var result = isArray(obj) ? [] : {};
    forOwnKeys(obj, frozenFunc(function(k, v) {
      result[k] = v;
    }));
    return result;
  }

  /**
   * A snapshot of a JSON container is a frozen shallow copy of that
   * container.
   */
  function snapshot(obj) {
    return primFreeze(copy(obj));
  }


  ////////////////////////////////////////////////////////////////////////
  // Accessing property attributes
  ////////////////////////////////////////////////////////////////////////

  /**
   * Tests whether the fast-path canRead flag is set.
   */
  function canRead(obj, name)   {
    if (obj === void 0 || obj === null) { return false; }
    return !!obj[name + '_canRead___'];
  }

  /**
   * Tests whether the fast-path canEnum flag is set.
   */
  function canEnum(obj, name)   {
    if (obj === void 0 || obj === null) { return false; }
    return !!obj[name + '_canEnum___']; 
  }

  /**
   * Tests whether the fast-path canCall flag is set, or grantCall() has been
   * called.
   */
  function canCall(obj, name)   {
    if (obj === void 0 || obj === null) { return false; }
    if (obj[name + '_canCall___']) { return true; }
    if (obj[name + '_grantCall___']) {
      fastpathCall(obj, name);
      return true;
    }
    return false;
  }
  /**
   * Tests whether the fast-path canSet flag is set, or grantSet() has been
   * called, on this object itself as an own (non-inherited) attribute.
   */
  function canSet(obj, name) {
    if (obj === void 0 || obj === null) { return false; }
    if (obj[name + '_canSet___'] === obj) { return true; }
    if (obj[name + '_grantSet___'] === obj) {
      fastpathSet(obj, name);
      return true;
    }
    return false;
  }

  /**
   * Tests whether the fast-path canDelete flag is set, on this
   * object itself as an own (non-inherited) attribute.
   */
  function canDelete(obj, name) {
    if (obj === void 0 || obj === null) { return false; }
    return obj[name + '_canDelete___'] === obj; 
  }

  /**
   * Sets the fast-path canRead flag.
   * <p>
   * These are called internally to memoize decisions arrived at by
   * other means.
   */
  function fastpathRead(obj, name) {
    if (name === 'toString') { fail("internal: Can't fastpath .toString"); }
    obj[name + '_canRead___'] = obj;
  }

  function fastpathEnumOnly(obj, name) {
    obj[name + '_canEnum___'] = obj;
  }

  /**
   * Simple functions should callable and readable, but methods
   * should only be callable.
   */
  function fastpathCall(obj, name) {
    if (name === 'toString') { fail("internal: Can't fastpath .toString"); }
    if (obj[name + '_canSet___']) {
      obj[name + '_canSet___'] = false;
    }
    if (obj[name + '_grantSet___']) {
      obj[name + '_grantSet___'] = false;
    }
    obj[name + '_canCall___'] = obj;
  }

  /**
   * fastpathSet implies fastpathEnumOnly and fastpathRead. It also
   * disables the ability to call.
   */
  function fastpathSet(obj, name) {
    if (name === 'toString') { fail("internal: Can't fastpath .toString"); }
    if (isFrozen(obj)) {
      fail("Can't set .", name, ' on frozen (', debugReference(obj), ')');
    }
    fastpathEnumOnly(obj, name);
    fastpathRead(obj, name);
    if (obj[name + '_canCall___']) {
      obj[name + '_canCall___'] = false;
    }
    if (obj[name + '_grantCall___']) {
      obj[name + '_grantCall___'] = false;
    }
    obj[name + '_canSet___'] = obj;
  }

  /**
   * fastpathDelete allows delete of a member on a constructed object via
   * the private API.
   * <p>
   * TODO(erights): Having a fastpath flag for this probably doesn't
   * make sense.
   */
  function fastpathDelete(obj, name) {
    if (name === 'toString') { fail("internal: Can't fastpath .toString"); }
    if (isFrozen(obj)) {
      fail("Can't delete .", name, ' on frozen (', debugReference(obj), ')');
    }
    obj[name + '_canDelete___'] = obj;
  }

  /**
   * The various <tt>grant*</tt> functions are called externally by
   * Javascript code to express whitelisting taming decisions.
   */
  function grantRead(obj, name) {
    fastpathRead(obj, name);
  }

  // TODO(mikesamuel): none of the other grants grant enum, is "Only" operable?
  function grantEnumOnly(obj, name) {
    fastpathEnumOnly(obj, name);
  }

  function grantCall(obj, name) {
    fastpathCall(obj, name);
    obj[name + '_grantCall___'] = obj;
  }

  function grantSet(obj, name) {
    fastpathSet(obj, name);
    obj[name + '_grantSet___'] = obj;
  }

  function grantDelete(obj, name) {
    fastpathDelete(obj, name);
  }

  ////////////////////////////////////////////////////////////////////////
  // Classifying functions
  ////////////////////////////////////////////////////////////////////////

  function isCtor(constr)    {
    return constr && !!constr.CONSTRUCTOR___;
  }
  function isFunc(fun) {
    return fun && !!fun.FUNC___;
  }
  function isXo4aFunc(func) {
    return func && !!func.XO4A___;
  }

  /**
   * Mark <tt>constr</tt> as a constructor.
   * <p>
   * A function is tamed and classified by calling one of
   * <tt>ctor()</tt>, <tt>method()</tt>, or <tt>func()</tt>. Each
   * of these checks that the function hasn't already been classified by
   * any of the others. A function which has not been so classified is an
   * <i>untamed function</i>.
   * <p>
   * If <tt>opt_Sup</tt> is provided, record that const.prototype
   * inherits from opt_Sup.prototype. This bookkeeping helps
   * directConstructor().
   * <p>
   * <tt>opt_name</tt>, if provided, should be the name of the constructor
   * function. Currently, this is used only to generate friendlier
   * error messages.
   */
  function ctor(constr, opt_Sup, opt_name) {
    enforceType(constr, 'function', opt_name);
    if (isFunc(constr)) {
      fail("Simple functions can't be constructors: ", constr);
    }
    if (isXo4aFunc(constr)) {
      fail("Exophoric functions can't be constructors: ", constr);
    }
    constr.CONSTRUCTOR___ = true;
    if (opt_Sup) {
      derive(constr, opt_Sup);
    }
    if (opt_name) {
      constr.NAME___ = String(opt_name);
    }
    return constr;  // translator freezes constructor later
  }

  function derive(constr, sup) {
    sup = asCtor(sup);
    if (isFrozen(constr)) {
      fail('Derived constructor already frozen: ', constr);
    }
    if (!isFrozen(constr.prototype)) {
      // Some platforms, like Safari, actually conform to the part
      // of the ES3 spec which states that the constructor property
      // of implicitly created prototypical objects are not
      // deletable. But this prevents the inheritance-walking
      // algorithm (kludge) in directConstructor from working. Thus,
      // we set proto___ here so that directConstructor can skip
      // that impossible case.
      constr.prototype.proto___ = sup.prototype;
    }
  }

  /**
   * Enables first-class reification of exophoric functions as
   * pseudo-functions -- frozen records with call, bind, and apply
   * functions. 
   */
  function reifyIfXo4a(xfunc, opt_name) {
    if (!isXo4aFunc(xfunc)) {
      return asFirstClass(xfunc);
    }
    var result = {
      call: frozenFunc(function callXo4a(self, var_args) {
        if (self === null || self === void 0) { self = USELESS; }
        return xfunc.apply(self, Array.slice(arguments, 1));
      }),
      apply: frozenFunc(function applyXo4a(self, args) {
        if (self === null || self === void 0) { self = USELESS; }
        return xfunc.apply(self, args);
      }),
      bind: frozenFunc(function bindXo4a(self, var_args) {
        var args = arguments;
        if (self === null || self === void 0) { 
          self = USELESS;
          args = [self].concat(Array.slice(args, 1));
        }
        return frozenFunc(xfunc.bind.apply(xfunc, args));
      }),
      length: xfunc.length,
      toString: frozenFunc(function xo4aToString() {
        return xfunc.toString();
      })
    };
    if (opt_name !== void 0) {
      result.name = opt_name;
    }
    return primFreeze(result);
  }

  /**
   * Marks an anonymous function as exophoric:
   * the function mentions <tt>this</tt>,
   * but only accesses the public interface.
   * <p>
   * @param opt_name if provided, should be the message name associated
   *   with the method. Currently, this is used only to generate
   *   friendlier error messages.
   */
  function xo4a(func, opt_name) {
    enforceType(func, 'function', opt_name);
    if (isCtor(func)) {
      fail("Internal: Constructors can't be exophora: ", func);
    }
    if (isFunc(func)) {
      fail("Internal: Simple functions can't be exophora: ", func);
    }
    func.XO4A___ = true;
    return primFreeze(func);
  }

  /**
   * Mark fun as a simple function.
   * <p>
   * opt_name, if provided, should be the name of the
   * function. Currently, this is used only to generate friendlier
   * error messages.
   */
  function func(fun, opt_name) {
    enforceType(fun, 'function', opt_name);
    if (isCtor(fun)) {
      fail("Constructors can't be simple functions: ", fun);
    }
    if (isXo4aFunc(fun)) {
      fail("Exophoric functions can't be simple functions: ", fun);
    }
    fun.FUNC___ = true;
    if (opt_name) {
      fun.NAME___ = String(opt_name);
    }
    return fun;  // translator freezes fun later
  }

  /**
   * Mark fun as a simple function and freeze it.
   */
  function frozenFunc(fun, opt_name) {
    return primFreeze(func(fun, opt_name));
  }

  /** This "Only" form doesn't freeze */
  function asCtorOnly(constr) {
    if (isCtor(constr) || isFunc(constr)) {
      return constr;
    }

    enforceType(constr, 'function');
    fail("Untamed functions can't be called as constructors: ", constr);
  }

  /** Only constructors and simple functions can be called as constructors */
  function asCtor(constr) {
    return primFreeze(asCtorOnly(constr));
  }

  /** 
   * Only simple functions can be called as simple functions.
   * <p>
   * It is now <tt>asFunc</tt>'s responsibility to
   * <tt>primFreeze(fun)</tt>. 
   */
  function asFunc(fun) {
    if (fun && fun.FUNC___) {
      // fastpath shortcut
      if (fun.FROZEN___ === fun) {
        return fun;
      } else {
        return primFreeze(fun);
      }
    }
    enforceType(fun, 'function');
    if (isCtor(fun)) {
      if (fun === Number || fun === String || fun === Boolean) {
        // TODO(erights): To avoid accidents, <tt>method</tt>,
        // <tt>func</tt>, and <tt>ctor</tt> each ensure that
        // these classifications are exclusive. A function can be
        // classified as in at most one of these categories. However,
        // some primordial type conversion functions like
        // <tt>String</tt> need to be invocable both ways, so we
        // should probably relax this constraint.
        // <p>
        // But before we do, we should reexamine other
        // implications. For example, simple-functions, when called
        // reflectively by <tt>call</tt> or <tt>apply</tt> (and
        // therefore <tt>bind</tt>), ignore their first argument,
        // whereas constructors can be called reflectively by
        // <tt>call</tt> to do super-initialization on behalf of a
        // derived constructor.
        // <p>
        // Curiously, ES3 also defines function behavior different
        // from constructor behavior for <tt>Object</tt>,
        // <tt>Date</tt>, <tt>RegExp</tt>, and <tt>Error</tt>. (Not
        // sure about <tt>Array</tt>.) We should understand these as
        // well before introducing a proper solution.
        return primFreeze(fun);
      }
      fail("Constructors can't be called as simple functions: ", fun);
    }
    if (isXo4aFunc(fun)) {
      fail("Exophoric functions can't be called as simple functions: ", fun);
    }
    fail("Untamed functions can't be called as simple functions: ", fun);
  }

  /**
   * Is <tt>funoid</tt> an applicator -- a non-function object with a
   * callable <tt>apply</tt> method, such as a pseudo-function or
   * disfunction? 
   * <p>
   * If so, then it can be used as a function in some contexts.
   */
  function isApplicator(funoid) {
    if (typeof funoid !== 'object') { return false; }
    if (funoid === null) { return false; }
    return canCallPub(funoid, 'apply');
  }

  /**
   * Coerces fun to a genuine simple-function.
   * <p>
   * If fun is an applicator, then return a simple-function that invokes
   * fun's apply method. Otherwise, asFunc().
   */
  function toFunc(fun) {
    if (isApplicator(fun)) { 
      return frozenFunc(function applier(var_args) {
        return callPub(fun, 'apply', [USELESS, Array.slice(arguments, 0)]);
      });
    }
    return asFunc(fun);
  }

  /**
   * An object is prototypical iff its 'constructor' property points
   * at a genuine function whose 'prototype' property points back at
   * it.
   * <p>
   * Cajita code cannot access or create prototypical objects since
   * the 'prototype' property of genuine functions is inaccessible,
   * and since the transient function used by <tt>beget</tt> to create
   * inheritance chains does not escape.
   */
  function isPrototypical(obj) {
    if (typeOf(obj) !== 'object') { return false; }
    if (obj === null) { return false; }
    var constr = obj.constructor;
    if (typeOf(constr) !== 'function') { return false; }
    return constr.prototype === obj;
  }

  /**
   * Throws an exception if the value is an unmarked function or a
   * prototypical object.
   */
  function asFirstClass(value) {
    switch (typeOf(value)) {
      case 'function': {
        if (isFunc(value) || isCtor(value)) {
          if (isFrozen(value)) {
            return value;
          }
          // TODO(metaweta): make this a cajita-uncatchable exception
          fail('Internal: non-frozen function encountered: ', value);
        } else if (isXo4aFunc(value)) {
          // TODO(metaweta): make this a cajita-uncatchable exception
          // TODO(erights): non-user-hostile error message
          fail('Internal: toxic exophora encountered: ', value);
        } else {
          // TODO(metaweta): make this a cajita-uncatchable exception
          fail('Internal: toxic function encountered: ', value);
        }
        break;
      }
      case 'object': {
        if (value !== null && isPrototypical(value)) {
          // TODO(metaweta): make this a cajita-uncatchable exception
          fail('Internal: prototypical object encountered: ', value);
        }
        return value;
      }
      default: {
        return value;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // Accessing properties
  ////////////////////////////////////////////////////////////////////////

  /**
   * Can a Cajita client of <tt>obj</tt> read its {@code name} property?
   * <p>
   * If the property is unmentionable (i.e. ends in an '__'), then no.
   * If the property was defined by Cajita code, then yes. If it was
   * whitelisted, then yes. Or if the property is an own property of
   * <i>some</i> JSON container, then yes.
   * <p>
   * Why "some"? If record y inherits from record x, and 'foo' is an own
   * property of x, then canReadPub(y, 'foo') must be true.
   */
  function canReadPub(obj, name) {
    if (typeof name === 'number') { return name in obj; }
    name = String(name);
    if (obj === null) { return false; }
    if (obj === void 0) { return false; }
    if (obj[name + '_canRead___']) { return true; }
    if (endsWith__.test(name)) { return false; }
    if (name === 'toString') { return false; }
    if (!isJSONContainer(obj)) { return false; }
    if (!myOriginalHOP.call(obj, name)) { return false; }
    fastpathRead(obj, name);
    return true;
  }

  function hasOwnPropertyOf(obj, name) {
    if (typeof name === 'number') { return hasOwnProp(obj, name); }
    name = String(name);
    if (obj && obj[name + '_canRead___'] === obj) { return true; }
    return canReadPub(obj, name) && myOriginalHOP.call(obj, name);
  }

  /**
   * Implements Cajita's <tt><i>name</i> in <i>obj</i></tt>
   */
  function inPub(name, obj) {
    if (obj === null || obj === void 0) {
      throw new TypeError('invalid "in" operand: ' + obj);
    }
    obj = Object(obj);
    if (canReadPub(obj, name)) { return true; }
    if (canCallPub(obj, name)) { return true; }
    if ((name + '_getter___') in obj) { return true; }
    if ((name + '_handler___') in obj) { return true; }
    return false;
  }

  /**
   * Called by Caja code attempting to read a property.
   * <p>
   * If it can't then <tt>readPub</tt> returns <tt>undefined</tt> instead.
   */
  function readPub(obj, name) {
    if (typeof name === 'number') {
      if (typeof obj === 'string') {
        // In partial anticipation of ES3.1. 
        // TODO(erights): Once ES3.1 settles, revisit this and 
        // correctly implement the agreed semantics.
        // Mike Samuel suggests also making it conditional on
        //  (+name) === (name & 0x7fffffff)
        return obj.charAt(name);
      } else {
        return obj[name]; 
      }
    }
    name = String(name);
    if (canReadPub(obj, name)) { return obj[name]; }
    if (obj === null || obj === void 0) {
      throw new TypeError("Can't read " + name + " on " + obj);
    }
    return obj.handleRead___(name);
  }

  /**
   * If <tt>obj</tt> is an object with a property <tt>name</tt> that
   * should be objectively readable from Valija, return
   * <tt>obj[name]</tt>, else <tt>pumpkin</tt>. 
   * <p>
   * Provides a fastpath for Valija's <tt>read()</tt> function
   * <tt>$v.r()</tt>. The reason for returning the passed in pumpkin
   * rather than, for example, <tt>undefined</tt>, is so that the
   * caller can pass in a known unique value and distinguish it, on
   * return, from any possible valid value.
   * <p>
   * A property should be objectively readable iff<ul>
   * <li>It is readable from Cajita, and
   * <li><tt>obj</tt> is not a function, and
   * <li>either<ul>
   *     <li><tt>name</tt> is an own property of <tt>obj</tt>, or
   *     <li><tt>obj</tt> inherits <tt>name</tt> from an ancestor that
   *         Cajita considers first-class. The one such possibility is
   *         when <tt>obj</tt> is a record inheriting <tt>name</tt>
   *         from another record. (A record is a non-prototypical
   *         object whose directConstructor is Object.)
   *     </ul>
   * </ul>
   */
  function readOwn(obj, name, pumpkin) {
    if (typeof obj !== 'object' || !obj) {
      if (typeOf(obj) !== 'object') {
        return pumpkin;
      }
    }
    if (typeof name === 'number') {
      if (myOriginalHOP.call(obj, name)) { return obj[name]; }
      return pumpkin;
    }
    name = String(name);
    if (obj[name + '_canRead___'] === obj) { return obj[name]; }
    if (!myOriginalHOP.call(obj, name)) { return pumpkin; }
    // inline remaining relevant cases from canReadPub
    if (endsWith__.test(name)) { return pumpkin; }
    if (name === 'toString') { return pumpkin; }
    if (!isJSONContainer(obj)) { return pumpkin; }
    fastpathRead(obj, name);
    return obj[name];
  }

  /**
   * Ensure that all the permitsUsed starting at result are forever
   * safe to allow without runtime checks.
   */
  function enforceStaticPath(result, permitsUsed) {
    forOwnKeys(permitsUsed, frozenFunc(function(name, subPermits) {
      // Don't factor out since we don't enforce frozen if permitsUsed
      // are empty. 
      // TODO(erights): Once we have ES3.1ish attribute control, it
      // will suffice to enforce that each used property is frozen
      // independent of the object as a whole.
      enforce(isFrozen(result), 'Assumed frozen: ', result);
      if (name === '()') {
        // TODO(erights): Revisit this case
      } else {
        enforce(canReadPub(result, name),
                'Assumed readable: ', result, '.', name);
        if (inPub('()', subPermits)) {
          enforce(canCallPub(result, name),
                  'Assumed callable: ', result, '.', name, '()');
        }
        enforceStaticPath(readPub(result, name), subPermits);
      }
    }));
  }

  /**
   * Privileged code attempting to read an imported value from a module's
   * <tt>IMPORTS___</tt>. This function is NOT available to Cajita code.
   * <p>
   * This delegates to <tt>readOwn()</tt>, and so will only read
   * those properties from module_imports that are objectively visible
   * from both Cajita and Valija.
   */
  function readImport(module_imports, name, opt_permitsUsed) {
    var pumpkin = {};
    var result = readOwn(module_imports, name, pumpkin);
    if (result === pumpkin) {
      log('Linkage warning: ' + name + ' not importable');
      return void 0;
    }
    if (opt_permitsUsed) {
      enforceStaticPath(result, opt_permitsUsed);
    }
    return result;
  }

  /**
   * Can "innocent" code enumerate the named property on this object?
   * <p>
   * "Innocent" code is code which we assume to be ignorant of Caja,
   * not to be actively hostile, but which may be buggy (and
   * therefore accidentally harmful or exploitable). This
   * corresponds to legacy code, such as libraries, that we decide
   * to run untranslated, perhaps hidden or tamed, but which needs
   * to co-exist smoothly with the Caja runtime.
   * <p>
   * An earlier version of canInnocentEnum() filtered out exactly those
   * names ending with a double underbar. It now filters out exactly
   * those names ending in a triple underbar. Cajita code can't see names
   * ending in a double underbar, since existing platforms (like
   * Firefox) use such names for purposes that should be hidden from
   * Caja code. However, it is not up to Caja to shield innocent code
   * from seeing such platform properties. All the magic names Cajita
   * adds for its own internal bookkeeping end in triple underbar, so
   * that is all we need to hide from innocent code.
   */
  function canInnocentEnum(obj, name) {
    name = String(name);
    if (endsWith___.test(name)) { return false; }
    return true;
  }

  /**
   * Would a Cajita for/in loop by a client of obj see this name?
   * <p>
   * For properties defined in Cajita, this is generally the same as
   * canReadPub.  Otherwise according to whitelisting.
   */
  function canEnumPub(obj, name) {
    if (obj === null) { return false; }
    if (obj === void 0) { return false; }
    name = String(name);
    if (obj[name + '_canEnum___']) { return true; }
    if (endsWith__.test(name)) { return false; }
    if (!isJSONContainer(obj)) { return false; }
    if (!myOriginalHOP.call(obj, name)) { return false; }
    fastpathEnumOnly(obj, name);
    if (name === 'toString') { return true; }
    fastpathRead(obj, name);
    return true;
  }

  /**
   * Like canEnumPub, but allows only non-inherited properties.
   */
  function canEnumOwn(obj, name) {
    name = String(name);
    if (obj && obj[name + '_canEnum___'] === obj) { return true; }
    return canEnumPub(obj, name) && myOriginalHOP.call(obj, name);
  }

  /**
   * Returns a new object whose only utility is its identity and (for
   * diagnostic purposes only) its name.
   */
  function Token(name) {
    name = String(name);
    return primFreeze({
      toString: frozenFunc(function tokenToString() { return name; })
    });
  }
  frozenFunc(Token);

  /**
   * Inside a <tt>cajita.forOwnKeys()</tt>, or <tt>cajita.forAllKeys()</tt>, the
   * body function can terminate early, as if with a conventional
   * <tt>break;</tt>, by doing a <pre>return cajita.BREAK;</pre>
   */
  var BREAK = Token('BREAK');

  /**
   * A unique value that should never be made accessible to untrusted
   * code, for distinguishing the absence of a result from any 
   * returnable result.
   * <p>
   * See makeNewModuleHandler's getLastOutcome().
   */
  var NO_RESULT = Token('NO_RESULT');

  /**
   * For each sensible key/value pair in obj, call fn with that
   * pair.
   * <p>
   * If obj is an array, then enumerate indexes. Otherwise, enumerate
   * the canEnumOwn() property names.
   */
  function forOwnKeys(obj, fn) {
    fn = toFunc(fn);
    var keys = ownKeys(obj);
    for (var i = 0; i < keys.length; i++) {
      if (fn(keys[i], readPub(obj, keys[i])) === BREAK) {
        return;
      }
    }
  }

  /**
   * For each sensible key/value pair in obj, call fn with that
   * pair.
   * <p>
   * If obj is an array, then enumerate indexes. Otherwise, enumerate
   * the canEnumPub() property names.
   */
  function forAllKeys(obj, fn) {
    fn = toFunc(fn);
    var keys = allKeys(obj);
    for (var i = 0; i < keys.length; i++) {
      if (fn(keys[i], readPub(obj, keys[i])) === BREAK) {
        return;
      }
    }
  }

  /**
   * Return an array of the publicly readable own keys of obj.
   * <p>
   * If obj is an array, then enumerate indexes. Otherwise, enumerate
   * the canEnumOwn() property names.
   */
  function ownKeys(obj) {
    var result = [];
    if (isArray(obj)) {
      var len = obj.length;
      for (var i = 0; i < len; i++) {
        result.push(i);
      }
    } else {
      for (var k in obj) {
        if (canEnumOwn(obj, k)) {
          result.push(k);
        }
      }
      if (obj !== void 0 && obj !== null && obj.handleEnum___) {
        result = result.concat(obj.handleEnum___(true));
      }
    }
    return result;
  }

  /**
   * Return an array of the publicly readable own and inherited keys of obj.
   * <p>
   * If obj is an array, then enumerate indexes. Otherwise, enumerate
   * the canEnumPub() property names.
   */
  function allKeys(obj) {
    if (isArray(obj)) {
      return ownKeys(obj);
    } else {
      var result = [];
      for (var k in obj) {
        if (canEnumPub(obj, k)) {
          result.push(k);
        }
      }
      if (obj !== void 0 && obj !== null && obj.handleEnum___) {
        result = result.concat(obj.handleEnum___(false));
      }
      return result;
    }
  }

  /**
   * Can this be called as a public method?
   * <p>
   * For genuine methods, they are only callable if the canCall
   * attribute is set. Otherwise, if this property is readable and
   * holds a simple function, then it's also callable as a function,
   * which we can memoize.
   */
  function canCallPub(obj, name) {
    if (obj === null) { return false; }
    if (obj === void 0) { return false; }
    name = String(name);
    if (obj[name + '_canCall___']) { return true; }
    if (obj[name + '_grantCall___']) { 
      fastpathCall(obj, name);
      return true; 
    }
    if (!canReadPub(obj, name)) { return false; }
    if (endsWith__.test(name)) { return false; }
    if (name === 'toString') { return false; }
    var func = obj[name];
    if (!isFunc(func) && !isXo4aFunc(func)) {
      return false;
    }
    fastpathCall(obj, name);
    return true;
  }

  /**
   * A client of obj tries to call one of its methods.
   */
  function callPub(obj, name, args) {
    name = String(name);
    if (obj === null || obj === void 0) {
      throw new TypeError("Can't call " + name + " on " + obj);
    }
    if (obj[name + '_canCall___'] || canCallPub(obj, name)) {
      return obj[name].apply(obj, args);
    }
    if (obj.handleCall___) { return obj.handleCall___(name, args); }
    fail('not callable:', debugReference(obj), '.', name);
  }

  /**
   * Can a client of obj directly assign to its name property?
   * <p>
   * If this property is unmentionable (i.e., ends with a '__') or if this
   * object is frozen, then no.
   * Else if this is an own property defined by Cajita code,
   * then yes. If the object is a JSON container, then
   * yes. Otherwise according to whitelisting decisions.
   */
  function canSetPub(obj, name) {
    name = String(name);
    if (canSet(obj, name)) { return true; }
    if (endsWith__.test(name)) { return false; }
    if (name === 'valueOf') { return false; }
    if (name === 'toString') { return false; }
    return !isFrozen(obj) && isJSONContainer(obj);
  }

  /** A client of obj attempts to assign to one of its properties. */
  function setPub(obj, name, val) {
    // asFirstClass() here would be a useful safety check, to prevent
    // the further propogation of, for example, a leaked toxic
    // function. However, its security benefit is questionable, and
    // the check is expensive in this position.
//  val = asFirstClass(val);
    if (typeof name === 'number' &&
        // See issue 875
        obj instanceof Array &&
        obj.FROZEN___ !== obj) {
      return obj[name] = val;
    }
    name = String(name);
    if (obj === null || obj === void 0) {
      throw new TypeError("Can't set " + name + " on " + obj);
    }
    if (obj[name + '_canSet___'] === obj) {
      return obj[name] = val;
    } else if (canSetPub(obj, name)) {
      fastpathSet(obj, name);
      return obj[name] = val;
    } else {
      return obj.handleSet___(name, val);
    }
  }

  /**
   * Can the given constructor have the given static method added to it?
   * @param {Function} ctor
   * @param {string} staticMemberName an identifier in the public namespace.
   */
  function canSetStatic(ctor, staticMemberName) {
    staticMemberName = '' + staticMemberName;
    if (typeOf(ctor) !== 'function') {
      log('Cannot set static member of non function', ctor);
      return false;
    }
    if (isFrozen(ctor)) {
      log('Cannot set static member of frozen function', ctor);
      return false;
    }
    // disallows prototype, call, apply, bind
    if (staticMemberName in ctor) {
      log('Cannot override static member ', staticMemberName);
      return false;
    }
    // statics are public
    if (endsWith__.test(staticMemberName) || staticMemberName === 'valueOf') {
      log('Illegal static member name ', staticMemberName);
      return false;
    }
    if (staticMemberName === 'toString') {
      // no diagnostic as this is a normal fault-handling case.
      return false;
    }
    return true;
  }

  /**
   * Sets a static members of a ctor, making sure that it can't be used to
   * override call/apply/bind and other builtin members of function.
   * @param {Function} ctor
   * @param {string} staticMemberName an identifier in the public namespace.
   * @param staticMemberValue the value of the static member.
   */
  function setStatic(ctor, staticMemberName, staticMemberValue) {
    staticMemberName = '' + staticMemberName;
    if (canSetStatic(ctor, staticMemberName)) {
      ctor[staticMemberName] = staticMemberValue;
      fastpathEnumOnly(ctor, staticMemberName);
      fastpathRead(ctor, staticMemberName);
    } else {
      ctor.handleSet___(staticMemberName, staticMemberValue);
    }
  }

  /**
   * Can a client of obj delete the named property?
   */
  function canDeletePub(obj, name) {
    name = String(name);
    if (isFrozen(obj)) { return false; }
    if (endsWith__.test(name)) { return false; }
    if (name === 'valueOf') { return false; }
    if (name === 'toString') { return false; }
    if (isJSONContainer(obj)) { return true; }
    return false;
  }

  /**
   * A client of obj can only delete a property of obj if obj is a
   * non-frozen JSON container.
   */
  function deletePub(obj, name) {
    name = String(name);
    if (obj === null || obj === void 0) {
      throw new TypeError("Can't delete " + name + " on " + obj);
    }
    if (canDeletePub(obj, name)) {
      // See deleteFieldEntirely for reasons why we don't cache deletability.
      return deleteFieldEntirely(obj, name);
    } else {
      return obj.handleDelete___(name);
    }
  }

  /**
   * Deletes a field removing any cached permissions.
   * @param {object} obj
   * @param {string} name of field in obj to delete.
   * @return {boolean}
   * @throws {Error} if field not deletable or name not in field.
   * @private
   */
  function deleteFieldEntirely(obj, name) {
    // Can't cache fastpath delete since deleting the field should remove
    // all privileges for that field.
    delete obj[name + '_canRead___'];
    delete obj[name + '_canEnum___'];
    delete obj[name + '_canCall___'];
    delete obj[name + '_grantCall___'];
    delete obj[name + '_grantSet___'];
    delete obj[name + '_canSet___'];
    delete obj[name + '_canDelete___'];
    return (delete obj[name]) || (fail('not deleted: ', name), false);
  }

  ////////////////////////////////////////////////////////////////////////
  // Other
  ////////////////////////////////////////////////////////////////////////

  /**
   * This returns a frozen array copy of the original array or
   * array-like object.
   * <p>
   * If a Cajita program makes use of <tt>arguments</tt> in any
   * position other than <tt>arguments.callee</tt>, this is
   * rewritten to use a frozen array copy of arguments instead. This
   * way, if Cajita code passes its arguments to someone else, they
   * are not giving the receiver the rights to access the passing
   * function nor to modify the parameter variables of the passing
   * function.
   */
  function args(original) {
    return primFreeze(Array.slice(original, 0));
  }

  /**
   * When a <tt>this</tt> value must be provided but nothing is
   * suitable, provide this useless object instead.
   */
  var USELESS = Token('USELESS');

  /**
   * A call to cajita.manifest(data) is dynamically ignored, but if the
   * data expression is valid static JSON text, its value is made
   * statically available to the module loader. 
   */
  function manifest(ignored) {}

  /** Sealer for call stacks as from {@code (new Error).stack}. */
  var callStackSealer = makeSealerUnsealerPair();

  /**
   * Receives the exception caught by a user defined catch block.
   * @param ex a value caught in a try block.
   * @return a tamed exception.
   */
  function tameException(ex) {
    try {
      switch (typeOf(ex)) {
        case 'object': {
          if (ex === null) { return null; }
          if (isInstanceOf(ex, Error)) {
            // See Ecma-262 S15.11 for the definitions of these properties.
            var message = ex.message || ex.desc;
            var stack = ex.stack;
            var name = ex.constructor && ex.constructor.name;  // S15.11.7.9
            // Convert to undefined if falsy, or a string otherwise.
            message = !message ? void 0 : '' + message;
            stack = !stack ? void 0 : callStackSealer.seal('' + stack);
            name = !name ? void 0 : '' + name;
            return primFreeze({ message: message, name: name, stack: stack });
          }
          return '' + ex;
        }
        case 'string':
        case 'number':
        case 'boolean': {
          // Immutable.
          return ex;
        }
        case 'undefined': {
          return (void 0);
        }
        case 'function': {
          // According to Pratap Lakhsman's "JScript Deviations" S2.11
          // If the caught object is a function, calling it within the catch
          // supplies the head of the scope chain as the "this value".  The
          // called function can add properties to this object.  This implies
          // that for code of this shape:
          //     var x;
          //     try {
          //       // ...
          //     } catch (E) {
          //       E();
          //       return s;
          //     }
          // The reference to 'x' within the catch is not necessarily to the
          // local declaration of 'x'; this gives Catch the same performance
          // problems as with.

          // We return undefined to make sure that caught functions cannot be
          // evaluated within the catch block.
          return void 0;
        }
        default: {
          log('Unrecognized exception type ' + (typeOf(ex)));
          return 'Unrecognized exception type ' + (typeOf(ex));
        }
      }
    } catch (_) {
      // Can occur if coercion to string fails, or if ex has getters
      // that fail.  This function must never throw an exception
      // because doing so would cause control to leave a catch block
      // before the handler fires.
      log('Exception during exception handling.');
      return 'Exception during exception handling.';
    }
  }

  /**
   * Makes a new empty object that directly inherits from <tt>proto</tt>.
   */
  function primBeget(proto) {
    if (proto === null) { fail("Cannot beget from null."); }
    if (proto === (void 0)) { fail("Cannot beget from undefined."); }
    function F() {}
    F.prototype = proto;
    var result = new F();
    result.proto___ = proto;
    return result;
  }

  ////////////////////////////////////////////////////////////////////////
  // Taming mechanism
  ////////////////////////////////////////////////////////////////////////

  /**
   * Arrange to handle read-faults on <tt>obj[name]</tt>
   * by calling <tt>getHandler()</tt> as a method on
   * the faulted object.
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>grantRead()</tt>.
   * FIXME(ben): and fastpathRead()?
   */
  function useGetHandler(obj, name, getHandler) {
    obj[name + '_getter___'] = getHandler;
  }

  /**
   * Arrange to handle call-faults on <tt>obj[name](args...)</tt> by
   * calling <tt>applyHandler(args)</tt> as a method on the faulted
   * object.
   * <p>
   * Note that <tt>applyHandler</tt> is called with a single argument,
   * which is the list of arguments in the original call.
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting grantCall() or other grants which
   * imply grantCall().
   * FIXME(ben): also fastpath?
   */
  function useApplyHandler(obj, name, applyHandler) {
    obj[name + '_handler___'] = applyHandler;
  }

  /**
   * Arrange to handle call-faults on <tt>obj[name](args...)</tt> by
   * calling <tt>callHandler(args...)</tt> as a method on the faulted
   * object.
   * <p>
   * Note that <tt>callHandler</tt> is called with the same arguments
   * as the original call.
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting grantCall() or other grants which
   * imply grantCall().
   * FIXME(ben): also fastpath?
   */
  function useCallHandler(obj, name, callHandler) {
    useApplyHandler(obj, name, function callApplier(args) {
      return callHandler.apply(this, args);
    });
  }

  /**
   * Arrange to handle set-faults on <tt>obj[name] = newValue</tt> by
   * calling <tt>setHandler(newValue)</tt> as a method on the faulted
   * object.
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting grantSet().
   * FIXME(ben): also fastpath?
   */
  function useSetHandler(obj, name, setHandler) {
    obj[name + '_setter___'] = setHandler;
  }

  /**
   * Arrange to handle delete-faults on <tt>delete obj[name]</tt> by
   * calling <tt>deleteHandler()</tt> as a method on the faulted object.
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting grantDelete().
   * FIXME(ben): also fastpath?
   */
  function useDeleteHandler(obj, name, deleteHandler) {
    obj[name + '_deleter___'] = deleteHandler;
  }

  /**
   * Whilelist obj[name] as a simple frozen function that can be either
   * called or read.
   */
  function grantFunc(obj, name) {
    frozenFunc(obj[name], name);
    grantCall(obj, name);
    grantRead(obj, name);
  }

  /**
   * Whitelist proto[name] as a generic exophoric function that can
   * safely be called with its <tt>this</tt> bound to other objects.
   * <p>
   * Since exophoric functions are not first-class, reading
   * proto[name] returns the corresponding pseudo-function -- a record
   * with simple-functions for its call, bind, and apply.
   */
  function grantGeneric(proto, name) {
    var func = xo4a(proto[name], name);
    grantCall(proto, name);
    var pseudoFunc = reifyIfXo4a(func, name);
    useGetHandler(proto, name, function xo4aGetter() { 
      return pseudoFunc; 
    });
  }

  /**
   * Mark func as exophoric and use it as a virtual generic
   * exophoric function.
   * <p>
   * Since exophoric functions are not first-class, reading
   * proto[name] returns the corresponding pseudo-function -- a record
   * with simple-functions for its call, bind, and apply.
   */
  function handleGeneric(obj, name, func) {
    xo4a(func);
    useCallHandler(obj, name, func);
    var pseudoFunc = reifyIfXo4a(func, name);
    useGetHandler(obj, name, function genericGetter() { 
      return pseudoFunc; 
    });
  }

  /**
   * Virtually replace proto[name] with a fault-handler
   * wrapper that first verifies that <tt>this</tt> inherits from
   * proto. 
   * <p>
   * When a pre-existing Javascript method may do something unsafe
   * when applied to a <tt>this</tt> of the wrong type, we need to
   * provide a fault-handler instead to prevent such mis-application.
   * <p>
   * In order for this fault handler to get control, it's important
   * that no one does an grantCall() or other grants which imply
   * grantCall(). 
   * FIXME(ben): also fastpath?
   */
  function grantTypedGeneric(proto, name) {
    var original = proto[name];
    handleGeneric(proto, name, function guardedApplier(var_args) {
      if (!inheritsFrom(this, proto)) {
        fail("Can't call .", name, ' on a non ',
             directConstructor(proto), ': ', this);
      }
      return original.apply(this, arguments);
    });
  }

  /**
   * Virtually replace proto[name] with a fault-handler
   * wrapper that first verifies that <tt>this</tt> isn't frozen.
   * <p>
   * When a pre-existing Javascript method would mutate its object,
   * we need to provide a fault handler instead to prevent such
   * mutation from violating Cajita semantics. 
   * <p>
   * In order for this fault handler to get control, it's important
   * that no one does an grantCall() or other grants which imply
   * grantCall(). 
   * FIXME(ben): also fastpath?
   */
  function grantMutator(proto, name) {
    var original = proto[name];
    handleGeneric(proto, name, function nonMutatingApplier(var_args) {
      if (isFrozen(this)) {
        fail("Can't .", name, ' a frozen object');
      }
      return original.apply(this, arguments);
    });
  }

  /**
   * Verifies that regexp is something that can appear as a
   * parameter to a Javascript method that would use it in a match.
   * <p>
   * If it is a RegExp, then this match might mutate it, which must
   * not be allowed if regexp is frozen. Otherwise it must be a string.
   */
  function enforceMatchable(regexp) {
    if (isInstanceOf(regexp, RegExp)) {
      if (isFrozen(regexp)) {
        fail("Can't match with frozen RegExp: ", regexp);
      }
    } else {
      enforceType(regexp, 'string');
    }
  }

  /**
   * A shorthand that happens to be useful here.
   * <p>
   * For all i in arg2s: func2(arg1,arg2s[i]).
   */
  function all2(func2, arg1, arg2s) {
    var len = arg2s.length;
    for (var i = 0; i < len; i += 1) {
      func2(arg1, arg2s[i]);
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // Taming decisions
  ////////////////////////////////////////////////////////////////////////

  /// Math

  all2(grantRead, Math, [
    'E', 'LN10', 'LN2', 'LOG2E', 'LOG10E', 'PI', 'SQRT1_2', 'SQRT2'
  ]);
  all2(grantFunc, Math, [
    'abs', 'acos', 'asin', 'atan', 'atan2', 'ceil', 'cos', 'exp', 'floor',
    'log', 'max', 'min', 'pow', 'random', 'round', 'sin', 'sqrt', 'tan'
  ]);

  /// toString

  function grantToString(proto) {
    proto.TOSTRING___ = xo4a(proto.toString, 'toString');
  }
  useGetHandler(Object.prototype, 'toString',
                function toStringGetter() {
    if (hasOwnProp(this, 'toString') &&
        typeOf(this.toString) === 'function' &&
        !hasOwnProp(this, 'TOSTRING___')) {
      // This case is a kludge that doesn't work for undiagnosed reasons.
//    this.TOSTRING___ = xo4a(this.toString, 'toString');
      // TODO(erights): This case is a different kludge that needs to
      // be explained.
      // TODO(erights): This is probably wrong in that it can leak xo4a.
      return this.toString;
    }
    return reifyIfXo4a(this.TOSTRING___, 'toString');
  });
  useApplyHandler(Object.prototype, 'toString',
                  function toStringApplier(args) {
    return this.toString.apply(this, args);
  });
  useSetHandler(Object.prototype, 'toString',
                function toStringSetter(meth) {
    if (isFrozen(this)) {
      return myKeeper.handleSet(this, 'toString', meth);
    }
    meth = asFirstClass(meth);
    this.TOSTRING___ = meth;
    this.toString = function delegatingToString(var_args) {
      var args = Array.slice(arguments, 0);
      if (typeOf(meth) === 'function') {
        return meth.apply(this, args);
      }
      var methApply = readPub(meth, 'apply');
      if (typeOf(methApply) === 'function') {
        return methApply.call(meth, this, args);
      }
      var result = Object.toString.call(this);
      log('Not correctly printed: ' + result);
      return result;
    };
    return meth;
  });
  useDeleteHandler(Object.prototype, 'toString', 
                   function toStringDeleter() {
    if (isFrozen(this)) {
      return myKeeper.handleDelete(this, 'toString');
    }
    return (delete this.toString) && (delete this.TOSTRING___);
  });

  /// Object

  ctor(Object, void 0, 'Object');
  grantToString(Object.prototype);
  all2(grantGeneric, Object.prototype, [
    'toLocaleString', 'valueOf', 'isPrototypeOf'
  ]);
  grantRead(Object.prototype, 'length');
  handleGeneric(Object.prototype, 'hasOwnProperty',
                function hasOwnPropertyHandler(name) {
    return hasOwnPropertyOf(this, name);
  });
  handleGeneric(Object.prototype, 'propertyIsEnumerable',
                function propertyIsEnumerableHandler(name) {
    name = String(name);
    return canEnumPub(this, name);
  });

  /// Function

  handleGeneric(Function.prototype, 'apply',
                function applyHandler(self, realArgs) {
    return toFunc(this).apply(self, realArgs);
  });
  handleGeneric(Function.prototype, 'call',
                function callHandler(self, var_args) {
    return toFunc(this).apply(self, Array.slice(arguments, 1));
  });
  handleGeneric(Function.prototype, 'bind',
                function bindHandler(self, var_args) {
    var thisFunc = this;
    var leftArgs = Array.slice(arguments, 1);
    function boundHandler(var_args) {
      var args = leftArgs.concat(Array.slice(arguments, 0));
      return callPub(thisFunc, 'apply', [self, args]);
    }
    return frozenFunc(boundHandler);
  });

  /// Array

  ctor(Array, Object, 'Array');
  grantFunc(Array, 'slice');
  grantToString(Array.prototype);
  all2(grantTypedGeneric, Array.prototype, [ 'toLocaleString' ]);
  all2(grantGeneric, Array.prototype, [
    'concat', 'join', 'slice', 'indexOf', 'lastIndexOf'
  ]);
  all2(grantMutator, Array.prototype, [
    'pop', 'push', 'reverse', 'shift', 'splice', 'unshift'
  ]);
  handleGeneric(Array.prototype, 'sort',
                function sortHandler(comparator) {
    if (isFrozen(this)) {
      fail("Can't sort a frozen array.");
    }
    if (comparator) {
      return Array.prototype.sort.call(this, toFunc(comparator));
    } else {
      return Array.prototype.sort.call(this);
    }
  });

  /// String

  ctor(String, Object, 'String');
  grantFunc(String, 'fromCharCode');
  grantToString(String.prototype);
  all2(grantTypedGeneric, String.prototype, [
    'toLocaleString', 'indexOf', 'lastIndexOf'
  ]);
  all2(grantGeneric, String.prototype, [
    'charAt', 'charCodeAt', 'concat',
    'localeCompare', 'slice', 'substr', 'substring',
    'toLowerCase', 'toLocaleLowerCase', 'toUpperCase', 'toLocaleUpperCase'
  ]);

  handleGeneric(String.prototype, 'match',
                function matchHandler(regexp) {
    enforceMatchable(regexp);
    return this.match(regexp);
  });
  handleGeneric(String.prototype, 'replace',
                function replaceHandler(searcher, replacement) {
    enforceMatchable(searcher);
    if (isFunc(replacement)) {
      replacement = asFunc(replacement);
    } else if (isApplicator(replacement)) {
      replacement = toFunc(replacement);
    } else {
      replacement = '' + replacement;
    }
    return this.replace(searcher, replacement);
  });
  handleGeneric(String.prototype, 'search',
                function searchHandler(regexp) {
    enforceMatchable(regexp);
    return this.search(regexp);
  });
  handleGeneric(String.prototype, 'split',
                function splitHandler(separator, limit) {
    enforceMatchable(separator);
    return this.split(separator, limit);
  });

  /// Boolean

  ctor(Boolean, Object, 'Boolean');
  grantToString(Boolean.prototype);

  /// Number

  ctor(Number, Object, 'Number');
  all2(grantRead, Number, [
    'MAX_VALUE', 'MIN_VALUE', 'NaN',
    'NEGATIVE_INFINITY', 'POSITIVE_INFINITY'
  ]);
  grantToString(Number.prototype);
  all2(grantTypedGeneric, Number.prototype, [
    'toFixed', 'toExponential', 'toPrecision'
  ]);

  /// Date

  ctor(Date, Object, 'Date');
  grantFunc(Date, 'parse');
  grantFunc(Date, 'UTC');
  grantToString(Date.prototype);
  all2(grantTypedGeneric, Date.prototype, [
    'toDateString','toTimeString', 'toUTCString',
    'toLocaleString', 'toLocaleDateString', 'toLocaleTimeString',
    'toISOString',
    'getDay', 'getUTCDay', 'getTimezoneOffset',

    'getTime', 'getFullYear', 'getUTCFullYear', 'getMonth', 'getUTCMonth',
    'getDate', 'getUTCDate', 'getHours', 'getUTCHours',
    'getMinutes', 'getUTCMinutes', 'getSeconds', 'getUTCSeconds',
    'getMilliseconds', 'getUTCMilliseconds'
  ]);
  all2(grantMutator, Date.prototype, [
    'setTime', 'setFullYear', 'setUTCFullYear', 'setMonth', 'setUTCMonth',
    'setDate', 'setUTCDate', 'setHours', 'setUTCHours',
    'setMinutes', 'setUTCMinutes', 'setSeconds', 'setUTCSeconds',
    'setMilliseconds', 'setUTCMilliseconds'
  ]);

  /// RegExp

  ctor(RegExp, Object, 'RegExp');
  grantToString(RegExp.prototype);
  handleGeneric(RegExp.prototype, 'exec',
                function execHandler(specimen) {
    if (isFrozen(this)) {
      fail("Can't .exec a frozen RegExp");
    }
    specimen = String(specimen); // See bug 528
    return this.exec(specimen);
  });
  handleGeneric(RegExp.prototype, 'test',
                function testHandler(specimen) {
    if (isFrozen(this)) {
      fail("Can't .test a frozen RegExp");
    }
    specimen = String(specimen); // See bug 528
    return this.test(specimen);
  });

  all2(grantRead, RegExp.prototype, [
    'source', 'global', 'ignoreCase', 'multiline', 'lastIndex'
  ]);

  /// errors

  ctor(Error, Object, 'Error');
  grantToString(Error.prototype);
  grantRead(Error.prototype, 'name');
  grantRead(Error.prototype, 'message');
  ctor(EvalError, Error, 'EvalError');
  ctor(RangeError, Error, 'RangeError');
  ctor(ReferenceError, Error, 'ReferenceError');
  ctor(SyntaxError, Error, 'SyntaxError');
  ctor(TypeError, Error, 'TypeError');
  ctor(URIError, Error, 'URIError');


  var sharedImports;

  ////////////////////////////////////////////////////////////////////////
  // Module loading
  ////////////////////////////////////////////////////////////////////////

  var myNewModuleHandler;

  /**
   * Gets the current module handler.
   */
  function getNewModuleHandler() {
    return myNewModuleHandler;
  }

  /**
   * Registers a new-module-handler, to be called back when a new
   * module is loaded.
   * <p>
   * This callback mechanism is provided so that translated Cajita
   * modules can be loaded from a trusted site with the
   * &lt;script&gt; tag, which runs its script as a statement, not
   * an expression. The callback is of the form
   * <tt>newModuleHandler.handle(newModule)</tt>.
   */
  function setNewModuleHandler(newModuleHandler) {
    myNewModuleHandler = newModuleHandler;
  }

  /**
   * A new-module-handler which returns the new module without
   * instantiating it.
   */
  var obtainNewModule = freeze({
    handle: frozenFunc(function handleOnly(newModule){ return newModule; })
  });

  /**
   * Makes and returns a fresh "normal" module handler whose imports
   * are initialized to a copy of the sharedImports.
   * <p>
   * This handles a new module by calling it, passing it the imports
   * object held in this handler. Successive modules handled by the
   * same "normal" handler thereby see a simulation of successive
   * updates to a shared global scope.
   */
  function makeNormalNewModuleHandler() {
    var imports = copy(sharedImports);
    var lastOutcome = void 0;
    return freeze({
      getImports: frozenFunc(function getImports() { return imports; }),
      setImports: frozenFunc(function setImports(newImports) { 
        imports = newImports; 
      }),

      /**
       * An outcome is a pair of a success flag and a value. 
       * <p>
       * If the success flag is true, then the value is the normal
       * result of calling the module function. If the success flag is
       * false, then the value is the thrown error by which the module
       * abruptly terminated.
       * <p>
       * An html page is cajoled to a module that runs to completion,
       * but which reports as its outcome the outcome of its last
       * script block. In order to reify that outcome and report it
       * later, the html page initializes moduleResult___ to
       * NO_RESULT, the last script block is cajoled to set
       * moduleResult___ to something other than NO_RESULT on success
       * but to call handleUncaughtException() on
       * failure, and the html page returns moduleResult___ on
       * completion. handleUncaughtException() records a failed
       * outcome. This newModuleHandler's handle() method will not
       * overwrite an already reported outcome with NO_RESULT, so the
       * last script-block's outcome will be preserved.
       */
      getLastOutcome: frozenFunc(function getLastOutcome() { 
        return lastOutcome; 
      }),

      /**
       * If the last outcome is a success, returns its value;
       * otherwise <tt>undefined</tt>.
       */
      getLastValue: frozenFunc(function getLastValue() {
        if (lastOutcome && lastOutcome[0]) {
          return lastOutcome[1];
        } else {
          return void 0;
        }
      }),

      /**
       * Runs the newModule's module function.
       * <p>
       * Updates the last outcome to report the module function's
       * reported outcome. Propogate this outcome by terminating in
       * the same manner. 
       */
      handle: frozenFunc(function handle(newModule) {
        lastOutcome = void 0;
        try {
          var result = newModule.instantiate(___, imports);
          if (result !== NO_RESULT) {
            lastOutcome = [true, result];
          }
        } catch (ex) {
          lastOutcome = [false, ex];
        }
        if (lastOutcome) {
          if (lastOutcome[0]) {
            return lastOutcome[1];
          } else {
            throw lastOutcome[1];
          }
        } else {
          return void 0;
        }
      }),

      /**
       * This emulates HTML5 exception handling for scripts as discussed at
       * http://code.google.com/p/google-caja/wiki/UncaughtExceptionHandling
       * and see HtmlCompiler.java for the code that calls this.
       * @param exception a raw exception.  Since {@code throw} can raise any
       *   value, exception could be any value accessible to cajoled code, or
       *   any value thrown by an API imported by cajoled code.
       * @param onerror the value of the raw reference "onerror" in top level
       *   cajoled code.  This will likely be undefined much of the time, but
       *   could be anything.  If it is a func, it can be called with
       *   three strings (message, source, lineNum) as the
       *   {@code window.onerror} event handler.
       * @param {string} source a URI describing the source file from which the
       *   error originated.
       * @param {string} lineNum the approximate line number in source at which
       *   the error originated.
       */
      handleUncaughtException: function handleUncaughtException(exception,
                                                                onerror,
                                                                source,
                                                                lineNum) {
        lastOutcome = [false, exception];

        // Cause exception to be rethrown if it is uncatchable.
        tameException(exception);

        var message = 'unknown';
        if ('object' === typeOf(exception) && exception !== null) {
          message = String(exception.message || exception.desc || message);
        }

        // If we wanted to provide a hook for containers to get uncaught
        // exceptions, it would go here before onerror is invoked.

        // See the HTML5 discussion for the reasons behind this rule.
        if (isApplicator(onerror)) { onerror = toFunc(onerror); }
        var shouldReport = (
            isFunc(onerror)
            ? onerror.CALL___(message, String(source), String(lineNum))
            : onerror !== null);
        if (shouldReport !== false) {
          cajita.log(source + ':' + lineNum + ': ' + message);
        }
      }
    });
  }

  /**
   * A module is an object literal containing metadata and an
   * <code>instantiate</code> member, which is a plugin-maker function.
   * <p>
   * loadModule(module) marks module's <code>instantiate</code> member as a
   * func, freezes the module, asks the current new-module-handler to handle it
   * (thereby notifying the handler), and returns the new module.
   */
  function loadModule(module) {
    freeze(module);
    frozenFunc(module.instantiate);
    return callPub(myNewModuleHandler, 'handle', [module]);
  }

  var registeredImports = [];

  /**
   * Gets or assigns the id associated with this (assumed to be)
   * imports object, registering it so that
   * <tt>getImports(getId(imports)) === imports</tt>.
   * <p>
   * This system of registration and identification allows us to
   * cajole html such as
   * <pre>&lt;a onmouseover="alert(1)"&gt;Mouse here&lt;/a&gt;</pre>
   * into html-writing JavaScript such as<pre>
   * ___IMPORTS___.document.innerHTML = "
   *  &lt;a onmouseover=\"
   *    (function(___IMPORTS___) {
   *      ___IMPORTS___.alert(1);
   *    })(___.getImports(" + ___.getId(___IMPORTS___) + "))
   *  \"&gt;Mouse here&lt;/a&gt;
   * ";
   * </pre>
   * If this is executed by a plugin whose imports is assigned id 42,
   * it generates html with the same meaning as<pre>
   * &lt;a onmouseover="___.getImports(42).alert(1)"&gt;Mouse here&lt;/a&gt;
   * </pre>
   * <p>
   * An imports is not registered and no id is assigned to it until the
   * first call to <tt>getId</tt>. This way, an imports that is never
   * registered, or that has been <tt>unregister</tt>ed since the last
   * time it was registered, will still be garbage collectable.
   */
  function getId(imports) {
    enforceType(imports, 'object', 'imports');
    var id;
    if ('id___' in imports) {
      id = enforceType(imports.id___, 'number', 'id');
    } else {
      id = imports.id___ = registeredImports.length;
    }
    registeredImports[id] = imports;
    return id;
  }

  /**
   * Gets the imports object registered under this id.
   * <p>
   * If it has been <tt>unregistered</tt> since the last
   * <tt>getId</tt> on it, then <tt>getImports</tt> will fail.
   */
  function getImports(id) {
    var result = registeredImports[enforceType(id, 'number', 'id')];
    if (result === void 0) {
      fail('imports#', id, ' unregistered');
    }
    return result;
  }

  /**
   * If you know that this <tt>imports</tt> no longers needs to be
   * accessed by <tt>getImports</tt>, then you should
   * <tt>unregister</tt> it so it can be garbage collected.
   * <p>
   * After unregister()ing, the id is not reassigned, and the imports
   * remembers its id. If asked for another <tt>getId</tt>, it
   * reregisters itself at its old id.
   */
  function unregister(imports) {
    enforceType(imports, 'object', 'imports');
    if ('id___' in imports) {
      var id = enforceType(imports.id___, 'number', 'id');
      registeredImports[id] = void 0;
    }
  }


  ////////////////////////////////////////////////////////////////////////
  // Trademarking
  ////////////////////////////////////////////////////////////////////////

  /**
   * Return a trademark object.
   */
  function Trademark(name) {
    return Token(name);
  }
  frozenFunc(Trademark);

  /**
   * Returns true if the object has a list of trademarks
   * and the given trademark is in the list.
   */
  function hasTrademark(trademark, obj) {
    if (!hasOwnProp(obj, 'trademarks___')) { return false; }
    var list = obj.trademarks___;
    for (var i = 0; i < list.length; ++i) {
      if (list[i] === trademark) { return true; }
    }
    return false;
  }

  /**
   * Throws an exception if the object does not have any trademarks or
   * the given trademark is not in the list of trademarks.
   */
  function guard(trademark, obj) {
    if (!hasTrademark(trademark, obj)) {
      fail('Object "' + obj + '" does not have the "'
	   + (trademark.toString() || '*unknown*') + '" trademark');
    }
  }

  /**
   * This function adds the given trademark to the given object's list of
   * trademarks.
   * If the trademark list doesn't exist yet, this function creates it.
   * JSON containers and functions may be stamped at any time; constructed
   * objects may only be stamped during construction unless the third
   * parameter is truthy.
   */
  function stamp(trademark, obj, opt_allow_constructed) {
    if (typeOf(trademark) !== 'object') {
      fail('The supplied trademark is not an object.');
    }
    if (isFrozen(obj)) { fail('The supplied object ' + obj + ' is frozen.'); }
    if (!isJSONContainer(obj) &&
        (typeOf(obj) !== 'function') &&
        !obj.underConstruction___ &&
        !opt_allow_constructed) {
      fail('The supplied object ', obj,
           ' has already been constructed and may not be stamped.');
    }
    var list = obj.underConstruction___ ?
        'delayedTrademarks___' : 'trademarks___';
    if (!obj[list]) { obj[list] = []; }
    obj[list].push(trademark);
    return obj;
  }

  function initializeMap(list) {
    var result = {};
    for (var i = 0; i < list.length; i += 2) {
      // Call asFirstClass() here to prevent, for example, a toxic
      // function being used at the toString property of an object
      // literal.
      setPub(result, list[i], asFirstClass(list[i + 1]));
    }
    return result;
  }

  ////////////////////////////////////////////////////////////////////////
  // Sealing and Unsealing
  ////////////////////////////////////////////////////////////////////////

  /**
   * Returns a pair of functions such that the seal(x) wraps x in an object
   * so that only unseal can get x back from the object.
   *
   * @return {object} of the form
   *     { seal: function seal(x) { return {}; },
   *       unseal: function unseal(obj) { return x; } }.
   */
  function makeSealerUnsealerPair() {
    var flag = false;  // Was a box successfully unsealed
    var squirrel = null;  // Receives the payload from an unsealed box.
    function seal(payload) {
      function box() {
        flag = true;
        squirrel = payload;
      }
      box.toString = frozenFunc(function toString() { return '(box)'; });
      return frozenFunc(box);
    }
    function unseal(box) {
      // Start off in a known good state.
      flag = false;
      squirrel = null;
      try {  // Don't do anything outside try to foil forwarding functions.
        box.CALL___();
        if (!flag) { throw new Error('Sealer/Unsealer mismatch'); }
        return squirrel;
      } finally {
        // Restore to a known good state.
        flag = false;
        squirrel = null;
      }
    }
    return freeze({
      seal: frozenFunc(seal),
      unseal: frozenFunc(unseal)
    });
  }

  ////////////////////////////////////////////////////////////////////////
  // Needed for Valija
  ////////////////////////////////////////////////////////////////////////

  /**
   * <tt>cajita.construct(ctor, [args...])</tt> invokes a simple function as
   * a constructor using 'new'.
   */
  function construct(ctor, args) {
    ctor = asCtor(ctor);
    // This works around problems with (new Array()) and (new Date()) where
    // the returned object is not really a Date or Array on SpiderMonkey and
    // other interpreters.
    switch (args.length) {
      case 0:  return new ctor();
      case 1:  return new ctor(args[0]);
      case 2:  return new ctor(args[0], args[1]);
      case 3:  return new ctor(args[0], args[1], args[2]);
      case 4:  return new ctor(args[0], args[1], args[2], args[3]);
      case 5:  return new ctor(args[0], args[1], args[2], args[3], args[4]);
      case 6:  return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5]);
      case 7:  return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6]);
      case 8:  return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6], args[7]);
      case 9:  return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6], args[7], args[8]);
      case 10: return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6], args[7], args[8], args[9]);
      case 11: return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6], args[7], args[8], args[9],
                               args[10]);
      case 12: return new ctor(args[0], args[1], args[2], args[3], args[4],
                               args[5], args[6], args[7], args[8], args[9],
                               args[10], args[11]);
      default:
        if (ctor.typeTag___ === 'Array') {
          return ctor.apply(USELESS, args);
        }
        var tmp = function tmp(args) {
          return ctor.apply(this, args);
        };
        tmp.prototype = ctor.prototype;
        return new tmp(args);
    }
  }

  /**
   * Create a unique identification of a given table identity that can
   * be used to invisibly (to Cajita code) annotate a key object to
   * index into a table.
   * <p>
   * magicCount and MAGIC_TOKEN together represent a
   * unique-across-frames value safe against collisions, under the
   * normal Caja threat model assumptions. magicCount and
   * MAGIC_NAME together represent a probably unique across frames
   * value, with which can generate strings in which collision is
   * unlikely but possible.
   * <p>
   * The MAGIC_TOKEN is a unique unforgeable per-Cajita runtime
   * value. magicCount is a per-Cajita counter, which increments each
   * time a new one is needed.
   */
  var magicCount = 0;
  var MAGIC_NUM = Math.random();
  var MAGIC_TOKEN = Token('MAGIC_TOKEN_FOR:' + MAGIC_NUM);
  // Using colons in the below causes it to fail on IE since getting a
  // property whose name contains a semicolon on a DOM table element causes
  // an exception.
  var MAGIC_NAME = '_index;'+ MAGIC_NUM + ';';

  /**
   * Creates a new mutable associative table mapping from the
   * identity of arbitrary keys (as defined by tt>identical()</tt>) to
   * arbitrary values.
   * <p>
   * Once there is a conventional way for JavaScript implementations
   * to provide weak-key tables, this should feature-test and use that
   * where it is available, in which case the opt_useKeyLifetime flag
   * can be ignored. When no weak-key table is primitively provided,
   * this flag determines which of two possible approximations to
   * use. In all three cases (actual weak key tables,
   * opt_useKeyLifetime is falsy, and opt_useKeyLifetime is
   * truthy), the table returned
   * <ul>
   * <li>should work across frames, 
   * <li>should have O(1) complexity measure within a frame where
   *     collision is impossible,
   * <li>and should have O(1) complexity measure between frames with
   *     high probability.
   * <li>the table should not retain its keys. In other words, if a
   *     given table T is non-garbage but a given value K is otherwise
   *     garbage, the presence of K as a key in table T will
   *     not, by itself, prevent K from being garbage collected. (Note
   *     that this is not quite as aggressive as the contract provided
   *     by ephemerons.)
   * </ul>
   * Given that a K=>V association has been stored in table T, the
   * three cases differ according to how long they retain V:
   * <li>A genuine weak-key table retains V only while both T and K
   *     are not garbage.
   * <li>If opt_useKeyLifetime is falsy, retain V while T is not
   *     garbage.
   * <li>If opt_useKeyLifetime is truthy, retain V while K is not
   *     garbage. In this case, K must be an object rather than a
   *     primitive value.
   * </ul>
   * <p>
   * To support Domita, the keys might be host objects.
   */
  function newTable(opt_useKeyLifetime) {
    magicCount++;
    var myMagicIndexName = MAGIC_NAME + magicCount + '___';

    function setOnKey(key, value) {
      if (key !== Object(key)) {
        fail("Can't use key lifetime on primitive keys: ", key);
      }
      var list = key[myMagicIndexName];
      if (!list) {
        key[myMagicIndexName] = [MAGIC_TOKEN, value];
      } else {
        var i = 0;
        for (; i < list.length; i += 2) {
          if (list[i] === MAGIC_TOKEN) { break; }
        }
        list[i] = MAGIC_TOKEN;
        list[i + 1] = value;
      }
    }

    function getOnKey(key) {
      if (key !== Object(key)) {
        fail("Can't use key lifetime on primitive keys: ", key);
      }
      var list = key[myMagicIndexName];
      if (!list) {
        return void 0;
      } else {
        var i = 0;
        for (; i < list.length; i += 2) {
          if (list[i] === MAGIC_TOKEN) { return list[i + 1]; }
        }
        return void 0;
      }
    }

    if (opt_useKeyLifetime) {
      return primFreeze({
        set: frozenFunc(setOnKey),
        get: frozenFunc(getOnKey)
      });
    }

    var myValues = [];

    function setOnTable(key, value) {
      switch (typeof key) {
        case 'object':
        case 'function': {
          if (null === key) { myValues.prim_null = value; return; }
          var index = getOnKey(key);
          if (index === void 0) {
            index = myValues.length;
            setOnKey(key, index);
          }
          myValues[index] = value;
          return;
        }
        case 'string': { myValues['str_' + key] = value; return; }
        default: { myValues['prim_' + key] = value; return; }
      }
    }

    /**
     * If the key is absent, returns <tt>undefined</tt>.
     * <p>
     * Users of this table cannot distinguish an <tt>undefined</tt>
     * value from an absent key.
     */
    function getOnTable(key) {
      switch (typeof key) {
        case 'object':
        case 'function': {
          if (null === key) { return myValues.prim_null; }
          var index = getOnKey(key);
          if (void 0 === index) { return void 0; }
          return myValues[index];
        }
        case 'string': { return myValues['str_' + key]; }
        default: { return myValues['prim_' + key]; }
      }
    }

    return primFreeze({
      set: frozenFunc(setOnTable),
      get: frozenFunc(getOnTable)
    });
  }


  /**
   * Is <tt>allegedParent</tt> on <obj>'s prototype chain?
   * <p>
   * Although in raw JavaScript <tt>'foo' instanceof String</tt> is
   * false, to reduce the differences between primitives and their
   * wrappers, <tt>inheritsFrom('foo', String.prototype)</tt> is true.
   */
  function inheritsFrom(obj, allegedParent) {
    if (null === obj) { return false; }
    if (void 0 === obj) { return false; }
    if (typeOf(obj) === 'function') { return false; }
    if (typeOf(allegedParent) !== 'object') { return false; }
    if (null === allegedParent) { return false; }
    function F() {}
    F.prototype = allegedParent;
    return Object(obj) instanceof F;
  }

  /**
   * Return func.prototype's directConstructor.
   * <p>
   * When following the "classical" inheritance pattern (simulating
   * class-style inheritance as a pattern of prototypical
   * inheritance), func may represent (the constructor of) a class; in
   * which case getSuperCtor() returns (the constructor of) its
   * immediate superclass.
   */
  function getSuperCtor(func) {
    enforceType(func, 'function');
    if (isCtor(func) || isFunc(func)) {
      var result = directConstructor(func.prototype);
      if (isCtor(result) || isFunc(result)) {
        return result;
      }
    }
    return void 0;
  }

  var attribute = new RegExp(
      '^([\\s\\S]*)_(?:canRead|canCall|getter|handler)___$');

  /**
   * Returns a list of all cajita-readable own properties, whether or
   * not they are cajita-enumerable. 
   */
  function getOwnPropertyNames(obj) {
    var result = [];
    var seen = {};
    // TODO(erights): revisit once we do es3.1ish attribute control.
    var implicit = isJSONContainer(obj);
    for (var k in obj) {
      if (hasOwnProp(obj, k)) {
        if (implicit && !endsWith__.test(k)) {
          if (!myOriginalHOP.call(seen, k)) {
            seen[k] = true;
            result.push(k);
          }
        } else {
          var match = attribute.exec(k);
          if (match !== null) {
            var base = match[1];
            if (!myOriginalHOP.call(seen, base)) {
              seen[base] = true;
              result.push(base);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Return the names of the accessible own properties of
   * func.prototype.
   * <p>
   * Since prototypical objects are not themselves accessible in
   * Cajita, this means in effect: the properties contributed by
   * func.prototype that would be accessible on objects that inherit
   * from func.prototype.
   */
  function getProtoPropertyNames(func) {
    enforceType(func, 'function');
    return getOwnPropertyNames(func.prototype);
  }

  /**
   * Return the value associated with func.prototype[name].
   * <p>
   * Since prototypical objects are not themselves accessible in
   * Cajita, this means in effect: If x inherits name from
   * func.prototype, what would the value of x[name] be? If the value
   * associated with func.prototype[name] is an exophoric function
   * (resulting from taming a generic method), then return the
   * corresponding pseudo-function. See reifyIfXo4a().
   */
  function getProtoPropertyValue(func, name) {
    return asFirstClass(readPub(func.prototype, name));
  }

  /**
   * Like primBeget(), but applicable only to records.
   */
  function beget(parent) {
    if (!isRecord(parent)) {
      fail('Can only beget() records: ', parent);
    }
    return primBeget(parent);
  }

  
  ////////////////////////////////////////////////////////////////////////
  // Exports
  ////////////////////////////////////////////////////////////////////////
  cajita = {
    // Diagnostics and condition enforcement
    log: log,
    fail: fail,
    enforce: enforce,
    enforceType: enforceType,
    enforceNat: enforceNat,

    // walking prototype chain, checking JSON containers
    directConstructor: directConstructor,
    getFuncCategory: getFuncCategory,
    isDirectInstanceOf: isDirectInstanceOf,
    isInstanceOf: isInstanceOf,
    isRecord: isRecord,           isArray: isArray,
    isJSONContainer: isJSONContainer,
    freeze: freeze,               isFrozen: isFrozen,
    copy: copy,                   snapshot: snapshot,

    // Accessing properties
    canReadPub: canReadPub,       readPub: readPub,
    hasOwnPropertyOf: hasOwnPropertyOf,
                                  readOwn: readOwn,
    canEnumPub: canEnumPub,
    canEnumOwn: canEnumOwn,
    canInnocentEnum: canInnocentEnum,
    BREAK: BREAK,
    allKeys: allKeys,             forAllKeys: forAllKeys,
    ownKeys: ownKeys,             forOwnKeys: forOwnKeys,
    canCallPub: canCallPub,       callPub: callPub,
    canSetPub: canSetPub,         setPub: setPub,
    canDeletePub: canDeletePub,   deletePub: deletePub,

    // Trademarking
    Trademark: Trademark,
    hasTrademark: hasTrademark,
    guard: guard,

    // Sealing & Unsealing
    makeSealerUnsealerPair: makeSealerUnsealerPair,

    // Other
    USELESS: USELESS,
    manifest: manifest,

    // Needed for Valija
    construct: construct,
    newTable: newTable,
    inheritsFrom: inheritsFrom,
    getSuperCtor: getSuperCtor,
    getOwnPropertyNames: getOwnPropertyNames,
    getProtoPropertyNames: getProtoPropertyNames,
    getProtoPropertyValue: getProtoPropertyValue,
    beget: beget
  };

  forOwnKeys(cajita, frozenFunc(function(k, v) {
    switch (typeOf(v)) {
      case 'object': {
        if (v !== null) { primFreeze(v); }
        break;
      }
      case 'function': {
        frozenFunc(v);
        break;
      }
    }
  }));

  safeJSON = {
    parse: frozenFunc(jsonParse),
    stringify: frozenFunc(function(obj) {
      // TODO(ihab.awad): Missing support
      return '';
    })
  };

  sharedImports = {
    cajita: cajita,

    'null': null,
    'false': false,
    'true': true,
    'NaN': NaN,
    'Infinity': Infinity,
    'undefined': void 0,
    parseInt: frozenFunc(parseInt),
    parseFloat: frozenFunc(parseFloat),
    isNaN: frozenFunc(isNaN),
    isFinite: frozenFunc(isFinite),
    decodeURI: frozenFunc(decodeURI),
    decodeURIComponent: frozenFunc(decodeURIComponent),
    encodeURI: frozenFunc(encodeURI),
    encodeURIComponent: frozenFunc(encodeURIComponent),
    escape: escape ? frozenFunc(escape) : (void 0),
    Math: Math,
    JSON: safeJSON,

    Object: Object,
    Array: Array,
    String: String,
    Boolean: Boolean,
    Number: Number,
    Date: Date,
    RegExp: RegExp,

    Error: Error,
    EvalError: EvalError,
    RangeError: RangeError,
    ReferenceError: ReferenceError,
    SyntaxError: SyntaxError,
    TypeError: TypeError,
    URIError: URIError
  };

  forOwnKeys(sharedImports, frozenFunc(function(k, v) {
    switch (typeOf(v)) {
      case 'object': {
        if (v !== null) { primFreeze(v); }
        break;
      }
      case 'function': {
        primFreeze(v);
        break;
      }
    }
  }));
  primFreeze(sharedImports);

  ___ = {
    // Diagnostics and condition enforcement
    getLogFunc: getLogFunc,
    setLogFunc: setLogFunc,

    primFreeze: primFreeze,

    // Accessing property attributes.
    canRead: canRead,        grantRead: grantRead,
    canEnum: canEnum,        grantEnumOnly: grantEnumOnly,
    // TODO(mikesamuel): should grantCall be exported?
    canCall: canCall,        grantCall: grantCall,
    canSet: canSet,          grantSet: grantSet,
    canDelete: canDelete,    grantDelete: grantDelete,

    // Module linkage
    readImport: readImport,

    // Classifying functions
    isCtor: isCtor,
    isFunc: isFunc,
    isXo4aFunc: isXo4aFunc,
    ctor: ctor,
    func: func,       frozenFunc: frozenFunc,
    asFunc: asFunc,   toFunc: toFunc,
    xo4a: xo4a,
    initializeMap: initializeMap,

    // Accessing properties
    inPub: inPub,
    canSetStatic: canSetStatic,   setStatic: setStatic,

    // Other
    typeOf: typeOf,
    hasOwnProp: hasOwnProp,
    identical: identical,
    args: args,
    tameException: tameException,
    primBeget: primBeget,
    callStackUnsealer: callStackSealer.unseal,
    RegExp: RegExp,  // Available to rewrite rule w/o risk of masking
    stamp: stamp,
    asFirstClass: asFirstClass,

    // Taming mechanism
    useGetHandler: useGetHandler,
    useApplyHandler: useApplyHandler,
    useCallHandler: useCallHandler,
    useSetHandler: useSetHandler,
    useDeleteHandler: useDeleteHandler,

    grantFunc: grantFunc,
    grantGeneric: grantGeneric,
    handleGeneric: handleGeneric,
    grantTypedGeneric: grantTypedGeneric,
    grantMutator: grantMutator,

    enforceMatchable: enforceMatchable,
    all2: all2,

    // Taming decisions
    sharedImports: sharedImports,

    // Module loading
    getNewModuleHandler: getNewModuleHandler,
    setNewModuleHandler: setNewModuleHandler,
    obtainNewModule: obtainNewModule,
    makeNormalNewModuleHandler: makeNormalNewModuleHandler,
    loadModule: loadModule,
    NO_RESULT: NO_RESULT,

    getId: getId,
    getImports: getImports,
    unregister: unregister
  };

  forOwnKeys(cajita, frozenFunc(function(k, v) {
    if (k in ___) {
      fail('internal: initialization conflict: ', k);
    }
    if (typeOf(v) === 'function') {
      grantFunc(cajita, k);
    }
    ___[k] = v;
  }));
  setNewModuleHandler(makeNormalNewModuleHandler());
})(this);
