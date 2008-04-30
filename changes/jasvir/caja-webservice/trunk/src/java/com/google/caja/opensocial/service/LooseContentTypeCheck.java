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

import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

/**
 * Loose content-type check to handle different but consistent content types
 * 
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class LooseContentTypeCheck extends ContentTypeCheck {
  
  final private Map<String,String> canonicalMimeType = new HashMap<String,String>();
  
  public LooseContentTypeCheck () {
    canonicalMimeType.put("application/x-javascript", "text/javascript");
    canonicalMimeType.put("text/xml", "application/xml");
  }
    
  /**
   * Checks if the {@code candidate} is consistent with {@code spec}
   * @return true {@code candidate} is consistent with {@code spec}
   */
  @Override
  public boolean check(String spec, String candidate) {
    boolean result = false;
    ContentType ct_spec;
    ContentType ct_candidate;
    try {
      ct_spec = new ContentType(spec);
      ct_candidate = new ContentType(candidate);
      result = ct_spec.match(ct_candidate) 
          || ct_spec.match(canonicalMimeType.get(ct_candidate.getBaseType()));
    } catch (ParseException e) {
      e.printStackTrace();
      result = false;
    }
    return result;
  }

}
