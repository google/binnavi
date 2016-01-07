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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;



/**
 * Renderer class that is used to render the availability cells in a debugger options table.
 */
public final class COptionRenderer extends JLabel implements TableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6567820902659020437L;

  /**
   * Creates a new renderer object.
   */
  public COptionRenderer() {
    setOpaque(true);
  }

  // ESCA-JAVA0138: Not my method => Can not decrease number of arguments
  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object object,
      final boolean arg2,
      final boolean arg3,
      final int arg4,
      final int arg5) {
    setFont(table.getFont());

    final String value = (String) object;

    if (value.equals("Supported")) {
      super.setHorizontalAlignment(CENTER);

      setForeground(Color.WHITE);
      setBackground(new Color(0, 160, 0));
    } else if (value.equals("Not supported"))
 {
      super.setHorizontalAlignment(CENTER);

      setForeground(Color.WHITE);
      setBackground(Color.RED);
    } else {
      super.setHorizontalAlignment(LEFT);

      setForeground(Color.BLACK);
      setBackground(Color.WHITE);
    }

    setText(value);

    return this;
  }
}
