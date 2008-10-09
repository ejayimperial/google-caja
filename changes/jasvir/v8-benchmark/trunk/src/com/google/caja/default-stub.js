var imports = ___.copy(___.sharedImports);
imports.outers = imports;
imports.$v = valijaMaker(imports);
var htmlContainer = document.createElement("div");
htmlContainer.setAttribute("id", "-jas");
imports.htmlEmitter___ = new HtmlEmitter(htmlContainer);
imports.getCssContainer___ = function () {
  return htmlContainer;
};
attachDocumentStub(
    "-jas",
    {rewrite: function(uri, mimetype) { return uri; }},
    imports);
___.getNewModuleHandler().setImports(imports);
