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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation;



import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model used to display search results in a table.
 */
public final class CSearchResultModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3951676276993932033L;

  /**
   * Names of the columns shown by the table.
   */
  private static final String[] COLUMN_NAMES = {"Offset"};

  /**
   * Search results displayed in the table.
   */
  private final List<CSearchResult> m_results = new ArrayList<CSearchResult>();

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  /**
   * Returns the search result with the given index.
   * 
   * @param index The index of the search result.
   * 
   * @return The search result with the given index.
   */
  public CSearchResult getResult(final int index) {
    return m_results.get(index);
  }

  @Override
  public int getRowCount() {
    return m_results.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    return String.format("%08X", m_results.get(rowIndex).m_offset);
  }

  /**
   * Updates the search results.
   * 
   * @param results The new search results to display in the table.
   */
  public void setResults(final List<CSearchResult> results) {
    m_results.clear();
    m_results.addAll(results);

    fireTableDataChanged();
  }
}
