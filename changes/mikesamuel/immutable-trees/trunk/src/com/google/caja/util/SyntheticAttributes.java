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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * A set of attributes attached to a parse tree node that have been inferred by
 * the parser.
 *
 * @author mikesamuel@gmail.com
 */
public final class SyntheticAttributes
    extends AbstractMap<SyntheticAttributes.Key<?>, Object> {
  private static final Object[] NO_OBJECTS = new Object[0];
  /** Maps {@code Key.index} to values of type {@code Key.type}. */
  private Object[] values = NO_OBJECTS;

  public SyntheticAttributes() {}

  public SyntheticAttributes(SyntheticAttributes sa) {
    values = sa.values.clone();
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Key<T> k) {
    int i = k.index;
    if (i >= values.length) { return k.defaultValue; }
    Object v = values[i];
    if (v == null) { return k.defaultValue; }
    return (T) v;
  }

  @Override
  public Object get(Object k) {
    return get((Key<?>) k);
  }

 /**
   * associate the value v with the key k.
   * @param k non null.
   * @param v non null.
   * @return the old value associated with k or null if none.
   */
  @SuppressWarnings("unchecked")
  public <T> T set(Key<T> k, T v) {
    if (v == k.defaultValue) { return remove(k); }
    if (!(null == v || k.getType().isInstance(v))) {
      throw new ClassCastException(v + " to " + k.getType());
    }
    int index = k.index;
    if (values.length <= index) {
      Object[] newValues = new Object[index + 1];
      System.arraycopy(values, 0, newValues, 0, values.length);
      values = newValues;
    }
    T old = (T) values[index];
    values[index] = v;
    return old != null ? old : k.defaultValue;
  }

  @Deprecated
  @Override
  public Object put(Key<?> k, Object v) {
    return putHelper(k, v);
  }

  public void putAll(SyntheticAttributes a) {
    int len = a.values.length;
    while (len > 0 && a.values[len - 1] == null) { --len; }
    if (values.length < len) {
      Object[] newValues = new Object[len];
      System.arraycopy(values, 0, newValues, 0, values.length);
      values = newValues;
    }
    for (int i = len; --i >= 0;) {
      if (a.values[i] != null) { values[i] = a.values[i]; }
    }
  }

  private <T> Object putHelper(Key<T> k, Object v) {
    return set(k, k.type.cast(v));
  }

  /**
   * @return true iff the value associated with the given key is
   * {@link Boolean#TRUE}.
   */
  public boolean is(Key<Boolean> k) {
    return Boolean.TRUE.equals(get(k));
  }

  /**
   * @see #remove(Object)
   */
  @SuppressWarnings("unchecked")
  public <T> T remove(Key<T> k) {
    int index = k.index;
    if (index >= values.length) { return k.defaultValue; }
    T old = (T) values[index];
    values[index] = null;
    return old;
  }

  @Override
  public Object remove(Object k) {
    if (!(k instanceof Key)) { return null; }
    return remove((Key<?>) k);
  }

  @Override
  public int size() { return counter + 1; }

  @Override
  public boolean containsKey(Object k) { return k instanceof Key; }

  @Override
  public boolean containsValue(Object v) {
    throw new UnsupportedOperationException();
  }

  /**
   * Not currently implemented.
   */
  @Override
  public Set<Map.Entry<Key<?>, Object>> entrySet() {
    throw new UnsupportedOperationException();
  }


  private static int counter = -1;
  /**
   * A key into {@link SyntheticAttributes} which asserts the type of the
   * corresponding value.
   *
   * @author mikesamuel@gmail.com
   */
  public static final class Key<T> implements Comparable<Key<?>> {
    private final Class<T> type;
    private final String name;
    private final T defaultValue;
    private final int index;

    public static <T> Key<T> declare(
        Class<T> type, String name, T defaultValue) {
      return new Key<T>(type, name, defaultValue);
    }

    private Key(Class<T> type, String name, T defaultValue) {
      if (null == type || null == name) { throw new NullPointerException(); }
      this.type = type;
      this.name = name;
      this.defaultValue = defaultValue;
      synchronized (Key.class) {
        this.index = ++counter;
      }
    }

    public T getDefaultValue() { return defaultValue; }

    public String getName() {
      return this.name;
    }

    public Class<T> getType() {
      return this.type;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Key)) {
        return false;
      }
      Key<?> that = (Key<?>) obj;
      return type.equals(that.type) && this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode() ^ type.hashCode();
    }

    @Override
    public String toString() {
      String shortType = type.getName();
      shortType = shortType.substring(shortType.lastIndexOf('.') + 1);
      return name + ":" + shortType;
    }

    public int compareTo(Key<?> that) {
      int delta = this.name.compareTo(that.name);
      if (0 == delta) {
        delta = this.type.getName().compareTo(that.type.getName());
      }
      return delta;
    }
  }
}
