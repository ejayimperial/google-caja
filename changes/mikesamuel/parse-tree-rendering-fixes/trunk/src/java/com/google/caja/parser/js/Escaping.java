// Copyright (C) 2007 Google Inc.
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

package com.google.caja.parser.js;

import com.google.caja.util.SparseBitSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Escaping of strings and regular expressions.
 *
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class Escaping {

  /**
   * Given a plain text string writes an unquoted javascript string literal.
   *
   * @param s the plain text string to escape.
   * @param asciiOnly Makes sure that only ASCII characters are written to out.
   *     This is a good idea if you don't have control over the charset that
   *     the javascript will be served with.
   * @param paranoid True to make sure that nothing is written to out that could
   *     interfere with embedding inside a script tag or CDATA section, or
   *     other tag that typically contains markup.
   *     This does not make it safe to embed in an HTML attribute without
   *     further escaping.
   * @param out written to.
   */
  public static void escapeJsString(
      CharSequence s, boolean asciiOnly, boolean paranoid, Appendable out)
      throws IOException {
    new Escaper(s, paranoid ? STRING_PARANOID_ESCAPES : STRING_MINIMAL_ESCAPES,
                asciiOnly ? NO_NON_ASCII : ALLOW_NON_ASCII, out).escape();
  }

  public static void escapeJsString(
      CharSequence s, boolean asciiOnly, boolean paranoid, StringBuilder out) {
    try {
      escapeJsString(s, asciiOnly, paranoid, (Appendable) out);
    } catch (IOException ex) {
      // StringBuilders don't throw IOException
      throw new RuntimeException(ex);
    }
  }

  /**
   * Given a plain text string, write to out unquoted regular expression text
   * that would match that substring and only that substring.
   *
   * @param s the plain text string to escape.
   * @param asciiOnly Makes sure that only ASCII characters are written to out.
   *     This is a good idea if you don't have control over the charset that
   *     the javascript will be served with.
   * @param paranoid True to make sure that nothing is written to out that could
   *     interfere with embedding inside a script tag or CDATA section, or
   *     other tag that typically contains markup.
   *     This does not make it safe to embed in an HTML attribute without
   *     further escaping.
   * @param out written to.
   */
  public static void escapeRegex(
      CharSequence s, boolean asciiOnly, boolean paranoid, Appendable out)
      throws IOException {
    new Escaper(
        s, paranoid ? REGEX_LITERAL_PARANOID_ESCAPES : REGEX_LITERAL_ESCAPES,
        asciiOnly ? NO_NON_ASCII : ALLOW_NON_ASCII, out).escape();
  }

  public static void escapeRegex(
      CharSequence s, boolean asciiOnly, boolean paranoid, StringBuilder out) {
    try {
      escapeRegex(s, asciiOnly, paranoid, (Appendable) out);
    } catch (IOException ex) {
      // StringBuilders don't throw IOException
      throw new RuntimeException(ex);
    }
  }

  /**
   * Given a regular expression pattern, write a version to out that has the
   * same meaning, but with enough characters escaped to satisfy the conditions
   * imposed by the flags passed to this method.
   *
   * @param s the plain text string to escape.
   * @param asciiOnly Makes sure that only ASCII characters are written to out.
   *     This is a good idea if you don't have control over the charset that
   *     the javascript will be served with.
   * @param paranoid True to make sure that nothing is written to out that could
   *     interfere with embedding inside a script tag or CDATA section, or
   *     other tag that typically contains markup.
   *     This does not make it safe to embed in an HTML attribute without
   *     further escaping.
   * @param out written to.
   */
  public static void normalizeRegex(
      CharSequence s, boolean asciiOnly, boolean paranoid, Appendable out)
      throws IOException {
    new Escaper(s, paranoid ? REGEX_PARANOID_ESCAPES : REGEX_MINIMAL_ESCAPES,
                asciiOnly ? NO_NON_ASCII : ALLOW_NON_ASCII, out).normalize();
  }

  public static void normalizeRegex(
      CharSequence s, boolean asciiOnly, boolean paranoid, StringBuilder out) {
    try {
      normalizeRegex(s, asciiOnly, paranoid, (Appendable) out);
    } catch (IOException ex) {
      // StringBuilders don't throw IOException
      throw new RuntimeException(ex);
    }
  }

  // Escape only the characters in string that must be escaped.
  private static final AsciiEscaper STRING_MINIMAL_ESCAPES = new AsciiEscaper(
      new AsciiEscape('\0', "\\000"),
      new AsciiEscape('\b', "\\b"),
      new AsciiEscape('\r', "\\r"),
      new AsciiEscape('\n', "\\n"),
      new AsciiEscape('\\', "\\\\"),
      new AsciiEscape('\'', "\\'"),
      new AsciiEscape('\"', "\\\"")
      );
  // Escape enough characters in a string to make sure it can be safely embedded
  // in the body of an XML and HTML document.
  private static final AsciiEscaper STRING_PARANOID_ESCAPES = new AsciiEscaper(
      new AsciiEscape('\b', "\\b"),
      new AsciiEscape('\t', "\\t"),
      new AsciiEscape('\n', "\\n"),
      new AsciiEscape('\13', "\\v"),
      new AsciiEscape('\f', "\\f"),
      new AsciiEscape('\r', "\\r"),
      new AsciiEscape('\\', "\\\\"),
      new AsciiEscape('\'', "\\'"),
      new AsciiEscape('\"', "\\\""),
      new AsciiEscape('<', "\\074"),
      new AsciiEscape('>', "\\076")
      ).plus(octalEscapes('\0', '\u001f'));
  // Escape minimal characters in a regular expression that guarantee it will
  // parse properly, without escaping regular expression specials.
  private static final AsciiEscaper REGEX_MINIMAL_ESCAPES = new AsciiEscaper(
      new AsciiEscape('\0', "\\000"),
            new AsciiEscape('\b', "\\b"),
      new AsciiEscape('\r', "\\r"),
      new AsciiEscape('\n', "\\n"),
      new AsciiEscape('/', "\\/"));
  // Escape enough characters in a string to make sure it can be safely embedded
  // in XML and HTML without changing the meaning of regular expression
  // specials.
  private static final AsciiEscaper REGEX_PARANOID_ESCAPES = new AsciiEscaper(
      new AsciiEscape('\b', "\\b"),
      new AsciiEscape('\t', "\\t"),
      new AsciiEscape('\n', "\\n"),
      new AsciiEscape('\13', "\\v"),
      new AsciiEscape('\f', "\\f"),
      new AsciiEscape('\r', "\\r"),
      new AsciiEscape('/', "\\/"),
      new AsciiEscape('<', "\\074"),
      new AsciiEscape('>', "\\076")
      ).plus(octalEscapes('\0', '\u001f'));

  // Escape all characters that have a special meaning in a regular expression
  private static final AsciiEscaper REGEX_LITERAL_ESCAPES
      = REGEX_MINIMAL_ESCAPES.plus(
            simpleEscapes("()[]{}*+?.^$|\\".toCharArray()));

  // Escape all characters that have a special meaning in a regular expression
  private static final AsciiEscaper REGEX_LITERAL_PARANOID_ESCAPES
      = REGEX_PARANOID_ESCAPES.plus(
            simpleEscapes("()[]{}*+?.^$|\\".toCharArray()));

  // TODO(mikesamuel): we can't use UnicodeSet [:Cf:] since IE 6 and other
  // older browsers disagree on what format control characters are.
  // We need to come up with a character set empirically.
  private static final SparseBitSet ALLOW_NON_ASCII = SparseBitSet.withRanges(
      new int[] { 0xad, 0xae, 0x600, 0x604, 0x70f, 0x710,
                  0x17b4, 0x17b6, 0x200c, 0x2010, 0x2028, 0x202f,
                  0x2060, 0x2070, 0xfeff, 0xff00, 0xfff9, 0xfffc,
                  0x1d173, 0x1d17b, 0xe0001, 0xe0002, 0xe0020, 0xe0080 });

  private static final SparseBitSet NO_NON_ASCII = SparseBitSet.withRanges(
      new int[] { 0x7f, Character.MAX_CODE_POINT + 1 });

  /**
   * A short lived object that encapsulates all the information needed to
   * escape a string onto an output buffer.
   */
  private static class Escaper {
    private final CharSequence chars;
    private final AsciiEscaper ascii;
    private final SparseBitSet nonAscii;
    private final Appendable out;

    Escaper(CharSequence chars, AsciiEscaper ascii, SparseBitSet nonAscii,
            Appendable out) {
      this.chars = chars;
      this.ascii = ascii;
      this.nonAscii = nonAscii;
      this.out = out;
    }

    /**
     * Treats chars as plain text, and appends to out the escaped version.
     */
    private void escape() throws IOException {
      int pos = 0;
      int end = chars.length();
      int charCount;
      for (int i = 0; i < end; i += charCount) {
        int codepoint = Character.codePointAt(chars, i);
        charCount = Character.charCount(codepoint);
        if (escapeOneCodepoint(pos, i, codepoint)) { pos = i + charCount; }
      }
      out.append(chars, pos, end);
    }

    /**
     * Like escape, but treates the input as an already escaped string and only
     * tries to ensure that characters that might or need not be escaped for
     * correctness are consistently escaped.
     */
    private void normalize() throws IOException {
      int pos = 0;
      int end = chars.length();
      int charCount;
      for (int i = 0; i < end; i += charCount) {
        int codepoint = Character.codePointAt(chars, i);
        charCount = Character.charCount(codepoint);

        if (codepoint == '\\') {
          // Already escaped.
          int escStart = i;
          i += charCount;
          if (i < end) {
            codepoint = Character.codePointAt(chars, i);
            charCount = Character.charCount(codepoint);
          }
          // Try to escape it anyway.  Since we pass escStart in as the second
          // arg instead of i, the output will not include the leading backslash
          // if it does decide to re-escape.
          if (escapeOneCodepoint(pos, escStart, codepoint)) {
            pos = i + charCount;
          }
          // Otherwise don't unescape.  Maybe it has a special significance in
          // the current context, like ? in regexps.
        } else {
          if (escapeOneCodepoint(pos, i, codepoint)) { pos = i + charCount; }
        }
      }
      out.append(chars, pos, end);
    }

    /**
     * Escapes a single unicode codepoint onto the output buffer iff it is
     * contained by either the nonAscii set or the ascii set.
     * @param pos the position past the last character in chars that has been
     *   written to out.
     * @param limit the position past the last character in chars that should
     *   be written preceding codepoint.
     * @param codepoint the codepoint to check.
     * @return true iff characters between pos and limit and codepoint itself
     *   were written to out.  false iff out was not changed by this call.
     */
    private boolean escapeOneCodepoint(int pos, int limit, int codepoint)
        throws IOException {
      if (codepoint < 0x7f) {
        int offset = codepoint - ascii.min;
        if (offset < ascii.escapes.length) {
          String esc = ascii.escapes[offset];
          if (esc != null) {
            out.append(chars, pos, limit).append(esc);
            return true;
          }
        }
      } else if (nonAscii.contains(codepoint)) {
        out.append(chars, pos, limit);
        if (!Character.isSupplementaryCodePoint(codepoint)) {
          if (codepoint < 0x100) {
            octalEscape((char) codepoint, out);
          } else {
            unicodeEscape((char) codepoint, out);
          }
        } else {
          for (char surrogate : Character.toChars(codepoint)) {
            unicodeEscape(surrogate, out);
          }
        }
        return true;
      }
      return false;
    }
  }

  static void octalEscape(char ch, Appendable out) throws IOException {
    out.append('\\').append((char) ('0' + ((ch & 0x1c0) >> 6)))
        .append((char) ('0' + ((ch & 0x38) >> 3)))
        .append((char) ('0' + (ch & 0x7)));
  }

  static void unicodeEscape(char ch, Appendable out) throws IOException {
    out.append("\\u").append("0123456789abcdef".charAt((ch >> 12) & 0xf))
        .append("0123456789abcdef".charAt((ch >> 8) & 0xf))
        .append("0123456789abcdef".charAt((ch >> 4) & 0xf))
        .append("0123456789abcdef".charAt(ch & 0xf));
  }

  /**
   * Maps ascii codepoints (lower 7b) to the escaped form.  This is a lookup
   * table that performs efficiently for latin strings.
   */
  private static class AsciiEscaper {
    private final int min;
    private final String[] escapes;

    private AsciiEscaper(AsciiEscape... asciiEscapes) {
      this(null, asciiEscapes);
    }

    private AsciiEscaper(AsciiEscaper base, AsciiEscape... asciiEscapes) {
      assert asciiEscapes.length != 0;
      Arrays.sort(asciiEscapes);
      int max;
      if (base == null) {
        this.min = asciiEscapes[0].raw;
        max = asciiEscapes[asciiEscapes.length - 1].raw;
      } else {
        this.min = Math.min(base.min, asciiEscapes[0].raw);
        max = Math.max(asciiEscapes[asciiEscapes.length - 1].raw,
                       base.min + base.escapes.length - 1);
      }
      this.escapes = new String[max - min + 1];
      if (base != null) {
        System.arraycopy(base.escapes, 0, this.escapes, base.min - this.min,
                         base.escapes.length);
      }
      for (AsciiEscape esc : asciiEscapes) {
        int idx = esc.raw - min;
        if (escapes[idx] == null) { escapes[idx] = esc.escaped; }
      }
    }

    AsciiEscaper plus(AsciiEscape... asciiEscapes) {
      return new AsciiEscaper(this, asciiEscapes);
    }
  }

  private static class AsciiEscape implements Comparable<AsciiEscape> {
    private final byte raw;
    private final String escaped;

    AsciiEscape(char ch, String escaped) {
      if ((ch & ~0x7f) != 0) { throw new IllegalArgumentException(); }
      this.raw = (byte) ch;
      this.escaped = escaped;
    }

    public int compareTo(AsciiEscape other) {
      return this.raw - other.raw;
    }
  }

  /** Produces octal escapes for all characters in the given inclusive range. */
  private static AsciiEscape[] octalEscapes(char min, char max) {
    AsciiEscape[] out = new AsciiEscape[max - min + 1];
    for (int i = 0; i < out.length; ++i) {
      StringBuilder sb = new StringBuilder(4);
      char ch = (char) (min + i);
      try {
        octalEscape(ch, sb);
      } catch (IOException ex) {
        // StringBuilders do not throw IOException
        throw new RuntimeException(ex);
      }
      out[i] = new AsciiEscape(ch, sb.toString());
    }
    return out;
  }

  /**
   * For each character, produces an escape that simply prefixes that character
   * with a backslash, so "*" => "\\*"
   */
  private static AsciiEscape[] simpleEscapes(char[] chars) {
    AsciiEscape[] out = new AsciiEscape[chars.length];
    for (int i = 0; i < out.length; ++i) {
      out[i] = new AsciiEscape(chars[i], "\\" + chars[i]);
    }
    return out;
  }

  private Escaping() { /* non instantiable */ }
}
