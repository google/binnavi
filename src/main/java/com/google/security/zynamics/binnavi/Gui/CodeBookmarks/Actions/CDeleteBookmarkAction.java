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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Implemementations.CBookmarkFunctions;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;

/**
 * Action class used for deleting code bookmarks.
 */
public final class CDeleteBookmarkAction extends AbstractAction {

  /**
   * Code bookmark manager from which the bookmarks are deleted.
   */
  private final CCodeBookmarkManager m_manager;

  /**
   * Indices of the table rows of the bookmarks to delete.
   */
  private final int[] m_rows;

  /**
   * Creates a new action object.
   *
   * @param manager Code bookmark manager from which the bookmarks are deleted.
   * @param rows Indices of the table rows of the bookmarks to delete.
   */
  public CDeleteBookmarkAction(final CCodeBookmarkManager manager, final int[] rows) {
    super(rows.length == 1 ? "Delete Bookmark" : "Delete Bookmarks");

    m_manager =
        Preconditions.checkNotNull(manager, "IE01314: Bookmark manager argument can not be null");
    m_rows = rows.clone();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBookmarkFunctions.deleteBookmarks(m_manager, m_rows);
  }
}
