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
package com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.IBookmarkManagerListener;
import com.google.security.zynamics.zylib.general.Triple;

/**
 * Table model for the bookmark table. The model provides three columns, debugger, address, and
 * description, for each bookmark. The debugger column and the address column is constant, the
 * description column can be edited.
 */
public final class CBookmarkTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1596072260232647317L;

  /**
   * Index of the column where the debugger name is shown.
   */
  private static final int DEBUGGER_COLUMN = 0;

  /**
   * Index of the column where the bookmark address is shown.
   */
  private static final int ADDRESS_COLUMN = 1;

  /**
   * Index of the column where the bookmark description is shown.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Names of columns of the bookmark table.
   */
  private static final String[] COLUMN_NAMES = {"Debugger", "Address", "Description"};

  /**
   * Listener that updates the table model when the managed bookmarks of the debuggers change.
   */
  private final InternalBookmarkListener m_listener = new InternalBookmarkListener();

  /**
   * Listens on changing debuggers.
   */
  private final DebuggerProviderListener m_debuggerListener = new InternalDebuggerListener();

  /**
   * Provides the debuggers that contain the bookmarks.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Creates a new bookmarks table model.
   *
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public CBookmarkTableModel(final BackEndDebuggerProvider debuggerProvider) {
    m_debuggerProvider =
        Preconditions.checkNotNull(debuggerProvider, "IE01325: Bookmarks manager can't be null");
    initializeListeners();
  }

  /**
   * Returns the total number of bookmarks in all debuggers.
   *
   * @return The total number of bookmarks.
   */
  private int countBookmarks() {
    int bookmarks = 0;

    for (final IDebugger debugger : m_debuggerProvider) {
      bookmarks += debugger.getBookmarkManager().getNumberOfBookmarks();
    }

    return bookmarks;
  }

  /**
   * Finds the bookmark address of the bookmark that is displayed in a given row of the table.
   *
   * @param row The table row where the bookmark is displayed.
   *
   * @return The bookmark address of the bookmark in the given row.
   */
  private String getBookmarkAddress(final int row) {
    final Triple<IDebugger, BookmarkManager, Integer> bookmarkTriple =
        CBookmarkTableHelpers.findBookmark(m_debuggerProvider, row);

    final BookmarkManager manager = bookmarkTriple.second();
    final int index = bookmarkTriple.third();

    return manager.getBookmark(index).getAddress().toHexString();
  }

  /**
   * Finds the bookmark description of the bookmark that is displayed in a given row of the table.
   *
   * @param row The table row where the bookmark is displayed.
   *
   * @return The bookmark description of the bookmark in the given row.
   */
  private String getBookmarkDescription(final int row) {
    final Triple<IDebugger, BookmarkManager, Integer> bookmarkTriple =
        CBookmarkTableHelpers.findBookmark(m_debuggerProvider, row);

    final BookmarkManager manager = bookmarkTriple.second();
    final int index = bookmarkTriple.third();

    return manager.getBookmark(index).getDescription();
  }

  /**
   * Finds the debugger of the bookmark that is displayed in a given row of the table.
   *
   * @param row The table row where the bookmark is displayed.
   *
   * @return The debugger of the bookmark in the given row.
   */
  private String getDebugger(final int row) {
    final Triple<IDebugger, BookmarkManager, Integer> bookmarkTriple =
        CBookmarkTableHelpers.findBookmark(m_debuggerProvider, row);

    return bookmarkTriple.first().getPrintableString();
  }

  /**
   * Initializes the listeners which are necessary to keep the bookmarks shown in the table
   * synchronized with the managed bookmarks.
   */
  private void initializeListeners() {
    for (final IDebugger debugger : m_debuggerProvider) {
      debugger.getBookmarkManager().addListener(m_listener);
    }

    m_debuggerProvider.addListener(m_debuggerListener);
  }

  /**
   * Removes the listeners which were necessary to keep the bookmarks shown in the table
   * synchronized with the managed bookmarks.
   */
  private void removeListeners() {
    for (final IDebugger debugger : m_debuggerProvider) {
      debugger.getBookmarkManager().removeListener(m_listener);
    }

    m_debuggerProvider.removeListener(m_debuggerListener);
  }

  /**
   * Modifies the bookmark description of the bookmark displayed in the given row.
   *
   * @param row The row of the bookmark.
   * @param description The new description of the bookmark.
   */
  private void setBookmarkDescription(final int row, final String description) {
    final Triple<IDebugger, BookmarkManager, Integer> bookmarkTriple =
        CBookmarkTableHelpers.findBookmark(m_debuggerProvider, row);

    final BookmarkManager manager = bookmarkTriple.second();
    final int index = bookmarkTriple.third();

    manager.getBookmark(index).setDescription(description);
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
    return countBookmarks();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    switch (col) {
      case DEBUGGER_COLUMN:
        return getDebugger(row);
      case ADDRESS_COLUMN:
        return getBookmarkAddress(row);
      case DESCRIPTION_COLUMN:
        return getBookmarkDescription(row);
      default:
        throw new IllegalArgumentException("IE01326: Unknown column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return col == DESCRIPTION_COLUMN;
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    Preconditions.checkArgument(
        col == DESCRIPTION_COLUMN, "IE01327: Only the description column can be modified");

    setBookmarkDescription(row, value.toString());
  }

  /**
   * Updates the table on changes to the bookmarks.
   */
  private class InternalBookmarkListener implements IBookmarkManagerListener {
    @Override
    public void addedBookmark(final BookmarkManager manager, final CBookmark bookmark) {
      fireTableDataChanged();
    }

    @Override
    public void removedBookmark(final BookmarkManager manager, final CBookmark bookmark) {
      fireTableDataChanged();
    }
  }

  /**
   * Listens on changing debuggers.
   */
  private class InternalDebuggerListener implements DebuggerProviderListener {
    @Override
    public void debuggerAdded(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      debugger.getBookmarkManager().addListener(m_listener);
    }

    @Override
    public void debuggerRemoved(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      debugger.getBookmarkManager().removeListener(m_listener);
    }
  }
}
