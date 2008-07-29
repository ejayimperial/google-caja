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

package com.google.caja.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An immutable list whose elements are guaranteed to be of a type matching the
 * type parameter used to construct it.
 *
 * <p>
 * Since this list is immutable, there is a mutable
 * {@link ImmutableList.Builder builder} object that can be used to construct
 * immutable lists without unnecessary array copies.
 *
 * @param <T> a lower bound on the type of content.
 *
 * @author mikesamuel@gmail.com
 */
public final class ImmutableList<T> extends AbstractList<T> {
  private final T[] content;
  private final int start;
  private final int end;

  public static <E> ImmutableList<E> emptyList(Class<E> elType) {
    return new ImmutableList<E>(makeArray(elType, 0), 0, 0);
  }

  public static <E> ImmutableList<E> fromArray(
      Class<E> elType, Object[] elements) {
    return fromArray(elType, elements, 0, elements.length);
  }

  public static <E> ImmutableList<E> fromArray(
      Class<E> elType, Object[] elements, int start, int end) {
    int len = end - start;
    E[] arr = makeArray(elType, len);
    System.arraycopy(elements, start, arr, 0, len);
    return new ImmutableList<E>(arr, 0, len);
  }

  private ImmutableList(T[] content, int start, int end) {
    this.content = content;
    this.start = start;
    this.end = end;
  }

  /** The parameter type that all values must match. */
  public Class<T> getElementType() {
    return componentTypeOfObjectArray(content);
  }

  @Override
  public boolean add(T o) { throw new UnsupportedOperationException(); }

  @Override
  public void add(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return new HashSet<T>(this).containsAll(c);
  }

  @Override
  public T get(int index) {
    int off = index + start;
    if (start > off || off >= end) {
      throw new IndexOutOfBoundsException("" + index);
    }
    return content[off];
  }

  @Override
  public int indexOf(Object o) {
    if (o == null) {
      for (int i = start; i < end; ++i) {
        if (content[i] == null) { return i - start; }
      }
    } else {
      for (int i = start; i < end; ++i) {
        if (o.equals(content[i])) { return i - start; }
      }
    }
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return content.length == 0;
  }

  @Override
  public Iterator<T> iterator() {
    return listIterator();
  }

  @Override
  public int lastIndexOf(Object o) {
    if (o == null) {
      for (int i = end; --i >= start;) {
        if (content[i] == null) { return i - start; }
      }
    } else {
      for (int i = end; --i >= start;) {
        if (o.equals(content[i])) { return i - start; }
      }
    }
    return -1;
  }

  @Override
  public ListIterator<T> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<T> listIterator(final int index) {
    return new ListIterator<T>() {
      int i = start + index;
      { if (i < start || i > end) { throw new IndexOutOfBoundsException(); } }

      public void add(T arg0) { throw new UnsupportedOperationException(); }

      public boolean hasNext() { return i < end; }

      public boolean hasPrevious() { return i > start; }

      public T next() {
        if (i == end) { throw new NoSuchElementException(); }
        return content[i++];
      }

      public int nextIndex() { return i - start; }

      public T previous() {
        if (i == start) { throw new NoSuchElementException(); }
        return content[--i];
      }

      public int previousIndex() {
        return i - start - 1;
      }

      public void remove() { throw new UnsupportedOperationException(); }

      public void set(T v) { throw new UnsupportedOperationException(); }
    };
  }

  @Override
  public boolean remove(Object o) { throw new UnsupportedOperationException(); }

