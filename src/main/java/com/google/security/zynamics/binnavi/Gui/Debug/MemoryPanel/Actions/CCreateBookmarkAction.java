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
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action class that is used to create new bookmarks.
 */
public final class CCreateBookmarkAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7487387798120275224L;

  /**
   * The bookmark manager where the bookmark is set.
   */
  private final BookmarkManager m_manager;

  /**
   * The offset where the bookmark is set.
   */
  private final IAddress m_offset;

  /**
   * Creates a new bookmark action.
   *
   * @param manager The bookmark manager where the bookmark is set.
   * @param offset The offset where the bookmark is set.
   */
  public CCreateBookmarkAction(final BookmarkManager manager, final IAddress offset) {
    super(String.format("Create bookmark at offset %s", offset.toHexString()));

    m_manager = Preconditions.checkNotNull(manager, "IE01410: Manager argument can not be null");
    m_offset = Preconditions.checkNotNull(offset, "IE02289: Offset arguemnt can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_manager.addBookmark(new CBookmark(m_offset, ""));
  }
}
