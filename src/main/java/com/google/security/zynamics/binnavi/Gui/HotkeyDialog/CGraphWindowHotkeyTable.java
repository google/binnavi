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
package com.google.security.zynamics.binnavi.Gui.HotkeyDialog;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table that can be used to display the available hotkeys of the graph window.
 */
public final class CGraphWindowHotkeyTable extends JTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2452990599228921464L;

  /**
   * Renderer for hotkey entries in the table.
   */
  private static CHotkeyRenderer m_hotkeyRenderer = new CHotkeyRenderer();

  /**
   * Renderer used for the left half of title rows.
   */
  private static CLeftTitleRenderer m_leftRenderer = new CLeftTitleRenderer();

  /**
   * Renderer used for the right half of title rows.
   */
  private static CRightTitleRenderer m_rightRenderer = new CRightTitleRenderer();

  /**
   * Creates a new table object.
   */
  public CGraphWindowHotkeyTable() {
    super(new CGraphWindowHotkeyTableModel());

    setShowVerticalLines(false);
    getColumnModel().setColumnMargin(0);

    setEnabled(false); // Do not allow any kind of interaction with the table
  }

  @Override
  public TableCellRenderer getCellRenderer(final int row, final int col) {
    final Object object = getModel().getValueAt(row, col);

    if (object instanceof HotKey) {
      return m_hotkeyRenderer;
    } else if (object instanceof CLeftTitle) {
      return m_leftRenderer;
    } else if (object instanceof CRightTitle) {
      return m_rightRenderer;
    } else {
      return super.getCellRenderer(row, col);
    }
  }
}
