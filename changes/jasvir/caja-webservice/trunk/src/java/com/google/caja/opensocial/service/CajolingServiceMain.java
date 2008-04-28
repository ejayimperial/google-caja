// Copyright (C) 2008 Google Inc.
//
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

import com.google.caja.plugin.Config;

/**
 * An webservice that can be run and proxies connections, cajoling any 
 * javascript.
 *
 * @author jasvir@gmail.com (Jasvir Nagra)
 */
public class CajolingServiceMain {
  private Config config = new Config(getClass(), System.err, "Cajoling service");
  
  public static void main(String[] args) {
    CajolingServiceMain csm = new CajolingServiceMain();
    CajolingService service = new CajolingService(csm.config);
  }  
}
