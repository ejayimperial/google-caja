/*
    JSON.js
    2007-10-02

    Based on Doug Crockford's Public Domain
    json.js and json2.js
    2007-09-27

...............................................................................

This file adds these methods to JavaScript:

        Date.toJSON()

            Returns an ISO string encoding the date. When serializing
            with the default replacer, this brings about the same effect
            as json.js's Date.toJSONString().

JSON.js is written in Javascript, not Caja, and will not pass the Caja
translator. However, it is meant to co-exist smoothly with the Caja
runtime. If loaded after caja.js is loaded, it will allow Caja code to
call Date.toJSON(), JSON.stringify(), and JSON.parse().

Use your own copy. It is extremely unwise to load untrusted third
party code into your pages.  
*/


/**
 * Like the date.toJSONString() method defined in json.js, except
 * without the surrounding quotes. This should be identical to
 * Date.prototype.toISOString when that is defined, as it is in caja.js
 */
Date.prototype.toJSON = function () {
  function f(n) {
    return n < 10 ? '0' + n : n;
  }
  return (this.getUTCFullYear()     + '-' +
          f(this.getUTCMonth() + 1) + '-' +
          f(this.getUTCDate())      + 'T' +
          f(this.getUTCHours())     + ':' +
          f(this.getUTCMinutes())   + ':' +
          f(this.getUTCSeconds())   + 'Z');
};


