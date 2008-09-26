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
// See the License for the specific   language governing permissions and
// limitations under the License.

package com.google.caja.cajole;

/**
 * Determines the language in which Caja content should be cajoled.
 *
 * @author ihab.awad@gmail.com
 */
public enum CajolingMode {
  /**
   * Cajita, the secure and small subset of JavaScript.
   */
  CAJITA,

  /**
   * Valija, the variant of ES.3.1 strict mode that supports idiomatic
   * JavaScript and global variables.
   */
  VALIJA,

  /**
   * Innocent code rewriting, a mode which adds checks to plain JavaScript
   * destined for container-level use, to help guard make the code less likely
   * to be used as a confused deputy.
   */
  INNOCENT;
}