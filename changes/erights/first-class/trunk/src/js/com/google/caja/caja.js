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
// .............................................................................

// This module is the Caja runtime library. It is written in
// Javascript, not Caja, and would be rejected by the Caja
// translator. This module exports two globals: 
// * "<tt>___</tt>" for use by the output of the Caja translator and
//   by some other untranslated Javascript code.
// * "<tt>caja</tt>" providing some common services to the Caja
//   programmer. 
// <p>
// <i>Innocent code</i> is code which we assume to be ignorant of
// Caja, not to be actively hostile, but which may be buggy (and
// therefore accidentally harmful or exploitable). This corresponds to
// legacy code, such as libraries, that we decide to run untranslated,
// perhaps hidden or tamed, but which needs to co-exist smoothly with
// the Caja runtime. 
// <p>
// We assume that innocent code uses unfiltered for/in loops only to
// enumerate direct instances of <tt>Object</tt>, i.e., objects that
// directly inherit from <tt>Object.prototype</tt>.
// <p>
// In order not to disrupt innocent code, the Caja runtime adds no
// properties to <tt>Object.prototype</tt> itself. The only property
// names Caja adds to other primordial objects are names ending in
// triple underbar. The actual spelling of all names ending in triple
// underbar is considered private to the Caja implementation and may
// change without notice over time. Any for/in loops in innocent code
// used to enumerate any primordial object other than
// <tt>Object.prototype</tt>, or any object that inherits from such an
// object, may need to be modified to skip properties ending with
// triple underbar. See <tt>canInnocentEnum</tt> below.


var caja;
var ___;

// Explicitly passing in the actual global object to avoid
// <tt>ReferenceError</tt>s when referring to potentially nonexistent
// objects like <tt>HTMLDivElement</tt>.

