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

/**
 * This script exports one global function, <tt>xo4a</tt>
 * (short for "exophora"), for enabling Caja code to create safe
 * exophoric functions, so it can use JavaScript APIs that expect
 * them. This script cannot itself be cajoled since, without this
 * extension, Caja code cannot create exophoric functions. This script
 * depends only on caja.js. The <tt>xo4a</tt> function it exports is
 * marked as a simple-function and can safely be provided to Caja 
 * code. 
 * <p>
 * An genuine exophoric function is a function that mentions
 * <tt>this</tt>, but whose binding for <tt>this</tt> is not lexically
 * apparent. Caja prohibits genuine exophoric functions, as they would
 * enable a Caja program to violate the encapsulation of other Caja
 * objects. 
 * <p>
 * Instead, the <tt>xo4a</tt> function takes a simple-function
 * representing a <i>call-function</i> as an argument, and returns an
 * safe exophoric wrapper function that accepts direct method calls and
 * reflective calls using <tt>call</tt>, <tt>apply</tt>, and
 * <tt>bind</tt>. All of these are implemented in terms of the
 * call-function argument passed to <tt>xo4a</tt>. By
 * <i>call-function</i>, we mean a function with the same parameter
 * list as the <tt>call</tt> methods of functions, taking the
 * receiving object as first parameter, and the actual arguments as
 * remaining parameters.
 * <p>
 * The exophoric functions that <tt>xo4a()</tt> returns are, for now,
 * marked as unattached methods of <tt>Object</tt>. TODO(erights): that
 * is obviously wrong. In particular, if <tt>fn</tt> is an exophoric
 * function and <tt>p</tt> is <tt>({m:fn})</tt>, then <tt>p.m(a,b)</tt>
 * should call <tt>fn</tt> as a method on p. It instead fails, since 
 * <tt>p.m</tt> will be marked canRead but not canCall. Fortunately,
 * <tt>p.m.call(p,a,b)</tt>, and similar reflective calls, will still
 * succeed. Existing libraries that assume exophoric functions, like
 * jQuery, generally call them only reflectively anyway, so this should
 * be adequate for now. 
 */
var xo4a = (function() {

  function xo4a(callFn) {
    callFn = ___.asSimpleFunc(callFn);
    function result() {
      if (this === ___.theGlobalObject) {
	___.fail('Cannot call xo4a as a simple function');
      }
      return callFn.apply(___.USELESS, 
			  [this].concat(___.args(arguments)));
    }
    return ___.method(Object,result);
  }
  return ___.primFreeze(___.simpleFunc(xo4a, 'xo4a'));

})();
