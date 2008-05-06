package com.google.caja.opensocial.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public class ImageHandler implements ContentHandler {

  public boolean canHandle(URI uri, String contentType, ContentTypeCheck checker) {
    return checker.check("image/*", contentType);    
  }
  
  public void apply(URI uri, String contentType, Reader stream,
      Writer response) throws UnsupportedContentTypeException {
    try {
      int next;
      while ((next = stream.read()) != -1) {
        response.write(next);
      } 
    } catch (IOException e) {
      throw new UnsupportedContentTypeException();
    }
  }
}
