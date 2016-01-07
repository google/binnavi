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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Bookmark class that is used to store information about a single memory bookmark.
 *
 */
public final class CBookmark {
  /**
   * The address of the memory bookmark.
   */
  private final IAddress m_address;

  /**
   * The description of the memory bookmark.
   */
  private String m_description;

  /**
   * Listeners that are notified about changes in the bookmarks.
   */
  private final ListenerProvider<IBookmarkListener> m_listeners =
      new ListenerProvider<IBookmarkListener>();

  /**
   * Creates a new bookmark object.
   *
   * @param address The address of the bookmark.
   * @param description The description of the bookmark.
   *
   * @throws IllegalArgumentException Thrown if either of the parameters is null.
   */
  public CBookmark(final IAddress address, final String description) {
    m_address = Preconditions.checkNotNull(address, "IE00333: Bookmark addresses can't be null");
    m_description =
        Preconditions.checkNotNull(description, "IE00334: Bookmark descriptions can't be null");
  }

  /**
   * Adds a listener that is notified about changes in the bookmark.
   *
   * @param listener The listener object that is notified about changes in the bookmark.
   */
  public void addListener(final IBookmarkListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the address of the bookmark.
   *
   * @return The address of the bookmark.
   */
  public IAddress getAddress() {
    return m_address;
  }

  /**
   * Returns the description of the bookmark.
   *
   * @return The description of the bookmark.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Removes a listener object that was previously listening on the bookmark.
   *
   * @param listener The bookmark object to remove.
   */
  public void removeListener(final IBookmarkListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Sets the description of the bookmark.
   *
   * @param description The description of the bookmark.
   *
   * @throws IllegalArgumentException Thrown if the new description is null.
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description, "IE00335: Bookmark description can't be null");

    if (m_description.equals(description)) {
      return;
    }

    m_description = description;

    for (final IBookmarkListener listener : m_listeners) {
      try {
        listener.changedDescription(this, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
