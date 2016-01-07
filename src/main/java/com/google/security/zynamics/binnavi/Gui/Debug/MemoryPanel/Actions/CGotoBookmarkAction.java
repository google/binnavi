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

import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;


/**
 * Action class that is executed when the user selects a bookmark. The memory view is then scrolled
 * to the bookmark.
 */
public final class CGotoBookmarkAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8086948556412584886L;

  /**
   * The bookmark to go to.
   */
  private final CBookmark m_bookmark;

  /**
   * Debug GUI where that contains the memory viewer.
   */
  private final CDebugPerspectiveModel m_model;

  /**
   * Creates a new Goto Bookmark action object.
   *
   * @param model Debug GUI where that contains the memory viewer.
   * @param bookmark The bookmark to go to.
   */
  public CGotoBookmarkAction(final CDebugPerspectiveModel model, final CBookmark bookmark) {
    super(String.format("%s: %s", bookmark.getAddress().toHexString(), bookmark.getDescription()));

    m_model = model;
    m_bookmark = bookmark;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.gotoOffset(m_model, m_bookmark.getAddress(), true);
  }
}
