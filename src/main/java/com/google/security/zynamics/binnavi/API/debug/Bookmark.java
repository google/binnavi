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

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.zylib.general.ListenerProvider;



// / Represents a single memory bookmark.
/**
 * Represents a single memory bookmark. Memory bookmarks can be used to quickly navigate to a memory
 * address in the memory panel of the GUI.
 */
public final class Bookmark implements ApiObject<CBookmark> {
  // / @cond INTERNAL

  /**
   * Wrapped native bookmark.
   */
  private final CBookmark m_bookmark;

  /**
   * Keeps track of changes in the internal API.
   */
  private final InternalBookmarkListener m_internalBookmarkListener =
      new InternalBookmarkListener();

  /**
   * Listeners that are notified about changes in the bookmark.
   */
  private final ListenerProvider<IBookmarkListener> m_listeners =
      new ListenerProvider<IBookmarkListener>();

  /**
   * Creates a new API bookmark object.
   * 
   * @param bookmark The internal bookmark object wrapped by the API bookmark object.
   */
  public Bookmark(final CBookmark bookmark) {
    m_bookmark = bookmark;

    m_bookmark.addListener(m_internalBookmarkListener);
  }

  @Override
  public CBookmark getNative() {
    return m_bookmark;
  }

  // ! Adds a bookmark listener.
  /**
   * Adds an object that is notified about changes in the bookmark.
   * 
   * @param listener The listener object that is notified about changes in the bookmark.
   * 
   */
  public void addListener(final IBookmarkListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Address of the bookmark.
  /**
   * Returns the address of the bookmark.
   * 
   * The address of a bookmark is guaranteed to be non-null.
   * 
   * @return The address of the bookmark.
   */
  public Address getAddress() {
    return new Address(m_bookmark.getAddress().toBigInteger());
  }

  // ! Description of the bookmark.
  /**
   * Returns the description of the bookmark.
   * 
   * The description of a bookmark is guaranteed to be non-null.
   * 
   * @return The description of the bookmark.
   */
  public String getDescription() {
    return m_bookmark.getDescription();
  }

  // ! Removes a bookmark listener.
  /**
   * Removes a listener object from the bookmark.
   * 
   * @param listener The listener object to remove from the bookmark.
   * 
   */
  public void removeListener(final IBookmarkListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the bookmark description.
  /**
   * Changes the description of the bookmark.
   * 
   * @param description The new description of the bookmark (must be non-null).
   * 
   */
  public void setDescription(final String description) {
    m_bookmark.setDescription(description);
  }

  // ! Printable representation of the bookmark.
  /**
   * Returns a string representation of the bookmark.
   * 
   * @return A string representation of the bookmark.
   */
  @Override
  public String toString() {
    return String.format("Bookmark %s/'%s'", getAddress().toHexString(), getDescription());
  }

  /**
   * Keeps track of changes in the mapped native bookmark.
   */
  private class InternalBookmarkListener implements
      com.google.security.zynamics.binnavi.models.Bookmarks.memory.IBookmarkListener {
    @Override
    public void changedDescription(final CBookmark bookmark, final String description) {
      for (final IBookmarkListener listener : m_listeners) {
        listener.changedDescription(Bookmark.this, description);
      }
    }
  }
}
