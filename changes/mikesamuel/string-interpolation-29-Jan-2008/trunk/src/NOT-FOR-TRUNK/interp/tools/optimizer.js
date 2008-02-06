(function () {
var REGEXP_PRECEDER_PATTERN = (function () {
    var preceders = [
        "!", "!=", "!==", "#", "%", "%=", "&", "&&", "&&=",
        "&=", "(", "*", "*=", "+", "+=", ",", "-", "-=",
        "->", /*".", "..", "...", handled below */ "/", "/=", ":", "::", ";",
        "<", "<<", "<<=", "<=", "=", "==", "===", ">",
        ">=", ">>", ">>=", ">>>", ">>>=", "?", "@", "[",
        "^", "^=", "^^", "^^=", "{", "|", "|=", "||",
        "||=", "~", "break", "case", "continue", "delete",
        "do", "else", "finally", "instanceof",
        "return", "throw", "try", "typeof"
        ];
    var pattern = '(?:' +
      '(?:(?:^|[^0-9\.])\\.{1,3})|' +  // a dot that's not part of a number
      '(?:(?:^|[^\\+])\\+)|' +  // allow + but not ++
      '(?:(?:^|[^\\-])-)'  // allow - but not --
      ;
    for (var i = 0; i < preceders.length; ++i) {
      var preceder = preceders[i];
      if (/\w/.test(preceder.charAt(0))) {
        pattern += '|\\b' + preceder;
      } else {
        pattern += '|' + preceder.replace(/([^a-z=<>:&])/g, '\\$1');
      }
    }
    pattern += '|^)\\s*$';  // matches at end, and matches empty string
    return new RegExp(pattern);
    // CAVEAT: this does not properly handle the case where a regular expression
    // immediately follows another since a regular expression may have flags
    // for case-sensitivity and the like.  Having regexp tokens adjacent is not
    // valid in any language I'm aware of, so I'm punting.
    // TODO: maybe style special characters inside a regexp as punctuation.
  })();

var TOKENS = new RegExp(
    '^(?:(?:' +
    ['\\s+',
     '[a-z$_][a-z$_0-9]*',
     '"(?:[^"\\\\]|\\\\.)*"',
     '\'(?:[^\'\\\\]|\\\\.)*\'',
     '0x[0-9a-f]+',
     '(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+\\-]?\\d+)?',
     '//.*',
     '/\\*(?:[^\\*]|\\*+[^\\*/])*\\*/',
     '[^\\w\\s\\/\\{\\}\\(\\)]+',
     '[\\{\\}\\(\\)]'
     ].join(')|(?:') +
    '))',
    'i');

function isSpaceOrComment(tok) {
  return !/[^\u0000-\u0020\u2028\u2029]/.test(tok) || /\/[\*\/]/.test(tok);
}
 
function tokenize(s) {
  var toks = [];
  var lastNonComment = '';
  while (s) {
    var m = s.match(TOKENS);
    if (!m) {
      // handle regexp literal or division op
      if (REGEXP_PRECEDER_PATTERN.test(lastNonComment)) {
        m = s.match(/^\/(?:\[(?:[^\]\\]|\\.)*\]|[^\/\\]|\\.)*\/[gmis]*/);
      } else {
        m = s.match(/^\/[^\w\s\/]*/);
      }
    }
    if (!m) {
      throw new Error(
          'Failed to parse at '
          + toJsString(s.substring(0, Math.min(s.length, 10)))
          + (s.length > 10 ? '...' : ''));
    }
    var token = m[0];
    toks.push(token);
    if (!isSpaceOrComment(token)) {
      lastNonComment = token;
    }
    s = s.substring(token.length);
  }
  return toks;
}

function assertTokensBalanced(tokens) {
  var complement = {
    '(': ')',
    '{': '}',
    '[': ']'
  };

  var stack = [];
  for (var i = 0, n = tokens.length; i < n; ++i) {
    switch (tokens[i]) {
      case '(': case '[': case '{':
        stack.push(tokens[i]);
        break;
      case '}': case ']': case ')':
        if (stack.length === 0) {
          throw new Error(
              'No open bracket for ' + tokens[i]
              + ' in `' + tokens.join('') + '`');
        } else if (tokens[i] !== complement[stack[stack.length - 1]]) {
          throw new Error(
              'Unbalanced bracket ' + tokens[i]
              + ' in `' + tokens.join('') + '`');
        }
        stack.pop();
        break;
    }
  }
  if (stack.length !== 0) {
    throw new Error(
        'Unclosed brackets ' + stack.join + ' in `' + tokens.join(''));
  }
}

function arrayReplaceSection(array, start, n, replacements) {
  var spliceArgs = replacements;
  spliceArgs.splice(0, 0, start, n);
  array.splice.apply(array, spliceArgs);
  spliceArgs.splice(0, 2);
}

function toStringInterpolationCtor(stringLiteralTokens) {
  var stringContent = eval('(' + stringLiteralTokens.join(' + ') + ')');
  var interpParts = interpSplitStringIntoParts(stringContent);

  // Build the constructor.
  var outputTokens = ['(', 'new', ' ', 'StringInterpolation', '(', '['];
  for (var i = 0, n = interpParts.length; i < n; ++i) {
    if (i) { outputTokens.push(','); }
    if (i & 1) {
      outputTokens.push('(');
      var substitutionTokens = tokenize(interpParts[i]);
      assertTokensBalanced(substitutionTokens);
      outputTokens.push.apply(outputTokens, substitutionTokens);
      outputTokens.push(')');
    } else {
      outputTokens.push(javascriptStringLiteral(interpParts[i]));
    }
  }
  outputTokens.push(']', ')', ')');
  return outputTokens;
}
 
/**
 * Given javascript source, returns equivalent javascript source.
 */
function optimizeOpenTemplate(jsSource) {
  var tokens = tokenize(jsSource);

  function nextRealToken() {
    while (++i < tokens.length) {
      var tok = tokens[i];
      if (!isSpaceOrComment(tok)) {
        return tok;
      }
    }
    return null;
  }

  function isNextRealTokenString() {
    var tok = nextRealToken();
    return tok !== null && /^[\'\"]/.test(tok);
  }
  
  for (var i = 0; i < tokens.length; ++i) {
    if (tokens[i] != 'open') { continue; }
    var openTemplateStart = i;
    if ('(' === nextRealToken()
        && 'Template' === nextRealToken()
        && '(' === nextRealToken()
        && isNextRealTokenString()) {
      var stringLiterals = [tokens[i]];
      while ('+' === nextRealToken() && isNextRealTokenString()) {
        stringLiterals.push(tokens[i]);
      }
      if (')' === tokens[i]
          && ')' === nextRealToken()) {
        // We've seen open(Template(<string> + <string> + ...)).
        var openTemplateEnd = i + 1;
        var replacementTokens = toStringInterpolationCtor(stringLiterals);
        arrayReplaceSection(
            tokens, openTemplateStart, openTemplateEnd - openTemplateStart,
            replacementTokens);
        i = openTemplateStart = replacementTokens.length - 1;
        continue;
      }

      // Next time through the loop, start scanning for 'open' at the
      // unexpected token
      --i;
    }
  }

  return tokens.join('');
}

this.optimizeOpenTemplate = optimizeOpenTemplate;

})();
