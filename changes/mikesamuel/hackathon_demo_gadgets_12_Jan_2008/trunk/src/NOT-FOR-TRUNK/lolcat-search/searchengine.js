function SearchEngine() {
  this.webSearch_ = new GwebSearch();
  this.imageSearch_ = new GimageSearch();
  this.imageSearch_.setRestriction(
      GSearch.RESTRICT_SAFESEARCH, GSearch.SAFESEARCH_STRICT);
}

SearchEngine.prototype.doSearch_
    = function (engine, query, processResult, callback) {
  engine.clearResults();
  engine.execute(query);
  engine.setSearchCompleteCallback(
      null,
      function () {
        var results = [];
        for (var i = 0, n = engine.results.length; i < n; ++i) {
          var result = processResult(engine.results[i]);
          result && results.push(result);
        }
        callback(results);
      });
};

SearchEngine.prototype.webSearch = function (query, callback) {
  return this.doSearch_(
      this.webSearch_,
      query,
      function (result) {
        return {
              url: result.unescapedUrl,
              titleHtml: result.title,
              snippetHtml: result.content
            };
      },
      callback);
};

SearchEngine.prototype.imageSearch = function (query, callback) {
  return this.doSearch_(
      this.imageSearch_,
      query,
      function (result) {
        var imageUrl = result.unescapedUrl;
        return imageUrl && /\.(jpg|gif|png)/.test(imageUrl)
            ? {
                url: result.unescapedUrl,
                titleHtml: result.title,
                snippetHtml: result.content
              }
            : null;
      },
      callback);
};

___.ctor(SearchEngine, undefined, 'SearchEngine');
___.all2(___.allowMethod, SearchEngine, ['webSearch', 'imageSearch']);
