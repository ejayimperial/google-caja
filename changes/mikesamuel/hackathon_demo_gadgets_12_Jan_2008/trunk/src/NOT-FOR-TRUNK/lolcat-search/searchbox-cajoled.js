{
  ___.loadModule(function (___OUTERS___) {
      ___OUTERS___.c_1___ = function (thisNode___, event) {
        ___.asSimpleFunc(___.primFreeze(___OUTERS___.doSearch))(thisNode___);
      };
      ___OUTERS___.searchEngine;
      ___OUTERS___.resultConsumer;
      ___OUTERS___.resultsOfLastSearch = [];
      ___OUTERS___.doSearch = ___.simpleFunc(function doSearch(button) {
          var form = (function () {
              var x___ = button;
              return x___.getForm_canCall___ ? x___.getForm() : ___.callPub(x___, 'getForm', []);
            })();
          var query = (function () {
              var x___ = (function () {
                  var x___ = (function () {
                      var x___ = (function () {
                          var x___ = form;
                          return x___.getElements_canCall___ ? x___.getElements() : ___.callPub(x___, 'getElements', []);
                        })();
                      return x___.q_canRead___ ? x___.q : ___.readPub(x___, 'q');
                    })();
                  return x___.getValue_canCall___ ? x___.getValue() : ___.callPub(x___, 'getValue', []);
                })();
              var x0___ = /^\s+|\s+$/g;
              var x1___ = '';
              return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
            })();
          if (!query) {
            (function () {
                var x___ = (function () {
                    var x___ = (function () {
                        var x___ = form;
                        return x___.getElements_canCall___ ? x___.getElements() : ___.callPub(x___, 'getElements', []);
                      })();
                    return x___.q_canRead___ ? x___.q : ___.readPub(x___, 'q');
                  })();
                return x___.focus_canCall___ ? x___.focus() : ___.callPub(x___, 'focus', []);
              })();
            return undefined;
          }
          (function () {
              var x___ = ___OUTERS___.searchEngine;
              var x0___ = query;
              var x1___ = ___.primFreeze(___.simpleFunc(function (results) {
                    ___OUTERS___.resultsOfLastSearch = (function () {
                        var x___ = results;
                        var x0___ = 0;
                        return x___.slice_canCall___ ? x___.slice(x0___) : ___.callPub(x___, 'slice', [x0___]);
                      })();
                    var resultList = (function () {
                        var x___ = ___OUTERS___.document;
                        var x0___ = 'results';
                        return x___.getElementById_canCall___ ? x___.getElementById(x0___) : ___.callPub(x___, 'getElementById', [x0___]);
                      })();
                    ___.asSimpleFunc(___OUTERS___.log)('resultList=' + resultList);
                    for (var child; child = (function () {
                          var x___ = resultList;
                          return x___.getFirstChild_canCall___ ? x___.getFirstChild() : ___.callPub(x___, 'getFirstChild', []);
                        })(); ) {
                      (function () {
                          var x___ = resultList;
                          var x0___ = child;
                          return x___.removeChild_canCall___ ? x___.removeChild(x0___) : ___.callPub(x___, 'removeChild', [x0___]);
                        })();
                    }
                    ___.asSimpleFunc(___OUTERS___.log)('removed children');
                    var n = (function () {
                        var x___ = results;
                        return x___.length_canRead___ ? x___.length : ___.readPub(x___, 'length');
                      })();
                    if (!n) {
                      (function () {
                          var x___ = resultList;
                          var x0___ = new (___.asCtor(___OUTERS___.StringInterpolation))(['\074center\076No results\074/center\076']);
                          return x___.setInnerHTML_canCall___ ? x___.setInnerHTML(x0___) : ___.callPub(x___, 'setInnerHTML', [x0___]);
                        })();
                      return undefined;
                    }
                    for (var i = 0; i < n; ++i) {
                      var result = ___.readPub(results, i);
                      var li = (function () {
                          var x___ = ___OUTERS___.document;
                          var x0___ = 'LI';
                          return x___.createElement_canCall___ ? x___.createElement(x0___) : ___.callPub(x___, 'createElement', [x0___]);
                        })();
                      var snippetText = (function () {
                          var x___ = (function () {
                              var x___ = result;
                              return x___.snippetHtml_canRead___ ? x___.snippetHtml : ___.readPub(x___, 'snippetHtml');
                            })();
                          var x0___ = /\074\/?[A-Za-z][^\076]*\076/g;
                          var x1___ = ' ';
                          return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
                        })();
                      var titleText = (function () {
                          var x___ = (function () {
                              var x___ = result;
                              return x___.titleHtml_canRead___ ? x___.titleHtml : ___.readPub(x___, 'titleHtml');
                            })();
                          var x0___ = /\074\/?[A-Za-z][^\076]*\076/g;
                          var x1___ = ' ';
                          return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
                        })();
                      (function () {
                          var x___ = li;
                          var x0___ = new (___.asCtor(___OUTERS___.StringInterpolation))(['\074b\076', titleText, '\074/b\076 &mdash; \074tt\076\074a href=\"#\"\076', (function () {
                                  var x___ = result;
                                  return x___.url_canRead___ ? x___.url : ___.readPub(x___, 'url');
                                })(), '\074/a\076\074/tt\076']);
                          return x___.setInnerHTML_canCall___ ? x___.setInnerHTML(x0___) : ___.callPub(x___, 'setInnerHTML', [x0___]);
                        })();
                      (function () {
                          var x___ = li;
                          var x0___ = 'click';
                          var x1___ = ___.asSimpleFunc(___.primFreeze(___OUTERS___.curry))(___.primFreeze(___OUTERS___.showResult), [i]);
                          return x___.addEventListener_canCall___ ? x___.addEventListener(x0___, x1___) : ___.callPub(x___, 'addEventListener', [x0___, x1___]);
                        })();
                      (function () {
                          var x___ = resultList;
                          var x0___ = li;
                          return x___.appendChild_canCall___ ? x___.appendChild(x0___) : ___.callPub(x___, 'appendChild', [x0___]);
                        })();
                    }
                  }));
              return x___.webSearch_canCall___ ? x___.webSearch(x0___, x1___) : ___.callPub(x___, 'webSearch', [x0___, x1___]);
            })();
        });
      ___OUTERS___.showResult = ___.simpleFunc(function showResult(index) {
          var result = ___.readPub(___OUTERS___.resultsOfLastSearch, index);
          if (result) {
            ___.asSimpleFunc(___OUTERS___.resultConsumer)(___.asSimpleFunc(___.primFreeze(___OUTERS___.clone))(result));
          }
          return false;
        });
      ___OUTERS___.curry = ___.simpleFunc(function curry(fn, args) {
          return ___.primFreeze(___.simpleFunc(function () {
                return (function () {
                    var x___ = fn;
                    var x0___ = {
                    };
                    var x1___ = args;
                    return x___.apply_canCall___ ? x___.apply(x0___, x1___) : ___.callPub(x___, 'apply', [x0___, x1___]);
                  })();
              }));
        });
      ___OUTERS___.clone = ___.simpleFunc(function clone(original) {
          var cloned = {
          };
          {
            var x0___ = original;
            var x1___ = undefined;
            var k;
            for (x1___ in x0___) {
              if (___.canEnumPub(x0___, x1___)) {
                k = x1___;
                ___.setPub(cloned, k, ___.readPub(original, k));
              }
            }
          }
          return cloned;
        });
      ___OUTERS___.emitHtml___('\n\n\074center\076\n  \074form onsubmit=\"return false\"\076\n    \074input type=\"text\" size=\"60\" name=\"q\" value=\"it was the best of times\"\076\n    \074input type=\"button\" value=\"Search\" onclick=\"return plugin_dispatchEvent___(this, event || window.event, ', ___.getId(___OUTERS___), ', \'c_1___\')\"\076\n  \074/form\076\n\074/center\076\n');
    });
}
