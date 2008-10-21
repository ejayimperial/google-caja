function(___) {
  /**
   * Function called from rewritten event handlers to dispatch an event safely.
   */
  function plugin_dispatchEvent___(thisNode, event, pluginId, handler) {
    (typeof console !== 'undefined' && console.log) &&
    console.log(
        'Dispatch %s event thisNode=%o, event=%o, pluginId=%o, handler=%o',
        event.type, thisNode, event, pluginId, handler);
    var imports = ___.getImports(pluginId);
    switch (typeof handler) {
      case 'string':
        handler = imports[handler];
        break;
      case 'function': case 'object':
        break;
      default:
        throw new Error(
            'Expected function as event handler, not ' + typeof handler);
    }
    ___.startCallerStack && ___.startCallerStack();
    imports.isProcessingEvent___ = true;
    try {
      return ___.callPub(
          handler, 'call',
          [___.USELESS,
           imports.tameEvent___(event),
           imports.tameNode___(thisNode, true)]);
    } catch (ex) {
      if (ex && ex.cajitaStack___ && 'undefined' !== (typeof console)) {
        console.error('Event dispatch %s: %s',
            handler, ___.unsealCallerStack(ex.cajitaStack___).join('\n'));
      }
      throw ex;
    } finally {
      imports.isProcessingEvent___ = false;
    }
  }
};