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
package com.google.security.zynamics.binnavi.models.Bookmarks.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Bookmark manager class that is used to keep information about the memory bookmarks set by the
 * user.
 *
 */
public final class BookmarkManager implements Iterable<CBookmark> {
  /**
   * List of currently active bookmarks.
   */
  private final List<CBookmark> m_bookmarks = new ArrayList<CBookmark>();

  /**
   * Listeners that want to be informed about status changes in the bookmark manager.
   */
  private final ListenerProvider<IBookmarkManagerListener> m_listeners =
      new ListenerProvider<IBookmarkManagerListener>();

  /**
   * Adds a bookmark to the list of active bookmarks.
   *
   * @param bookmark The bookmark to add to the list.
   */
  public void addBookmark(final CBookmark bookmark) {
    Preconditions.checkNotNull(bookmark, "IE00382: Bookmark can not be null");
    Preconditions.checkArgument(getBookmark(bookmark.getAddress()) == null,
        "IE00383: Bookmark already exists at offset %s", bookmark.getAddress().toHexString());

    m_bookmarks.add(bookmark);

    for (final IBookmarkManagerListener listener : m_listeners) {
      try {
        listener.addedBookmark(this, bookmark);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Add a bookmark listener to the list of bookmark listeners.
   *
   * @param listener The listener to add.
   *
   * @throws IllegalArgumentException Thrown if the listener is null.
   */
  public void addListener(final IBookmarkManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the bookmark at the given address.
   *
   * @param address The address of the bookmark to search for.
   *
   * @return The bookmark at the given address or null if there is no such bookmark.
   */
  public CBookmark getBookmark(final IAddress address) {
    Preconditions.checkNotNull(address, "IE00384: Bookmark address can not be null");

    for (final CBookmark bookmark : m_bookmarks) {
      if (bookmark.getAddress().equals(address)) {
        return bookmark;
      }
    }

    return null;
  }

  /**
   * Returns the bookmark with the given index.
   *
   * @param index The index of the bookmark.
   *
   * @return The bookmark with the given index.
   */
  public CBookmark getBookmark(final int index) {
    return m_bookmarks.get(index);
  }

  /**
   * Returns the number of active bookmarks.
   *
   * @return The number of active bookmarks.
   */
  public int getNumberOfBookmarks() {
    return m_bookmarks.size();
  }

  @Override
  public Iterator<CBookmark> iterator() {
    return m_bookmarks.iterator();
  }

  /**
   * Removes the bookmark at the given index.
   *
   * @param bookmark The bookmark to remove.
   */
  public void removeBookmark(final CBookmark bookmark) {
    Preconditions.checkNotNull(bookmark, "IE00385: Bookmark can not be null");
    Preconditions.checkArgument(
        m_bookmarks.remove(bookmark), "IE00386: Bookmark is not managed by this manager");

    for (final IBookmarkManagerListener listener : m_listeners) {
      try {
        listener.removedBookmark(this, bookmark);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a bookmark manager listener.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IBookmarkManagerListener listener) {
    m_listeners.removeListener(listener);
  }
}
