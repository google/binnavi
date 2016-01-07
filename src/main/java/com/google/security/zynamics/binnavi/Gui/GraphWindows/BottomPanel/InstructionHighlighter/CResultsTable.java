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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.gui.tables.CMonospaceRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;



/**
 * Table where the special instructions highlighting is shown.
 */
public final class CResultsTable extends JTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3620728872126946791L;

  /**
   * Graph where the highlighting happens.
   */
  private final ZyGraph m_graph;

  /**
   * Provides the results to display.
   */
  private final CSpecialInstructionsModel m_model;

  /**
   * Original table model.
   */
  private final CResultsTableModel m_tableModel;

  /**
   * Creates a new results table.
   *
   * @param graph Graph where the highlighting happens.
   * @param model Provides the results to display.
   */
  public CResultsTable(final ZyGraph graph, final CSpecialInstructionsModel model) {
    m_graph = graph;
    m_model = model;

    m_tableModel = new CResultsTableModel(model);
    setModel(m_tableModel);

    final TableRowSorter<CResultsTableModel> sorter =
        new TableRowSorter<CResultsTableModel>(m_tableModel);
    sorter.setComparator(CResultsTableModel.TYPE_COLUMN, new CTypeComparator());
    sorter.setComparator(CResultsTableModel.ADDRESS_COLUMN, new HexStringComparator());
    setRowSorter(sorter);

    // Make sure that the status cells are colored.
    getColumnModel()
        .getColumn(CResultsTableModel.TYPE_COLUMN).setCellRenderer(new CTypeColumnRenderer());
    getColumnModel()
        .getColumn(CResultsTableModel.INSTRUCTION_COLUMN).setCellRenderer(new CMonospaceRenderer());

    final InternalSelectionListener listener = new InternalSelectionListener();

    getSelectionModel().addListSelectionListener(listener);
    addMouseListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_tableModel.dispose();
  }

  /**
   * Listener that updates on selection changes in the table and updates the graph accordingly when
   * a selection change happens.
   */
  private class InternalSelectionListener extends MouseAdapter implements ListSelectionListener {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
        final int sortedRow = rowAtPoint(event.getPoint());

        if (sortedRow == -1) {
          return;
        }

        final int row = convertRowIndexToModel(sortedRow);

        ZyZoomHelpers.zoomToInstruction(m_graph, m_model.getInstruction(row).getInstruction());
      }
    }

    @Override
    public void valueChanged(final ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }

      // When the selection of the table changes, clear the previous
      // register tracking highlighting and highlight the instructions
      // which are selected now.

      CTypeResultsHighlighter.updateHighlighting(m_graph, m_model.getInstructions());
    }
  }
}
