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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks;

import javax.swing.table.AbstractTableModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.ICodeBookmarkListener;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.ICodeBookmarkManagerListener;

/**
 * Table model for the code bookmark table. The model provides three columns, module, address, and
 * description, for each bookmark. The module column and the address column are constant, the
 * description column can be edited.
 */
public final class CCodeBookmarkTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1397090388866442638L;

  /**
   * Index of the column where the module name is displayed.
   */
  private static final int MODULE_COLUMN = 0;

  /**
   * Index of the column where the bookmark address is displayed.
   */
  private static final int ADDRESS_COLUMN = 1;

  /**
   * Index of the column where the bookmark description is displayed.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Names of the columns of the bookmark table.
   */
  private static final String[] COLUMN_NAMES = {"Module", "Address", "Description"};

  /**
   * Listener that updates the table model when the managed bookmarks of the debuggers change.
   */
  private final InternalBookmarkListener m_listener = new InternalBookmarkListener();

  /**
   * Provides the debuggers that contain the bookmarks.
   */
  private final CCodeBookmarkManager m_bookmarkManager;

  /**
   * Creates a new bookmarks table model.
   *
   * @param bookmarkManager Provides the bookmark manager whose bookmarks are shown.
   */
  public CCodeBookmarkTableModel(final CCodeBookmarkManager bookmarkManager) {
    m_bookmarkManager =
        Preconditions.checkNotNull(bookmarkManager, "IE01311: Bookmarks manager can't be null");
    initializeListeners();
  }

  /**
   * Sets up the listeners that are necessary for keeping the data shown in the table synchronized
   * with the bookmarks managed by the bookmark manager.
   */
  private void initializeListeners() {
    m_bookmarkManager.addListener(m_listener);

    for (final CCodeBookmark bookmark : m_bookmarkManager) {
      bookmark.addListener(m_listener);
    }
  }

  /**
   * Removes all listeners that were previously added to keep the data shown in the table
   * synchronized with the bookmarks managed by the bookmark manager.
   */
  private void removeListeners() {
    m_bookmarkManager.removeListener(m_listener);

    for (final CCodeBookmark bookmark : m_bookmarkManager) {
      bookmark.removeListener(m_listener);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    removeListeners();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int col) {
    return COLUMN_NAMES[col];
  }

  @Override
  public int getRowCount() {
    return m_bookmarkManager.size();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    switch (col) {
      case MODULE_COLUMN:
        return m_bookmarkManager.get(row).getModule().getConfiguration().getName();
      case ADDRESS_COLUMN:
        return m_bookmarkManager.get(row).getAddress().toHexString();
      case DESCRIPTION_COLUMN:
        return m_bookmarkManager.get(row).getDescription();
      default:
        throw new IllegalArgumentException("IE01312: Unknown column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return col == DESCRIPTION_COLUMN;
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    Preconditions.checkArgument(
        col == DESCRIPTION_COLUMN, "IE01313: Only the description column can be modified");
    m_bookmarkManager.get(row).setDescription(value.toString());
  }

  /**
   * Updates the table on changes to the bookmarks.
   */
  private class InternalBookmarkListener
      implements ICodeBookmarkManagerListener, ICodeBookmarkListener {
    @Override
    public void addedBookmark(final CCodeBookmarkManager manager, final CCodeBookmark bookmark) {
      bookmark.addListener(m_listener);

      fireTableDataChanged();
    }

    @Override
    public void changedDescription(final CCodeBookmark bookmark) {
      fireTableDataChanged();
    }

    @Override
    public void removedBookmark(final CCodeBookmarkManager manager, final CCodeBookmark bookmark) {
      bookmark.removeListener(m_listener);

      fireTableDataChanged();
    }
  }
}
