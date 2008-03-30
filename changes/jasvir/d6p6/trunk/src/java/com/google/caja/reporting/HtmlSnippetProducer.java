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

package com.google.caja.reporting;

import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.InputSource;

import java.io.IOException;
import java.util.Map;

/**
 * Given original source code, produces HTML snippets for error messages.
 * <p>
 * For a {@link Message} message like {@code file:16+10-13: bar not defined},
 * the snippet might look like
 * <pre>
 *     file:16 var foo = <i>bar</i>() + baz
 *                       ^^^
 * </pre>
 *
 * @author jasvir@gmail.com
 */

public class HtmlSnippetProducer extends SnippetProducer {
  public HtmlSnippetProducer(
      Map<InputSource, ? extends CharSequence> originalSource, MessageContext mc) {
    super(originalSource, mc);
  }

  @Override
  protected void formatSnippet(
      FilePosition pos, CharSequence line, int start, int end, Appendable out)
      throws IOException {

    // Write out "file:14: <line-of-sourcecode>"
    StringBuilder posBuf = new StringBuilder();
    posBuf.append(line.subSequence(0, start));
    if ( start == end ) {
      posBuf.append("<i style='{background-color:red}'>");
      posBuf.append("&nbsp;");
      posBuf.append("</i>");
    } else {
      posBuf.append("<i style='{color:red}'>");
      posBuf.append(line.subSequence(start, end));
      posBuf.append("</i>");
    }
    posBuf.append(line.subSequence(end, line.length()-1));
    out.append(posBuf);
  }

  @Override
  protected void formatFilePosition(FilePosition pos, Appendable out)
      throws IOException {
    pos.source().format(mc, out);
    out.append(":");
    out.append(String.valueOf(pos.startLineNo()));
  }
}
