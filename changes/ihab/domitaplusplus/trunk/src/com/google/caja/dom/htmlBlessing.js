function (html) {
  // Define a wrapper type for known safe HTML, and a trademarker.
  // This does not actually use the trademarking functions since trademarks
  // cannot be applied to strings.
  function Html(htmlFragment) { this.html___ = String(htmlFragment || ''); }
  Html.prototype.valueOf = Html.prototype.toString
      = function () { return this.html___; };
  
  return {

    /**
     * returns 
     * @param htmlFragment
     */
    safeHtml: function(htmlFragment) {
      return (htmlFragment instanceof Html)
          ? htmlFragment.html___
          : html.escapeAttrib(String(htmlFragment || ''));
    },
    blessHtml: function(htmlFragment) {
      return (htmlFragment instanceof Html)
          ? htmlFragment
          : new Html(htmlFragment);
    }
  };
};