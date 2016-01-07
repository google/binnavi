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
package com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.Implementations.CBookmarkFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Action that deletes bookmarks after the user selected the right popup menu.
 */
public final class CDeleteBookmarkAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3439106501997883212L;

  /**
   * Contains the debuggers where bookmarks can be set.
   */
  private final BackEndDebuggerProvider m_actionProvider;

  /**
   * Bookmark table rows that contains the bookmarks to delete.
   */
  private final int[] m_rows;

  /**
   * Creates a new action object.
   *
   * @param provider Contains the debuggers where bookmarks can be set.
   * @param rows Bookmark table rows that contains the bookmarks to delete.
   */
  public CDeleteBookmarkAction(final BackEndDebuggerProvider provider, final int[] rows) {
    super(rows.length == 1 ? "Delete Bookmark" : "Delete Bookmarks");

    m_actionProvider =
        Preconditions.checkNotNull(provider, "IE01328: Provider argument can not be null");

    m_rows = rows.clone();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBookmarkFunctions.deleteBookmarks(m_actionProvider, m_rows);
  }
}
