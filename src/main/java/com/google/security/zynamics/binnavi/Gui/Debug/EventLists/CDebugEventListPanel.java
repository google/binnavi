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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Help.CTraceEventFilterHelp;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Help.CTraceFilterHelp;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterFieldListener;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilteredTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CTracesFilterCreator;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;

/**
 * Panel class that is used to display recorded traces.
 */
public final class CDebugEventListPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6320890258811231050L;

  /**
   * The individual traces are stored in this table which is located on the left side of the debug
   * event list panel.
   */
  private final CEventListTable m_tracesTable;

  /**
   * Table where the trace events of the currently selected trace are shown.
   */
  private final CEventTable m_traceEventTable;

  /**
   * Table where the register values for individual events are shown.
   */
  private final CEventValueTable m_eventValueTable;

  /**
   * Table model for the register values.
   */
  private final CEventValueTableModel m_eventModel = new CEventValueTableModel();

  /**
   * Panel used for filtering traces.
   */
  private final CTracesPanel m_tracesPanel;

  /**
   * Creates a new debug event list panel where debug events are displayed.
   *
   * @param eventListManager Provides the event lists that are displayed in this panel.
   */
  public CDebugEventListPanel(final ITraceListProvider eventListManager) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        eventListManager, "IE01369: Event list manager argument can't be null");

    // Create the table that shows the recorded event lists.
    m_tracesTable = new CEventListTable(eventListManager);

    // Add a selection listener that updates the right table
    // if the selection in the left table changes.
    m_tracesTable.getSelectionModel().addListSelectionListener(new InternalSelectionListener());

    final JPanel rightPanel = new JPanel(new BorderLayout());

    m_traceEventTable = new CEventTable(new CEventTableModel());
    m_traceEventTable.getSelectionModel()
        .addListSelectionListener(new InternalEventSelectionListener());

    final CTablePanel<ITraceEvent> filteredPanel = new CTraceEventsPanel(m_traceEventTable);

    m_eventValueTable = new CEventValueTable(m_eventModel);
    m_eventValueTable.addMouseListener(new InternalMouseListener());

    final JSplitPane splitPane1 = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true, filteredPanel, new JScrollPane(m_eventValueTable));
    rightPanel.add(splitPane1, BorderLayout.CENTER);
    splitPane1.setResizeWeight(0.5);

    m_tracesPanel = new CTracesPanel(m_tracesTable);

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_tracesPanel, rightPanel);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setResizeWeight(0.5);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_tracesPanel.dispose();
  }

  /**
   * Returns the table where the trace events are shown.
   *
   * @return The table where the trace events are shown.
   */
  public CEventTable getEventsTable() {
    return m_traceEventTable;
  }

  /**
   * Returns the table where the debug traces are shown.
   *
   * @return The table where the debug traces are shown.
   */
  public CEventListTable getTracesTable() {
    return m_tracesTable;
  }

  /**
   * Panel used for filtering traces.
   */
  private static class CTraceEventsPanel extends CTablePanel<ITraceEvent> {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 4765258747952807418L;

    /**
     * Shows a context menu when the trace events filter field is clicked.
     */
    private final InternalTracesTableMouseListener m_internalTracesTableMouseListener =
        new InternalTracesTableMouseListener();

    /**
     * Creates a new panel object.
     *
     * @param table The table shown on the panel.
     */
    public CTraceEventsPanel(final CEventTable table) {
      super(table, new CTraceFilterCreator(), new CTraceEventFilterHelp());

      addListener(m_internalTracesTableMouseListener);
    }

    @Override
    protected void disposeInternal() {
      removeListener(m_internalTracesTableMouseListener);
    }

    /**
     * Shows a context menu when the trace events filter field is clicked.
     */
    private class InternalTracesTableMouseListener implements IFilterFieldListener {
      /**
       * Shows a popup menu depending on a mouse event.
       *
       * @param event The mouse event.
       */
      private void showPopupMenu(final MouseEvent event) {
        final CTraceEventsTableFilterMenu menu = new CTraceEventsTableFilterMenu(getFilterField());

        menu.show(event.getComponent(), event.getX(), event.getY());
      }

      @Override
      public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
          showPopupMenu(event);
        }
      }

      @Override
      public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()) {
          showPopupMenu(event);
        }
      }
    }
  }

  /**
   * Panel where traces can be filtered.
   */
  private static final class CTracesPanel extends CTablePanel<TraceList> {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5321341959824234448L;

    /**
     * Shows a context menu on clicks of the filter field.
     */
    private final InternalTracesTableMouseListener m_internalTracesTableMouseListener =
        new InternalTracesTableMouseListener();

    /**
     * Creates a new panel object.
     *
     * @param table The table to be filtered.
     */
    public CTracesPanel(final IFilteredTable<TraceList> table) {
      super(table, new CTracesFilterCreator(), new CTraceFilterHelp());

      addListener(m_internalTracesTableMouseListener);
    }

    @Override
    protected void disposeInternal() {
      removeListener(m_internalTracesTableMouseListener);
    }

    /**
     * Shows a context menu on clicks of the filter field.
     */
    private class InternalTracesTableMouseListener implements IFilterFieldListener {
      /**
       * Shows a context menu depending on the event.
       *
       * @param event The mouse event.
       */
      private void showPopupMenu(final MouseEvent event) {
        final CTracesTableFilterMenu menu = new CTracesTableFilterMenu(getFilterField());

        menu.show(event.getComponent(), event.getX(), event.getY());
      }

      @Override
      public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
          showPopupMenu(event);
        }
      }

      @Override
      public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()) {
          showPopupMenu(event);
        }
      }
    }
  }

  /**
   * Updates the event value table on changes to the trace event table.
   */
  private class InternalEventSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final int first = m_traceEventTable.getSelectionModel().getMinSelectionIndex();

      if (first == -1) {
        m_eventValueTable.setEvent(null);

        return;
      }

      final boolean single = first == m_traceEventTable.getSelectionModel().getMaxSelectionIndex();

      m_eventValueTable.setEnabled(single);

      // It's only possible to display the events of
      // a single list.
      if (single) {
        final CEventTableModel model = m_traceEventTable.getTreeTableModel();
        m_eventValueTable.setEvent(
            model.getEvents().get(m_traceEventTable.convertRowIndexToModel(first)));
      }
    }
  }

  /**
   * Handles clicks on memory data.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if ((event.getClickCount() == 2) && SwingUtilities.isLeftMouseButton(event)) {
        final Point clickPoint = event.getPoint();
        final int row = m_eventValueTable.rowAtPoint(clickPoint);
        final int column = m_eventValueTable.columnAtPoint(clickPoint);

        if ((row != -1) && (column == CEventValueTableModel.MEMORY_COLUMN)) {
          final byte[] data =
              m_eventModel.getEvent().getRegisterValues().get(row).getMemory();

          CTraceMemoryDialog.show(SwingUtilities.getWindowAncestor(m_eventValueTable), data);
        }
      }
    }
  }

  /**
   * Updates the trace event table when the selected trace is changed.
   */
  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final int first = m_tracesTable.getSelectionModel().getMinSelectionIndex();

      if (first == -1) {
        m_traceEventTable.setEventList(null);

        return;
      }

      final boolean single = first == m_tracesTable.getSelectionModel().getMaxSelectionIndex();

      m_traceEventTable.setEnabled(single);

      // It's only possible to display the events of
      // a single list.
      if (single) {
        final CEventListTableModel model = m_tracesTable.getTreeTableModel();
        m_traceEventTable.setEventList(
            model.getTraces().get(m_tracesTable.convertRowIndexToModel(first)));
      }
    }
  }
}
