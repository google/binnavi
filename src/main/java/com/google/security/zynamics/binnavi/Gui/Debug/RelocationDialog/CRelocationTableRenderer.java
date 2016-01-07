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
package com.google.security.zynamics.binnavi.Gui.Debug.RelocationDialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer that is used to show incorrectly mapped modules in a table.
 */
public final class CRelocationTableRenderer extends DefaultTableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 568904105352493273L;

  /**
   * Background color of the module names column.
   */
  private static final Color NAME_COLUMN_BACKGROUND_COLOR = Color.WHITE;

  /**
   * Background color of the unrelocated base address column.
   */
  private static final Color SPECIFIED_COLUMN_BACKGROUND_COLOR = new Color(255, 128, 128);

  /**
   * Background color of the relocated base address column.
   */
  private static final Color REAL_COLUMN_BACKGROUND_COLOR = new Color(128, 255, 128);

  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    setHorizontalAlignment(CENTER);

    switch (column) {
      case CRelocationTableModel.NAME_COLUMN:
        setBackground(NAME_COLUMN_BACKGROUND_COLOR);
        break;
      case CRelocationTableModel.SPECIFIED_COLUMN:
        setBackground(SPECIFIED_COLUMN_BACKGROUND_COLOR);
        break;
      case CRelocationTableModel.REAL_COLUMN:
        setBackground(REAL_COLUMN_BACKGROUND_COLOR);
        break;
      default:
        throw new IllegalStateException("IE01924: Unknown column");
    }

    return this;
  }
}
