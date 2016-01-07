/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

// / Used to set and manage memory bookmarks.
/**
 * Keeps track of all known bookmarks of one debugger.
 */
public final class BookmarkManager {
  // / @cond INTERNAL

  /**
   * Wrapped internal bookmark manager.
   */
  private final com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager
      m_bookmarkManager;

  /**
   * Bookmarks managed by this manager.
   */
  private final List<Bookmark> m_bookmarks = new ArrayList<Bookmark>();

  /**
   * Listeners that are notified about changes in the bookmark manager.
   */
  private final ListenerProvider<IBookmarkManagerListener> m_listeners =
      new ListenerProvider<IBookmarkManagerListener>();

  /**
   * Keeps the API bookmark manager synchronized with the internal bookmark manager.
   */
  private final InternalBookmarkManagerListener m_internalBookmarkListener =
      new InternalBookmarkManagerListener();

  /**
   * Creates a new API bookmark manager object.
   *
   * @param bookmarkManager The internal bookmark manager object to wrap.
   */
  // / @endcond
  public BookmarkManager(
      final com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager bookmarkManager) {
    m_bookmarkManager = bookmarkManager;

    for (final CBookmark bookmark : m_bookmarkManager) {
      m_bookmarks.add(new Bookmark(bookmark));
    }

    m_bookmarkManager.addListener(m_internalBookmarkListener);
  }

  // ! Creates a new bookmark.
  /**
   * Creates a new bookmark with an address and a description.
   *
   * @param address The address of the new bookmark.
   * @param description The description of the new bookmark.
   */
  public void addBookmark(final Address address, final String description) {
    Preconditions.checkNotNull(address, "Error: Bookmark addresses can't be null");
    m_bookmarkManager.addBookmark(new CBookmark(new CAddress(address.toLong()), description));
  }

  // ! Adds a bookmark manager listener.
  /**
   * Adds an object that is notified about changes in the bookmark manager.
   *
   * @param listener The listener object that is notified about changes in the bookmark manager.
   *
   */
  public void addListener(final IBookmarkManagerListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Returns the bookmark at a given address.
  /**
   * Returns the bookmark at a given address.
   *
   * @param address The address of the bookmark.
   *
   * @return The bookmark at the given address or null if there is no such address.
   */
  public Bookmark getBookmark(final Address address) {
    Preconditions.checkNotNull(address, "Error: Bookmark addresses argument can't be null");

    final CBookmark bookmark = m_bookmarkManager.getBookmark(new CAddress(address.toLong()));

    return bookmark == null ? null : ObjectFinders.getObject(bookmark, m_bookmarks);
  }

  // ! Returns a bookmark identified by an index.
  /**
   * Returns a bookmark identified by an index.
   *
   * @param index The index of the bookmark. (0 <= index < getNumberOfBookmarks());
   *
   * @return The bookmark with the given index.
   */
  public Bookmark getBookmark(final int index) {
    final CBookmark bookmark = m_bookmarkManager.getBookmark(index);

    return ObjectFinders.getObject(bookmark, m_bookmarks);
  }

  // ! Number of bookmarks.
  /**
   * Returns the number of bookmarks managed by the bookmark manager.
   *
   * @return The number of managed bookmarks.
   */
  public int getNumberOfBookmarks() {
    return m_bookmarkManager.getNumberOfBookmarks();
  }

  // ! Removes a bookmark.
  /**
   * Removes the memory bookmark at the given address.
   *
   * @param address Address of the bookmark.
   */
  public void removeBookmark(final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    final CBookmark bookmark = m_bookmarkManager.getBookmark(new CAddress(address.toLong()));
    Preconditions.checkNotNull(bookmark, "Error: No bookmark exists at the specified address");
    m_bookmarkManager.removeBookmark(bookmark);
  }

  // ! Removes a bookmark manager listener.
  /**
   * Removes a listener object from the bookmark manager.
   *
   * @param listener The listener object to remove from the bookmark manager.
   */
  public void removeListener(final IBookmarkManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the bookmark manager.
  /**
   * Returns a string representation of the bookmark manager.
   *
   * @return A string representation of the bookmark manager.
   */
  @Override
  public String toString() {
    return String.format("Bookmark Manager (Managing %d Bookmarks)", getNumberOfBookmarks());
  }

  /**
   * Keeps the API bookmark manager object synchronized with the internal bookmark manager object.
   */
  private class InternalBookmarkManagerListener implements
      com.google.security.zynamics.binnavi.models.Bookmarks.memory.IBookmarkManagerListener {
    @Override
    public void addedBookmark(
        final com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager manager,
        final CBookmark bookmark) {
      final Bookmark newBookmark = new Bookmark(bookmark);

      m_bookmarks.add(newBookmark);

      for (final IBookmarkManagerListener listener : m_listeners) {
        try {
          listener.addedBookmark(BookmarkManager.this, newBookmark);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedBookmark(
        final com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager manager,
        final CBookmark bookmark) {
      final Bookmark internalBookmark = ObjectFinders.getObject(bookmark, m_bookmarks);

      m_bookmarks.remove(internalBookmark);

      for (final IBookmarkManagerListener listener : m_listeners) {
        try {
          listener.removedBookmark(BookmarkManager.this, internalBookmark);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
