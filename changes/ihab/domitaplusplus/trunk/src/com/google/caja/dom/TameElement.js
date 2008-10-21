
function(extend, bridal, TameNode, html4, cssSealerUnsealerPair, exportFields) {
  function TameElement(node, editable) {
    assert(node.nodeType === 1);
    TameNode.call(this, node, editable);
  }
  extend(TameElement, TameNode);

  TameElement.domNames___ = ['Element', 'HTMLElement'];

  TameElement.prototype.getId = function () {
    return this.getAttribute('id') || '';
  };
  TameElement.prototype.setId = function (newId) {
    return this.setAttribute('id', newId);
  };
  TameElement.prototype.getAttribute = function (name) {
    name = String(name).toLowerCase();
    var type = html4.ATTRIBS[name];
    if (type === void 0 || !html4.ATTRIBS.hasOwnProperty(name)) {
      return null;
    }
    var value = this.node___.getAttribute(name);
    if ('string' !== typeof value) { return value; }
    switch (type) {
      case html4.atype.IDREF:
        if (!value) { return ''; }
        var n = idSuffix.length;
        var len = value.length;
        var end = len - n;
        if (end > 0 && idSuffix === value.substring(end, len)) {
          return value.substring(0, end);
        }
        return '';
      default:
        return value;
    }
  };
  TameElement.prototype.setAttribute = function (name, value) {
    if (!this.editable___) { throw new Error(); }
    name = String(name).toLowerCase();
    var type = html4.ATTRIBS[name];
    if (type === void 0 || !html4.ATTRIBS.hasOwnProperty(name)) {
      throw new Error();
    }
    var sanitizedValue = rewriteAttribute(
        this.node___.tagName, name, type, value);
    if (sanitizedValue !== null) {
      switch (name) {
        case 'style':
          if (typeof this.node___.style.cssText === 'string') {
            // Setting the 'style' attribute does not work for IE, but
            // setting cssText works on IE 6, Firefox, and IE 7.
            this.node___.style.cssText = sanitizedValue;
            return value;
          }
          break;
      }
      bridal.setAttribute(this.node___, name, sanitizedValue);
    }
    return value;
  };
  TameElement.prototype.getClassName = function () {
    return this.getAttribute('class') || '';
  };
  TameElement.prototype.setClassName = function (classes) {
    if (!this.editable___) { throw new Error(); }
    return this.setAttribute('class', String(classes));
  };
  TameElement.prototype.getTagName = TameNode.prototype.getNodeName;
  TameElement.prototype.getInnerHTML = function () {
    var tagName = this.node___.tagName.toLowerCase();
    if (!html4.ELEMENTS.hasOwnProperty(tagName)) {
      return '';  // unknown node
    }
    var flags = html4.ELEMENTS[tagName];
    var innerHtml = this.node___.innerHTML;
    if (flags & html4.eflags.CDATA) {
      innerHtml = html.escapeAttrib(innerHtml);
    } else if (flags & html4.eflags.RCDATA) {
      // Make sure we return PCDATA.
      // TODO(mikesamuel): for RCDATA we only need to escape & if they're not
      // part of an entity.
      innerHtml = html.normalizeRCData(innerHtml);
    } else {
      innerHtml = tameInnerHtml(innerHtml);
    }
    return innerHtml;
  };
  TameElement.prototype.setInnerHTML = function (htmlFragment) {
    if (!this.editable___) { throw new Error(); }
    var tagName = this.node___.tagName.toLowerCase();
    if (!html4.ELEMENTS.hasOwnProperty(tagName)) { throw new Error(); }
    var flags = html4.ELEMENTS[tagName];
    if (flags & html4.eflags.UNSAFE) { throw new Error(); }
    var sanitizedHtml;
    if (flags & html4.eflags.RCDATA) {
      sanitizedHtml = html.normalizeRCData(String(htmlFragment || ''));
    } else {
      sanitizedHtml = (htmlFragment instanceof Html
                      ? safeHtml(htmlFragment)
                      : sanitizeHtml(String(htmlFragment || '')));
    }
    this.node___.innerHTML = sanitizedHtml;
    return htmlFragment;
  };
  TameElement.prototype.setStyle = function (style) {
    this.setAttribute('style', style);
    return this.getStyle();
  };
  TameElement.prototype.getStyle = function () {
    return new TameStyle(this.node___.style, this.editable___);
  };
  TameElement.prototype.updateStyle = function (style) {
    if (!this.editable___) { throw new Error(); }
    var cssPropertiesAndValues = cssSealerUnsealerPair.unseal(style);
    if (!cssPropertiesAndValues) { throw new Error(); }

    var styleNode = this.node___.style;
    for (var i = 0; i < cssPropertiesAndValues.length; i += 2) {
      var propName = cssPropertiesAndValues[i];
      var propValue = cssPropertiesAndValues[i + 1];
      // If the propertyName differs between DOM and CSS, there will
      // be a semicolon between the two.
      // E.g., 'background-color;backgroundColor'
      // See CssTemplate.toPropertyValueList.
      var semi = propName.indexOf(';');
      if (semi >= 0) { propName = propName.substring(semi + 1); }
      styleNode[propName] = propValue;
    }
  };

  TameElement.prototype.getOffsetLeft = function () {
    return this.node___.offsetLeft;
  };
  TameElement.prototype.getOffsetTop = function () {
    return this.node___.offsetTop;
  };
  TameElement.prototype.getOffsetWidth = function () {
    return this.node___.offsetWidth;
  };
  TameElement.prototype.getOffsetHeight = function () {
    return this.node___.offsetHeight;
  };
  TameElement.prototype.toString = function () {
    return '<' + this.node___.tagName + '>';
  };
  TameElement.prototype.addEventListener = tameAddEventListener;
  TameElement.prototype.removeEventListener = tameRemoveEventListener;
  TameElement.prototype.dispatchEvent = tameDispatchEvent;
  ___.ctor(TameElement, TameNode, 'TameElement');
  ___.all2(
     ___.grantTypedGeneric, TameElement.prototype,
     ['addEventListener', 'removeEventListener', 'dispatchEvent',
      'getAttribute', 'setAttribute',
      'getClassName', 'setClassName', 'getId', 'setId',
      'getInnerHTML', 'setInnerHTML', 'updateStyle', 'getStyle', 'setStyle',
      'getTagName', 'getOffsetLeft', 'getOffsetTop', 'getOffsetWidth',
      'getOffsetHeight']);
  exportFields(TameElement,
               ['className', 'id', 'innerHTML', 'tagName', 'style',
                'offsetLeft', 'offsetTop', 'offsetWidth', 'offsetHeight']);

  return TameElement;
};