function safeHtml(s) {
  var buf = [];
  if (s instanceof StringInterpolation) {
    var state =  s.interpolate(
        htmlScanner_, htmlEscaper_, html.State.PCDATA, buf);
    if (!isValidEndState(state)) {
      throw new Error('Interpolation ' + s + ' ended in invalid state');
    }
  } else {
    htmlEscaper_(s, html.State.PCDATA, buf);
  }
  return buf.join('');
}

var htmlScanner_ = (function () {
  window.cache = {};
  var size = 0;

  return function (s, state, buf) {
    if (isRecording(state)) { return process(s, state, buf); }
    var cacheKey = s + ':' + state;
    var endState = window.cache[cacheKey];
    if (endState !== undefined) { return endState; }
    endState = process(s, state, buf);
    if (!isRecording(endState)) {
      if (++size === 100) { window.cache = {}; }
      window.cache[cacheKey] = endState;
    }
    return endState;
  };
})();

var HTML_ENTITIES_ = {
  amp: '&',
  quot: '"',
  lt: '<',
  gt: '>',
  apos: '\'',
  nbsp: '\xA0'
};
function decodeHtmlEntity(entity) {
  if (entity.charAt(0) === '#') {
    if (entity.length > 1) {
      var ch1 = entity.charAt(1);
      var codepoint;
      if (ch1 === 'x' || ch1 === 'X') {
        codepoint = parseInt(entity.substring(2), 16);
      } else {
        codepoint = parseInt(entity.substring(1), 10);
      }
      return isNaN(codepoint) ? null : String.fromCharCode(codepoint);
    }
  } else {
    return HTML_ENTITIES_[entity.toLowerCase()];
  }
}

function htmlEscaper_(substitution, currentState, out) {
  var stateId = getStateId(currentState);
  switch (stateId) {
    case html.State.PCDATA:
    case html.State.RCDATA:
    case html.State.DQ_ATTRIB_VALUE:
    case html.State.SQ_ATTRIB_VALUE:
      out.push(htmlEscape_(substitution.toString()));
      break;
    case html.State.SP_ATTRIB_VALUE:
      out.push(htmlSpEscape_(substitution.toString()));
      break;
    case html.State.CDATA:
      // TODO: check for apparent end tag
      out.push(substitution.toString());
      break;
    case html.State.DQ_CSS:
      esc_('\'', substitution, out, cssEscape_, htmlEscape_);
      break;
    case html.State.SQ_CSS:
      esc_('"', substitution, out, cssEscape_, htmlEscape_);
      break;
    case html.State.SP_CSS:
      esc_('&#43;', substitution, out, cssEscape_, htmlSpEscape_);
      break;
    case html.State.CDATA_CSS:
      esc_('"', substitution, out, cssEscape_);
      break;
    case html.State.CDATA_CSS_DQ:
    case html.State.CDATA_CSS_SQ:
      esc_('', substitution, out, cssEscape_);
      break;
    case html.State.DQ_CSS_DQ:
    case html.State.DQ_CSS_SQ:
    case html.State.SQ_CSS_DQ:
    case html.State.SQ_CSS_SQ:
      esc_('', substitution, out, cssEscape_, htmlEscape_);
      break;
    case html.State.CDATA_JS:
    case html.State.DQ_JS:
    case html.State.SQ_JS:
    case html.State.SP_JS:
      switch (typeof substitution) {
        case 'object':
          if (null !== substitution) { break; }
          // fall through
        case 'number':
        case 'boolean':
        case 'undefined':
          out.push('' + substitution);
          return currentState;
      }
      switch (stateId) {
        case html.State.CDATA_JS:
          esc_('\'', substitution, out, jsEscape_);
          break;
        case html.State.DQ_JS:
          esc_('\'', substitution, out, jsEscape_, htmlEscape_);
          break;
        case html.State.SQ_JS:
          esc_('"', substitution, out, jsEscape_, htmlEscape_);
          break;
        default:
          esc_('&#39;', substitution, out, jsEscape_, htmlSpEscape_);
          break;
      }
      break;
    case html.State.CDATA_JS_DQ_STRING:
    case html.State.CDATA_JS_SQ_STRING:
      esc_('', substitution, out, jsEscape_);
      break;
    case html.State.DQ_JS_DQ_STRING:
    case html.State.DQ_JS_SQ_STRING:
    case html.State.SQ_JS_DQ_STRING:
    case html.State.SQ_JS_SQ_STRING:
      esc_('', substitution, out, jsEscape_, htmlEscape_);
      break;
    case html.State.SP_CSS_DQ:
    case html.State.SP_CSS_SQ:
      esc_('', substitution, out, cssEscape_, htmlSpEscape_);
      break;
    case html.State.SP_JS_DQ_STRING:
    case html.State.SP_JS_SQ_STRING:
      esc_('', substitution, out, jsEscape_, htmlSpEscape_);
      break;
    case html.State.TAG_AFTER_EQ:
      out.push('"');
      switch (variableAttribNameValue(currentState)) {
        case html.variable.AttribName.onblur:
          currentState = withStateId(currentState, html.State.DQ_JS);
          break;
        case html.variable.AttribName.style:
          currentState = withStateId(currentState, html.State.DQ_CSS);
          break;
        default:
          currentState = withStateId(currentState, html.State.DQ_ATTRIB_VALUE);
          break;
      }
      htmlEscaper_(substitution, currentState, out);
      out.push('"');
      currentState = html.State.TAG_BODY;
      break;
    case html.State.TAG_BODY:
      // Should start recording attrib name somehow
      currentState = withStateId(currentState, html.State.ATTRIB_NAME);
      // fall through
    case html.State.ATTRIB_NAME:
    case html.State.TAG_NAME:
      substitution = substitution.toString();
      if (/[^a-z0-9]i/.test(substitution)) {
        throw new Error('Bad HTML identifier `' + substitution + '`');
      }
      out.push(substitution);
      break;
    default:
      throw new Error('Bad state ' + currentState);
      break;
  }
  return currentState;
}

