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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto;

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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

public class CAddressSelectionDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2832808324997248079L;

  /**
   * The graph selected by the user.
   */
  private INaviModule m_selectionResult;

  /**
   * The table where the views and functions are shown for selection.
   */
  private JTable m_table;

  /**
   * List of views that are shown in the dialog
   */
  private final List<INaviModule> m_modules;

  /**
   * Listener class that is responsible for random GUI stuff
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Creates a new graph selection dialog.
   *
   * @param owner The parent frame of the dialog.
   */
  public CAddressSelectionDialog(final JFrame owner, final List<INaviModule> m_modules2) {
    super(owner, "Select a graph", ModalityType.APPLICATION_MODAL);

    Preconditions.checkNotNull(m_modules2, "IE01392: Function list can't be null");

    m_modules = new ArrayList<INaviModule>(m_modules2);

    createGui(m_modules2);

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
   */
  private void createGui(final List<INaviModule> modules) {
    setLayout(new BorderLayout());

    final JTextArea field = new JTextArea(
        "The current graph has more then one module.\nPlease choose the one to search in.");

    field.setEditable(false);

    add(field, BorderLayout.NORTH);

    m_table = new JTable(new CAddressSelectionTableModel(modules));

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
  public INaviModule getSelectionResult() {
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
        m_selectionResult = m_modules.get(selectedRow);
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
      if ((event.getButton() == 1) && (event.getClickCount() == 2)) {
        setSelectedElement();
      }

      // Close the dialog on double-click
      if (m_selectionResult != null) {
        closeDialog();
      }
    }
  }
}
