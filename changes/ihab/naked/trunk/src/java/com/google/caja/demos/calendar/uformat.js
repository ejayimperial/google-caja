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


/**
 * @fileoverview a schema system for various http://microformats.org/ formats.
 *
 * <p>Microformats define structured data-types composed of "properties",
 * key/value[] pairs.
 *
 * <p>
 * A Microformat instance can have multiple properties with the same name.
 * They are embedded in html by storing the field names in DOM elements'
 * class attribute.  The values are stored in a variety of field-specific ways
 * including in other attributes or in the text node, or as a nested microformat
 * instance.
 *
 * <p>This file introduces a micro-format "schema" and a standard way of
 * extracing an instance from a DOM tree given a schema, returning a list of
 * content lines, one for each property extracted.  A microformat schema
 * is represented as a mapping from the property name to a list of handler
 * function that, given a DOM node, will attempt to find the property value.
 * Handlers are tried in order until one succeeds.</p>
 *
 * @see http://microformats.org/wiki/microformats
 *
 * @author msamuel@google.com
 */


/**
 * a class that represents an ical content line.
 * In the content line <tt>RDATE;VALUE=DATE:20060101,20060102</tt>, the
 * content line has name <tt>RDATE</tt> attributes <tt>VALUE=DATE</tt> and
 * two date values.
 * @param {string} opt_name
 * @param {string} opt_value
 */
var ContentLine;

/**
 * applies a microformat schema to a DOM subtree, populating the given list
 * of content lines.
 *
 * @param {Node} node a DOM subtree.
 * @param {Object} schema a microformat schema as defined in the file overview
 * @param {Array.<ContentLine>} contentLines.
 */
var parseMicroFormat;

/**
 * groups related content lines into a single content lines.
 * e.g. group(lines, { CATEGORY: 'CATEGORIES' } takes all the values
 *   for content lines named CATEGORY and groups them under a single content
 *   line named CATEGORIES.
 * @param {Array.<ContentLine>} contentLines is modified in place as are the
 *   elements.
 */
var group;

/**
 * a null-like value used to indicate that a handler owns the value but that
 * it legitimately generates no content.
 * @type {ContentLine}
 */
var NO_CONTENT;


(function () {

function ContentLine(opt_name, opt_value) {
  /** 'RDATE' in the example above. @type {string} */
  this.name_ = null;
  /**
   * an array of key value pairs, <tt>['VALUE', 'DATE']</tt> in the example
   * above.
   * @type {Array.<string>}
   */
  this.attributes_ = [];
  /**
   * an array of values, the dates int the example above.
   * @type {Array.<string>}
   */
  this.values_ = [];
  if (opt_name !== undefined) {
    this.name_ = opt_name;
  }
  if (opt_value !== undefined) {
    this.values_.push(opt_value);
  }
  /**
   * true iff commas in the values do not separate values from others.
   * Some content lines, such as RRULEs and EXRULEs have structured values
   * composed of semicolon separated values with values that are themselves
   * comma separated lists.  These mini-syntaxes are uncommon enough, and
   * special purpose enough that we don't bother complicating this interface.
   * @type {Boolean}
   */
  this.noEscape_ = false;
}

/** outputs the content line as ICAL. */
ContentLine.prototype.toString = function () {
  var out = [this.name_];
  for (var i = 0, n = this.attributes_.length; i < n; i += 2) {
    var paramValue = this.attributes_[i + 1];
    if (/[:;]/.test(paramValue)) {
      paramValue = '"' + paramValue + '"';
    }
    out.push(';', this.attributes_[i], '=', paramValue);
  }
  out.push(':');
  for (var i = 0, n = this.values_.length; i < n; ++i) {
    if (i) { out.push(','); }
    if (!this.noEscape_) {
      out.push(this.values_[i].replace(/([;,\\])/g, '\\$1')
               .replace(/\r\n?|\n/g, '\\n'));
    } else {
      out.push(this.values_[i]);
    }
  }
  return out.join('').replace(/(.{75})(.)/g, '$1\r\n $2');
};

/**
 * gets the value of the corresponding attribute or null if none.
 * @param {string} name
 * @return {string|null}
 */
ContentLine.prototype.getAttribute_ = function (name) {
  var attributes = this.attributes_;
  for (var i = attributes.length - 2; i >= 0; i -= 2) {
    if (attributes[i] === name) { return attributes[i + 1]; }
  }
  return null;
};

var NO_CONTENT = new ContentLine('');


// Schema Processing

function parseMicroFormat(node, schema, globalProps, contentLines) {
  if (node.nodeType !== 1/*ELEMENT_NODE*/) { return; }
  if (!('#keyPattern#' in schema)) {
    var keys = [];
    for (var k in schema) {
      keys.push(k);
    }
    schema['#keyPattern#'] = classMatcher_(keys);
  }

  var matches = schema['#keyPattern#'](node);
  if (matches) {
    for (var j = 0; j < matches.length; ++j) {
      var fieldName = matches[j].toLowerCase();
      var handlers = schema[fieldName];

      for (var i = 0; i < handlers.length; ++i) {
        var handler = handlers[i];
        var contentLine = handler(node, globalProps);

        if (contentLine) {
          if (contentLine !== NO_CONTENT) {
            contentLine.name_ = fieldName.toUpperCase();
            contentLines.push(contentLine);
          }
          if (handler.noDescend_) {
            // don't look for further properties if e.g. the handler parses a
            // nested component
            return;
          }
          break;
        }
      }
    }
  }

  for (var child = node.firstChild; child; child = child.nextSibling) {
    parseMicroFormat(child, schema, globalProps, contentLines);
  }
}

function group(contentLines, groupings) {
  var groups = null;
  for (var i = 0; i < contentLines.length; ++i) {
    var cl = contentLines[i];
    var name = cl.name_;
    if (name in groupings) {
      if (!groups) { groups = {}; }
      if (groups[name]) {
        groups[name].values_.push.apply(groups[name].values_, cl.values_);
        contentLines.splice(i--, 1);
      } else {
        groups[name] = cl;
        cl.name_ = groupings[name];
      }
    }
  }
}
 
this.ContentLine = ContentLine;
this.parseMicroFormat = parseMicroFormat;
this.group = group;
this.NO_CONTENT = NO_CONTENT; 
})(); 