// Copyright (C) 2011 Google Inc.
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
 * @provides ES53FrameGroup
 * @overrides ___
 * @requires cajolingServiceClientMaker
 * @requires Domado
 * @requires GuestManager
 * @requires HtmlEmitter
 * @requires Q
 * @requires jsonRestTransportMaker
 * @requires taming
 * @requires window
 */

function ES53FrameGroup(cajaInt, config, tamingWin, feralWin) {
  if (tamingWin !== window) {
    throw new Error('wrong frame');
  }

  var cajoler = cajolingServiceClientMaker(
    cajaInt.joinUrl(config.server, 'cajole'),
    jsonRestTransportMaker(),
    true,
    config.debug,
    config.console);

  var domado = Domado(makeDomadoRuleBreaker());

  var frameGroup = {
    iframe: window.frameElement,

    makeES5Frame: makeES5Frame,
    
    grantMethod: taming.grantTameAsMethod,
    grantRead: taming.grantTameAsRead,
    grantReadWrite: taming.grantTameAsReadWrite,
    markCtor: taming.markTameAsCtor,
    markFunction: taming.markTameAsFunction,
    markReadOnlyRecord: taming.markTameAsReadOnlyRecord,
    markXo4a: taming.markTameAsXo4a,
    tame: taming.tame
  };

  return frameGroup;

  //----------------

  function makeES5Frame(div, uriPolicy, es5ready, opt_idClass) {
    var divs = cajaInt.prepareContainerDiv(div, feralWin, opt_idClass);
    cajaInt.loadCajaFrame(config, 'es53-guest-frame', function (guestWin) {
      var domicile = makeDomicile(divs, uriPolicy, guestWin);
      var gman = GuestManager(divs, domicile, guestWin, es53run);
      gman._loader = guestWin.loadModuleMaker(
        cajaInt.documentBaseUrl(), cajoler);
      es5ready(gman);
    });
  }

  function makeDomicile(divs, uriPolicy, guestWin) {
    if (!divs.inner) { return null; }

    // Needs to be accessible by Domado. But markFunction must be done at
    // most once, so markFunction(uriPolicy.rewrite) would only work once,
    // and having side effects on our arguments is best avoided.
    var uriPolicyWrapper = ___.whitelistAll({
      rewrite: ___.markFunc(function (uri, uriEffect, loaderType, hints) {
        return taming.tame(uriPolicy.rewrite(
          uri, uriEffect, loaderType, hints));
      })
    });

    // The Domita implementation is obtained from the taming window,
    // since we wish to protect Domita and its dependencies from the
    // ability of guest code to modify the shared primordials.

    // TODO(kpreid): This is probably wrong: we're replacing the feral
    // record imports with the tame constructed object 'window'.

    var domicile = domado.attachDocument(
      '-' + divs.idClass, uriPolicyWrapper, divs.inner);
    var imports = domicile.window;

    // Add JavaScript globals to the DOM window object.
    ___.copyToImports(imports, guestWin.___.sharedImports);

    // These ___ variables are interfaces used by cajoled code.
    imports.htmlEmitter___ = new HtmlEmitter(
      makeDOMAccessible, divs.inner, domicile, guestWin);
    imports.rewriteUriInCss___ = domicile.rewriteUriInCss.bind(domicile);
    imports.rewriteUriInAttribute___ =
      domicile.rewriteUriInAttribute.bind(domicile);
    imports.getIdClass___ = domicile.getIdClass.bind(domicile);
    imports.emitCss___ = domicile.emitCss.bind(domicile);
    imports.tameNodeAsForeign___ = domicile.tameNodeAsForeign.bind(domicile);

    ___.getId = cajaInt.getId;
    ___.getImports = cajaInt.getImports;
    ___.unregister = cajaInt.unregister;

    feralWin.___.getId = cajaInt.getId;
    feralWin.___.getImports = cajaInt.getImports;
    feralWin.___.unregister = cajaInt.unregister;

    guestWin.___.getId = cajaInt.getId;
    guestWin.___.getImports = cajaInt.getImports;
    guestWin.___.unregister = cajaInt.unregister;

    cajaInt.getId(imports);

    if (!feralWin.___.tamingWindows) {
      feralWin.___.tamingWindows = {};
    }
    feralWin.___.tamingWindows[imports.id___] = tamingWin;

    // domado innerHTML sanitizer uses feralWin.___.plugin_dispatchEvent___
    // html-emitter uses guestWin.___.plugin_dispatchEvent___
    feralWin.___.plugin_dispatchEvent___ = domado.plugin_dispatchEvent;
    guestWin.___.plugin_dispatchEvent___ = domado.plugin_dispatchEvent;
    feralWin.___.plugin_dispatchToHandler___ =
      function (pluginId, handler, args) {
        var tamingWin = feralWin.___.tamingWindows[pluginId];
        return tamingWin.___.plugin_dispatchToHandler___(
            pluginId, handler, args);
      };

    return domicile;
  }

  //----------------

  function es53run(gman, args, moreImports, opt_runDone) {
    function runModule(module) {
      var result = module.instantiate___(guestWin.___, gman.imports);
      if (opt_runDone) {
        opt_runDone(result);
      }
    }

    if (!moreImports.onerror) {
      moreImports.onerror = ___.markFunc(onerror);
    }
    var guestWin = gman.iframe.contentWindow;
    ___.copyToImports(gman.imports, moreImports);

    // TODO(felix8a): not right for multiple guests
    if (args.flash) {
      var tamingFlash = tamingWin.cajaFlash;
      if (gman.domicile && tamingFlash && tamingFlash.init) {
        tamingFlash.init(
          feralWin, gman.imports, tamingWin, gman.domicile, guestWin);
      }
    }

    if (args.isCajoled) {
      if (gman.domicile && args.cajoledHtml !== undefined) {
        gman.innerContainer.innerHTML = args.cajoledHtml;
      }
      runModule(guestWin.prepareModuleFromText___(args.cajoledJs));

    } else if (args.uncajoledContent !== undefined) {
      Q.when(
        // unspecified mimeType here means html
        cajoler.cajoleContent(
          args.url, args.uncajoledContent, args.mimeType || 'text/html',
          gman.idClass),
        function (jsonModule) {
          guestWin.Q.when(
            gman._loader.loadCajoledJson___(args.url, jsonModule),
            function (module) {
              runModule(module);
            },
            function (ex) {
              throw new Error('Error loading module: ' + ex);
            });
        },
        function (ex) {
          throw new Error('Error cajoling content: ' + ex);
        });

    } else {
      // uncajoled url
      Q.when(
        // unspecified mimeType here means loader will guess from url
        gman._loader.async(args.url, args.mimeType),
        function (module) {
          runModule(module);
        },
        function (ex) {
          throw new Error('Error loading module: ' + ex);
        });
    }
  }

  function onerror(message, source, lineNum) {
    config.log('Uncaught script error: ' + message +
               ' in source: "' + source +
               '" at line: ' + lineNum);
  }

  //----------------
      
  function makeDomadoRuleBreaker() {
    // TODO(felix8a): should markFunc be markFuncFreeze?
    var ruleBreaker = {
      makeDOMAccessible: ___.markFunc(makeDOMAccessible),
      makeFunctionAccessible: ___.markFunc(function (f) {
        return markCallableWithoutMembrane(f);
      }),
      permitUntaming: ___.markFunc(function (o) {
        // Allow guest constructed objects to be (uselessly) passed through
        // the taming membrane.  This is primarily for the test suite.
        if (typeof o === 'object' || typeof o === 'function') {
          taming.tamesTo(new FeralTwinStub(), o);
        } // else let primitives go normally
      }),
      tame: taming.tame,
      untame: taming.untame,
      tamesTo: taming.tamesTo,
      hasTameTwin: ___.markFunc(function (f) {
        return 'TAMED_TWIN___' in f;
      }),
      writeToPixelArray: ___.markFunc(writeToPixelArray),
      getId: ___.markFunc(function () {
        return cajaInt.getId.apply(undefined, arguments);
      }),
      getImports: ___.markFunc(function () {
        return cajaInt.getImports.apply(undefined, arguments);
      })
    };
    return ___.whitelistAll(ruleBreaker);
  }

  /**
   * Permit func to be called by cajoled code without modifying the
   * arguments. This should only be used for stuff which ignores the taming
   * membrane deliberately.
   */
  function markCallableWithoutMembrane(func) {
    if (func !== undefined && !func.i___) {
      func.i___ = function () {
        // hide that this is being invoked as a method
        return Function.prototype.apply.call(func, undefined, arguments);
      };
      func.new___ = function () {
        if (arguments.length !== 0) {
          throw new TypeError("construction with args not implemented");
        } else {
          return new func();
        }
      };
      func.call_m___ = func;
      func.apply_m___ = func;
      taming.tamesTo(func, func);
    }
    return func;
  }

  // On Firefox 4.0.1, at least, canvas pixel arrays cannot have added
  // properties (such as our w___). Therefore to be able to write them we
  // need uncajoled code to do it. An alternative approach would be to
  // muck with the "Uint8ClampedArray" prototype.
  function writeToPixelArray(source, target, length) {
    for (var i = length-1; i >= 0; i--) {
      target[+i] = source[+i];
    }
  }

  /**
   * This function adds magic ES5/3-runtime properties on an object from
   * the host DOM such that it can be accessed as if it were a guest
   * object. It effectively whitelists everything.
   *
   * This completely breaks the invariants of the ES5/3 taming membrane
   * and the resulting object should under no circumstance be given to
   * untrusted code.
   *
   * It returns its argument, both for convenience and because bridal.js
   * is written to be adaptable to an environment where this action
   * requires wrappers. (Domado is not.)
   */
  function makeDOMAccessible(o) {
    // This accepts functions because some objects are incidentally
    // functions. makeDOMAccessible does not make functions callable.

    // Testing for own properties, not 'in', because some quirk of Firefox
    // makes  event objects appear as if they have the taming frame's
    // prototype after being passed into taming frame code (!), so we want
    // to be able to override Object.prototype.v___ etc. Except for that,
    // it would be safer to not allow applying this to apparently defined-
    // in-taming-frame objects.
    if ((typeof o === 'object' || typeof o === 'function')
        && o !== null
        && !Object.prototype.hasOwnProperty.call(o, 'v___')) {
      o.v___ = function (p) {
        return this[p];
      };
      o.w___ = function (p, v) {
        this[p] = v;
      };
      o.m___ = function (p, as) {
        // From es53 tameObjectWithMethods without the membrane features.
        p = '' + p;
        if (('' + (+p)) !== p && !(/__$/).test(p)) {
          var method = o[p];
          if (typeof method === 'function') {
            return method.apply(o, as);
          }
        }
        throw new TypeError('Not a function: ' + p);
      };
      o.HasProperty___ = function (p) { return p in this; };
    }
    return o;
  }

  function FeralTwinStub() {}
}