JSON = (function (global) {

  var forbidden = new RegExp("_$");

  /**
   * A replacer is a filter function that takes a baseObj and a
   * key for indexing into that baseObj -- the name of one of
   * its properties. A replacer can: <ul>
   * <li> return baseObj[key], in which case serialization
   *      proceeds normally.
   * <li> return undefined, suppressing the apparent existence
   *      of this property on this baseObj.
   * <li> return something else, in which case it will be used
   *      instead of baseObj[key].
   * <li> throw, terminating serialization.
   * </ul>
   * If a replacer is provided to serialize(), it is applied in
   * top-down order, so traversal proceeds only into the
   * results of filtering. 
   * <p>
   * If no optReplacer argument is provided to serialize(),
   * this defaultReplacer is used. It is part of the API so that
   * other replacers can be built by composing with it. This
   * default replacer will return undefined unless either a) key
   * is a string, an own-property of baseObj, and does not end
   * in an underbar, or b) if key is a number and baseObj is an
   * array. If baseObj[key] implements toJSON(), the default
   * replacer will return baseObj[key].toJSON(), enabling
   * individual objects (such as dates) to offer replacements
   * for themselves. Otherwise, it returns baseObj[key]. 
   */  
  function defaultReplacer(baseObj, key) {
    var result;
    
    if (typeof key === 'string') {
      if (!Object.prototype.hasOwnProperty.call(baseObj, key)) {
        return undefined;
      }
      if (forbidden.test(key)) {
        return undefined;
      }
    } else if (typeof key === 'number') {
      if (!(baseObj instanceof Array)) {
        return undefined;
      }
    } else {
      return undefined;
    }
    result = baseObj[key];
    if (result && typeof result.toJSON === 'function') {
      return result.toJSON();
    } else {
      return result;
    }
  }

  /** m is a table of character substitutions. */
  var m = {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '"' : '\\"',
    '\\': '\\\\'
  };
  
  /**
   * JSON.serialize(value) does much the same job that
   * value.toJSONString() is supposed to do in the original
   * json.js library on normal JSON objects. However, they
   * provide different hooks for having their behavior extended.
   * <p>
   * For json.js, other objects can provide their own
   * implementation of toJSONString(), in which case JSON
   * serialization relies on these objects to return a correct
   * JSON string. But if one object instead returns an
   * unbalanced part of a JSON string and another object
   * returns a compensating unbalanced string, then an outer
   * toJSONString() can produce quoting confusions that invite
   * XSS-like attacks. The primary purpose of JSON.js is
   * to prevent such attacks.
   * <p>
   * The design of JSON.js borrows ideas from Java's object
   * serialization streams.
   */    
  function serialize(value, optReplacer) {
    var out = []; // array holding partial texts
    // var stack = []; // for diagnosing cycles
    var replacer = optReplacer || defaultReplacer;
    
    /**
     * The internal recursive serialization function.
     */
    function primSerialize(value) {
      var i,j; // loop counters
      var len; // array lengths;
      var needComma = false;
      var k,v; // property key and value
      
      // stack.push(value);
      
      switch (typeof value) {
      case 'object':
        if (value === null) {
          out.push('null');
          
        } else if (value instanceof Array) {
          len = value.length;
          out.push('[');
          for (i = 0; i < len; i += 1) {
            v = replacer(value, i);
            if (v !== undefined) {
              if (needComma) {
                out.push(',');
              } else {
                needComma = true;
              }
              primSerialize(v);
            }
          }
          out.push(']');
          
        } else {
          out.push('{');
          for (k in value) {
            v = replacer(value, k);
            if (v !== undefined) {
              if (needComma) {
                out.push(',');
              } else {
                needComma = true;
              }
              primSerialize(k);
              out.push(':');
              primSerialize(v);
            }
          }
          out.push('}');
        }
        break;
        
      case 'string':
        // If the string contains no control characters, no quote
        // characters, and no backslash characters, then we can
        // simply slap some quotes around it.  Otherwise we must
        // also replace the offending characters with safe
        // sequences.
        if ((/["\\\x00-\x1f]/).test(value)) { //"])){
          out.push('"' + 
                   value.replace((/[\x00-\x1f\\"]/g), //"]),
                                 function (a) {
                                   var c = m[a];
                                   if (c) {
                                     return c;
                                   }
                                   c = a.charCodeAt();
                                   return '\\u00' + 
                                     Math.floor(c / 16).toString(16) +
                                     (c % 16).toString(16);
                                 }) + '"');
        } else {
          out.push('"' + value + '"');
        }
        break;
        
      case 'number':
        // JSON numbers must be finite. Encode non-finite numbers
        // as null. 
        out.push(isFinite(value) ? String(value) : 'null');
        break;
        
      case 'boolean':
        out.push(String(value));
        break;
        
      default:
        out.push('null');
      }
      // stack.pop();
    }
    
    var fakeRoot = [value];
    primSerialize(replacer(fakeRoot, 0));
    return out.join('');
  }

  /**
   * Does much the same job that JSON.stringify() is supposed
   * to do in the original json2.js, but using a replacer that
   * also filters out all keys that end in an "_", so that Caja
   * code can be allowed to call JSON.stringify().
   * <p>
   * TODO(erights): whitelist handling is not yet implemented. Would
   * it be useful? 
   */
  function stringify(value, optWhitelist) {
    var replacer = defaultReplacer;
    if (optWhitelist !== undefined) {
      throw new Error("TODO(erights): whitelist not yet implemented");
    }
    return serialize(value, replacer);
  }
  
  /**
   * A reviver is filter function that takes a key and the
   * originally unserialized value associated with that key. A
   * reviver can<ul>
   * <li> return value, in which case unserialization
   *      proceeds normally.
   * <li> return undefined, causing this key/value pair to be
   *      dropped from the containing object.
   * <li> return something else, in which case this alternate
   *      value is associated with key in the containing object.
   * <li> throw, terminating unserialization.
   * </ul>
   * This default reviver returns undefined if the key ends in
   * underbar. Otherwise it return value.
   */
  function defaultReviver(key, value) {
    if (typeof key === 'string' && forbidden.test(key)) {
      return undefined;
    } else {
      return value;
    }
  }
  
  /**
   * JSON.unserialize(string, optReviver) acts like json.js's
   * string.parseJSON(optFilter). This version also fixes a bug
   * in the original: json.js specifies "If [the optional filter]
   * returns undefined then the member is deleted." However,
   * the implemenation in json.js instead defines the property to
   * have the value undefined. JSON.unserialize() does indeed
   * delete the property in this case.
   * <p>
   * Bug: If the JSON expression to be unserialized contains
   * the key "__proto__", this will be silently ignored on
   * Firefox independent of the behavior of optReviver. json.js
   * exhibits the same bug on Firefox. Whether this is a bug in
   * these JSON libraries, in the Javascript spec, or in the
   * Firefox implementation of Javascript is open to debate. In
   * any case, this problem is unlikely to be fixed.
   * <p>
   * If no optReviver argument is provided to unserialize(),
   * then the result is just the literal tree of JSON
   * objects. If an optReviver is provided to unserialize(), it
   * is applied in bottom-up order, so that reconstructed parts
   * are available for building reconstructed wholes. 
   */
  function unserialize(str, optReviver) {
    
    var result;
    
    function walk(value) {
      var i,len,k,v;
      
      if (value && typeof value === 'object') {
        if (value instanceof Array) {
          len = value.length;
          for (i = 0; i < len; i += 1) {
            walk(value[i]);
            v = optReviver(i, value[i]);
            if (v === undefined) {
              delete value[i];
            } else {
              value[i] = v;
            }
          }
        } else {
          for (k in value) {
            walk(value[k]);
            v = optReviver(k, value[k]);
            if (v === undefined) {
              delete value[k];
            } else {
              value[k] = v;
            }
          }
        }
      }
      
    }
    
    if ((/^[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]*$/).
        test(str.
             replace((/\\./g), '@').
             replace((/"[^"\\\n\r]*"/g), ''))) { //"))) {
      result = eval('(' + str + ')');
      if (optReviver !== undefined) {
        var fakeRoot = [result];
        walk(fakeRoot);
        return fakeRoot[0];
      } else {
        return result;
      }
    }
    throw new SyntaxError('parseJSON');
  }
  
  /**
   * JSON.parse() acts like json2.js's JSON.parse(), except
   * that the optReviver, if provided, is composed with
   * JSON.defaultReviver, which filters out keys ending with
   * underbar. As a result, Caja code can be allowed to call
   * JSON.parse().
   */
  function parse(string, optReviver) {
    var reviver = defaultReviver;
    if (optReviver !== undefined) {
      reviver = function(key, value) {
        var result = defaultReviver(key, value);
        if (result === undefined) {
          return undefined;
        } else {
          return optReviver(key, result);
        }
      };
    }
    return unserialize(string, reviver);
  }
  
  var result = {
    defaultReplacer: defaultReplacer,
    serialize: serialize,
    stringify: stringify,
    
    defaultReviver: defaultReviver,
    unserialize: unserialize,
    parse: parse
  };

  if (global.caja) {
    ___.allowMethod(Date, 'toJSON');
    ___.primFreeze(___.simpleFunc(stringify));
    ___.primFreeze(___.simpleFunc(parse));
    ___.primFreeze(result);
  }

  return result;

})(this);
