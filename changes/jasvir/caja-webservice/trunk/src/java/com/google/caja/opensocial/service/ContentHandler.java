// Copyright 2007 Google Inc. All Rights Reserved.
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

import java.io.Reader;
import java.io.Writer;
import java.net.URI;

/**
 * 
 * @author jasvir@google.com (Your Name Here)
 *
 */
public abstract class ContentHandler {
  
  public abstract boolean canHandle(URI uri, String contentType, ContentTypeCheck checker);
  public abstract void apply(URI uri, String contentType, Reader stream, Writer response);
    
}
