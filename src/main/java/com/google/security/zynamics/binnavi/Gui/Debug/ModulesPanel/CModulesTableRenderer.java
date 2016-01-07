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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.CFadingColorGenerator;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Table renderer for the table where the modules in the debugged address space are shown.
 */
public final class CModulesTableRenderer extends DefaultTableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1773559764450713575L;

  /**
   * Table rendered by this renderer.
   */
  private final JTable m_table;

  /**
   * Keeps track of modules and their background color in the table.
   */
  private final CFadingColorGenerator<MemoryModule> m_colorGenerator =
      new CFadingColorGenerator<MemoryModule>();

  /**
   * Creates a new renderer object.
   *
   * @param table Table rendered by this renderer.
   */
  public CModulesTableRenderer(final JTable table) {
    m_table = Preconditions.checkNotNull(table, "IE02290: table argument can not be null");
  }

  /**
   * Adds a new module to be rendered.
   *
   * @param module The new module.
   */
  public void addModule(final MemoryModule module) {
    Preconditions.checkNotNull(module, "IE02291: module argument can not be null");

    m_colorGenerator.next(module);

    final Timer timer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        final Color newColor = m_colorGenerator.next(module);

        if (newColor.equals(Color.WHITE)) {
          ((Timer) event.getSource()).stop();
        }

        m_table.updateUI();
      }
    });

    timer.start();
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    final MemoryModule module = ((CModulesTable) table).getTreeTableModel().getModules().get(
        table.convertRowIndexToModel(row));

    final Color color = m_colorGenerator.getColor(module);

    setBackground(color == null ? Color.WHITE : color);

    return this;
  }
}
