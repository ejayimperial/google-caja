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

// This Cajita module defines a set of generic simple-functions based
// on the functions defined or proposed in
// http://developer.mozilla.org/en/docs/New_in_JavaScript_1.6
// and
// http://wiki.ecmascript.org/doku.php?id=proposals:static_generics
// Although this module is written in Cajita, it is actually platform
// (or "trusted") code that is part of the Caja runtime library (or
// "TCB"). Its source is written in Cajita so that we can use a
// Cajita->Javascript translator to turn it into code which
// interoperates safely with untrusted Caja code. 

// This module must be loaded after caja.js. If extensions.js is to be
// loaded, this module must be loaded before extensions.js is
// loaded. extensions.js will add the generics defined by this module
// to various primordial objects. By "loading this module", we mean
// that the Caja-translation of this module to Javascript should be
// loaded.  As a development convenience, if this module is instead
// loaded untranslated, extensions.js will still be able to use it to
// install extensions, but these extensions may present exploitable
// vulnerabilities when used by untrusted Caja code.



// The generic functions defined by this module differ from those
// specified above as follows<ul>
// <li>When a function takes a parameter (such as opt_from, but
//     whether optional or not) representing an index into an array or
//     array-like object, and if this parameter is provided, then it
//     must be a valid array index.
// <li>Optional parameters default when they are bound to
//     <tt>undefined</tt>, independent of the value of
//     <tt>arguments.length</tt>.
// <li>Enumerators have no optional <tt>thisp</tt>.
// <li>Enumerators accepts only simple-functions (so a <tt>thisp</tt>
//     would be pointless anyway),
// <li>Enumerators call their function argument only with a value and
//     index, not with the array-like <tt>arr</tt> itself, as that
//     would violate the POLA (the Principle Of Least Authority).
// <li>We reserve the possibility of extending these enumerators to
//     terminate early if their function argument returns
//     <tt>caja.BREAK</tt>. 
// </ul>


var generics = (function() {
  
  /** 
   * Returns the first index at which the <tt>specimen</tt> is found
   * (by "<tt>===</tt>") or -1 if none. 
   */
  function indexOf(arr, specimen, opt_from) {
    var len = arr.length;
    var i = opt_from === undefined ? 
      0 : caja.enforceNat(opt_from);

    for (; i < len; i++) {
      if (arr[i] === specimen) {
        return i;
      }
    }
    return -1;
  };
  
  /** 
   * Returns the last index at which the <tt>specimen</tt> is found
   * (by "<tt>===</tt>") or -1 if none. 
   */
  function lastIndexOf(arr, specimen, opt_from) {
    var i = opt_from === undefined ? 
      arr.length -1 : caja.enforceNat(opt_from);

    for (; i >= 0; i--) {
      if (arr[i] === specimen) {
        return i;
      }
    }
    return -1;
  };
  
  /**
   * Is <tt>pred()</tt> true <i>for all</i> value/index associations
   * in (the presumably array-like) <tt>arr</tt>?
   */
  function every(arr, pred) {
    var len = arr.length;
    for (var i = 0; i < len; i++) {
      if (i in arr && !pred(arr[i], i)) {
        return false;
      }
    }
    return true;
  };

  /**
   * Call <tt>fun()</tt> for each value/index association in (the
   * presumably array-like) <tt>arr</tt>? 
   */
  function forEach(arr, fun) {
    every(arr, function(value, index) {
      fun(value, index);
      return true;
    });
  }

  /**
   * Return an array of the subset of <tt>arr</tt> for which
   * <tt>pred()</tt> is true. 
   */
  function filter(arr, pred) {
    var result = [];
    every(arr, function(value, index) {
      if (pred(value, index)) {
        result.push(value);
      }
      return true;
    });
    return result;
  }

  /**
   * Return an array with the results of calling <tt>fun(v, i)</tt> on
   * each value/index association in <tt>arr</tt>.
   */
  function map(arr, fun) {
    var result = [];
    every(arr, function(value, index) {
      result.push(fun(value, index));
      return true;
    });
    return result;
  }

  /**
   * Does <i>there exist</i> a value/index association in arr for
   * which <tt>pred()</tt> is true?
   */
  function some(arr, pred) {
    return !every(arr, function(value, index) {
      return !pred(value, index);
    });
  }
  
  /**
   * Call <tt>fun(sofar, value, index)</tt> on each successive
   * value/index association in <tt>arr</tt>, accumulating the results
   * so far, and returning the final reduction.
   * <p>
   * If <tt>opt_initial</tt> is provided, use it to seed the
   * reduction. Otherwise, use the first association in <tt>arr</tt>.
   */
  function reduce(arr, fun, opt_initial) {
    var result = opt_initial;
    var preparing = opt_initial === undefined;
    every(arr, function(value, index) {
      if (preparing) {
        result = value;
        preparing = false;
      } else {
        result = fun(result, value, index);
      }
      return true;
    });
    if (preparing) {
      caja.fail('Empty reduce without identity element: ', arr);
    }
    return result;
  }
  
  /** TODO(erights): <tt>reduceRight</tt> not yet implemented */
  function reduceRight(arr, fun, opt_initial) {
    caja.fail('TODO(erights): reduceRight not yet implemented');
  }
  
  /** TODO(erights): <tt>setSlice</tt> not yet implemented */
  function setSlice(arr) {
    caja.fail('TODO(erights): setSlice not yet implemented');
  }
  

  /** TODO(erights): <tt>substr</tt> not yet implemented */
  function substr(str, opt_length) {
    caja.fail('TODO(erights): substr not yet implemented');
  }
  

  /**
   * Like the <tt>date.toJSONString()</tt> method defined in the
   * original json.js, but without the surrounding quotes.
   * <p>
   * This should be identical to the <tt>Date.toJSON()</tt> defined in
   * the standard json2.js. We leave it to extensions.js to use this
   * function to define <tt>Date.toJSON()</tt> manually, since there
   * should not be a corresponding static generic
   * <tt>Date.toJSON(date)</tt>. If there were, that would confuse JSON
   * serialization.  
   */
  function toISOString(date) {
    function f(n) {
      return n < 10 ? '0' + n : n;
    }
    return (date.getUTCFullYear()     + '-' +
            f(date.getUTCMonth() + 1) + '-' +
            f(date.getUTCDate())      + 'T' +
            f(date.getUTCHours())     + ':' +
            f(date.getUTCMinutes())   + ':' +
            f(date.getUTCSeconds())   + 'Z');
  };


  return caja.freeze({
    Array: {
      '.': Array,

      indexOf: indexOf,
      lastIndexOf: lastIndexOf,

      every: every,
      forEach: forEach,
      filter: filter,
      map: map,
      some: some,

      reduce: reduce,
      reduceRight: reduceRight,
      setSlice: setSlice
    },
    String: {
      '.': String,

      substr: substr,
    },
    Date: {
      '.': Date,

      toISOString: toISOString
    }
  });
  
})();
