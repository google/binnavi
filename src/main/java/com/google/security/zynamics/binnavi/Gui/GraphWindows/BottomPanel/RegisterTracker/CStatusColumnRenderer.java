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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Used to render cells of the status column in register tracking tables.
 */
public final class CStatusColumnRenderer extends JLabel implements TableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1688805365007891498L;

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
  public CStatusColumnRenderer() {
    setOpaque(true); // MUST do this for background to show up.
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    final CResultColumnWrapper wrapper = (CResultColumnWrapper) value;

    final CInstructionResult result = wrapper.getResult();

    setBackground(CResultColor.determineBackgroundColor(wrapper.getStartInstruction(),
        wrapper.getTrackedRegister(), result));
    setForeground(CResultColor.determineForegroundColor(wrapper.getStartInstruction(),
        wrapper.getTrackedRegister(), result));
    setText(CResultText.determineDescription(wrapper.getStartInstruction(),
        wrapper.getTrackedRegister(), result));

    setHorizontalAlignment(SwingConstants.CENTER);

    if (isSelected) {
      if (selectedBorder == null) {
        selectedBorder =
            BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
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
