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

import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The renderer that converts a {@link TypeInstanceReference cross reference} to the corresponding
 * text representation for display in the {@link TypeInstanceTable type instance table}.
 */
public class TypeInstanceCrossReferenceRenderer extends DefaultTableCellRenderer {

  private static final String NO_REFERENCE = "no reference";

  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    this.setText(renderText((TypeInstanceReference) value));
    this.setFont(GuiHelper.MONOSPACED_FONT);
    this.setVerticalAlignment(SwingConstants.TOP);
    this.setVerticalTextPosition(SwingConstants.TOP);
    return this;
  }

  public static String renderText(final TypeInstanceReference reference) {
    if (reference == null) {
      return NO_REFERENCE;
    }
    final StringBuilder builder = new StringBuilder();
    builder.append(reference.getView().getConfiguration().getName());
    builder.append(" :: ");
    builder.append(reference.getAddress().toHexString());
    return builder.toString();
  }
}
