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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;

import com.google.security.zynamics.binnavi.Gui.Debug.CFadingColorGenerator;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;


/**
 * Table renderer for the table where the modules in the debugged address space are shown.
 */
public final class CThreadInformationTableRenderer extends DefaultTableCellRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4591645811572405945L;

  /**
   * Table rendered by this renderer.
   */
  private final JTable m_table;

  /**
   * Keeps track of modules and their background color in the table.
   */
  private final CFadingColorGenerator<TargetProcessThread> m_colorGenerator =
      new CFadingColorGenerator<TargetProcessThread>();

  /**
   * Creates a new renderer object.
   *
   * @param table Table rendered by this renderer.
   */
  public CThreadInformationTableRenderer(final JTable table) {
    m_table = table;
  }

  /**
   * Adds a new thread to be rendered.
   *
   * @param thread The new thread.
   */
  public void addThread(final TargetProcessThread thread) {
    m_colorGenerator.next(thread);

    final Timer timer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        final Color newColor = m_colorGenerator.next(thread);

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

    final TargetProcessThread module = ((CThreadInformationTable) table).getModel().getThreads().get(row);

    final Color color = m_colorGenerator.getColor(module);

    setBackground(color == null ? Color.WHITE : color);

    return this;
  }
}
