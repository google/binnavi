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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model that displays cross references.
 */
public final class CCrossReferencesModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3790314134849570999L;

  /**
   * Column names used by the model.
   */
  private static final String[] COLUMN_NAMES = {"Called Function", "Calling Function"};

  /**
   * Index of the column where called functions are shown.
   */
  private static final int CALLED_COLUMN = 0;

  /**
   * Index of the column where calling functions are shown.
   */
  private static final int CALLING_COLUMN = 1;

  /**
   * Cross references to show in the table.
   */
  private final List<CCrossReference> m_crossReferences = new ArrayList<CCrossReference>();

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    return m_crossReferences.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case CALLED_COLUMN:
        return m_crossReferences.get(rowIndex).getCalledFunction().getName();
      case CALLING_COLUMN:
        return m_crossReferences.get(rowIndex).getCallingFunction().getName();
      default:
        throw new IllegalStateException("IE01277: Invalid column");
    }
  }

  /**
   * Updates the cross references shown in the table.
   *
   * @param crossReferences The new cross references.
   */
  public void setCrossReferences(final List<CCrossReference> crossReferences) {
    m_crossReferences.clear();
    m_crossReferences.addAll(crossReferences);

    fireTableDataChanged();
  }
}
