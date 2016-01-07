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
package com.google.security.zynamics.binnavi.Gui.Debug.GraphSelectionDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

/**
 * When the debugger stops at an instruction that is not part of any currently open views, the
 * CGraphSelectionDialog is used to let the user open a graph that contains the current instruction.
 *
 * In the graph, a table is shown where the user can choose what graph to open.
 */
public final class CGraphSelectionDialog extends JDialog {
  /**
   * The graph selected by the user.
   */
  private INaviView m_selectionResult;

  /**
   * The table where the views and functions are shown for selection.
   */
  private JTable m_table;

  /**
   * List of views that are shown in the dialog
   */
  private final List<INaviView> m_views;

  /**
   * Listener class that is responsible for random GUI stuff
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Creates a new graph selection dialog.
   *
   * @param owner The parent frame of the dialog.
   * @param views The views to be displayed in the table.
   */
  public CGraphSelectionDialog(final JFrame owner, final List<INaviView> views) {
    super(owner, "Select a graph", ModalityType.APPLICATION_MODAL);
    Preconditions.checkNotNull(views, "IE01304: Function list can't be null");
    m_views = new ArrayList<INaviView>(views);
    createGui(views);
    new CDialogEscaper(this);
    setLocationRelativeTo(null);
  }

  /**
   * Closes the dialog.
   */
  private void closeDialog() {
    m_table.removeMouseListener(m_listener);
    setVisible(false);
    dispose();
  }

  /**
   * Creates the GUI of the dialog.
   *
   * @param views The views to be shown in the table.
   */
  private void createGui(final List<INaviView> views) {
    setLayout(new BorderLayout());

    final JTextArea field = new JTextArea(
        "The debugger stopped at an instruction that does not belong to any open graphs.\nPlease select a graph from the list to continue debugging.");

    field.setEditable(false);

    add(field, BorderLayout.NORTH);

    m_table = new JTable(new CGraphSelectionTableModel(views));

    m_table.addMouseListener(m_listener);

    add(new JScrollPane(m_table), BorderLayout.CENTER);

    final CPanelTwoButtons panel = new CPanelTwoButtons(m_listener, "OK", "Cancel");

    add(panel, BorderLayout.SOUTH);

    setSize(500, 300);
  }

  /**
   * Returns the last selected element of the table.
   *
   * @return The last selected element of the table.
   */
  public INaviView getSelectionResult() {
    return m_selectionResult;
  }

  /**
   * Listener class that is responsible for random GUI stuff.
   */
  private class InternalListener extends MouseAdapter implements ActionListener {
    /**
     * Sets the selected element from the last selected table row.
     */
    private void setSelectedElement() {
      final int selectedRow = m_table.getSelectedRow();

      if (selectedRow != -1) {
        m_selectionResult = m_views.get(selectedRow);
      }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        // If the user clicked the OK button, update the selected element.
        setSelectedElement();
      }

      // OK or Cancel was chosen => Close the dialog
      closeDialog();
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      // Update the selected element on double-click
      if (event.getButton() == 1 && event.getClickCount() == 2) {
        setSelectedElement();
      }

      // Close the dialog on double-click
      if (m_selectionResult != null) {
        closeDialog();
      }
    }
  }
}
