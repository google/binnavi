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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Base class for all table renderer classes that are used in the tree. This class provides generic
 * tooltip generation code which automatically creates standardized tooltips for any kind of project
 * table.
 */
public class CProjectTreeTableRenderer extends DefaultTableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8689762266363917827L;

  /**
   * Currently painted row.
   */
  private int m_row;

  /**
   * This function can be overwritten by child classes to post-process the renderer.
   * 
   * @param table the <code>JTable</code>
   * @param value the value to assign to the cell at <code>[row, column]</code>
   * @param isSelected true if cell is selected
   * @param hasFocus true if cell has focus
   * @param row the row of the cell to render
   * @param column the column of the cell to render
   */
  protected void postProcess(final JTable table, final Object value, final boolean isSelected,
      final boolean hasFocus, final int row, final int column) {
  }

  public int getM_row() {
    return m_row;
  }

  @Override
  public final Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    postProcess(table, value, isSelected, hasFocus, row, column);
    setM_row(row);

    return this;
  }

  public void setM_row(final int m_row) {
    this.m_row = m_row;
  }
}