var allHtmlRe_ = /[&<>\"\']/;
var allJsRe_ = /[<\"\'\\\r\n\u2028\u2029\/]/;
var allCssRe_ = /[<\"\'\\\r\n]/;
var allHtmlSpRe_ = /[&<>\"\' \t\r\n\/]/;

var ampRe_ = /&/g;
var ltRe_ = /</g;
var gtRe_ = />/g;
var quotRe_ = /\"/g;
var aposRe_ = /\'/g;
var bslashRe_ = /\\/g;
var slashRe_ = /\//g;
var crRe_ = /\r/g;
var lfRe_ = /\n/g;
var u2028Re_ = /\u2028/g;
var u2029Re_ = /\u2029/g;
var spRe_ = / /g;
var tabRe_ = /\t/g;

function htmlEscape_(str) {
  if (!allHtmlRe_.test(str)) { return str; }
  if (str.indexOf('&') !== -1) { str = str.replace(ampRe_, '&amp;'); }
  if (str.indexOf('<') !== -1) { str = str.replace(ltRe_, '&lt;'); }
  if (str.indexOf('>') !== -1) { str = str.replace(gtRe_, '&gt;'); }
  if (str.indexOf('"') !== -1) { str = str.replace(quotRe_, '&#34;'); }
  if (str.indexOf("'") !== -1) { str = str.replace(aposRe_, '&#39;'); }
  return str;
}

function cssEscape_(str) {
  if (!allCssRe_.test(str)) { return str; }
  if (str.indexOf("\\") !== -1) { str = str.replace(bslashRe_, '\\5C '); }
  if (str.indexOf('<') !== -1) { str = str.replace(ltRe_, '\\34 '); }
  if (str.indexOf('"') !== -1) { str = str.replace(quotRe_, '\\22 '); }
  if (str.indexOf("'") !== -1) { str = str.replace(aposRe_, '\\26 '); }
  if (str.indexOf("\r") !== -1) { str = str.replace(crRe_, '\\D '); }
  if (str.indexOf("\n") !== -1) { str = str.replace(lfRe_, '\\A '); }
  return str;
}

function jsEscape_(str) {
  // TODO: escape format control characters
  if (!allJsRe_.test(str)) { return str; }
  if (str.indexOf("\\") !== -1) { str = str.replace(bslashRe_, '\\134'); }
  if (str.indexOf("\u2028") !== -1) {
    str = str.replace(u2028Re_, '\\u2028');
  }
  if (str.indexOf("\u2029") !== -1) {
    str = str.replace(u2029Re_, '\\u2029');
  }
  if (str.indexOf("\r") !== -1) { str = str.replace(crRe_, '\\r'); }
  if (str.indexOf("\n") !== -1) { str = str.replace(lfRe_, '\\n'); }
  if (str.indexOf('<') !== -1) { str = str.replace(ltRe_, '\\074'); }
  if (str.indexOf('"') !== -1) { str = str.replace(quotRe_, '\\042'); }
  if (str.indexOf("'") !== -1) { str = str.replace(aposRe_, '\\047'); }
  if (str.indexOf("/") !== -1) { str = str.replace(slashRe_, '\\057'); }
  return str;
}

/** Escape characters that end a space delimited attribute. */
function htmlSpEscape_(str) {
  if (!allHtmlSpRe_.test(str)) { return str; }
  if (str.indexOf('&') !== -1) { str = str.replace(ampRe_, '&amp;'); }
  if (str.indexOf('<') !== -1) { str = str.replace(ltRe_, '&lt;'); }
  if (str.indexOf('>') !== -1) { str = str.replace(gtRe_, '&gt;'); }
  if (str.indexOf('"') !== -1) { str = str.replace(quotRe_, '&#34;'); }
  if (str.indexOf("'") !== -1) { str = str.replace(aposRe_, '&#39;'); }
  if (str.indexOf(' ') !== -1) { str = str.replace(spRe_, '&#32;'); }
  if (str.indexOf('\t') !== -1) { str = str.replace(tabRe_, '&#9;'); }
  if (str.indexOf('\r') !== -1) { str = str.replace(crRe_, '&#14;'); }
  if (str.indexOf('\n') !== -1) { str = str.replace(lfRe_, '&#10;'); }
  if (str.indexOf('/') !== -1) { str = str.replace(slashRe_, '&#47;'); }
  return str;
}

function esc_(delimiter, str, out, var_args) {
  for (var i = 3, n = arguments.length; i < n; ++i) {
    var esc = arguments[i];
    str = esc(str);
  }
  if (delimiter) {
    out.push(delimiter, str, delimiter);
  } else {
    out.push(str);
  }
}