  @Override
  public T remove(int index) { throw new UnsupportedOperationException(); }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T set(int index, T element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() { return end - start; }

  @Override
  public ImmutableList<T> subList(int fromIndex, int toIndex) {
    int nstart = this.start + fromIndex;
    int nend = this.start + toIndex;
    if (nstart < this.start || this.end < nend) {
      throw new IndexOutOfBoundsException();
    }
    return new ImmutableList<T>(this.content, nstart, nend);
  }

  private static final Object[] NO_OBJECTS = new Object[0];
  @Override
  public Object[] toArray() {
    return toArray(NO_OBJECTS);
  }

  @Override
  public <E> E[] toArray(E[] a) {
    int n = end - start;
    E[] result = (
        a.length >= n
        ? a
        : resize(a, n));
    System.arraycopy(content, start, result, 0, n);
    return result;
  }

  private static <E> E[] resize(E[] a, int length) {
    return makeArray(componentTypeOfObjectArray(a), length);
  }

  @SuppressWarnings("unchecked")
  private static <E> Class<E> componentTypeOfObjectArray(E[] a) {
    // We know a is a subclass of Object[] because type parameters can only
    // bind to reference types, and so the lower bound of E[] is Object[].
    // This cast is needed because Class instances for array types do not
    // override getComponentType to reflect the actual component type.
    return (Class<E>) a.getClass().getComponentType();
  }
  @SuppressWarnings("unchecked")
  private static <E> E[] makeArray(Class<E> elType, int length) {
    // The newInstqance method can produce arrays of primitives so the return
    // type is Object, not an array type.
    // But type parameters cannot be bound to primitive types, so this is safe.
    return (E[]) Array.newInstance(elType, length);
  }

  /**
   * A type-safe mutable list that can create an {@link ImmutableList}
   * without array copies.
   */
  public static final class Builder<T> extends AbstractList<T> {
    private T[] els;
    private Class<T> elType;
    private int size;
    private boolean shared;

    public static <E> Builder<E> instance(Class<E> elementType) {
      return instance(elementType, 16);
    }

    public static <E> Builder<E> instance(Class<E> elementType, int capacity) {
      return new Builder<E>(elementType, capacity);
    }

    public static <E> Builder<E> instance(
        Class<E> elementType, Collection<? extends E> els) {
      Builder<E> b = instance(elementType, els.size());
      b.addAll(els);
      return b;
    }

    private Builder(Class<T> elementType, int capacity) {
      this.els = makeArray(elementType, capacity);
      this.elType = elementType;
    }

    @Override
    public boolean add(T o) {
      add(size, o);
      return true;
    }

    @Override
    public void add(int index, T o) {
      if (o != null && !elType.isInstance(o)) {
        throw new ClassCastException();
      }
      if (index < 0 || index > size) { throw new IndexOutOfBoundsException(); }
      if (size == els.length) {
        T[] newEls = resize(els, size * 2);
        System.arraycopy(els, 0, newEls, 0, size);
        els = newEls;
        shared = false;
      } else if (shared) {
        els = els.clone();
        shared = false;
      }
      for (int i = ++size; --i > index;) {
        els[i] = els[i - 1];
      }
      els[index] = o;
    }

    @Override
    public boolean addAll(Collection<? extends T> all) {
      return addAll(size, all);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> all) {
      if (all.isEmpty()) { return false; }
      if (index < 0 || index > size) { throw new IndexOutOfBoundsException(); }
      int allSize = all.size();
      int newSize = size + allSize;
      if (newSize > els.length) {
        T[] newEls = resize(els, Math.max(size * 2, newSize));
        System.arraycopy(els, 0, newEls, 0, size);
        els = newEls;
        shared = false;
      } else if (shared) {
        els = els.clone();
        shared = false;
      }
      for (int i = size; --i >= index;) {
        els[i + allSize] = els[i];
      }
      if (all instanceof ImmutableList
          && elType.isAssignableFrom(
              ((ImmutableList<?>) all).getElementType())) {
        ImmutableList<?> other = (ImmutableList<?>) all;
        System.arraycopy(other.content, other.start, els, index, allSize);
      } else {
        size = newSize;
        Iterator<? extends T> it = all.iterator();
        for (int i = allSize; --i >= 0;) {
          els[index++] = elType.cast(it.next());
        }
        if (it.hasNext()) { throw new IllegalStateException(); }
      }
      return true;
    }

    @Override
    public void clear() {
      while (--size >= 0) { els[size] = null; }
    }

    @Override
    public T get(int i) {
      if (i < 0 || i >= size) { throw new IndexOutOfBoundsException(); }
      return els[i];
    }

    @Override
    public boolean isEmpty() {
      return size == 0;
    }

    @Override
    public T remove(int index) {
      if (index < 0 || index >= size) { throw new IndexOutOfBoundsException(); }
      if (shared) {
        els = els.clone();
        shared = false;
      }
      T old = els[index];
      --size;
      for (int i = index, last = size - 1; i < last; ++i) {
        els[i] = els[i + 1];
      }
      els[--size] = null;
      return old;
    }

    @Override
    public T set(int index, T o) {
      if (index < 0 || index >= size) { throw new IndexOutOfBoundsException(); }
      if (o != null && !elType.isInstance(o)) {
        throw new ClassCastException();
      }
      if (shared) {
        els = els.clone();
        shared = false;
      }
      T old = els[index];
      els[index] = o;
      return old;
    }

    @Override
    public int size() {
      return size;
    }

    public ImmutableList<T> toImmutableList() {
      if (size == 0) { return ImmutableList.emptyList(elType); }
      this.shared = true;
      return new ImmutableList<T>(els, 0, size);
    }
  }
}
