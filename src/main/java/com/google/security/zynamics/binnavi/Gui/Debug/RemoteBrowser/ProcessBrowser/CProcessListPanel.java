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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.ProcessBrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;

import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessDescription;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;


/**
 * Panel that contains the table where the running processes of a remote target system are shown.
 */
public final class CProcessListPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2155639399498133878L;

  /**
   * Process list shown in the table.
   */
  private final ProcessList m_processList;

  /**
   * Table where the process list is shown.
   */
  private final JTable m_table;

  /**
   * Sorted used by the table.
   */
  private final TableRowSorter<CProcessListModel> m_sorter;

  /**
   * Creates a new panel object.
   *
   * @param processList Process list shown in the table.
   */
  public CProcessListPanel(final ProcessList processList) {
    super(new BorderLayout());

    m_processList = processList;

    final CProcessListModel model = new CProcessListModel(processList);
    m_sorter = new TableRowSorter<CProcessListModel>(model);

    m_table = new JTable(model);
    m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_table.setRowSorter(m_sorter);

    final JScrollPane scrollPane = new JScrollPane(m_table);
    scrollPane.setBorder(new TitledBorder("Please select a process to debug"));

    add(scrollPane);

    setPreferredSize(new Dimension(200, 200));
  }

  /**
   * Returns the selected process.
   *
   * @return The selected process.
   */
  public ProcessDescription getSelectedProcess() {
    final int selectedRow = m_table.getSelectedRow();

    return selectedRow == -1 ? null : m_processList.get(
        m_table.convertRowIndexToModel(selectedRow));
  }
}
