var caja = {
  def: function (clazz, sup, props, statics) {
    function t() {}
    sup && (t.prototype = sup.prototype, clazz.prototype = new t);
    for (var k in props) { clazz.prototype[k] = props[k];}
    for (var k in (statics || {})) { clazz[k] = statics[k]; }
  },
  extend: function() {} 
};

Function.prototype.bind = function(anObject) {
  var self = this;
  return function() {
    self.apply(anObject, arguments);
  };
}