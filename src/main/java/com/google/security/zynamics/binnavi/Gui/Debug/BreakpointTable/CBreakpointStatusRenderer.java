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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer that is used to draw breakpoint status cells.
 */
public final class CBreakpointStatusRenderer extends JLabel implements TableCellRenderer {

  /**
   * Creates a new breakpoint status renderer.
   */
  public CBreakpointStatusRenderer() {
    setOpaque(true); // MUST do this for background to show up.
  }

  /**
   * Determines the cell text for a breakpoint status.
   * 
   * @param breakpointStatus The breakpoint status.
   * 
   * @return The corresponding cell text.
   */
  private static String getText(final BreakpointStatus breakpointStatus) {
    switch (breakpointStatus) {
      case BREAKPOINT_ACTIVE:
        return "Active";
      case BREAKPOINT_INACTIVE:
        return "Inactive";
      case BREAKPOINT_ENABLED:
        return "Enabled";
      case BREAKPOINT_DISABLED:
        return "Disabled";
      case BREAKPOINT_HIT:
        return "Hit";
      case BREAKPOINT_INVALID:
        return "Invalid";
      case BREAKPOINT_DELETING:
        return "Deleting";
      default:
        throw new IllegalStateException("IE01117: Unknown breakpoint status");
    }
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object object,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    super.setHorizontalAlignment(CENTER);

    final BreakpointStatus breakpointStatus = (BreakpointStatus) object;

    setForeground(Color.BLACK);
    setBackground(BreakpointManager.getBreakpointColor(breakpointStatus));

    setText(getText(breakpointStatus));

    // Create a small border effect for the status cells
    setBorder(BorderFactory.createMatteBorder(
        2, 5, 2, 5,
        isSelected ? table.getSelectionBackground() : table.getBackground()));

    return this;
  }
}
