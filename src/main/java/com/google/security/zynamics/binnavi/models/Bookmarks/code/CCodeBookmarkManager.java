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
package com.google.security.zynamics.binnavi.models.Bookmarks.code;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Manages code bookmarks.
 */
public final class CCodeBookmarkManager implements Iterable<CCodeBookmark> {
  /**
   * List of managed code bookmarks.
   */
  private final List<CCodeBookmark> m_bookmarks = new ArrayList<CCodeBookmark>();

  /**
   * Listener objects that are notified about changes in the code bookmark manager.
   */
  private final ListenerProvider<ICodeBookmarkManagerListener> m_listeners =
      new ListenerProvider<ICodeBookmarkManagerListener>();

  /**
   * Adds a new code bookmark to the code bookmark manager.
   * 
   * @param bookmark The code bookmark to add.
   */
  public void addBookmark(final CCodeBookmark bookmark) {
    Preconditions.checkNotNull(bookmark, "IE00328: Bookmark argument can not be null");
    Preconditions.checkArgument(!m_bookmarks.contains(bookmark),
        "IE00329: Bookmark can not be added more than once");

    m_bookmarks.add(bookmark);

    for (final ICodeBookmarkManagerListener listener : m_listeners) {
      try {
        listener.addedBookmark(this, bookmark);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a new listener object that is notified about changes in the manager.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final ICodeBookmarkManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns a single code bookmark identified through its index.
   * 
   * @param index The index of the code bookmark (0 <= index < size())
   * 
   * @return The code bookmark identified by the index.
   */
  public CCodeBookmark get(final int index) {
    return m_bookmarks.get(index);
  }

  /**
   * Determines whether there exists a bookmark for a given address and module.
   * 
   * @param module The module to check for.
   * @param address The address to check for.
   * 
   * @return True, if a bookmark exists for the given address and module. False, otherwise.
   */
  public boolean hasBookmark(final INaviModule module, final IAddress address) {
    Preconditions.checkNotNull(address, "IE00330: Address argument can not be null");

    for (final CCodeBookmark bookmark : m_bookmarks) {
      if ((bookmark.getModule() == module) && bookmark.getAddress().equals(address)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Iterator<CCodeBookmark> iterator() {
    return m_bookmarks.iterator();
  }

  /**
   * Removes a code bookmark from the code bookmark manager.
   * 
   * @param bookmark The bookmark to remove.
   */
  public void removeBookmark(final CCodeBookmark bookmark) {
    Preconditions.checkNotNull(bookmark, "IE00331: Bookmark argument can not be null");
    Preconditions.checkArgument(m_bookmarks.remove(bookmark),
        "IE00332: Bookmark is not managed by this manager");

    for (final ICodeBookmarkManagerListener listener : m_listeners) {
      try {
        listener.removedBookmark(this, bookmark);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener object from the code bookmark manager.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final ICodeBookmarkManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Returns the number of managed bookmarks.
   * 
   * @return The number of managed bookmarks.
   */
  public int size() {
    return m_bookmarks.size();
  }
}
