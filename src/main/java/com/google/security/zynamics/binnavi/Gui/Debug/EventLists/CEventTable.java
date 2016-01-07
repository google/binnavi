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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Help.CEventTableHelp;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTable;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;

/**
 * Table class that displays individual events of a single event list.
 */
public final class CEventTable extends CFilteredTable<ITraceEvent> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1826613914790065057L;

  /**
   * The table model that is used to display the events.
   */
  private final CEventTableModel m_model;

  /**
   * The event list that is displayed in the table.
   */
  private TraceList m_list = null;

  /**
   * Creates a new event list table.
   *
   * @param model Table model of the table.
   */
  public CEventTable(final CEventTableModel model) {
    super(model, new CEventTableHelp());

    m_model = model;
  }

  @Override
  public void dispose() {
    // Empty default implementation
  }

  /**
   * Returns the unsorted indices of the selected rows.
   *
   * @return The indices of the selected rows.
   */
  public int[] getConvertedSelectedRows() {
    final int[] selectedRows = super.getSelectedRows();

    final int[] ret = new int[selectedRows.length];

    for (int i = 0; i < selectedRows.length; i++) {
      ret[i] = convertRowIndexToModel(selectedRows[i]);
    }

    return ret;
  }

  /**
   * Returns the trace list that provides the events shown in the table.
   *
   * @return The trace list that provides the events shown in the table.
   */
  public TraceList getList() {
    return m_list;
  }

  /**
   * Returns the raw table model.
   *
   * @return The raw table model.
   */
  @Override
  public CEventTableModel getTreeTableModel() {
    return m_model;
  }

  /**
   * Sets the event list that is displayed in the table.
   *
   * @param list The event list that is displayed in the table. If no event list should be
   *        displayed, null is passed as the list argument.
   */
  public void setEventList(final TraceList list) {
    m_list = list;

    m_model.setEventList(list);
  }
}