(function(theGlobalObject) {
  
  ////////////////////////////////////////////////////////////////////////
  // Diagnostics and condition enforcement
  ////////////////////////////////////////////////////////////////////////
  
  var vowel_ = new RegExp('^[aeiouAEIOU]');

  /**
   * Given an array of values, concatenate string representations of
   * these together into a diagnostic string.
   * <p>
   * Objects and functions are converted to strings explaining
   * type-like information rather than the object's value. If you want
   * the diagnostic to show an object's value, convert that object to
   * a string yourself, e.g. by <tt>String(obj)</tt>.
   * <p>
   * TODO(erights,msamuel): Explore the possible relationship with
   * msamuel's lightweight safe interpolation proposal.
   */
  function diagnostic(arr) {
    var strs = [];
    var len = arr.length;
    for (var i = 0; i < len; i++) {
      var value = arr[i];
      if (typeof value === 'object') {
        if (value !== null) {
          var typ = diagnostic([constructorOf(value)]);
          if (vowel_.test(typ)) {
            value = '(an ' + typ + ')';
          } else {
            value = '(a ' + typ + ')';
          }
        }
      } else if (typeof value === 'function' && 'NAME___' in value) {
        value = value.NAME___;
      }
      strs.push(String(value));
    }
    return strs.join('');
  }

  /**
   * A logging function is a function of a single diagnostic string
   * argument. 
   * <p>
   * The initial default logging function does nothing. 
   * <p>
   * Note: JavaScript has no macros, so even in the "does nothing"
   * case, remember that the arguments are still evaluated. 
   */
  var myLogFunc_ = function(str) {};

  /**
   * Calls the currently registered logging function.
   */
  function log(var_args) { myLogFunc_(diagnostic(arguments)); }

  /**
   * Gets the currently registered logging function.
   */
  function getLogFunc() { return myLogFunc_; }

  /**
   * Register a new logging function.
   * <p>
   * For example, when using the squarefree shell, after loading
   * caja.js, it's often useful to do<pre>
   *     ___.setLogFunc(print);
   * </pre>
   */
  function setLogFunc(newLogFunc) {
    log('Log about to be redirected');
    myLogFunc_ = newLogFunc;
    log('Log redirected');
  }


  /** 
   * Throw, and optionally log, an error whose message is the
   * <tt>diagnostic()</tt> concatentation of the arguments.
   */
  function fail(var_args) {
    var message = diagnostic(arguments);
    myLogFunc_(message);
    throw new Error(message);
  }

  /**
   * A convenient static generic
   */
  function slice(arrLike, start) {
    return Array.prototype.slice.call(arrLike, start);
  }
  
  /** 
   * Like an assert that can't be turned off.
   * <p>
   * Either returns true (on success) or throws (on failure). The
   * arguments starting with <tt>var_args</tt> are converted to a
   * <tt>diagnostic()</tt> message of the Error that's thrown. 
   * <p>
   * TODO(erights): Remove, and replace all uses with<pre>
   *     if (!test) { fail(var_args...); }
   * </pre>
   *
   * @deprecated Use <tt>if (!test) { fail(var_args...); }</tt> instead.
   */
  function enforce(test, var_args) {
    return test || fail.apply({}, slice(arguments, 1));
  }
  
  /**
   * Enforces <tt>typeof specimen === typename</tt>, in which case
   * <tt>specimen</tt> is returned.
   * <p>
   * If not, throws an informative error.
   * <p>
   * <tt>opt_name</tt>, if provided, should be a name or description
   * of the <tt>specimen</tt> used only to generate friendlier error
   * messages. 
   */
  function enforceType(specimen, typename, opt_name) {
    if (typeof specimen !== typename) {
      fail('Expected ', typename, ' instead of ', typeof specimen,
           ': ', (opt_name ? String(opt_name) : specimen));
    }
    return specimen;
  }
  
  /**
   * Enforces that <tt>specimen</tt> is a non-negative integer within
   * the range of exactly representable consecutive integers, in which
   * case <tt>specimen</tt> is returned. 
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
    if (Math.floor(specimen-1) !== specimen-1) {
      fail('Beyond precision limit: ', specimen);
    }
    if (Math.floor(specimen-1) >= specimen) {
      fail('Must not be infinite: ', specimen);
    }
    return specimen;
  }

  /**
   * Returns a new object whose only utility is its identity and (for
   * diagnostic purposes only) its name.
   */
  function Token(name) {
    return primFreeze({
      toString: primFreeze(simpleFunc(function() {
        return name;
      }))
    });
  }

  primFreeze(simpleFunc(Token));

  /**
   * When a <tt>this</tt> value must be provided but nothing is
   * suitable, provide this useless object instead.
   */
  var USELESS = Token('USELESS');

  ////////////////////////////////////////////////////////////////////////
  // thePseudoProto
  ////////////////////////////////////////////////////////////////////////

  /**
   * The holder of properties pseudo-added to Object.prototype.
   * <p>
   * <i>Innocent code</i>
   * innocent code to enumerate direct instances of
   * <tt>Object</tt> (i.e., objects that inherit directly from
   * <tt>Object.prototype</tt>) safely with unfiltered for/in loops,
   * the Caja runtime must avoid adding any properties to
   * <tt>Object.prototype</tt>.
   */
  var thePseudoProto = {};
  
  /**
   * Acts like <tt>name in obj</tt>, but includes properties
   * pseudo-inherited from <tt>thePseudoProto</tt>.
   */
  function pIn(obj, name) {
    return (name in obj) || (name in thePseudoProto);
  }

  /**
   * Acts like <tt>obj[name]</tt>, but includes properties
   * pseudo-inherited from <tt>thePseudoProto</tt>.
   */
  function pRead(obj, name) {
    if (name in obj) {
      return obj[name];
    } else {
      return thePseudoProto[name];
    }
  }

  /**
   * Acts like <tt>obj[name](args...), but includes properties
   * pseudo-inherited from <tt>thePseudoProto</tt>.
   */
  function pCall(obj, name, args) {
    if (name in obj) {
      return obj[name].apply(obj, args);
    } else {
      return thePseudoProto[name].apply(name, args);
    }
  }

  /**
   * Acts like <tt>obj[name] = val</tt>, but if obj is
   * <tt>Object.prototype</tt>, adds the property instead to
   * <tt>thePseudoProto</tt>. 
   * <p>
   * Will refuse to pseudo-set a property on <tt>Object.prototype</tt>
   * that conflicts with a property on the actual
   * <tt>Object.prototype</tt>. If would be better to allow such a set
   * and have <tt>pRead</tt> consider <tt>thePseudoProto</tt> to have
   * precedence over <tt>Object.prototype</tt>. However, we know of no
   * cheap enough way to test whether <tt>obj</tt> inherits its
   * <tt>name</tt> property directly from
   * <tt>Object.prototype</tt>. The obvious <tt>obj[name] ===
   * Object.prototype[name]</tt> would be true if has or inherits a
   * property of the same name and value.
   */
  function pSet(obj, name, val) {
    if (obj === Object.prototype) {
      if (name in obj) {
        fail("Internal: Conflicts with existing Object.prototype.",
             name);
      }
      return thePseudoProto[name] = val;
    } else {
      return obj[name] = val;
    }
  }

  /**
   *
   */
  function pDelete(obj, name) {
    if (delete obj[name]) {
      return true;
    } else if (obj === Object.prototype) {
      return delete thePseudoProto[name];
    } else {
      return false;
    }
  }

  /**
   * Is defined and returns true only if we can call
   * <tt>Object.prototype.hasOwnProperty</tt> on this object without
   * exploding. 
   * <p>
   * On Firefox, it seems that calling <tt>hasOwnProperty</tt> on an
   * <tt>HTMLDivElement</tt> sometimes causes an
   * "Illegal operation on WrappedNative prototype object".
   * <p>
   * SECURITY BUG STOPGAP TODO(erights)
   */
  var canCallHasOwnProperty_;
  
  // When we're in a non-browser environment, such that there isn't
  // a global <tt>HTMLDivElement</tt>, then we don't need to worry
  // about this bug. We also don't need to worry about this bug for
  // internal uses of <tt>hasOwnProp()</tt> that happens during the
  // initialization of caja.js. Since <tt>hasOwnProp()</tt> might be
  // called before the following <tt>if</tt> runs, it guards the test
  // with <pre>!canCallHasOwnProperty_ ||</pre> which will be true
  // at least until the following <tt>if</tt> runs.
  if (typeof theGlobalObject.HTMLDivElement === 'function') {
    canCallHasOwnProperty_ = function(obj) {
      return !(obj instanceof theGlobalObject.HTMLDivElement);
    };
  }
  
  /**
   * <tt>hasOwnProp(obj.prop)</tt> means what
   * <tt>obj.hasOwnProperty(prop)</tt> would normally mean, but<ul>
   * <li>Avoids testing known-untestable objects
   * <li>Uses <tt>Object.prototype.hasOwnProperty.call</tt> so it
   *     doesn't get mislead by an override.
   * <li>If obj is <tt>Object.prototype</tt>, also checks
   *     <tt>thePseudoProto</tt>.
   * </ul>
   */
  function hasOwnProp(obj, name) { 
    var t = typeof obj;
    if (t !== 'object' && t !== 'function') { 
      return false; 
    }
    if (!canCallHasOwnProperty_ || canCallHasOwnProperty_(obj)) {
      // Fails in Firefox for some DOM objects intermittently(?!) 
      // with "Illegal operation on WrappedNative prototype object".
      // For these, <tt>canCallHasOwnProperty_()</tt> must say false.
      if (Object.prototype.hasOwnProperty.call(obj, name)) {
        return true;
      } else if (obj === Object.prototype) {
        return name in thePseudoProto;
      }
    } else {
      return false;
    }
  }
  
  ////////////////////////////////////////////////////////////////////////
  // Accessing property attributes
  ////////////////////////////////////////////////////////////////////////
  
  /** Tests whether the fast-path canRead flag is set. */
  function canRead(obj, name)   { 
    return !!pRead(obj, name + '_canRead___'); 
  }

  /** Tests whether the fast-path canEnum flag is set. */
  function canEnum(obj, name)   { 
    return !!pRead(obj, name + '_canEnum___'); 
  }

  /** Tests whether the fast-path canCall flag is set. */
  function canCall(obj, name)   { 
    return !!pRead(obj, name + '_canCall___'); 
  }
  
  /** Tests whether the fast-path canSet flag is set. */
  function canSet(obj, name)    { 
    return !!pRead(obj, name + '_canSet___'); 
  }

  /** Tests whether the fast-path canDelete flag is set. */
  function canDelete(obj, name) { 
    return !!pRead(obj, name + '_canDelete___'); 
  }
  
  /** 
   * Sets the fast-path canRead flag.
   * <p>
   * The various <tt>allow*</tt> functions are called externally by
   * Javascript code to express whitelisting taming decisions. And
   * they are called internally to record decisions arrived at by
   * other means. 
   */
  function allowRead(obj, name) { 
    pSet(obj, name + '_canRead___', true); 
  }
  
  /** <tt>allowEnum</tt> implies <tt>allowRead</tt> */
  function allowEnum(obj, name) { 
    allowRead(obj, name);
    pSet(obj, name + '_canEnum___', true);
  }
  
  /** 
   * Simple-functions should callable and readable, but methods
   * should only be callable.
   */
  function allowCall(obj, name) { 
    pSet(obj, name + '_canCall___', true); 
  }
  
  /**
   * <tt>allowSet</tt> implies <tt>allowEnum</tt> and <tt>allowRead</tt>.
   */
  function allowSet(obj, name) {
    if (isFrozen(obj)) {
      fail("Can't set .", name, ' on frozen: ', obj);
    }
    allowEnum(obj, name);
    pSet(obj, name + '_canSet___', true);
  }
  
  /**
   * BUG TODO(erights): <tt>allowDelete</tt> is not yet specified or
   * implemented. 
   */
  function allowDelete(obj, name) {
    if (isFrozen(obj)) {
      fail("Can't delete .", name, ' on frozen: ', obj);
    }
    fail('TODO(erights): allowDelete() not yet implemented');
    pSet(obj, name + '_canDelete___', true);
  }
  
  ////////////////////////////////////////////////////////////////////////
  // Privileged fault handlers
  ////////////////////////////////////////////////////////////////////////
  
  /**
   * Log additional secret diagnostic information.
   * <p>
   * We assume that Caja code can catch and examine thrown exceptions,
   * but that only <i>privileged code</i> can examine <tt>log()</tt>ed
   * information. If a JavaScript property is not accessible to Caja
   * code, it may be because that property does not exist, or that
   * Caja code has not been allowed to access it. Unprivileged Caja
   * code must not be able to distinguish these cases, but the
   * distinction should be logged for diagnostic purposes.
   */
  function logAbsence_(obj, name) {
    if (!pIn(obj, name)) {
      log('Since there is no ', obj, '.', name, '...');
    }
  }

  /**
   * The static generic form of <tt>bind()</tt>, but with the
   * arguments in an explicit list rather than spread out.
   * <p>
   * <tt>bindHandler()</tt> itself does check <tt>asMethodOf(that,fun)</tt>.
   */
  function bindHandler(fun, bindArgs) {
    var that = bindArgs[0];
    var subArgs = slice(bindArgs, 1);
    return primFreeze(simpleFunc(function(var_args) {
      return fun.apply(that, subArgs.concat(args(arguments)));
    }));
  }
  
  /**
   * A <i>Keeper</i> is a fault-handler of last resort.
   * <p>
   * This default keeper is installed in <tt>thePseudoProto</tt>, so
   * that it can be overridden on a per-class or per-instance basis.
   * <p>
   * <tt>theDefaultKeeper</tt> always <tt>log()</tt>s and reports
   * failure. For read-faults, it reports failure by returning
   * <tt>undefined</tt>. For other faults, it throws. Keepers should
   * generally only be replaced or shadowed for diagnostic purposes
   * (as permissive.js does), whereas handlers can be replaced or
   * shadowed for normal functionality, such as taming decisions or
   * access abstractions.
   */
  var theDefaultKeeper = primFreeze({

    toString: primFreeze(simpleFunc(function() { 
      return '<Default Logging Keeper>'; 
    })),

    /**
     * Handle a read-fault, tring to read 
     * <pre>obj[name]</pre>. 
     * <p>
     * The default keeper logs a diagnostic and returns
     * <tt>undefined</tt>. 
     */
    handleRead: primFreeze(simpleFunc(function(obj, name) {
      logAbsence_(obj, name);
      log('Not readable: ', obj, '.', name);
      return undefined; 
    })),

    /**
     * Handle call-fault, trying to call 
     * <pre>obj[name](args...)</pre>.
     * <p>
     * The default keeper logs and throws a diagnostic.
     */
    handleCall: primFreeze(simpleFunc(function(obj, name, args) {
      logAbsence_(obj, name);
      fail('Not callable: ', obj, '.', name);
    })),

    /**
     * Handle a set-fault, trying to set 
     * <pre>obj[name] = val</pre>.
     * <p>
     * The default keeper logs and throws a diagnostic.
     */
    handleSet: primFreeze(simpleFunc(function(obj, name, val) {
      logAbsence_(obj, name);
      fail('Not settable: ', obj, '.', name);
    })),

    /**
     * Handle a delete-fault, trying to
     * <pre>delete obj[name]</pre>.
     * <p>
     * The default keeper logs and throws a diagnostic.
     */
    handleDelete: primFreeze(simpleFunc(function(obj, name) {
      logAbsence_(obj, name);
      fail('Not deletable: ', obj, '.', name);
    }))
  });

  /**
   * If Caja code attempts to access some property of some object, and
   * normal access is denied, then the object's
   * <tt>.faultHandler___</tt> is invoked as a first resort to handle the
   * fault generically. 
   * <p>
   * This default fault-handler is installed in
   * <tt>thePseudoProto</tt>, so that it can be overridden on a
   * per-class or per-instance basis. <tt>theDefaultHandler</tt> first
   * checks if there is a fault-handling function available on that object
   * to handle this fault on this specific property name. If so, then it
   * delegates to that fault-handling function. Otherwise it delegates to
   * the object's <tt>.keeper___</tt>.
   */
  var theDefaultHandler = primFreeze({

    toString: primFreeze(simpleFunc(function() { 
      return '<Default fault handler>'; 
    })),

    /**
     * If there's no getter, but there is either is a callable method
     * or an apply-handler, then return as a method attached to
     * <tt>obj</tt>. 
     * <p>
     * In order to support the JavaScript feature testing pattern when
     * the feature being tested is a method, we need to return
     * <i>something</i> (other than <tt>undefined</tt>) when an attempt
     * is made to read a callable-but-not readable method. The most
     * useful safe thing to return that's still within a fail-stop
     * subset of JavaScript is the result of attaching that method to
     * this instance.
     */
    handleRead: primFreeze(simpleFunc(function(obj, name) {
      var handlerName = name + '_getter___';
      if (pIn(obj, handlerName)) {
        return pRead(obj, handlerName).call(USELESS, obj);
      }
      var applyHandlerName = name + '_handler___';
      var handler = pRead(obj, applyHandlerName);
      if (handler) {
	fail("TODO(erights): Can't yet attach fault handlers: ", name);
      }
      return pRead(obj, 'keeper___').handleRead(obj, name);
    })),

    handleCall: primFreeze(simpleFunc(function(obj, name, args) {
      var handlerName = name + '_handler___';
      if (pIn(obj, handlerName)) {
        return pRead(obj, handlerName).call(USELESS, obj, args);
      }
      return pRead(obj, 'keeper___').handleCall(obj, name, args);
    })),

    handleSet: primFreeze(simpleFunc(function(obj, name, val) {
      var handlerName = name + '_setter___';
      if (pIn(obj, handlerName)) {
        return pRead(obj, handlerName).call(USELESS, obj, val);
      }
      return pRead(obj, 'keeper___').handleSet(obj, name, val);
    })),

    handleDelete: primFreeze(simpleFunc(function(obj, name) {
      var handlerName = name + '_deleter___';
      if (pIn(obj, handlerName)) {
        return pRead(obj, handlerName).call(USELESS, obj);
      }
      return pRead(obj, 'keeper___').handleDelete(obj, name);
    }))
  });


  pSet(Object.prototype, 'keeper___', theDefaultKeeper);
  pSet(Object.prototype, 'faultHandler___', theDefaultHandler);
  
  ////////////////////////////////////////////////////////////////////////
  // walking prototype chain, checking JSON containers
  ////////////////////////////////////////////////////////////////////////
  
  /**
   * Does <tt>str</tt> end with <tt>suffix</tt>?
   * <p>
   * TODO(erights): Replace all uses of <tt>endsWith()</tt> with a
   * literal <tt>suffix</tt> to instead use a <tt>RegExp</tt>. When
   * doing so, be careful to ensure that <tt>str</tt> is <i>already</i> a
   * string. 
   */
  function endsWith(str, suffix) {
    enforceType(str, 'string');
    enforceType(suffix, 'string');
    var strLen = str.length;
    var sufLen = suffix.length;
    return strLen >= sufLen && 
      (str.substring(strLen-sufLen, strLen) === suffix);
  }
  
  /**
   * Returns the '<tt>.constructor</tt>' property of <tt>obj</tt>'s
   * prototype. 
   * <p>
   * By "<tt>obj</tt>'s prototype", we mean the object that
   * <tt>obj</tt> directly inherits from, not the value of its
   * '<tt>.prototype</tt>' property. If <tt>obj</tt> has a
   * '<tt>.__proto__</tt>' property, then we assume we're on a platform
   * (like Firefox) in which this reliably gives us <tt>obj</tt>'s
   * prototype. Otherwise, we memoize the apparent prototype into
   * '<tt>.__proto__</tt>' to speed up future queries. 
   * <p>
   * If <tt>obj</tt> is a function or not an object, return
   * <tt>undefined</tt>.
   * <p>
   * Assuming that all untranslated code creates only well formed
   * prototype chains, then, since (we claim that) all Caja-translated
   * code <i>can</i> only create well-formed prototype chains, then
   * for all Caja-visible objects that inherit from a prototype,
   * <tt>directConstructor(obj).prototype</tt> is <tt>obj</tt>'s
   * prototype. 
   */
  function directConstructor(obj) {
    if (obj === null) { return undefined; }
    if (typeof obj !== 'object') {
      // Note that functions thereby return <tt>undefined</tt>,
      // so <tt>directConstructor()</tt> doesn't provide access to the
      // forbidden <tt>Function</tt> constructor.
      return undefined;
    }
    if (obj === Object.prototype) {
      return undefined;
    }
    // The following test will initially return false in IE
    if (hasOwnProp(obj, '__proto__')) { 
      if (obj.__proto__ === null) { 
        log('Internal: Unexpected inheritance root: ', obj);
        return undefined; 
      }
      return obj.__proto__.constructor; 
    }
    var result;
    if (!hasOwnProp(obj, 'constructor')) { 
      result = obj.constructor;
    } else {
      var oldConstr = obj.constructor;
      if (!(delete obj.constructor)) { return undefined; }
      result = obj.constructor;
      obj.constructor = oldConstr;
    }
    if (result.prototype.constructor === result) {
      // Memoize, so it'll be faster next time.
      obj.__proto__ = result.prototype;
    }
    return result;
  }

  /**
   * Effectively, a <tt>.constructor</tt> property one can rely on.
   * <p>
   * Assuming that all untranslated code creates only well formed
   * prototype chains, then, since (we claim that) all Caja-translated
   * code <i>can</i> only create well-formed prototype chains, this
   * returns an object's constructor function, or <tt>undefined</tt> if
   * there isn't one.
   * <p>
   * If <tt>obj</tt> is a prototypical object, then
   * <pre>constructorOf(obj).prototype === obj</pre> and
   * <pre>constructorOf(obj) !== directConstructor(obj)</pre>. For all
   * other Caja-visible objects, <tt>constructorOf(obj).prototype</tt>
   * is <tt>obj</tt>'s prototype and 
   * <pre>constructorOf(obj) === directConstructor(obj)</pre>. 
   */
  function constructorOf(obj) {
    if (typeof obj === 'object' && obj !== null &&
        hasOwnProp(obj, 'constructor')) {
      var constr = obj.constructor;
      if (typeof constr === 'function' && constr.prototype === obj) {
        return constr;
      }
    }
    return directConstructor(obj);
  }
  
  /**
   * A JSON container is an object whose direct constructor is
   * <tt>Object</tt> or <tt>Array</tt>.
   * <p>
   * These are the kinds of non-primitive objects that can be
   * expressed in the JSON language.
   */
  function isJSONContainer(obj) {
    var constr = directConstructor(obj);
    return constr === Object || constr === Array;
  }
  
  /**
   * If <tt>obj</tt> is frozen, Caja code cannot directly assign to
   * properties of <tt>obj</tt>, nor directly add or delete properties to
   * <tt>obj</tt>.
   * <p>
   * The status of being frozen is not inherited. If A inherits from
   * B (i.e., if A's prototype is B), then (we claim) B must be
   * frozen regardless, but A may or may not be frozen.
   * <p>
   * BUG TODO(erights): If <pre>typeof obj</pre> is neither
   * <tt>'object'</tt> nor <tt>'function'</tt>, then it's currently
   * considered frozen. However, in some JavaScript environments (such
   * as certain browsers), the <tt>typeof</tt> some primitive objects
   * is a non-standard string. Should all of these be treated as if it
   * were <tt>'object'</tt>?
   */
  function isFrozen(obj) { 
    var t = typeof obj;
    if (t !== 'object' && t !== 'function') { 
      return true; 
    }
    return hasOwnProp(obj, '___FROZEN___'); 
  }
  
  /**
   * Mark <tt>obj</tt> as frozen so that Caja code cannot directly
   * assign to its properties.
   * <p>
   * If <tt>obj</tt> is a function, also freeze
   * <tt>obj.prototype</tt>. 
   * <p>
   * This function appears as <tt>___.primFreeze(obj)</tt> and is
   * wrapped by <tt>caja.freeze()</tt> and
   * <tt>thePseudoProto.freeze_()</tt>.
   */
  function primFreeze(obj) {
    if (null === obj) { return obj; }
    if (isFrozen(obj)) { return obj; }
    var typ = typeof obj;
    if (typ !== 'object' && typ !== 'function') { return obj; }

    // <tt>badFlags</tt> are names of properties we need to turn off.
    // We accumulate these first, so that we're not in the midst of a
    // for/in loop on <tt>obj</tt> while we're deleting properties
    // from <tt>obj</tt>.
    var badFlags = []; 
    for (var k in obj) {
      if (endsWith(k, '_canSet___') || endsWith(k, '_canDelete___')) { 
        if (pRead(obj, k)) {
          badFlags.push(k);
        }
      }
    }
    for (var i = 0; i < badFlags.length; i++) {
      var flag = badFlags[i];
      if (hasOwnProp(obj, flag)) {
        if (!(pDelete(obj, flag))) {
          fail('Internal: failed delete: ', obj, '.', flag);
        }
      }
      if (pRead(obj, flag)) {
        // At the time of this writing, this case should never be able
        // to happen, since prototypes are always frozen before use,
        // and frozen objects cannot have these flags set on them. We
        // code it this way to allow for a future optimization, where
        // the prototype can record as canSet those properties that
        // appear in instances that inherit from this prototype.
        pSet(obj, flag, false);
      }
    }
    pSet(obj, '___FROZEN___', true);
    if (typ === 'function') {
      // Do last to avoid possible infinite recursion.
      primFreeze(obj.prototype);
    }
    return obj;
  }
  
  /**
   * Like <tt>primFreeze(obj)</tt>, but applicable only to JSON
   * containers. 
   */
  function freeze(obj) {
    if (!isJSONContainer(obj)) {
      fail('caja.freeze(obj) applies only to JSON Containers: ', obj);
    }
    return primFreeze(obj);
  }
  
  /**
   * Makes a mutable copy of a JSON container.
   * <p>
   * Even if the original is frozen, the copy will still be mutable.
   * <p>
   * TODO(erights): Currently, this will copy only the indexes of an
   * <tt>Array</tt>, but will copy all enumerable properties of all
   * other JSON containers. If <tt>forEach</tt> should adopt a more
   * inclusive rule for regarding an object as array-like, and if it's
   * possible for a non-Array JSON container to satisfy this rule,
   * then we must revisit this code and its callers.
   */
  function copy(obj) {
    if (!isJSONContainer(obj)) {
      fail('caja.copy(obj) applies only to JSON Containers: ', obj);
    }
    var result = (obj instanceof Array) ? [] : {};
    forEach(obj, simpleFunc(function(v, k) {
      result[k] = v;
    }));
    return result;
  }
  
  /**
   * A snapshot of a JSON container is a frozen copy of that
   * container. 
   */
  function snapshot(obj) {
    return primFreeze(copy(obj));
  }
  
  /**
   * A method of a constructed object can freeze its object by saying
   * <tt>this.freeze_()</tt>.
   * <p>
   * Because this method ends in a "<tt>_</tt>", it is Internal, so
   * clients of a constructed object (a non-JSON container) cannot
   * freeze it without its cooperation. 
   */
  pSet(Object.prototype, 'freeze_', function() {
    return primFreeze(this);
  });
  
  ////////////////////////////////////////////////////////////////////////
  // Classifying functions
  ////////////////////////////////////////////////////////////////////////
  
  function isCtor(constr)    { return !!constr.CONSTRUCTOR___; }
  function isMethod(meth)    { return 'METHOD_OF___' in meth; }
  function isSimpleFunc(fun) { return !!fun.SIMPLE_FUNC___; }
  
  /** 
   * Mark <tt>constr</tt> as a constructor.
   * <p>
   * If <tt>opt_Sup</tt> is provided, set 
   * <pre>constr.Super = opt_Sup</pre>.
   * <p>
   * A function is tamed and classified by calling one of
   * <tt>ctor()</tt>, <tt>method()</tt>, or <tt>simpleFunc()</tt>. Each
   * of these checks that the function hasn't already been classified by
   * any of the others. A function which has not been so classified is an
   * <i>untamed function</i>. 
   * <p>
   * <tt>opt_name</tt>, if provided, should be the name of the constructor
   * function. Currently, this is used only to generate friendlier
   * error messages.
   */
  function ctor(constr, opt_Sup, opt_name) {
    enforceType(constr, 'function', opt_name);
    if (isMethod(constr)) {
      fail("Methods can't be constructors: ", constr);
    }
    if (isSimpleFunc(constr)) {
      fail("Simple-functions can't be constructors: ", constr);
    }
    constr.CONSTRUCTOR___ = true;
    if (opt_Sup) {
      opt_Sup = asCtor(opt_Sup);
      if (hasOwnProp(constr, 'Super')) {
        if (constr.Super !== opt_Sup) {
          fail("Can't inherit twice: ", constr, ',', opt_Sup);
        }
      } else {
        if (isFrozen(constr)) {
          fail('Derived constructor already frozen: ', constr);
        }
        constr.Super = opt_Sup;
      }
    }
    if (opt_name) {
      constr.NAME___ = String(opt_name);
    }
    return constr;  // translator freezes constructor later
  }

  /**
   * Supports the split-translation for first-class constructors.
   * <p>
   * The split translation translates a constructor definition (which
   * must not appear as an expression) like<pre>
   *   function Point(x, y) {
   *     this.x_ = x;
   *     this.y_ = y;
   * }</pre> into two function definitions and an initialization:<pre>
   *   function Point(var_args) // declares the original name
   *     return new Point.make___(arguments); // delegates the rest
   *   }
   *   function Point_init___(x, y) {   // normally translated params
   *     ___.setProp(this,'x_',x);      // normally translated body
   *     ___.setProp(this,'y_',y);
   *   }
   *   ___.splitCtor(Point,Point_init___); // Initialize Point
   * </pre>
   * The call to <tt>___.splitCtor()</tt> must be moved to the top of
   * the enclosing function so that it runs before any other possible
   * uses of <tt>Point</tt>.
   */
  function splitCtor(constr, initer, opt_Sup, opt_name) {
    ctor(constr, opt_Sup, opt_name);
    constr.init___ = initer;
    constr.make___ = function(args) {
      constr.init___.apply(this, args);
    };
    // We must preserve this identity, so anywhere that either
    // <tt>.prototype</tt> property might be assigned to, we must
    // assign to the other as well.
    constr.make___.prototype = constr.prototype;
    constr.call = function(that, var_args) {
        constr.init___.apply(that, arguments);
    };
    return constr;
  }
  
  /** 
   * Mark <tt>meth</tt> as a method of instances of <tt>constr</tt>. 
   * <p>
   * <tt>opt_name</tt>, if provided, should be the message name
   * associated with the method. Currently, this is used only to
   * generate friendlier error messages. 
   */
  function method(constr, meth, opt_name) {
    enforceType(meth, 'function', opt_name);
    if (isCtor(meth)) {
      fail("Constructors can't be methods: ", meth);
    }
    if (isSimpleFunc(constr)) {
      fail("Simple-functions can't be methods: ", meth);
    }
    meth.METHOD_OF___ = asCtorOnly(constr);
    if (opt_name) {
      meth.NAME___ = String(opt_name);
    }
    return primFreeze(meth);
  }
  
  /** 
   * Mark <tt>fun</tt> as a simple-function.
   * <p>
   * <tt>opt_name</tt>, if provided, should be the name of the 
   * function. Currently, this is used only to generate friendlier
   * error messages.
   */
  function simpleFunc(fun, opt_name) {
    enforceType(fun, 'function', opt_name);
    if (isCtor(fun)) {
      fail("Constructors can't be simple-functions: ", fun);
    }
    if (isMethod(fun)) {
      fail("Methods can't be simple-function: ", fun);
    }
    fun.SIMPLE_FUNC___ = true;

    // TODO(erights): enable this optimization (after testing without it)
//  fun.apply_canCall___ = true;
//  fun.call_canCall___ = true;

    if (opt_name) {
      fun.NAME___ = String(opt_name);
    }
    return fun;  // translator freezes fun later
  }
  
  /** This "Only" form doesn't freeze */
  function asCtorOnly(constr) {
    if (isCtor(constr) || isSimpleFunc(constr)) { 
      return constr; 
    }
    
    enforceType(constr, 'function');
    if (isMethod(constr)) {
      fail("Methods can't be called as constructors: ", constr);
    }
    fail("Untamed functions can't be called as constructors: ", constr);
  }
  
  /** 
   * Only constructors and simple-functions can be called as
   * constructors.   
   */
  function asCtor(constr) {
    return primFreeze(asCtorOnly(constr)); 
  }
  
  /** 
   * Only methods and simple-functions can be called as methods.
   */
  function asMethodOf(obj, meth) {
    if (isSimpleFunc(meth)) {
      return primFreeze(meth); 
    }
    if (isMethod(meth)) {
      if (meth.ATTACHMENT___ === obj) {
	return meth.ORIGINAL___;
      }
      if ('ATTACHMENT___' in meth) {
	fail("Can't reattach method: ", meth);
      }
      // TODO(erights): Should we fail() instead?
      log('Internal: Bare method: ', meth);
      return meth;
    }
    
    enforceType(meth, 'function');
    if (isCtor(meth)) {
      fail("Constructors can't be called as methods: ", meth);
    }
    fail("Untamed functions can't be called as methods: ", meth);
  }
  
  /** Only simple-functions can be called as simple-functions */
  function asSimpleFunc(fun) {
    if (isSimpleFunc(fun)) { 
      return primFreeze(fun); 
    }
    
    enforceType(fun, 'function');
    if (isCtor(fun)) {
      fail("Constructors can't be called as simple-functions: ", fun);
    }
    if (isMethod(fun)) {
      fail("Methods can't be called as simple-functions: ", fun);
    }
    fail("Untamed functions can't be called as simple-functions: ", fun);
  }

  /**
   * An attached method is a method that can only be successfully
   * called when its <tt>this</tt> is bound to <tt>that</tt>.
   * <p>
   * Only trusted code can create an attached method, to ensure that
   * it is not attached to some else's <tt>this</tt>. But once validly
   * attached, it is safe to treat an attached method as a first-class
   * value. 
   * <p>
   * In contrast to a bound method, an attached method either fails or
   * acts like its original method. Therefore, an attached method
   * is within a fail-stop subset of the semantics of its
   * original. 
   * <p>
   * <tt>attach(x,m).bind(x)</tt> acts just like
   * <tt>m.bind(x)</tt>, so the Caja expression <tt>x.foo.bind(x)</tt> 
   * means just what it means in JavaScript.
   * <p>
   * If <tt>x!==y</tt>, then <tt>attach(x,m).bind(y)</tt> is
   * useless. No matter how it is invoked, it will always immediately
   * fail. (We reserve the possibility that <tt>bind()</tt> itself
   * should fail in this case.)
   * <p>
   * Therefore, if in JavaScript <tt>x.foo</tt> is a method
   * inherited from a prototype, it would be within a safe fail-stop
   * subset of JavaScript for the Caja expression <tt>x.foo</tt> to
   * return the value of the JavaScript expression
   * <tt>attach(x,x.foo)</tt>.
   * <p>
   * Note that Cajita avoids all this complexity, since if
   * <tt>x.foo</tt> is a simple-function, then <tt>x.foo.bind(y)</tt>
   * is always equivalent to <tt>x.foo</tt> no matter what the value
   * of <tt>y</tt>.
   */
  function attach(that, meth) {
    if (typeof that != 'function') {
      enforceType(that, 'object');
    }
    if (that === null) {
      fail('Internal: may not attach null: ', meth);
    }
    if (!isMethod(meth)) {
      return meth;
    }
    if (meth.ATTACHMENT___ !== undefined) {
      fail('Method ', meth, ' cannot be reattached to: ', that);
    }
    function result(var_args) {
      if (this !== that) {
        fail('Method ', meth, ' is already attached: ', this);
      }
      return meth.apply(that, arguments);
    }
    var result = method(meth.METHOD_OF___, result, meth.NAME___);
    result.ATTACHMENT___ = that;
    result.ORIGINAL___ = meth;
    return result;
  }
  
  ////////////////////////////////////////////////////////////////////////
  // Accessing properties
  ////////////////////////////////////////////////////////////////////////
  
  /** 
   * Can a constructed Caja object read the <tt>name</tt> property on
   * itself?  
   * <p>
   * Can a Caja method whose <tt>this</tt> is bound to <tt>that</tt>
   * read its own <tt>name</tt> property? For properties added to
   * the object by Caja code, the answer is yes. For other
   * properties, which must therefore be inherited from a prototype
   * written in Javascript rather than Caja, the answer is: iff they
   * were whitelisted.
   */
  function canReadProp(that, name) {
    name = String(name);
    if (endsWith(name, '__')) { return false; }
    return canRead(that, name);
  }
  
  /** 
   * A constructed Caja object's attempt to read the <tt>name</tt>
   * property on itself.
   * <p>
   * If it can't, it reports <tt>undefined</tt> instead.
   */
  function readProp(that, name) {
    name = String(name);
    if (canReadProp(that, name)) {
      return pRead(that, name);
    } else if (canCallProp(that, name)) {
      return attach(that, pRead(that, name));
    } else {
      return pRead(that, 'faultHandler___').handleRead(that, name);
    }
  }
  
  /** 
   * Can a Caja client of <tt>obj</tt> read its <tt>name</tt> property? 
   * <p>
   * If the property is Internal (i.e. ends in an <tt>'_'</tt>), then no.
   * If the property was defined by Caja code, then yes. If it was
   * whitelisted, then yes. Or if the property is an own property of
   * a JSON container, then yes.
   * <p>
   * Currently says <tt>false</tt> if it would read by attaching a
   * method. What should it say in this case?
   */
  function canReadPub(obj, name) {
    name = String(name);
    if (endsWith(name, '_')) { return false; }
    if (canRead(obj, name)) { return true; }
    if (!isJSONContainer(obj)) { return false; }
    if (!hasOwnProp(obj, name)) { return false; }
    allowRead(obj, name);  // memoize
    return true;
  }
  
  /**
   * A client of <tt>obj</tt> attempting to read its public
   * <tt>name</tt> property.
   * <p>
   * If it can't, it reads <tt>undefined</tt> instead.
   */
  function readPub(obj, name) {
    name = String(name);
    if (canReadPub(obj, name)) {
      return pRead(obj, name);
    } else if (canCallPub(obj, name)) {
      return attach(obj, pRead(obj, name));
    } else {
      return pRead(obj, 'faultHandler___').handleRead(obj, name);
    }
  }
  
  /**
   * Can "innocent" code enumerate the <tt>name</tt> property on
   * <tt>obj</tt>? 
   * <p>
   * An earlier version of <tt>canInnocentEnum()</tt> filtered out
   * names ending with a double underbar. It now filters out exactly
   * those names ending in a triple underbar. Caja code can't see names
   * ending in a double underbar, since existing platforms (like
   * Firefox) use such names for purposes that should be hidden from
   * Caja code. However, it is not up to Caja to shield innocent code
   * from seeing such platform properties. All the magic names Caja
   * adds for its own internal bookkeeping end in triple underbar, so
   * that is all we need to hide from innocent code.
   */
  function canInnocentEnum(obj, name) {
    name = String(name);
    if (endsWith(name, '___')) { return false; }
    return true;
  }
  
  /** 
   * Would a Caja for/in loop on <tt>this</tt> see <tt>name</tt>? 
   * <p>
   * For properties defined in Caja, this is generally the same as
   * <tt>canReadProp()</tt>. Otherwise according to whitelisting.
   */
  function canEnumProp(that, name) {
    name = String(name);
    if (endsWith(name, '__')) { return false; }
    return canEnum(that, name);
  }
  
  /** 
   * Would a Caja for/in loop by a client of <tt>obj</tt> see
   * <tt>name</tt>?  
   * <p>
   * For properties defined in Caja, this is generally the same as
   * <tt>canReadPub()</tt>. Otherwise according to whitelisting.
   */
  function canEnumPub(obj, name) {
    name = String(name);
    if (endsWith(name, '_')) { return false; }
    if (canEnum(obj, name)) { return true; }
    if (!isJSONContainer(obj)) { return false; }
    if (!hasOwnProp(obj, name)) { return false; }
    allowEnum(obj, name);  // memoize
    return true;
  }
  
  /**
   * Like <tt>canEnumPub()</tt>, but allows only non-inherited
   * properties. 
   */
  function canEnumOwn(obj, name) {
    name = String(name);
    return hasOwnProp(obj, name) && canEnumPub(obj, name);
  }
  
  /**
   * Inside a <tt>caja.forEach()</tt>, the body function can terminate
   * early, as if with a conventional <tt>break;</tt>, by doing a
   * <pre>return caja.BREAK;</pre>
   */
  var BREAK = Token('BREAK');
  
  /**
   * For each sensible value/key pair in obj, call <tt>fn</tt> with
   * that pair. 
   * <p>
   * If <tt>obj instanceof Array</tt>, then enumerate
   * indexes. Otherwise, enumerate the <tt>canEnumOwn()</tt> property
   * names. 
   * <p>
   * TODO(erights): Instead of testing <tt>instanceof Array</tt>,
   * should we instead test to see if <tt>obj</tt> is array-like?
   * Which definition of array-like should we adopt?
   */
  function forEach(obj, fn) {
    fn = asSimpleFunc(fn);
    if (obj instanceof Array) {
      var len = obj.length;
      for (var i = 0; i < len; i++) {
        if (fn(readPub(obj, i), i) === BREAK) {
          return;
        }
      }
    } else {
      for (var k in obj) {
        if (canEnumOwn(obj, k)) {
          if (fn(readPub(obj, k), k) === BREAK) {
            return;
          }
        }
      }
    }
  }

  /**
   * As a transitional measure, here's the old <tt>each</tt> function,
   * which calls its <tt>fn</tt> argument with key/value pairs instead
   * of value/key pairs. 
   * <p>
   * TODO(erights): Remove once there aren't any more uses.
   *
   * @deprecated Use <tt>forEach</tt> instead, which provides
   * value/key pairs to <tt>fn</tt> instead of key/value pairs.
   */
  function each(obj, fn) {
    fn = asSimpleFunc(fn);
    forEach(obj, simpleFunc(function(v, k) {
      return fn(k, v);
    }));
  }
  
  /**
   * Can a Caja method call the <tt>name</tt> property as a method on
   * <tt>this</tt>? 
   * <p>
   * For genuine methods, they are only callable if the canCall
   * attribute is set. Otherwise, if this property is readable and
   * holds a simple-function, then it's also callable as a function,
   * which we can memoize.
   * <p>
   * SECURITY HAZARD TODO(erights): If a settable property is
   * first set to a 
   * simple-function, which is then called, memoizing canCall, and
   * then set to some other kind of function which leaked (such as
   * an untamed function), then that other function can be
   * inappropriately called as a method on <tt>that</tt>. We currently
   * classify this as a hazard and not a bug per se, since no such
   * function value should ever leak into value space. If one does,
   * there's a bug either in Caja or in the embedding app's taming
   * decisions.
   * <p>
   * In any case, the not-yet-implemented plan to fix this hazard is
   * to have two canSet flags: one that records the grant of
   * settability, and one to be tested in the fast-path. The
   * fast-path canCall and fast-path canSet flags will be exclusive,
   * to be faulted in by the last successful use. This way, repeated
   * calls are fast, and repeated sets are fast, but the first call
   * after a set will re-check the value to be called.
   * <p>
   * This plan must be reexamined when we implement property
   * deletion. 
   */
  function canCallProp(that, name) {
    name = String(name);
    if (endsWith(name, '__')) { return false; }
    if (canCall(that, name)) { return true; }
    if (!canReadProp(that, name)) { return false; }
    var func = pRead(that, name);
    if (!isSimpleFunc(func)) { return false; }
    allowCall(that, name);  // memoize
    return true;
  }
  
  /**
   * A Caja method tries to call a method on its <tt>this</tt>.
   */
  function callProp(that, name, args) {
    name = String(name);
    if (canCallProp(that, name)) {
      var meth = pRead(that, name);
      return meth.apply(that, args);
    } else {
      return pRead(that, 'faultHandler___').handleCall(that, name, args);
    }
  }
  
  /**
   * Like <tt>canCallProp</tt>, with differences that parallel the
   * differences between <tt>canReadProp</tt> vs <tt>canReadPub</tt>.
   */
  function canCallPub(obj, name) {
    name = String(name);
    if (endsWith(name, '_')) { return false; }
    if (canCall(obj, name)) { return true; }
    if (!canReadPub(obj, name)) { return false; }
    var func = pRead(obj, name);
    if (!isSimpleFunc(func)) { return false; }
    allowCall(obj, name);  // memoize
    return true;
  }
  
  /**
   * A client of <tt>obj</tt> tries to call one of its methods.
   */
  function callPub(obj, name, args) {
    name = String(name);
    if (canCallPub(obj, name)) {
      var meth = pRead(obj, name);
      return meth.apply(obj, args);
    } else {
      return pRead(obj, 'faultHandler___').handleCall(obj, name, args);
    }
  }
  
  /** 
   * Can a method of a Caja constructed object directly assign to
   * this property of its object?
   * <p>
   * Iff this object isn't frozen.
   */
  function canSetProp(that, name) {
    name = String(name);
    if (endsWith(name, '__')) { return false; }
    if (canSet(that, name)) { return true; }
    return !isFrozen(that);
  }
  
  /**
   * A Caja method tries to assign to this property of its object.
   */
  function setProp(that, name, val) {
    name = String(name);
    if (canSetProp(that, name)) {
      allowSet(that, name);  // grant
      pSet(that, name, val);
      return val;
    } else {
      return pRead(that, 'faultHandler___').handleSet(that, name, val);
    }
  }
  
  var theForbiddenStatics = {
    Super: true,
    prototype: true,
    constructor: true,
    apply: true,
    call: true,
    bind: true
  };

  /**
   * Can a client of <tt>obj</tt> directly assign to its <tt>name</tt>
   * property? 
   * <p>
   * If this property is Internal (i.e., ends with a '<tt>_</tt>') or
   * if <tt>obj</tt> is frozen, then no. If this property is not Internal
   * and was defined by Caja code, then yes. If <tt>obj</tt> is a JSON
   * container, then yes. If <tt>obj</tt> is a non-frozen constructor
   * or simple-function and name is a valid static member, then
   * yes. Otherwise according to whitelisting decisions. 
   * <p>
   * The non-obvious implication of this rule together with the
   * <tt>canSetProp</tt> rule is that a Caja client of a Caja
   * constructed object cannot add new properties to it. But a Caja
   * constructed object can add new properties to itself, and its
   * clients can then assign to these properties. 
   */
  function canSetPub(obj, name) {
    name = String(name);
    if (endsWith(name, '_')) { return false; }
    if (canSet(obj, name)) { return true; }
    if (isFrozen(obj)) { return false; }
    if (isJSONContainer(obj)) { return true; }
    if (!isSimpleFunc(obj) && !isCtor(obj)) { return false; }
    if (theForbiddenStatics[name]) {
      // TODO(erights): This should only be logged when it actually
      // causes a failure.
      log('Static ".', name, '" is reserved');
      return false;
    }
    return true;
  }
  
  /** 
   * A client of <tt>obj</tt> attempts to assign to one of its
   * properties.  
   */
  function setPub(obj, name, val) {
    name = String(name);
    if (canSetPub(obj, name)) {
      allowSet(obj, name);  // grant
      pSet(obj, name, val);
      return val;
    } else {
      return pRead(obj, 'faultHandler___').handleSet(obj, name, val);
    }
  }
  
  /**
   * Can a Caja constructed object delete the named property?
   * <p>
   * BUG TODO(erights): This is not yet supported. The precise
   * enabling conditions are not yet determined, as is the implied
   * bookkeeping. 
   */
  function canDeleteProp(that, name) {
    fail('TODO(erights): deletion not yet supported');
    return false;
  }
  
  /**
   * A Caja constructed object attempts to delete one of its own
   * properties. 
   * <p>
   * BUG TODO(erights): This is not yet supported. The precise
   * enabling conditions are not yet determined, as is the implied
   * bookkeeping.
   */
  function deleteProp(that, name) {
    name = String(name);
    if (canDeleteProp(that, name)) {
      fail('TODO(erights): deletion not yet supported');
      if (pDelete(that, name)) {
        return true;
      } else {
        fail('Not deleted: ', that, '.', name);
      }
    } else {
      return pRead(that, 'faultHandler___').handleDelete(that, name);
    }
  }
  
  /**
   * Can a client of <tt>obj</tt> delete the named property?
   * <p>
   * BUG TODO(erights): This is not yet supported. The precise
   * enabling conditions are not yet determined, as is the implied
   * bookkeeping. 
   */
  function canDeletePub(obj, name) {
    fail('TODO(erights): deletion not yet supported');
    return false;
  }
  
  /**
   * A client of <tt>obj</tt> can only delete a property of
   * <tt>obj</tt> if <tt>obj</tt> is a non-frozen JSON container. 
   * <p>
   * BUG TODO(erights): This is not yet supported. The precise
   * enabling conditions are not yet determined, as is the implied
   * bookkeeping. 
   */
  function deletePub(obj, name) {
    name = String(name);
    if (canDeletePub(obj, name)) {
      if (!isJSONContainer(obj)) {
        fail('Unable to delete: ', name);
      }
      fail('TODO(erights): deletion not yet supported');
      if (pDelete(obj, name)) {
        return true;
      } else {
        fail('Not deleted: ', name);
      }
    } else {
      return pRead(obj, 'faultHandler___').handleDelete(obj, name);
    }
  }
  
  /** 
   * Sets <pre>constr.prototype[name] = member</pre>.
   * <p>
   * If <tt>member</tt> is a method, make it callable. If
   * <tt>member</tt> is a simple-function, make it callable and
   * readable. Else make it readable. 
   * <p>
   * TODO(erights): Currently, when <tt>member</tt> is not a method,
   * <tt>setMember()</tt> also makes the <tt>name</tt> property
   * enumerable. Should it? Other than ES3 compatibility, such
   * enumerability does not help anyone.
   */
  function setMember(constr, name, member) {
    name = String(name);
    if (endsWith(name, '__') ||
        name === 'constructor' ||
        name === 'prototype') {
      fail('Reserved member name: ', name);
    }
    var proto = asCtorOnly(constr).prototype;
    // We allow prototype members to end in a single "<tt>_</tt>".
    if (!canSetProp(proto, name)) {
      fail('Not settable: ', name);
    }
    if (isMethod(member)) {
      if (member.METHOD_OF___ !== constr) {
        fail('Internal: method of wrong constructor: ', 
             member, ', ', constr);
      }
      allowCall(proto, name);  // grant
    } else if (isSimpleFunc(member)) {
      allowCall(proto, name);  // grant
      allowEnum(proto, name);  // grant
    } else {
      allowEnum(proto, name);  // grant
    }
    pSet(proto, name, member);
  }
  
  /**
   * Copies each of the member-name/member associations in
   * <tt>members</tt> into <tt>sub.prototype</tt>.
   */
  function setMemberMap(sub, members) {
    forEach(members, simpleFunc(function(member, mname) {
      setMember(sub, mname, member);
    }));
  }
  
  ////////////////////////////////////////////////////////////////////////
  // Other
  ////////////////////////////////////////////////////////////////////////
  
  /**
   * This returns a frozen array copy of the original array or
   * array-like object.
   * <p>
   * If a Caja program makes use of <tt>arguments</tt>, this is
   * rewritten to use a frozen array copy of arguments instead. This
   * way, if Caja code passes its arguments to someone else, they
   * are not giving the receiver the rights to access the passing
   * function nor to modify the parameter variables of the passing
   * function.
   */
  function args(original) {
    return primFreeze(slice(original, 0));
  }
  
  /**
   * Provides a shorthand for a class-like declaration of a fresh
   * Caja constructor.
   * <p>
   * Given that <tt>sub</tt> is a Caja constructor in formation, whose
   * '<tt>.prototype</tt>' property may not have been initialized yet, 
   * initialize <tt>sub</tt> and '<tt>sub.prototype</tt>' so
   * that <tt>sub</tt> acts as a subclass of <tt>opt_Sup</tt>. Add
   * <tt>opt_members</tt> as members to <tt>sub.prototype</tt>,
   * and add <tt>opt_statics</tt> as static members to <tt>sub</tt>. 
   * <p>
   * TODO(erights): return a builder object that allows further
   * initialization. 
   */
  function def(sub, opt_Sup, opt_members, opt_statics) {
    var sup = opt_Sup || Object;
    var members = opt_members || {};
    var statics = opt_statics || {};
    
    ctor(sub, sup);
    function PseudoSuper() {}
    PseudoSuper.prototype = sup.prototype;
    sub.prototype = new PseudoSuper();
    if (sub.make___) {
      // We must preserve this identity, so anywhere that either
      // <tt>.prototype</tt> property might be assigned to, we must
      // assign to the other as well.
      sub.make___.prototype = sub.prototype;
    }
    sub.prototype.constructor = sub;
    
    setMemberMap(sub, members);
    forEach(statics, simpleFunc(function(staticMember, sname) {
      setPub(sub, sname, staticMember);
    }));
    // translator freezes <tt>sub</tt> and <tt>sub.prototype</tt> later.
  }


  ////////////////////////////////////////////////////////////////////////
  // Taming mechanism
  ////////////////////////////////////////////////////////////////////////

  /**
   * Arrange to handle read-faults on <tt>obj[name]</tt>
   * by calling <tt>getHandler(obj)</tt>, where <tt>obj</tt> is the
   * faulting object. 
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>allowRead()</tt>.
   */
  function useGetHandler(obj, name, getHandler) {
    pSet(obj, name + '_getter___', getHandler);
  }

  /**
   * Arrange to handle call-faults on <tt>obj[name](args...)</tt> by
   * calling <tt>applyHandler(obj, args)</tt>, where <tt>obj</tt> is
   * the faulting object. 
   * <p>
   * Note that <tt>applyHandler</tt> is called with exactly two
   * arguments, the second of which is the list of arguments in the
   * original call. 
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>allowCall()</tt>,
   * <tt>allowSimpleFunc()</tt>, or <tt>allowMethod()</tt>. 
   */
  function useApplyHandler(obj, name, applyHandler) {
    pSet(obj, name + '_handler___', applyHandler);
  }

  /**
   * Arrange to handle call-faults on <tt>obj[name](args...)</tt> by
   * calling <tt>callHandler(obj, args...)</tt>, where <tt>obj</tt> is
   * the faulting object. 
   * <p>
   * Note that <tt>callHandler</tt> is called with the faulting object
   * followed by the same arguments as the original call. 
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>allowCall()</tt>,
   * <tt>allowSimpleFunc()</tt>, or <tt>allowMethod()</tt>. 
   */
  function useCallHandler(obj, name, callHandler) {
    useApplyHandler(obj, name, function(obj, args) {
      return callHandler.apply(obj, [obj].concat(args));
    });
  }

  /**
   * Arrange to handle set-faults on <tt>obj[name] = newValue</tt> by
   * calling <tt>setHandler(obj, newValue)</tt>, where <tt>obj</tt> is
   * the faulting object. 
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>allowSet()</tt>.
   */
  function useSetHandler(obj, name, setHandler) {
    pSet(obj, name + '_setter___', setHandler);
  }

  /**
   * Arrange to handle delete-faults on <tt>delete obj[name]</tt> by
   * calling <tt>deleteHandler(obj)</tt>, where <tt>obj</tt> is the
   * faulting object. 
   * <p>
   * In order for this fault-handler to get control, it's important
   * that no one does a conflicting <tt>allowDelete()</tt>.
   */
  function useDeleteHandler(obj, name, deleteHandler) {
    pSet(obj, name + '_deleter___', deleteHandler);
  }

  /**
   * Whilelist <tt>obj[name]</tt> as a simple-function that can be
   * either called or read. 
   */
  function allowSimpleFunc(obj, name) {
    simpleFunc(pRead(obj, name), name);
    allowCall(obj, name);
    allowRead(obj, name);
  }
  
  /**
   * Whitelist <tt>constr.prototype[name]</tt> as a method that can be
   * called on instances of <tt>constr</tt>. 
   */
  function allowMethod(constr, name) {
    method(constr, pRead(constr.prototype, name), name);
    allowCall(constr.prototype, name);
  }
  
  /**
   * Virtually replace <tt>constr.prototype[name]</tt> with a
   * fault-handling wrapper that first verifies that the faulting
   * object isn't frozen. 
   * <p>
   * When a pre-existing Javascript method would mutate its object,
   * we need to provide a fault handler instead to prevent such
   * mutation from violating Caja semantics. In order for this fault
   * handler to get control, it's important that no one does an
   * <tt>allowCall()</tt>, <tt>allowSimpleFunc()</tt>, or
   * <tt>allowMethod()</tt> on the original method.  
   */
  function allowMutator(constr, name) {
    var original = pRead(constr.prototype, name);
    useApplyHandler(
      constr.prototype, name, function(obj, args) {
        if (isFrozen(obj)) {
          fail("Can't .", name, '() a frozen object: ', obj);
        }
        return original.apply(obj, args);
      });
  }
  
  /**
   * Verifies that <tt>regexp</tt> is something that may appear as a
   * parameter to a Javascript method that would use it in a match.
   * <p>
   * If it is a <tt>RegExp</tt>, then this match might mutate it,
   * which must not be allowed if <tt>regexp</tt> is frozen. 
   */
  function enforceMatchable(regexp) {
    if (regexp instanceof RegExp) {
      if (isFrozen(regexp)) {
        fail("Can't match with frozen RegExp: ", regexp);
      }
    }
  }
  
  /**
   * A shorthand that happens to be useful here.
   * <p>
   * For each arg2 in arg2s: func2(arg1,arg2).
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


  all2(allowRead, Math, [
    'E', 'LN10', 'LN2', 'LOG2E', 'LOG10E', 'PI', 'SQRT1_2', 'SQRT2'
  ]);
  all2(allowSimpleFunc, Math, [
    'abs', 'acos', 'asin', 'atan', 'atan2', 'ceil', 'cos', 'exp', 'floor',
    'log', 'max', 'min', 'pow', 'random', 'round', 'sin', 'sqrt', 'tan'
  ]);
  
  
  ctor(Object, undefined, 'Object');
  all2(allowMethod, Object, [
    'toString', 'toLocaleString', 'valueOf', 'isPrototypeOf', 'freeze_'
  ]);
  allowRead(Object.prototype, 'length');
  useCallHandler(
    Object.prototype, 'hasOwnProperty', function(obj, name) {
      name = String(name);
      return canReadPub(obj, name) && hasOwnProp(obj, name);
    });
  var pie_ = Object.prototype.propertyIsEnumerable;
  useCallHandler(
    Object.prototype, 'propertyIsEnumerable', function(obj, name) {
      name = String(name);
      return canReadPub(obj, name) && pie_.call(obj, name);
    });


  useCallHandler(
    Function.prototype, 'apply', function(fun, that, realArgs) {
      return asMethodOf(that, fun).apply(that, realArgs);
    });
  useApplyHandler(
    Function.prototype, 'call', function(fun, callArgs) {
      var that = callArgs[0];
      var subArgs = slice(callArgs, 1);
      return asMethodOf(that, fun).apply(that, subArgs);
    });
  useApplyHandler(
    Function.prototype, 'bind', function(fun, bindArgs) {
      var that = bindArgs[0];
      var subArgs = slice(bindArgs, 1);
      var meth = asMethodOf(that, fun);
      return primFreeze(simpleFunc(function(var_args) {
	return meth.apply(that, subArgs.concat(args(arguments)));
      }));
    });
  
  
  ctor(Array, Object, 'Array');
  all2(allowMethod, Array, [
    'concat', 'join', 'slice'
  ]);
  all2(allowMutator, Array, [
    'pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'
  ]);
  
  
  ctor(String, Object, 'String');
  allowSimpleFunc(String, 'fromCharCode');
  all2(allowMethod, String, [
    'charAt', 'charCodeAt', 'concat', 'indexOf', 'lastIndexOf',
    'localeCompare', 'slice', 'substring',
    'toLowerCase', 'toLocaleLowerCase', 'toUpperCase', 'toLocaleUpperCase'
  ]);
  useCallHandler(
    String.prototype, 'match', function(str, regexp) {
      enforceMatchable(regexp);
      return str.match(regexp);
    });
  useCallHandler(
    String.prototype, 'replace', function(str, searchValue, replaceValue) {
      enforceMatchable(searchValue);
      return str.replace(searchValue, replaceValue);
    });
  useCallHandler(
    String.prototype, 'search', function(str, regexp) {
      enforceMatchable(regexp);
      return str.search(regexp);
    });
  useCallHandler(
    String.prototype, 'split', function(str, separator, limit) {
      enforceMatchable(separator);
      return str.split(separator, limit);
    });
  
  
  ctor(Boolean, Object, 'Boolean');
  
  
  ctor(Number, Object, 'Number');
  all2(allowRead, Number, [
    'MAX_VALUE', 'MIN_VALUE', 'NaN',
    'NEGATIVE_INFINITY', 'POSITIVE_INFINITY'
  ]);
  all2(allowMethod, Number, [
    'toFixed', 'toExponential', 'toPrecision'
  ]);
  
  
  ctor(Date, Object, 'Date');
  allowSimpleFunc(Date, 'parse');
  allowSimpleFunc(Date, 'UTC');
  
  all2(allowMethod, Date, [
    'toDateString', 'toTimeString', 'toUTCString',
    'toLocaleString', 'toLocaleDateString', 'toLocaleTimeString',
    'getDay', 'getUTCDay', 'getTimezoneOffset',
    'getTime', 'getFullYear', 'getUTCFullYear', 'getMonth', 'getUTCMonth',
    'getDate', 'getUTCDate', 'getHours', 'getUTCHours',
    'getMinutes', 'getUTCMinutes', 'getSeconds', 'getUTCSeconds',
    'getMilliseconds', 'getUTCMilliseconds'
  ]);
  all2(allowMutator, Date, [
    'setTime', 'setFullYear', 'setUTCFullYear', 'setMonth', 'setUTCMonth',
    'setDate', 'setUTCDate', 'setHours', 'setUTCHours',
    'setMinutes', 'setUTCMinutes', 'setSeconds', 'setUTCSeconds',
    'setMilliseconds', 'setUTCMilliseconds'
  ]);
  
  
  ctor(RegExp, Object, 'RegExp');
  allowMutator(RegExp, 'exec');
  allowMutator(RegExp, 'test');
  
  all2(allowRead, RegExp, [
    'source', 'global', 'ignoreCase', 'multiline', 'lastIndex'
  ]);
  
  
  ctor(Error, Object, 'Error');
  allowRead(Error, 'name');
  allowRead(Error, 'message');
  ctor(EvalError, Error, 'EvalError');
  ctor(RangeError, Error, 'RangeError');
  ctor(ReferenceError, Error, 'ReferenceError');
  ctor(SyntaxError, Error, 'SyntaxError');
  ctor(TypeError, Error, 'TypeError');
  ctor(URIError, Error, 'URIError');
  
  
  var sharedOuters;
  
  ////////////////////////////////////////////////////////////////////////
  // Module loading
  ////////////////////////////////////////////////////////////////////////
  
  (function(){
    if (this !== theGlobalObject) {
      fail('Global object confusion: ', theGlobalObject);
    }
  })();
  
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
   * This callback mechanism is provided so that translated Caja
   * modules can be loaded from a trusted site with a
   * &lt;script&gt; tag, which runs its script as a statement, not
   * an expression. The callback is of the form
   * <tt>myNewModuleHandler.handle(newModule)</tt>.
   */
  function setNewModuleHandler(newModuleHandler) {
    myNewModuleHandler = newModuleHandler;
  }
  
  /**
   * A new-module-handler which does nothing.
   */
  var ignoreNewModule = freeze({
    handle: primFreeze(simpleFunc(function(newModule){}))
  });
  
  /**
   * Makes and returns a fresh "normal" module handler whose
   * <tt>outers</tt> are initialized to a copy of the
   * <tt>sharedOuters</tt>. 
   * <p>
   * This handles a new module by calling it, passing it the
   * <tt>outers</tt> object held in this handler. Successive modules
   * handled by the same "normal" handler thereby see a simulation of
   * successive updates to a shared global scope.
   */
  function makeNormalNewModuleHandler() {
    var outers = copy(sharedOuters);
    return freeze({
      getOuters: primFreeze(simpleFunc(function() { return outers; })),
      setOuters: primFreeze(simpleFunc(function(newOuters) { 
        outers = newOuters; })),
      handle: primFreeze(simpleFunc(function(newModule) {
        newModule(outers);
      }))
    });
  }
  
  /**
   * A module is a plugin-maker function.
   * <p>
   * loadModule(module) marks module as a simpleFunc, freezes it,
   * asks the current new-module-handler to handle it (thereby
   * notifying the handler), and returns the new module.  
   */
  function loadModule(module) {
    callPub(myNewModuleHandler, 'handle',
            [primFreeze(simpleFunc(module))]);
    return module;
  }

  var registeredOuters = [];

  /**
   * Gets or assigns the id associated with this (assumed to be)
   * <tt>outers</tt> object, registering it so that 
   * <tt>getOuters(getId(outers)) ==== outers</tt>.
   * <p>
   * This system of registration and identification allows us to
   * cajole html such as
   * <pre>&lt;a onmouseover="alert(1)"&gt;Mouse here&lt;/a&gt;</pre>
   * into html-writing JavaScript such as<pre>
   * ___OUTERS___.document.innerHTML = "
   *  &lt;a onmouseover=\"
   *    (function(___OUTERS___) {
   *      ___OUTERS___.alert(1);
   *    })(___.getOuters(" + ___.getId(___OUTERS___) + "))
   *  \"&gt;Mouse here&lt;/a&gt;
   * ";
   * </pre>
   * If this is executed by a plugin whose <tt>outers</tt> is assigned
   * id 42, it generates html with the same meaning as<pre> 
   * &lt;a onmouseover="___.getOuters(42).alert(1)"&gt;Mouse here&lt;/a&gt;
   * </pre>
   * <p>
   * An <tt>outers</tt> is not registered and no id is assigned to it
   * until the first call to <tt>getId</tt>. This way, an <tt>outers</tt>
   * that is never registered, or that has been <tt>unregister</tt>ed
   * since the last time it was registered, will still be garbage
   * collectable. 
   */
  function getId(outers) {
    enforceType(outers, 'object', 'outers');
    var id;
    if ('id___' in outers) {
      id = enforceType(outers.id___, 'number', 'id');
    } else {
      id = outers.id___ = registeredOuters.length;
    }
    registeredOuters[id] = outers;
    return id;
  }

  /**
   * Gets the <tt>outers</tt> object registered under this id.
   * <p>
   * If it has been <tt>unregistered</tt> since the last
   * <tt>getId()</tt> on it, then <tt>getOuters()</tt> will fail.
   */
  function getOuters(id) {
    var result = registeredOuters[enforceType(id, 'number', 'id')];
    if (result === undefined) {
      fail('outers#', id, ' unregistered');
    }
    return result;
  }

  /**
   * If you know that this <tt>outers</tt> no longers ever need to be
   * accessed by <tt>getOuters()</tt>, then you should
   * <tt>unregister()</tt> it so it can be garbage collected. 
   * <p>
   * After <tt>unregister()</tt>ing, the id is not reassigned, and the
   * <tt>outers</tt> remembers its id. If asked for another
   * <tt>getId()</tt>, it reregisters itself at its old id. 
   */
  function unregister(outers) {
    enforceType(outers, 'object', 'outers');      
    if ('id___' in outers) {
      var id = enforceType(outers.id___, 'number', 'id');
      registeredOuters[id] = undefined;
    }
  }
  
  ////////////////////////////////////////////////////////////////////////
  // Exports
  ////////////////////////////////////////////////////////////////////////
  
  caja = {

    // Diagnostics and condition enforcement
    log: log,                     fail: fail,
    enforce: enforce,
    enforceType: enforceType,     enforceNat: enforceNat,
    Token: Token,                 USELESS: USELESS,
    
    // walking prototype chain, checking JSON containers
    isJSONContainer: isJSONContainer,
    freeze: freeze,
    copy: copy,                   snapshot: snapshot,
    
    // Accessing properties
    canReadPub: canReadPub,       readPub: readPub,
    canCallPub: canCallPub,       callPub: callPub,
    canSetPub: canSetPub,         setPub: setPub,
    canDeletePub: canDeletePub,   deletePub: deletePub,

    canEnumPub: canEnumPub,       canEnumOwn: canEnumOwn,       
    BREAK: BREAK,                 
    forEach: forEach,             each: each,                   
    
    // Other
    def: def
  };
  
  sharedOuters = {
    caja: caja,
    
    'null': null,
    'false': false,               'true': true,
    'NaN': NaN,                   'Infinity': Infinity,
    'undefined': undefined,
    parseInt: parseInt,           parseFloat: parseFloat,
    isNaN: isNaN,                 isFinite: isFinite,
    decodeURI: decodeURI,
    decodeURIComponent: decodeURIComponent,
    encodeURI: encodeURI,
    encodeURIComponent: encodeURIComponent,
    Math: Math,
    
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
  
  forEach(sharedOuters, simpleFunc(function(v, k) {
    switch (typeof v) {
    case 'object':
      if (v !== null) { primFreeze(v); }
      break;
    case 'function':
      primFreeze(v);
      break;
    }
  }));
  primFreeze(sharedOuters);
  
  ___ = {

    // Diagnostics and condition enforcement
    theGlobalObject: theGlobalObject,
    diagnostic: diagnostic,
    getLogFunc: getLogFunc,       setLogFunc: setLogFunc,

    // thePseudoProto
    thePseudoProto: thePseudoProto,
    pIn: pIn,                     pRead: pRead,
    pCall: pCall,                 pSet: pSet,
    pDelete: pDelete,
    hasOwnProp: hasOwnProp,
    
    // Accessing property attributes
    canRead: canRead,             allowRead: allowRead,
    canEnum: canEnum,             allowEnum: allowEnum,
    canCall: canCall,             allowCall: allowCall,
    canSet: canSet,               allowSet: allowSet,
    canDelete: canDelete,         allowDelete: allowDelete,

    // walking prototype chain, checking JSON containers
    directConstructor: directConstructor,
    constructorOf: constructorOf,
    isFrozen: isFrozen,           primFreeze: primFreeze,
    
    // Classifying functions
    isCtor: isCtor,               isMethod: isMethod,
    isSimpleFunc: isSimpleFunc,
    ctor: ctor,                   splitCtor: splitCtor,
    asCtorOnly: asCtorOnly,       asCtor: asCtor,
    method: method,               asMethodOf: asMethodOf,
    simpleFunc: simpleFunc,       asSimpleFunc: asSimpleFunc,

    attach: attach,
    
    // Accessing properties
    canReadProp: canReadProp,     readProp: readProp,
    canInnocentEnum: canInnocentEnum,
    canEnumProp: canEnumProp,
    canCallProp: canCallProp,     callProp: callProp,
    canSetProp: canSetProp,       setProp: setProp,
    canDeleteProp: canDeleteProp, deleteProp: deleteProp,

    setMember: setMember,
    setMemberMap: setMemberMap,
    
    // Other
    args: args,
    
    // Taming mechanism
    theDefaultKeeper: theDefaultKeeper,
    theDefaultHandler: theDefaultHandler,

    useGetHandler: useGetHandler, 
    useApplyHandler: useApplyHandler,
    useCallHandler: useCallHandler,
    useSetHandler: useSetHandler,
    useDeleteHandler: useDeleteHandler,

    allowSimpleFunc: allowSimpleFunc,
    allowMethod: allowMethod,
    allowMutator: allowMutator,
    enforceMatchable: enforceMatchable,
    all2: all2,
    
    // Taming decisions
    sharedOuters: sharedOuters,
    
    // Module loading
    getNewModuleHandler: getNewModuleHandler,
    setNewModuleHandler: setNewModuleHandler,
    ignoreNewModule: ignoreNewModule,
    makeNormalNewModuleHandler: makeNormalNewModuleHandler,
    loadModule: loadModule,

    getId: getId,
    getOuters: getOuters,
    unregister: unregister
  };
  
  forEach(caja, simpleFunc(function(v, k) {
    if (k in ___) {
      fail('Internal: initialization conflict: ', k);
    }
    if (typeof v === 'function') {
      simpleFunc(v);
      allowCall(caja, k);
    }
    ___[k] = v;
  }));
  primFreeze(caja);
  
  setNewModuleHandler(makeNormalNewModuleHandler());
  
})(this);
