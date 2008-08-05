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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import junit.framework.TestCase;

public class ImmutableListTest extends TestCase {
  public void testList() {
    ImmutableList<Integer> ints = ImmutableList.builder(
        Integer.class, Arrays.asList(0, 1, 2, 3, 4)).toImmutableList();
    assertEquals(5, ints.size());
    assertEquals(0, ints.get(0).intValue());
    assertEquals(1, ints.get(1).intValue());
    assertEquals(2, ints.get(2).intValue());
    assertEquals(3, ints.get(3).intValue());
    assertEquals(4, ints.get(4).intValue());
    assertFalse(ints.isEmpty());
    assertEquals("[0, 1, 2, 3, 4]", ints.toString());
    List<Integer> al = new ArrayList<Integer>(ints);
    assertEquals(ints.hashCode(), al.hashCode());
    assertEquals(ints, al);
    try {
      ints.get(-1);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // pass
    }
    try {
      ints.get(5);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // pass
    }
    assertEquals(Integer.class, ints.getElementType());
    assertEquals(Integer.class, ((ImmutableList<?>) ints).getElementType());
  }

  public void testSubList() {
    ImmutableList<Integer> ints = ImmutableList.builder(
        Integer.class, Arrays.asList(0, 1, 2, 3, 4)).toImmutableList();
    ImmutableList<Integer> someInts = ints.subList(1, 3);
    assertEquals(2, someInts.size());
    assertEquals(1, someInts.get(0).intValue());
    assertEquals(2, someInts.get(1).intValue());
    assertEquals(-1, someInts.indexOf(0));
    assertEquals(1, someInts.indexOf(2));
    assertEquals(-1, someInts.indexOf(3));
    assertEquals(-1, someInts.lastIndexOf(0));
    assertEquals(1, someInts.lastIndexOf(2));
    assertEquals(-1, someInts.lastIndexOf(3));
    assertFalse(someInts.isEmpty());
    assertEquals("[1, 2]", someInts.toString());
    List<Integer> al = new ArrayList<Integer>(someInts);
    assertEquals(someInts.hashCode(), al.hashCode());
    assertEquals(someInts, al);
    try {
      someInts.get(-1);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // pass
    }
    try {
      someInts.get(2);
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // pass
    }
    assertEquals(Integer.class, someInts.getElementType());
    assertEquals(Integer.class, ((ImmutableList<?>) someInts).getElementType());
  }

  public void testListIterator() throws Exception {
    ImmutableList<Integer> il = ImmutableList.builder(
        Integer.class, Arrays.asList(0, 1, 2, 3)).toImmutableList()
        .subList(1, 3);
    ArrayList<Integer> al = new ArrayList<Integer>();
    al.add(1);
    al.add(2);

    ListIterator<Integer> li1 = il.listIterator(1);
    ListIterator<Integer> li2 = al.listIterator(1);

    assertTrue(li2.hasNext());
    assertTrue(li1.hasNext());
    assertEquals(li1.nextIndex(), li2.nextIndex());
    assertTrue(li2.hasPrevious());
    assertTrue(li1.hasPrevious());
    assertEquals(li1.previousIndex(), li2.previousIndex());

    assertEquals(li1.next(), li2.next());
    assertFalse(li2.hasNext());
    assertFalse(li1.hasNext());
    assertEquals(li1.nextIndex(), li2.nextIndex());
    assertTrue(li2.hasPrevious());
    assertTrue(li1.hasPrevious());
    assertEquals(li1.previousIndex(), li2.previousIndex());

    try {
      li1.next();
      fail();
    } catch (NoSuchElementException ex) {
      // pass
    }

    assertEquals(li1.previous(), li2.previous());
    assertTrue(li2.hasNext());
    assertTrue(li1.hasNext());
    assertEquals(li1.nextIndex(), li2.nextIndex());
    assertTrue(li2.hasPrevious());
    assertTrue(li1.hasPrevious());
    assertEquals(li1.previousIndex(), li2.previousIndex());

    assertEquals(li1.previous(), li2.previous());
    assertTrue(li2.hasNext());
    assertTrue(li1.hasNext());
    assertEquals(li1.nextIndex(), li2.nextIndex());
    assertFalse(li2.hasPrevious());
    assertFalse(li1.hasPrevious());
    assertEquals(li1.previousIndex(), li2.previousIndex());

    try {
      li1.previous();
      fail();
    } catch (NoSuchElementException ex) {
      // pass
    }

    assertEquals(li1.next(), li2.next());
    assertTrue(li2.hasNext());
    assertTrue(li1.hasNext());
    assertEquals(li1.nextIndex(), li2.nextIndex());
    assertTrue(li2.hasPrevious());
    assertTrue(li1.hasPrevious());
    assertEquals(li1.previousIndex(), li2.previousIndex());
  }

  public void testTypeSafety() {
    ImmutableList.Builder<String> b = ImmutableList.builder(String.class);
    assertTrue(b.isEmpty());
    b.add("foo");
    assertFalse(b.isEmpty());
    assertEquals(1, b.size());
    ImmutableList<String> l1 = b.toImmutableList();
    b.add("bar");
    assertEquals(2, b.size());
    ImmutableList<String> l2 = b.toImmutableList();

    assertEquals("[foo]", l1.toString());
    assertEquals("[foo, bar]", l2.toString());
    assertEquals(l2, b);
  }

  public void testImmutable() {
    ImmutableList<String> il = ImmutableList.builder(
       String.class, Arrays.asList("foo", "bar")).toImmutableList();
    try {
      il.clear();
      fail();
    } catch (UnsupportedOperationException ex) {
      // pass
    }
    assertEquals("[foo, bar]", il.toString());
    try {
      il.remove(0);
      fail();
    } catch (UnsupportedOperationException ex) {
      // pass
    }
    assertEquals("[foo, bar]", il.toString());
    try {
      il.remove("foo");
      fail();
    } catch (UnsupportedOperationException ex) {
      // pass
    }
    assertEquals("[foo, bar]", il.toString());
    try {
      il.add("baz");
      fail();
    } catch (UnsupportedOperationException ex) {
      // pass
    }
    assertEquals("[foo, bar]", il.toString());
    try {
      Iterator<String> it = il.iterator();
      it.next();
      it.remove();
      fail();
    } catch (UnsupportedOperationException ex) {
      // pass
    }
    assertEquals("[foo, bar]", il.toString());
  }
}
