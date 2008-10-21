function(___) {
  return {

    /**
     * Makes the first a subclass of the second.
     */
    extend: function(subClass, baseClass) {
      var noop = function () {};
      noop.prototype = baseClass.prototype;
      subClass.prototype = new noop();
      subClass.prototype.constructor = subClass;
    },

    /**
     * Add setter and getter hooks so that the caja
     * {@code node.innerHTML = '...'} works as expected.
     *
     * @param ctor a constructor function.
     * @param fields an array of String field names. 
     */
    exportFields: function(ctor, fields) {
      for (var i = fields.length; --i >= 0;) {
        var field = fields[i];
        var fieldUCamel = field.charAt(0).toUpperCase() + field.substring(1);
        var getterName = 'get' + fieldUCamel;
        var setterName = 'set' + fieldUCamel;
        var count = 0;
        if (ctor.prototype.hasOwnProperty(getterName)) {
          ++count;
          ___.useGetHandler(
             ctor.prototype, field, ctor.prototype[getterName]);
        }
        if (ctor.prototype.hasOwnProperty(setterName)) {
          ++count;
          ___.useSetHandler(
             ctor.prototype, field, ctor.prototype[setterName]);
        }
        if (!count) {
          throw new Error('Failed to export field ' + field + ' on ' + ctor);
        }
      }
    }
  };
};