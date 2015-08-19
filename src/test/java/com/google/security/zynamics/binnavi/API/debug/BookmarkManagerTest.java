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
package com.google.security.zynamics.binnavi.API.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BookmarkManagerTest {
  private final BookmarkManager m_nativeManager = new BookmarkManager();

  private final com.google.security.zynamics.binnavi.API.debug.BookmarkManager m_apiManager =
      new com.google.security.zynamics.binnavi.API.debug.BookmarkManager(m_nativeManager);

  private final MockBookmarkManagerListener m_mockListener = new MockBookmarkManagerListener();

  @Test
  public void testAddBookmark() {
    m_apiManager.addListener(m_mockListener);

    try {
      m_apiManager.addBookmark(null, "Fark");

      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_apiManager.addBookmark(new Address(0x123), null);

      fail();
    } catch (final NullPointerException exception) {
    }

    m_apiManager.addBookmark(new Address(0x123), "Fark");

    try {
      m_apiManager.addBookmark(new Address(0x123), "Fark");

      fail();
    } catch (final IllegalArgumentException exception) {
    }

    assertEquals(1, m_nativeManager.getNumberOfBookmarks());
    assertEquals(0x123, m_nativeManager.getBookmark(0).getAddress().toLong());
    assertEquals("Fark", m_nativeManager.getBookmark(0).getDescription());

    assertEquals(1, m_apiManager.getNumberOfBookmarks());
    assertEquals(0x123, m_apiManager.getBookmark(0).getAddress().toLong());
    assertEquals("Fark", m_apiManager.getBookmark(0).getDescription());

    assertEquals("addedBookmark/123;", m_mockListener.events);

    m_apiManager.removeListener(m_mockListener);
  }

  @Test
  public void testGetBookmark() {
    try {
      assertEquals(0x123, m_apiManager.getBookmark(null).getAddress().toLong());
    } catch (final NullPointerException exception) {
    }

    assertNull(m_apiManager.getBookmark(new Address(0x123)));

    m_apiManager.addBookmark(new Address(0x123), "Fark");

    assertEquals(1, m_apiManager.getNumberOfBookmarks());
    assertEquals(0x123, m_apiManager.getBookmark(new Address(0x123)).getAddress().toLong());
    assertEquals("Fark", m_apiManager.getBookmark(new Address(0x123)).getDescription());
  }

  @Test
  public void testPreinitialized() {
    final BookmarkManager nativeManager = new BookmarkManager();
    nativeManager.addBookmark(new CBookmark(new CAddress(0), "foo"));

    final com.google.security.zynamics.binnavi.API.debug.BookmarkManager apiManager =
        new com.google.security.zynamics.binnavi.API.debug.BookmarkManager(nativeManager);
    final Bookmark bm = apiManager.getBookmark(0);

    assertEquals(0, bm.getAddress().toLong());
    assertEquals("foo", bm.getDescription());
  }

  @Test
  public void testRemoveBookmark() {
    try {
      m_apiManager.removeBookmark(null);

      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_apiManager.removeBookmark(new Address(0x123));

      fail();
    } catch (final NullPointerException exception) {
    }

    m_apiManager.addBookmark(new Address(0x123), "Fark");

    m_apiManager.addListener(m_mockListener);

    m_apiManager.removeBookmark(new Address(0x123));

    assertEquals(0, m_nativeManager.getNumberOfBookmarks());

    assertEquals(0, m_apiManager.getNumberOfBookmarks());

    assertEquals("removedBookmark/123;", m_mockListener.events);

    m_apiManager.removeListener(m_mockListener);
  }

  @Test
  public void testToString() {
    m_apiManager.addBookmark(new Address(0x1233), "Fark");
    m_apiManager.addBookmark(new Address(0x1234), "Fark");
    m_apiManager.addBookmark(new Address(0x1235), "Fark");

    assertEquals("Bookmark Manager (Managing 3 Bookmarks)", m_apiManager.toString());
  }
}
