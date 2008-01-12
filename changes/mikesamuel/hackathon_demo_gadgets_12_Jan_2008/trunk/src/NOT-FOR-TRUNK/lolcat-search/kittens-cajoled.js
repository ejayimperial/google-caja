{
  ___.loadModule(function (___OUTERS___) {
      ___OUTERS___.katTranzlator = ___.simpleFunc(function (text) {
          text = (function () {
              var x___ = (function () {
                  var x___ = (function () {
                      var x___ = text;
                      return x___.toUpperCase_canCall___ ? x___.toUpperCase() : ___.callPub(x___, 'toUpperCase', []);
                    })();
                  var x0___ = /\s+/g;
                  var x1___ = ' ';
                  return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
                })();
              var x0___ = /^ +| +$/g;
              var x1___ = '';
              return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
            })();
          for (var i = 0, n = (function () {
                  var x___ = ___OUTERS___.TRANSFORMATIONS;
                  return x___.length_canRead___ ? x___.length : ___.readPub(x___, 'length');
                })(); i < n; i += 2) {
            text = (function () {
                var x___ = text;
                var x0___ = ___.readPub(___OUTERS___.TRANSFORMATIONS, i);
                var x1___ = ___.readPub(___OUTERS___.TRANSFORMATIONS, i + 1);
                return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
              })();
          }
          return text;
        });
      ___OUTERS___.TRANSFORMATIONS = [/\bCAN (I|YOU|HE|SHE|IT|WE|THEY)\b/g, '$1 CAN', /\bTO\b/g, '2', /\bFOR\b/g, '4', /\bYOUR\b/g, 'UR', /\bYOU\b/g, 'U', /\bTHIS\b/g, 'DIS', /\bWITH\b/g, 'WIF', /\bHAVE\b/g, 'HAZ', /\bARE\b/g, 'IS', /\bAM\b/g, 'IS', /\bPLEASE\b/g, 'PLZ', /\bTHANKS\b/g, 'THX', /\bOH MY (GOD|GOSH)\b/g, 'OMG', /\bATE\b/g, 'EATED', /\bSAID\b/g, 'SED', /\bSERIOUSLY\b/g, 'SRSLY', /\bKNOW\b/g, 'KNOE', /\bLOVE\b/g, 'LUV', /\bHELP\b/g, 'HALP', /\bMAYBE\b/g, 'MEBBE', /\bWAS\b/g, 'WUZ', /\bOF\b/g, 'OV', /\bOH\b/g, 'O', /\bREALLY\b/g, 'RLY', /\bGREAT\b/g, 'GRAET', /\bMY\b/g, 'MAH', /\b(HELLO|HI)\b/g, 'HAI', /THI/g, 'TI', /\bKN/g, 'N', /([^ ])SE(\b|[^AEIOU])/g, '$1ZE$2', /IES\b/g, 'EHS', /TION(S?)\b/g, 'SHUN', /LE\b/g, 'L', /IENDS\b/g, 'ENZ', /([^R])ING\b/g, '$1IN', /I([KM])E\b/g, 'IE$1', /ER( [^AEIOU]|$)/g, 'AH$1', /ORE\b/g, 'OAR', /IE\b/g, 'EE', /AIR\b/g, 'EH', /IEF\b/g, 'EEF', /TY\b/g, 'TI', /NESS\b/g, 'NES', /([^ AEIOU])E([RD])\b/g, '$1$2', /IC\b/g, 'IK', /VE\b/g, 'V', /FORE\b/g, 'FOA', /(O[^ AEIOU])E\b/g, '$1', /\bPH([AEIOU])/g, 'F$1', /([^AEIOU ])IR/g, '$1UR', /([^AEIOU ])S\b/g, '$1Z', /([^ AEIOU]) OV\b/g, '$1A', /N\'T/g, 'NT', /OAR/g, 'OR', /IGHT/g, 'ITE', /([AEIOU])S([BDFJV])/g, '$1Z$2', /CEIV/g, 'CEEV', /AUGHT/g, 'AWT', /OO/g, 'U', /U([^ AEIOU])E/g, 'OO$1', /U([^ AEIOU]I)/g, 'OO$1', /CIOUS/g, 'SHUS', /OUCH/g, 'OWCH', /ISON/g, 'ISUN', /OIS/g, 'OYZ', /\bSEAR/g, 'SER', /\bSEA/g, 'SEE', /\bGOD/g, 'CEILING CAT', /\bHEAVEN/g, 'CEILING', /([AEIOU])[SZ]E/g, '$1Z', /\bI AM\b/g, 'I', /\bIZ A\b/g, 'IS', /\bHAZ NO\b/g, 'NO HAZ', /\bDO YOU\b/g, 'YOU', /\bA ([A-Z]+)\b/g, '$1', /\bI IS\b/g, 'IM'];
      ___OUTERS___.emitHtml___('\n');
      ___OUTERS___.searchEngine = undefined;
      ___OUTERS___.showKitten = ___.simpleFunc(function (result) {
          var title = (function () {
              var x___ = (function () {
                  var x___ = result;
                  return x___.titleHtml_canRead___ ? x___.titleHtml : ___.readPub(x___, 'titleHtml');
                })();
              var x0___ = /\074\/?\w[^\076]*\076/g;
              var x1___ = '';
              return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
            })();
          var snippet = (function () {
              var x___ = (function () {
                  var x___ = result;
                  return x___.snippetHtml_canRead___ ? x___.snippetHtml : ___.readPub(x___, 'snippetHtml');
                })();
              var x0___ = /\074\/?\w[^\076]*\076/g;
              var x1___ = '';
              return x___.replace_canCall___ ? x___.replace(x0___, x1___) : ___.callPub(x___, 'replace', [x0___, x1___]);
            })();
          ___.asSimpleFunc(___.primFreeze(___OUTERS___.renderKittenTable))('loading.jpg', snippet);
          (function () {
              var x___ = ___OUTERS___.searchEngine;
              var x0___ = 'cute (kitten OR cat) ' + title;
              var x1___ = ___.primFreeze(___.simpleFunc(function (imageResults) {
                    var n = (function () {
                        var x___ = imageResults;
                        return x___.length_canRead___ ? x___.length : ___.readPub(x___, 'length');
                      })();
                    if (!n) {
                      ___.asSimpleFunc(___.primFreeze(___OUTERS___.renderKittenTable))('error.jpg', snippet);
                      return undefined;
                    }
                    var k = (function () {
                        var x___ = ___OUTERS___.Math;
                        return x___.random_canCall___ ? x___.random() : ___.callPub(x___, 'random', []);
                      })() * n | 0;
                    ___.asSimpleFunc(___OUTERS___.log)('chose ' + k + ' from ' + (function () {
                          var x___ = imageResults;
                          return x___.length_canRead___ ? x___.length : ___.readPub(x___, 'length');
                        })());
                    ___.asSimpleFunc(___.primFreeze(___OUTERS___.renderKittenTable))((function () {
                          var x___ = ___.readPub(imageResults, k);
                          return x___.url_canRead___ ? x___.url : ___.readPub(x___, 'url');
                        })(), snippet);
                  }));
              return x___.imageSearch_canCall___ ? x___.imageSearch(x0___, x1___) : ___.callPub(x___, 'imageSearch', [x0___, x1___]);
            })();
        });
      ___OUTERS___.renderKittenTable = ___.simpleFunc(function (imageUrl, snippet) {
          (function () {
              var x___ = (function () {
                  var x___ = ___OUTERS___.document;
                  var x0___ = 'base';
                  return x___.getElementById_canCall___ ? x___.getElementById(x0___) : ___.callPub(x___, 'getElementById', [x0___]);
                })();
              var x0___ = new (___.asCtor(___OUTERS___.StringInterpolation))(['\074table\076\074tr\076\074td align=center\076\074img src=\"', imageUrl, '\"\076\074/tr\076\074tr\076\074td style=width:30em align=center\076', ___.asSimpleFunc(___.primFreeze(___OUTERS___.katTranzlator))(snippet || ''), '\074/table\076']);
              return x___.setInnerHTML_canCall___ ? x___.setInnerHTML(x0___) : ___.callPub(x___, 'setInnerHTML', [x0___]);
            })();
        });
      ___OUTERS___.emitHtml___('\n\n');
    });
}
