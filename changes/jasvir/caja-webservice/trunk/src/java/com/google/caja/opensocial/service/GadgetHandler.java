package com.google.caja.opensocial.service;

import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public class GadgetHandler extends ContentHandler {

  @Override
  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
    return checker.check("text/gadget",contentType);
  }

  @Override
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) {
  }
    
}
