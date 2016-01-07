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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Used to render cells of the type column in special instruction highlighting tables.
 */
public final class CTypeColumnRenderer extends JLabel implements TableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7874455686774985130L;

  /**
   * Border used for unselected table rows.
   */
  private Border unselectedBorder = null;

  /**
   * Border used for selected border rows.
   */
  private Border selectedBorder = null;

  /**
   * Creates a new renderer object.
   */
  public CTypeColumnRenderer() {
    setOpaque(true); // MUST do this for background to show up.
  }

  // ESCA-JAVA0138: Not one of our functions
  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    final ITypeDescription type = (ITypeDescription) value;

    setBackground(type.getColor());
    setForeground(Color.BLACK);
    setText(type.getDescription());

    setHorizontalAlignment(SwingConstants.CENTER);

    if (isSelected) {
      if (selectedBorder == null) {
        selectedBorder = BorderFactory.createMatteBorder(
            2, 5, 2, 5, table.getSelectionBackground());
      }

      setBorder(selectedBorder);
    } else {
      if (unselectedBorder == null) {
        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
      }

      setBorder(unselectedBorder);
    }

    return this;
  }
}
