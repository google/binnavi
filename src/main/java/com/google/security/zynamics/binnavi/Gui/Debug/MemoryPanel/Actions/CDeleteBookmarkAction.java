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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;

/**
 * Action class that is used to delete bookmarks.
 */
public final class CDeleteBookmarkAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3076220946294808895L;

  /**
   * The bookmark manager from which the bookmark is removed.
   */
  private final BookmarkManager m_manager;

  /**
   * The bookmark to be removed.
   */
  private final CBookmark m_bookmark;

  /**
   * Creates a new delete bookmark action.
   *
   * @param manager The bookmark manager from which the bookmark is removed.
   * @param bookmark The bookmark to be removed.
   */
  public CDeleteBookmarkAction(final BookmarkManager manager, final CBookmark bookmark) {
    super(String.format("Remove bookmark at offset %s", bookmark.getAddress().toHexString()));

    m_manager = Preconditions.checkNotNull(manager, "IE01411: Manager argument can't be null");
    m_bookmark = bookmark;
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    m_manager.removeBookmark(m_bookmark);
  }
}
