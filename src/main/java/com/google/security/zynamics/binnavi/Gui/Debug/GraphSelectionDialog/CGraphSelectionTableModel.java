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
package com.google.security.zynamics.binnavi.Gui.Debug.GraphSelectionDialog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * Table model that is used in the table that can be used to display a number of views for the user
 * to choose from.
 * 
 * The model provides three columns. The first column contains the type of the graph, the second
 * column contains the name of the graph, and the third column contains the address of the graph.
 */
public final class CGraphSelectionTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7443878071502021510L;

  /**
   * The column names of the table.
   */
  private static final String[] columns = {"Type", "Name", "Module"};

  /**
   * List of flowgraphs to display in the table.
   */
  private List<INaviView> m_views = null;

  /**
   * Creates a new model object.
   * 
   * @param views The views shown in the table.
   */
  public CGraphSelectionTableModel(final List<INaviView> views) {
    Preconditions.checkNotNull(views, "IE01393: Views argument can not be null");

    m_views = new ArrayList<INaviView>(views);
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
    return m_views == null ? 0 : m_views.size();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    if ((row >= 0) && (row < m_views.size())) {
      switch (col) {
        case 0:
          return m_views.get(row).getType() == ViewType.Native ? "Function" : "Flowgraph";
        case 1:
          return m_views.get(row).getName();
        case 2:
          if (m_views.get(row).getConfiguration().getModule() != null) {
            return m_views.get(row).getConfiguration().getModule().getConfiguration().getName();
          } else {
            return "Error: no module.";
          }
        default:
          throw new IllegalArgumentException("IE01305: Unknown column");
      }
    } else {
      throw new IllegalStateException("IE01307: Unknown row");
    }
  }

  /**
   * Returns the views that are displayed in the table.
   * 
   * @return The views that are displayed in the table.
   */
  public List<INaviView> getViews() {
    return new ArrayList<INaviView>(m_views);
  }

  /**
   * Sets the views that should be displayed in the table.
   * 
   * @param views The views that should be displayed in the table.
   */
  public void setViews(final List<INaviView> views) {
    Preconditions.checkNotNull(views, "IE01450: List of flow graphs can't be null");

    m_views = new ArrayList<INaviView>(views);

    fireTableDataChanged();
  }
}
