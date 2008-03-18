// Copyright (C) 2005 Google Inc.
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

package com.google.caja.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * Given any {@code Appendable}, wraps it in a {@code Writer} interface.
 *
 * @author ihab.awad@gmail.com (Ihab Awad)
 */
public class AppendableWriter extends Writer {
  private final Appendable appendable;

  public AppendableWriter(Appendable appendable) {
    this.appendable = appendable;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    appendable.append(CharBuffer.wrap(cbuf, off, len));
  }
  
  @Override
  public void flush() { }
  
  @Override
  public void close() throws IOException {
    if (appendable instanceof Closeable) {
      ((Closeable) appendable).close();
    }
  }
}
