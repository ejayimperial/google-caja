{
  ___.loadModule(function (___, IMPORTS___) {
                   var StringInterpolation = ___.readImport(IMPORTS___, 'StringInterpolation');
                   var console = ___.readImport(IMPORTS___, 'console');
                   var document = ___.readImport(IMPORTS___, 'document');
                   var exports = ___.readImport(IMPORTS___, 'exports');
                   var searchEngine = ___.readImport(IMPORTS___, 'searchEngine');
                   var katTranzlator;
                   var x0___;
                   var x1___;
                   var renderKittenTable;
                   IMPORTS___.emitCss___([ '.', ' table {\n  border: 1px dotted #888\n}\n.', ' .snippet {\n  width: 30em\n}' ].join(IMPORTS___.getIdClass___()));
                   {
                     katTranzlator = ___.simpleFunc(function katTranzlator(text) {
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
                                                      text = (x2___ = (x5___ = (x6___ = text, undefined, x6___.toUpperCase_canCall___? x6___.toUpperCase(): ___.callPub(x6___, 'toUpperCase', [ ])), (x3___ = new ___.RegExp('\\s+', 'g'), x4___ = ' '), x5___.replace_canCall___? x5___.replace(x3___, x4___): ___.callPub(x5___, 'replace', [ x3___, x4___ ])), (x0___ = new ___.RegExp('^ +| +$', 'g'), x1___ = ''), x2___.replace_canCall___? x2___.replace(x0___, x1___): ___.callPub(x2___, 'replace', [ x0___, x1___ ]));
                                                      for (var i = 0, n = (x7___ = TRANSFORMATIONS, x7___.length_canRead___? x7___.length: ___.readPub(x7___, 'length')); i < n; i = i + 2) {
                                                        text = (x10___ = text, (x8___ = ___.readPub(TRANSFORMATIONS, i), x9___ = ___.readPub(TRANSFORMATIONS, i + 1)), x10___.replace_canCall___? x10___.replace(x8___, x9___): ___.callPub(x10___, 'replace', [ x8___, x9___ ]));
                                                      }
                                                      return text;
                                                    });
                     ;
                     var TRANSFORMATIONS = [ new ___.RegExp('\\bCAN (I|YOU|HE|SHE|IT|WE|THEY)\\b', 'g'), '$1 CAN', new ___.RegExp('\\bTO\\b', 'g'), '2', new ___.RegExp('\\bFOR\\b', 'g'), '4', new ___.RegExp('\\bYOUR\\b', 'g'), 'UR', new ___.RegExp('\\bYOU\\b', 'g'), 'U', new ___.RegExp('\\bTHIS\\b', 'g'), 'DIS', new ___.RegExp('\\bWITH\\b', 'g'), 'WIF', new ___.RegExp('\\bHAVE\\b', 'g'), 'HAZ', new ___.RegExp('\\bARE\\b', 'g'), 'IS', new ___.RegExp('\\bAM\\b', 'g'), 'IS', new ___.RegExp('\\bPLEASE\\b', 'g'), 'PLZ', new ___.RegExp('\\bTHANKS\\b', 'g'), 'THX', new ___.RegExp('\\bOH MY (GOD|GOSH)\\b', 'g'), 'OMG', new ___.RegExp('\\bATE\\b', 'g'), 'EATED', new ___.RegExp('\\bSAID\\b', 'g'), 'SED', new ___.RegExp('\\bSERIOUSLY\\b', 'g'), 'SRSLY', new ___.RegExp('\\bKNOW\\b', 'g'), 'KNOE', new ___.RegExp('\\bLOVE\\b', 'g'), 'LUV', new ___.RegExp('\\bHELP\\b', 'g'), 'HALP', new ___.RegExp('\\bMAYBE\\b', 'g'), 'MEBBE', new ___.RegExp('\\bWAS\\b', 'g'), 'WUZ', new ___.RegExp('\\bOF\\b', 'g'), 'OV', new ___.RegExp('\\bOH\\b', 'g'), 'O', new ___.RegExp('\\bREALLY\\b', 'g'), 'RLY', new ___.RegExp('\\bGREAT\\b', 'g'), 'GRAET', new ___.RegExp('\\bMY\\b', 'g'), 'MAH', new ___.RegExp('\\b(HELLO|HI)\\b', 'g'), 'HAI', new ___.RegExp('THI', 'g'), 'TI', new ___.RegExp('\\bKN', 'g'), 'N', new ___.RegExp('([^ ])SE(\\b|[^AEIOU])', 'g'), '$1ZE$2', new ___.RegExp('IES\\b', 'g'), 'EHS', new ___.RegExp('TION(S?)\\b', 'g'), 'SHUN', new ___.RegExp('LE\\b', 'g'), 'L', new ___.RegExp('IENDS\\b', 'g'), 'ENZ', new ___.RegExp('([^R])ING\\b', 'g'), '$1IN', new ___.RegExp('I([KM])E\\b', 'g'), 'IE$1', new ___.RegExp('ER( [^AEIOU]|$)', 'g'), 'AH$1', new ___.RegExp('ORE\\b', 'g'), 'OAR', new ___.RegExp('IE\\b', 'g'), 'EE', new ___.RegExp('AIR\\b', 'g'), 'EH', new ___.RegExp('AIN\\b', 'g'), 'ANE', new ___.RegExp('IEF\\b', 'g'), 'EEF', new ___.RegExp('TY\\b', 'g'), 'TI', new ___.RegExp('NESS\\b', 'g'), 'NES', new ___.RegExp('([^ AEIOU])E([RD])\\b', 'g'), '$1$2', new ___.RegExp('IC\\b', 'g'), 'IK', new ___.RegExp('VE\\b', 'g'), 'V', new ___.RegExp('FORE\\b', 'g'), 'FOA', new ___.RegExp('(O[^ AEIOU])E\\b', 'g'), '$1', new ___.RegExp('\\bPH([AEIOU])', 'g'), 'F$1', new ___.RegExp('([^AEIOU ])IR', 'g'), '$1UR', new ___.RegExp('([^AEIOU ])S\\b', 'g'), '$1Z', new ___.RegExp('([^ AEIOU]) OV\\b', 'g'), '$1A', new ___.RegExp('N\\\'T', 'g'), 'NT', new ___.RegExp('OAR', 'g'), 'OR', new ___.RegExp('IGHT', 'g'), 'ITE', new ___.RegExp('([AEIOU])S([BDFJV])', 'g'), '$1Z$2', new ___.RegExp('CEIV', 'g'), 'CEEV', new ___.RegExp('AUGHT', 'g'), 'AWT', new ___.RegExp('OO', 'g'), 'U', new ___.RegExp('U([^ AEIOU])E', 'g'), 'OO$1', new ___.RegExp('U([^ AEIOU]I)', 'g'), 'OO$1', new ___.RegExp('CIOUS', 'g'), 'SHUS', new ___.RegExp('OUCH', 'g'), 'OWCH', new ___.RegExp('ISON', 'g'), 'ISUN', new ___.RegExp('OIS', 'g'), 'OYZ', new ___.RegExp('\\bSEAR', 'g'), 'SER', new ___.RegExp('\\bSEA', 'g'), 'SEE', new ___.RegExp('\\bGOD', 'g'), 'CEILING CAT', new ___.RegExp('\\bHEAVEN', 'g'), 'CEILING', new ___.RegExp('([AEIOU])[SZ]E', 'g'), '$1Z', new ___.RegExp('\\bI AM\\b', 'g'), 'I', new ___.RegExp('\\bIZ A\\b', 'g'), 'IS', new ___.RegExp('\\bHAZ NO\\b', 'g'), 'NO HAZ', new ___.RegExp('\\bDO YOU\\b', 'g'), 'YOU', new ___.RegExp('\\bA ([A-Z]+)\\b', 'g'), '$1', new ___.RegExp('\\bI IS\\b', 'g'), 'IM' ];
                   }
                   IMPORTS___.htmlEmitter___.pc('\n');
                   {
                     renderKittenTable = ___.simpleFunc(function renderKittenTable(imageUrl, snippet) {
                                                          var x0___;
                                                          var x1___;
                                                          var x2___;
                                                          var x3___;
                                                          x0___ = (x3___ = document, x2___ = 'base', x3___.getElementById_canCall___? x3___.getElementById(x2___): ___.callPub(x3___, 'getElementById', [ x2___ ])), x1___ = new (___.asCtor(StringInterpolation))([ '<table><tr><td align=center><img src=\"', imageUrl, '\"></tr><tr><td class=snippet align=center>', ___.asSimpleFunc(___.primFreeze(katTranzlator))(snippet || ""), '</table>' ]), x0___.innerHTML_canSet___? (x0___.innerHTML = x1___): ___.setPub(x0___, 'innerHTML', x1___);
                                                        });
                     searchEngine;
                     x0___ = exports, x1___ = ___.primFreeze(___.simpleFunc(function showKitten(result) {
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
                                                                              var title = (x2___ = (x3___ = result, x3___.titleHtml_canRead___? x3___.titleHtml: ___.readPub(x3___, 'titleHtml')), (x0___ = new ___.RegExp('<\\/?\\w[^>]*>', 'g'), x1___ = ''), x2___.replace_canCall___? x2___.replace(x0___, x1___): ___.callPub(x2___, 'replace', [ x0___, x1___ ]));
                                                                              var snippet = (x6___ = (x7___ = result, x7___.snippetHtml_canRead___? x7___.snippetHtml: ___.readPub(x7___, 'snippetHtml')), (x4___ = new ___.RegExp('<\\/?\\w[^>]*>', 'g'), x5___ = ''), x6___.replace_canCall___? x6___.replace(x4___, x5___): ___.callPub(x6___, 'replace', [ x4___, x5___ ]));
                                                                              ___.asSimpleFunc(___.primFreeze(renderKittenTable))('loading.jpg', snippet);
                                                                              x10___ = searchEngine, (x8___ = '(+kitten OR +cat) ' + title, x9___ = ___.primFreeze(___.simpleFunc(function (imageResults) {
                                                                                                                                                                                    var x0___;
                                                                                                                                                                                    var x1___;
                                                                                                                                                                                    var x2___;
                                                                                                                                                                                    var x3___;
                                                                                                                                                                                    var x4___;
                                                                                                                                                                                    var n = (x0___ = imageResults, x0___.length_canRead___? x0___.length: ___.readPub(x0___, 'length'));
                                                                                                                                                                                    if (!n) {
                                                                                                                                                                                      ___.asSimpleFunc(___.primFreeze(renderKittenTable))('error.jpg', snippet);
                                                                                                                                                                                      return undefined;
                                                                                                                                                                                    }
                                                                                                                                                                                    var k = 0;
                                                                                                                                                                                    x3___ = console, x1___ = 'chose ' + k + ' from ' + (x2___ = imageResults, x2___.length_canRead___? x2___.length: ___.readPub(x2___, 'length')), x3___.log_canCall___? x3___.log(x1___): ___.callPub(x3___, 'log', [ x1___ ]);
                                                                                                                                                                                    ___.asSimpleFunc(___.primFreeze(renderKittenTable))((x4___ = ___.readPub(imageResults, k), x4___.url_canRead___? x4___.url: ___.readPub(x4___, 'url')), snippet);
                                                                                                                                                                                  }))), x10___.imageSearch_canCall___? x10___.imageSearch(x8___, x9___): ___.callPub(x10___, 'imageSearch', [ x8___, x9___ ]);
                                                                            })), x0___.showKitten_canSet___? (x0___.showKitten = x1___): ___.setPub(x0___, 'showKitten', x1___);
                     ;
                   }
                   IMPORTS___.htmlEmitter___.pc('\n\n');
                 });
}