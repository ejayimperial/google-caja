{
  ___.loadModule(function (___, IMPORTS___) {
                   var StringInterpolation = ___.readImport(IMPORTS___, 'StringInterpolation');
                   var document = ___.readImport(IMPORTS___, 'document');
                   var resultConsumer = ___.readImport(IMPORTS___, 'resultConsumer');
                   var searchEngine = ___.readImport(IMPORTS___, 'searchEngine');
                   var doSearch;
                   var showResult;
                   var clone;
                   IMPORTS___.emitCss___([ '.', ' #results-', ' li {\n  list-style-type: none;\n  margin-top: .5em;\n  border-bottom: 1px dotted #888\n}\n.', ' form {\n  display: inline\n}' ].join(IMPORTS___.getIdClass___()));
                   IMPORTS___.c_1___ = ___.primFreeze(___.simpleFunc(function (thisNode___, event) {
                                                                       ___.asSimpleFunc(___.primFreeze(doSearch))(thisNode___);
                                                                     }));
                   {
                     doSearch = ___.simpleFunc(function doSearch(button) {
                                                 var x0___;
                                                 var x1___;
                                                 var x2___;
                                                 var x3___;
                                                 var x4___;
                                                 var x5___;
                                                 var x6___;
                                                 var x7___;
                                                 var x8___;
                                                 var x9___;
                                                 var x10___;
                                                 var x11___;
                                                 var x12___;
                                                 var form = (x0___ = button, undefined, x0___.getForm_canCall___? x0___.getForm(): ___.callPub(x0___, 'getForm', [ ]));
                                                 var query = (x3___ = (x4___ = (x5___ = (x6___ = form, undefined, x6___.getElements_canCall___? x6___.getElements(): ___.callPub(x6___, 'getElements', [ ])), x5___.q_canRead___? x5___.q: ___.readPub(x5___, 'q')), undefined, x4___.getValue_canCall___? x4___.getValue(): ___.callPub(x4___, 'getValue', [ ])), (x1___ = new ___.RegExp('^\\s+|\\s+$', 'g'), x2___ = ''), x3___.replace_canCall___? x3___.replace(x1___, x2___): ___.callPub(x3___, 'replace', [ x1___, x2___ ]));
                                                 if (!query) {
                                                   x7___ = (x8___ = (x9___ = form, undefined, x9___.getElements_canCall___? x9___.getElements(): ___.callPub(x9___, 'getElements', [ ])), x8___.q_canRead___? x8___.q: ___.readPub(x8___, 'q')), undefined, x7___.focus_canCall___? x7___.focus(): ___.callPub(x7___, 'focus', [ ]);
                                                   return undefined;
                                                 }
                                                 x12___ = searchEngine, (x10___ = query, x11___ = ___.primFreeze(___.simpleFunc(function (results) {
                                                                                                                                  var x0___;
                                                                                                                                  var x1___;
                                                                                                                                  var x2___;
                                                                                                                                  var x3___;
                                                                                                                                  var x4___;
                                                                                                                                  var x5___;
                                                                                                                                  var x6___;
                                                                                                                                  var x7___;
                                                                                                                                  var x8___;
                                                                                                                                  var x9___;
                                                                                                                                  var x10___;
                                                                                                                                  var x11___;
                                                                                                                                  var x12___;
                                                                                                                                  var x13___;
                                                                                                                                  var x14___;
                                                                                                                                  var x15___;
                                                                                                                                  var x16___;
                                                                                                                                  var x17___;
                                                                                                                                  var x18___;
                                                                                                                                  var x19___;
                                                                                                                                  var x20___;
                                                                                                                                  var x21___;
                                                                                                                                  var x22___;
                                                                                                                                  var x23___;
                                                                                                                                  var x24___;
                                                                                                                                  var x25___;
                                                                                                                                  var x26___;
                                                                                                                                  var x27___;
                                                                                                                                  resultsOfLastSearch = (x1___ = results, x0___ = 0, x1___.slice_canCall___? x1___.slice(x0___): ___.callPub(x1___, 'slice', [ x0___ ]));
                                                                                                                                  var resultList = (x3___ = document, x2___ = 'results', x3___.getElementById_canCall___? x3___.getElementById(x2___): ___.callPub(x3___, 'getElementById', [ x2___ ]));
                                                                                                                                  for (var child; child = (x4___ = resultList, undefined, x4___.getFirstChild_canCall___? x4___.getFirstChild(): ___.callPub(x4___, 'getFirstChild', [ ]));) {
                                                                                                                                    x6___ = resultList, x5___ = child, x6___.removeChild_canCall___? x6___.removeChild(x5___): ___.callPub(x6___, 'removeChild', [ x5___ ]);
                                                                                                                                  }
                                                                                                                                  var n = (x7___ = results, x7___.length_canRead___? x7___.length: ___.readPub(x7___, 'length'));
                                                                                                                                  if (!n) {
                                                                                                                                    x8___ = resultList, x9___ = new (___.asCtor(StringInterpolation))([ '<center>No results</center>' ]), x8___.innerHTML_canSet___? (x8___.innerHTML = x9___): ___.setPub(x8___, 'innerHTML', x9___);
                                                                                                                                    return undefined;
                                                                                                                                  }
                                                                                                                                  for (var i = 0; i < n; ++i) {
                                                                                                                                    var result = ___.readPub(results, i);
                                                                                                                                    var li = (x11___ = document, x10___ = 'LI', x11___.createElement_canCall___? x11___.createElement(x10___): ___.callPub(x11___, 'createElement', [ x10___ ]));
                                                                                                                                    var snippetText = (x14___ = (x15___ = result, x15___.snippetHtml_canRead___? x15___.snippetHtml: ___.readPub(x15___, 'snippetHtml')), (x12___ = new ___.RegExp('<\\/?[A-Za-z][^>]*>', 'g'), x13___ = ' '), x14___.replace_canCall___? x14___.replace(x12___, x13___): ___.callPub(x14___, 'replace', [ x12___, x13___ ]));
                                                                                                                                    var titleText = (x18___ = (x19___ = result, x19___.titleHtml_canRead___? x19___.titleHtml: ___.readPub(x19___, 'titleHtml')), (x16___ = new ___.RegExp('<\\/?[A-Za-z][^>]*>', 'g'), x17___ = ' '), x18___.replace_canCall___? x18___.replace(x16___, x17___): ___.callPub(x18___, 'replace', [ x16___, x17___ ]));
                                                                                                                                    x20___ = li, x21___ = new (___.asCtor(StringInterpolation))([ '<b>', titleText, '</b> &mdash; <tt><a href=\"#\">', (x22___ = result, x22___.url_canRead___? x22___.url: ___.readPub(x22___, 'url')), '</a></tt>' ]), x20___.innerHTML_canSet___? (x20___.innerHTML = x21___): ___.setPub(x20___, 'innerHTML', x21___);
                                                                                                                                    x25___ = li, (x23___ = 'click', x24___ = ___.asSimpleFunc(___.primFreeze(___.simpleFunc(function (i) {
                                                                                                                                                                                                                              return ___.primFreeze(___.simpleFunc(function () {
                                                                                                                                                                                                                                                                     return ___.asSimpleFunc(___.primFreeze(showResult))(i);
                                                                                                                                                                                                                                                                   }));
                                                                                                                                                                                                                            })))(i)), x25___.addEventListener_canCall___? x25___.addEventListener(x23___, x24___): ___.callPub(x25___, 'addEventListener', [ x23___, x24___ ]);
                                                                                                                                    x27___ = resultList, x26___ = li, x27___.appendChild_canCall___? x27___.appendChild(x26___): ___.callPub(x27___, 'appendChild', [ x26___ ]);
                                                                                                                                  }
                                                                                                                                }))), x12___.webSearch_canCall___? x12___.webSearch(x10___, x11___): ___.callPub(x12___, 'webSearch', [ x10___, x11___ ]);
                                               });
                     showResult = ___.simpleFunc(function showResult(index) {
                                                   var result = ___.readPub(resultsOfLastSearch, index);
                                                   if (result) {
                                                     ___.asSimpleFunc(resultConsumer)(___.asSimpleFunc(___.primFreeze(clone))(result));
                                                   }
                                                   return false;
                                                 });
                     clone = ___.simpleFunc(function clone(original) {
                                              var k;
                                              var x0___;
                                              var x1___;
                                              var cloned = ___.initializeMap([ ]);
                                              {
                                                x0___ = original;
                                                for (x1___ in x0___) {
                                                  if (___.canEnumPub(x0___, x1___)) {
                                                    k = x1___;
                                                    {
                                                      ___.setPub(cloned, k, ___.readPub(original, k));
                                                    }
                                                  }
                                                }
                                              }
                                              return cloned;
                                            });
                     searchEngine;
                     resultConsumer;
                     var resultsOfLastSearch = [ ];
                     ;
                     ;
                     ;
                   }
                   IMPORTS___.htmlEmitter___.pc('\n\n').b('center').f(false).pc('\n  ').b('form').a('onsubmit', 'return false').f(false).pc('\n    ').b('input').a('type', 'text').a('size', '60').a('name', 'q').a('value', 'it was the best of times').f(true).pc('\n    ').b('input').a('type', 'button').a('value', 'Search').a('onclick', 'return plugin_dispatchEvent___(this, event || window.event, ' + ___.getId(IMPORTS___) + ', \'c_1___\')').f(true).pc('\n  ').e('form').pc('\n').e('center').pc('\n');
                 });
}