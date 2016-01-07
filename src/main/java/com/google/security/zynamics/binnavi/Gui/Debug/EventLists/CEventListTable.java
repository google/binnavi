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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CTableSearcherHelper;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Help.CEventListTableHelp;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTable;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;

/**
 * This table class can be used to display recorded event lists.
 */
public final class CEventListTable extends CFilteredTable<TraceList> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3558144500500581447L;

  /**
   * Provides all traces that are displayed in the table.
   */
  private final ITraceListProvider m_traceProvider;

  /**
   * Creates a new event list table that displays all event lists from the given event list
   * provider.
   * 
   * @param traceProvider Provides all traces that are displayed in the table.
   */
  public CEventListTable(final ITraceListProvider traceProvider) {
    super(new CEventListTableModel(traceProvider), new CEventListTableHelp());

    m_traceProvider =
        Preconditions.checkNotNull(traceProvider, "IE01370: Trace list provider can't be null");

    getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.SEARCH_HK.getKeyStroke(), "SEARCH");
    getActionMap().put("SEARCH", CActionProxy.proxy(new SearchAction()));

    windowImap.put(HotKeys.DELETE_HK.getKeyStroke(), "DELETE");
    getActionMap().put("DELETE", CActionProxy.proxy(new DeleteAction()));
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    getTreeTableModel().delete();
  }

  /**
   * Returns the unsorted row indices of the selected rows.
   * 
   * @return The unsorted row indices of the selected rows.
   */
  public int[] getConvertedSelectedRows() {
    final int[] selectedRows = super.getSelectedRows();

    final int[] ret = new int[selectedRows.length];

    for (int i = 0; i < selectedRows.length; i++) {
      ret[i] = convertRowIndexToModel(selectedRows[i]);
    }

    return ret;
  }

  @Override
  public CEventListTableModel getTreeTableModel() {
    return (CEventListTableModel) super.getTreeTableModel();
  }

  /**
   * Action class used for deleting rows.
   */
  private class DeleteAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 5699451707851809103L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      CTraceFunctions.deleteTrace(SwingUtilities.getWindowAncestor(CEventListTable.this),
          m_traceProvider, getConvertedSelectedRows());
    }
  }

  /**
   * Action class used to search through the table.
   */
  private class SearchAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2696203205349441890L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      CTableSearcherHelper.search(SwingUtilities.getWindowAncestor(CEventListTable.this),
          CEventListTable.this);
    }
  }
}
