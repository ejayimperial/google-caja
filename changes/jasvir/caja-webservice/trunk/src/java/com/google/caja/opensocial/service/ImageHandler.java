// Copyright 2008 Google Inc. All Rights Reserved.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.caja.opensocial.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

/**
 * Retrieves image objects and returns them unchecked
 * to the requester
 * 
 * @author jasvir@google.com (Jasvir Nagra)
 */
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
