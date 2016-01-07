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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

public final class CAddressSelectionTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 744387021510L;

  /**
   * The column names of the table.
   */
  private static final String[] columns = {"Module"};

  private List<INaviModule> m_modules = null;

  /**
   * Creates a new model object.
   */
  public CAddressSelectionTableModel(final List<INaviModule> modules) {
    Preconditions.checkNotNull(modules, "IE01175: Modules argument can not be null");

    m_modules = new ArrayList<INaviModule>(modules);
  }

  @Override
  public int getColumnCount() {
    return columns.length;
  }

  @Override
  public String getColumnName(final int col) {
    return columns[col];
  }

  @Override
  public int getRowCount() {
    return m_modules == null ? 0 : m_modules.size();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    if ((row >= 0) && (row < m_modules.size())) {
      switch (col) {
        case 0:
          return m_modules.get(row);
        default:
          throw new IllegalArgumentException("IE01394: Unknown column");
      }
    } else {
      throw new IllegalStateException("IE01122: Unknown row");
    }
  }

  /**
   * Returns the views that are displayed in the table.
   * 
   * @return The views that are displayed in the table.
   */
  public List<INaviModule> getViews() {
    return new ArrayList<INaviModule>(m_modules);
  }

  /**
   * Sets the views that should be displayed in the table.
   */
  public void setViews(final List<INaviModule> modules) {
    Preconditions.checkNotNull(modules, "IE01395: List of flow graphs can't be null");

    m_modules = new ArrayList<INaviModule>(modules);

    fireTableDataChanged();
  }
}
