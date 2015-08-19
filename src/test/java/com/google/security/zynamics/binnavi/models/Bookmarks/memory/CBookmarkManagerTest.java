/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.models.Bookmarks.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public final class CBookmarkManagerTest {
  @Test
  public void testAddRemoveBookmark() {
    final BookmarkManager manager = new BookmarkManager();

    final MockBookmarkListener listener = new MockBookmarkListener();

    manager.addListener(listener);

    try {
      manager.addBookmark(null);
      fail();
    } catch (final NullPointerException e) {
    }

    assertEquals(0, listener.changes);
    assertEquals(0, manager.getNumberOfBookmarks());

    manager.addBookmark(new CBookmark(new CAddress(BigInteger.valueOf(123)), "A"));

    assertEquals(1, listener.changes);
    assertEquals(1, manager.getNumberOfBookmarks());

    manager.addBookmark(new CBookmark(new CAddress(BigInteger.valueOf(124)), "B"));

    assertEquals(2, listener.changes);
    assertEquals(2, manager.getNumberOfBookmarks());

    try {
      manager.addBookmark(new CBookmark(new CAddress(BigInteger.valueOf(124)), "C"));
      fail();
    } catch (final IllegalArgumentException e) {
    }

    manager.removeBookmark(manager.getBookmark(new CAddress(BigInteger.valueOf(124))));

    assertEquals(3, listener.changes);
    assertEquals(1, manager.getNumberOfBookmarks());

    try {
      manager.removeBookmark(null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      manager.removeBookmark(new CBookmark(new CAddress(BigInteger.valueOf(125)), "C"));
      fail();
    } catch (final IllegalArgumentException e) {
    }

    assertEquals(3, listener.changes);
    assertEquals(1, manager.getNumberOfBookmarks());

    assertEquals(BigInteger.valueOf(123), manager.getBookmark(0).getAddress().toBigInteger());

    manager.removeListener(listener);
  }

  @Test
  public void testGetBookmark() {
    final BookmarkManager manager = new BookmarkManager();

    try {
      manager.getBookmark(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
