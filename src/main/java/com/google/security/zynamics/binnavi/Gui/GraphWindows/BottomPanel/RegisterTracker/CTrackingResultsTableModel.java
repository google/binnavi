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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;

/**
 * Table model used to display the results of register tracking operations.
 */
public final class CTrackingResultsTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1024629687711193824L;

  /**
   * Names of the columns used by the model.
   */
  private static final String[] COLUMN_NAMES =
      {"Status", "Address", "Instruction", "Reads", "Updates", "Defines", "Undefines", "Tainted"};

  /**
   * Results container from where the results can be read.
   */
  private final CTrackingResultContainer m_container;

  /**
   * Listener that redraws the table in case new results were written to the results container.
   */
  private final ITrackingResultsListener m_resultsListener = new InternalResultsListener();

  /**
   * Creates a new results tracking tables model.
   *
   * @param container Results container from where the results can be read.
   */
  public CTrackingResultsTableModel(final CTrackingResultContainer container) {
    m_container =
        Preconditions.checkNotNull(container, "IE01692: Container argument can not be null");
    m_container.addListener(m_resultsListener);
  }

  public void dispose() {
    m_container.removeListener(m_resultsListener);
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    return m_container.getResult() == null ? 0 : m_container.getResult().getResults().size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final List<CInstructionResult> results = m_container.getResult().getResults();

    final CInstructionResult result = results.get(rowIndex);

    switch (columnIndex) {
      case CTrackingResultsTable.STATUS_COLUMN:
        return new CResultColumnWrapper(m_container.getResult().getStartInstruction(),
            m_container.getResult().getTrackedRegister(), result);
      case CTrackingResultsTable.ADDRESS_COLUMN:
        return CTrackingResultsTableFiller.getAddressColumnText(result);
      case CTrackingResultsTable.INSTRUCTION_COLUMN:
        return CTrackingResultsTableFiller.getInstructionColumnText(result);
      case CTrackingResultsTable.READS_COLUMN:
        return CTrackingResultsTableFiller.getReadsColumnText(result);
      case CTrackingResultsTable.UPDATES_COLUMN:
        return CTrackingResultsTableFiller.getUpdatedColumnText(result);
      case CTrackingResultsTable.DEFINES_COLUMN:
        return CTrackingResultsTableFiller.getWritesColumnText(result);
      case CTrackingResultsTable.UNDEFINES_COLUMN:
        return CTrackingResultsTableFiller.getUndefinesColumnText(result);
      case CTrackingResultsTable.TAINTED_COLUMN:
        return CTrackingResultsTableFiller.getDefinedColumnText(result);
      default:
        throw new IllegalStateException("IE01147: Invalid column");
    }
  }

  /**
   * Listener that redraws the table in case new results were written to the results container.
   */
  private class InternalResultsListener implements ITrackingResultsListener {
    @Override
    public void updatedResult(
        final CTrackingResultContainer trackingResultContainer, final CTrackingResult result) {
      fireTableDataChanged();
    }
  }
}
