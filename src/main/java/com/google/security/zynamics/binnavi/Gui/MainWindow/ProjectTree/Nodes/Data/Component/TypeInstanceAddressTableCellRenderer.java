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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceAddress;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders a {@link TypeInstanceAddress} to string for display in the {@link TypeInstanceTable}
 * component.
 *
 * Note: this renderer assumes that the underlying model is of type {@link TypeInstanceTableModel}.
 */
public class TypeInstanceAddressTableCellRenderer extends DefaultTableCellRenderer {

  private boolean showvirtualAddress;

  public static String renderText(final TypeInstanceAddress address) {
    final StringBuilder builder =
        new StringBuilder(String.format("VA: 0x%X", address.getVirtualAddress()));
    builder.append(
        String.format("0x%X:%X", address.getBaseAddress().toLong(), address.getOffset()));
    return builder.toString();
  }

  public static String getStringToDisplay(boolean showvirtualAddress, TypeInstanceAddress value) {
    if (showvirtualAddress) {
      return String.format("VA: 0x%X", value.getVirtualAddress());
    }
    return String.format("0x%X:%X", value.getBaseAddress().toLong(), value.getOffset());
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof TypeInstanceAddress) {
      this.setText(getStringToDisplay(showvirtualAddress, (TypeInstanceAddress) value));
      this.setFont(GuiHelper.MONOSPACED_FONT);
      this.setVerticalAlignment(SwingConstants.TOP);
      this.setVerticalTextPosition(SwingConstants.TOP);
    }
    return this;
  }

  public void showVirtualAddress(final boolean toggle) {
    showvirtualAddress = toggle;
  }
}
