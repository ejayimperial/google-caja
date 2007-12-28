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

// This module virtually adds some "consensus extensions" to the ES3
// primordial objects, to ease the porting of library code from
// unconstrained Javascript to Caja-compliant Javascript. Many
// existing libraries add variants of these to Javascript
// themselves. But a Caja-compliant variant of these libraries would
// not be able to, so this module grandfathers in some of these, much
// as ES4 <a href=
// "http://wiki.ecmascript.org/doku.php?id=proposals:static_generics"
// >proposes</a> to do. 

// As agreed <a href="http://tinyurl.com/2pnpqo" >here</a>, if this
// module detects a conflict when trying to virtually add its
// extensions to these primordial objects, it logs the
// conflict. However, since the extensions are only virtually added
// --- as fault-handling functions so that they won't be enumerable to
// innocent code --- there can't be any actual conflict. Untranslated
// innocent JavaScript code will only see whatever extensions come
// with the browser or are added to the primordial objects by other
// untranslated libraries. Caja code will only see the generic
// extensions virtually added by this module.

// This module is written in unconstrained Javascript, not
// Caja-compliant Javascript, and would be rejected by the Caja
// translator. It depends on caja.js are generics-cajita.js. Since
// caja.js marks primordial objects as frozen, but this module causes
// Caja-visible mutations of these objects, if this module is indeed
// loaded into a Javascript system, it MUST be loaded after caja.js.
// It SHOULD be loaded before any modules (other than
// generics-cajita.js) originally written in Caja are loaded. And it
// MUST be loaded before any untrusted modules originally written in
// Caja are loaded.


(function(global) {

  /**
   * If there is an actual (non-virtual) <tt>obj[name]</tt>, log a
   * report of a conflicting extension.
   */
  function report(obj, name, callable) {
    if (name in obj) {
      var original = obj[name];
      var kind = '?';
      if (Object.prototype.propertyIsEnumerable.call(obj, name)) {
        // If it is enumerable, it probably came from some other
        // library.
        kind = 'library';
      } else {
        if (String(original).indexOf('[native code]') === -1) {
          kind = 'original script';
        } else {
          kind = 'native';
        }
      }
      ___.log('Extension conflict with ', kind, ': ', 
              obj, '.', name);
    }
  }

  /**
   * For virtually extending <tt>constr</tt> with a new generic from 
   * generic-cajita.js.
   * <p>
   * Arrange to handle call-faults on <tt>constr[name](args...)</tt>
   * by calling <tt>generic(args...)</tt>.
   */
  function extendFunc(constr, name, generic) {
    ___.useApplyHandler(constr, name, function(obj, args) {
      return generic.apply(obj, args);
    });
    report(constr, name, generic);
  }

  /**
   * For virtually extending a prototype with a new generic from
   * generic-cajita.js. 
   *
   * Arrange to handle call-faults on <tt>obj[name](args...)</tt>,
   * where <tt>obj</tt> inherits from <tt>proto</tt>, by
   * calling <tt>generic(obj, args...)</tt>.
   */
  function extendProto(proto, name, generic) {
    ___.useApplyHandler(proto, name, function(obj, args) {
      return generic.apply(obj, [obj].concat(args));
    });
    report(proto, name, generic);
  }

  /**
   * For virtually extending <tt>constr</tt> with a generic derived
   * from the existing standard allowed instance method at
   * <tt>constr.prototype[name]</tt>. 
   * <p>
   * Arrange to handle call-faults on <tt>constr[name](obj, args...)</tt>
   * by calling <tt>obj[name](args...)</tt> as a method of
   * <tt>obj</tt>.
   */
  function extendStatic(constr, name) {
    var meth = constr.prototype[name];
    ___.enforceType(meth, 'function', name);
    ___.useApplyHandler(constr, name, function(obj, args) {
      return meth.apply(args[0], Array.prototype.slice.call(args, 1));
    });
    report(constr, name, meth);
  }


  ___.forEach(generics, ___.simpleFunc(function(members, tname) {
    var constr = members['.'];
    ___.forEach(members, ___.simpleFunc(function(generic, mname) {
      if (mname !== '.') {
        extendFunc(constr, mname, generic);
        extendProto(constr.prototype, mname, generic);
      }
    }));
  }));

  
  extendProto(Date.prototype, 'toJSON', generics.Date.toISOString);

  ___.all2(extendStatic, Array, [
    'concat', 'join', 'pop', 'push', 'reverse', 'shift', 'slice',
    'sort', 'splice', 'unshift',
  ]);

  ___.all2(extendStatic, String, [
    'substring', 'charAt', 'charCodeAt', 'indexOf', 'lastIndexOf',
    'toLowerCase', 'toUpperCase', 'toLocaleLowerCase',
    'toLocaleUpperCase', 'localeCompare', 'match', 'search',
    'replace', 'split', 'concat', 'slice'
  ]);
  
})(this);
