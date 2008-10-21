/**
 * Create a base class for a DOM node wrapper.
 *
 * @param ___ the global wonderbar object.
 * @param cajita the global Cajita object.
 * @param exportFields ....
 * @return a constructor for tame nodes.
 *
 * The returned constructor has the signature:
 *
 * @param node a native DOM node.
 * @param editable whether editing the attributes of this node should be
 * permitted.
 * @return a DOM wrapper instance.
 */
function (___, cajita, exportFields) {

  var tameNodeTrademark = {};

  function TameNode(node, editable) {
    this.node___ = node;
    this.editable___ = editable;
    ___.stamp(tameNodeTrademark, this, true);
  }
  TameNode.domNames___ = ['Node'];

  /**
   * @return the node type per W3C specifications.
   * @see cajita.foo
   */
  TameNode.prototype.getNodeType = function () {
    return this.node___.nodeType;
  };
  
  TameNode.prototype.getNodeName = function () {
    return this.node___.nodeName;
  };
  TameNode.prototype.getNodeValue = function () {
    return this.node___.nodeValue;
  };
  TameNode.prototype.appendChild = function (child) {
    // Child must be editable since appendChild can remove it from its parent.
    cajita.guard(tameNodeTrademark, child);
    if (!this.editable___ || !child.editable___) {
      throw new Error();
    }
    this.node___.appendChild(child.node___);
  };
  TameNode.prototype.insertBefore = function (toInsert, child) {
    cajita.guard(tameNodeTrademark, toInsert);
    if (child === void 0) { child = null; }
    if (child !== null) { cajita.guard(tameNodeTrademark, child); }
    if (!this.editable___ || !toInsert.editable___) {
      throw new Error();
    }
    this.node___.insertBefore(
        toInsert.node___, child !== null ? child.node___ : null);
  };
  TameNode.prototype.removeChild = function (child) {
    cajita.guard(tameNodeTrademark, child);
    if (!this.editable___ || !child.editable___) {
      throw new Error();
    }
    this.node___.removeChild(child.node___);
  };
  TameNode.prototype.replaceChild = function (child, replacement) {
    cajita.guard(tameNodeTrademark, child);
    cajita.guard(tameNodeTrademark, replacement);
    if (!this.editable___ || !replacement.editable___) {
      throw new Error();
    }
    this.node___.replaceChild(child.node___, replacement.node___);
  };
  TameNode.prototype.getFirstChild = function () {
    return tameNode(this.node___.firstChild, this.editable___);
  };
  TameNode.prototype.getLastChild = function () {
    return tameNode(this.node___.lastChild, this.editable___);
  };
  TameNode.prototype.getNextSibling = function () {
    // TODO(mikesamuel): replace with cursors so that subtrees are delegable
    return tameNode(this.node___.nextSibling, this.editable___);
  };
  TameNode.prototype.getPreviousSibling = function () {
    // TODO(mikesamuel): replace with cursors so that subtrees are delegable
    return tameNode(this.node___.previousSibling, this.editable___);
  };
  TameNode.prototype.getParentNode = function () {
    var parent = this.node___.parentNode;
    for (var ancestor = parent; ancestor; ancestor = ancestor.parentNode) {
      // TODO(mikesamuel): replace with cursors so that subtrees are delegable
      if (idClass === ancestor.className) {  // TODO: handle multiple classes.
        return tameNode(parent, this.editable___);
      }
    }
    return null;
  };
  TameNode.prototype.getElementsByTagName = function (tagName) {
    return tameNodeList(
        this.node___.getElementsByTagName(String(tagName)), this.editable___);
  };
  TameNode.prototype.getChildNodes = function() {
    return tameNodeList(this.node___.childNodes);
  };
  ___.ctor(TameNode, void 0, 'TameNode');
  var tameNodeMembers = [
      'getNodeType', 'getNodeValue', 'getNodeName',
      'appendChild', 'insertBefore', 'removeChild', 'replaceChild',
      'getFirstChild', 'getLastChild', 'getNextSibling', 'getPreviousSibling',
      'getElementsByTagName'];
  var tameNodeFields = [
      'nodeType', 'nodeValue', 'nodeName', 'firstChild',
      'lastChild', 'nextSibling', 'previousSibling', 'parentNode',
      'childNodes'];
  ___.all2(___.grantTypedGeneric, TameNode.prototype, tameNodeMembers);
  exportFields(TameNode, tameNodeFields);

  return TameNode;
};