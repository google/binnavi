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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import com.google.security.zynamics.zylib.gui.tables.CMonospaceRenderer;

/**
 * In this table, the results of a register tracking operation are shown.
 */
public final class CTrackingResultsTable extends JTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1286932901902302435L;

  /**
   * This column shows quick status information about the instruction and its effect on the register
   * tracking process.
   */
  public static final int STATUS_COLUMN = 0;

  /**
   * This column shows the address of the instruction.
   */
  public static final int ADDRESS_COLUMN = 1;

  /**
   * This column shows the instruction.
   */
  public static final int INSTRUCTION_COLUMN = 2;

  /**
   * This column shows what registers the instruction uses.
   */
  public static final int READS_COLUMN = 3;

  /**
   * This column shows what registers the instruction updates.
   */
  public static final int UPDATES_COLUMN = 4;

  /**
   * This column shows what registers the instruction defines.
   */
  public static final int DEFINES_COLUMN = 5;

  /**
   * This column shows what registers the instruction undefines.
   */
  public static final int UNDEFINES_COLUMN = 6;

  /**
   * This column shows what registers are defined after the instruction is executed.
   */
  public static final int TAINTED_COLUMN = 7;

  /**
   * Results container from where the results can be read.
   */
  private final CTrackingResultContainer m_container;

  /**
   * The table model.
   */
  private final CTrackingResultsTableModel m_model;

  /**
   * The row sorter for this table.
   */
  private final TableRowSorter<TableModel> m_sorter;

  /**
   * Creates a new results tracking table.
   *
   * @param container Results container from where the results can be read.
   */
  public CTrackingResultsTable(final CTrackingResultContainer container) {
    m_container =
        Preconditions.checkNotNull(container, "IE01691: Container argument can not be null");

    m_model = new CTrackingResultsTableModel(container);
    setModel(m_model);

    m_sorter = new TableRowSorter<TableModel>(m_model);
    setRowSorter(m_sorter);
    m_sorter.setComparator(ADDRESS_COLUMN, new LexicalComparator());

    // Make sure that the status cells are colored.
    getColumnModel().getColumn(STATUS_COLUMN).setCellRenderer(new CStatusColumnRenderer());
    getColumnModel().getColumn(INSTRUCTION_COLUMN).setCellRenderer(new CMonospaceRenderer());

    final InternalSelectionListener listener = new InternalSelectionListener();

    getSelectionModel().addListSelectionListener(listener);
    addMouseListener(listener);
  }

  /**
   * Returns the instruction results associated with a given list of table rows.
   *
   * @param result The tracking result that contains all individual instruction results.
   * @param normalizedRows A list of normalized row indices.
   *
   * @return The instruction results associated with the given row indices.
   */
  private static List<CInstructionResult> getInstructionResults(
      final CTrackingResult result, final int[] normalizedRows) {
    final List<CInstructionResult> results = new ArrayList<CInstructionResult>();

    for (final int row : normalizedRows) {
      results.add(result.getResults().get(row));
    }

    return results;
  }

  public void dispose() {
    m_model.dispose();
  }

  /**
   * Listener that updates on selection changes in the table and updates the graph accordingly when
   * a selection change happens.
   */
  private class InternalSelectionListener extends MouseAdapter implements ListSelectionListener {
    private int[] normalizeRows(final int[] sortedRows) {
      final int[] rawRows = new int[sortedRows.length];

      for (int i = 0; i < sortedRows.length; i++) {
        rawRows[i] = convertRowIndexToModel(sortedRows[i]);
      }

      return rawRows;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
        final int sortedRow = rowAtPoint(event.getPoint());

        if (sortedRow == -1) {
          return;
        }

        final int row = convertRowIndexToModel(sortedRow);

        ZyZoomHelpers.zoomToInstruction(
            m_container.getGraph(), m_container.getResult().getResults().get(row).getInstruction());
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

      final int[] normalizedRows = normalizeRows(getSelectedRows());

      final List<CInstructionResult> instructionResults = getInstructionResults(
          m_container.getResult(), normalizedRows);

      CTrackingResultsHighlighter.updateHighlighting(m_container.getGraph(),
          m_container.getResult().getStartInstruction(),
          m_container.getResult().getTrackedRegister(), instructionResults);
    }
  }
}
