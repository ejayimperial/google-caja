// Copyright (C) 2008 Google Inc.
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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Evaluates a Cajita module and returns the value provided by the module. The
 * module must be written according to the following idiom:
 *
 * <pre>
 * (function(args...) {
 *   // module code here
 *   return someObj;
 * });
 * </pre>
 *
 * where <code>args...</code> is the set of capabilities provided to the module upon
 * initialization, and <code>someObj</code> is whatever the module evaluates to, which
 * <em>must</em> be an object (not a primitive value such as an integer). Typically,
 * <code>someObj</code> is an object with public attributes and methods that the module
 * exposes to its clients. Variables declared within the module but not made available
 * via <code>someObj</code> are the module's private data.
 */
public class JsImporter {
  private final Map<String, Function> functionCache = new HashMap<String, Function>();

  /**
   * Import a module.
   *
   * @param moduleName the name of the module file, in a form that can be loaded by the
   *   ClassLoader of this class.
   * @param args the arguments provided to the module initialization function.
   * @return the module return value.
   */  
  public ScriptableObject importModule(
      String moduleName,
      Object... args) {
    return (ScriptableObject)getFunction(moduleName).call(
        Context.getCurrentContext(),
        Context.getCurrentContext().initStandardObjects(),
        null,
        args);
  }

  private Function getFunction(String moduleName) {
    Function result = functionCache.get(moduleName);
    if (result == null) {
      try {        
        result = (Function)Context.getCurrentContext().evaluateReader(
            Context.getCurrentContext().initStandardObjects(),
            new InputStreamReader(getClass().getClassLoader().getResource(moduleName).openStream()),
            moduleName,
            1,
            null);
        functionCache.put(moduleName, result);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }
}
