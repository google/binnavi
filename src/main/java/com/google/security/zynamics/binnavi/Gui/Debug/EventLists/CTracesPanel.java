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

import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.tables.TableHelpers;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Panel where the traces tables are shown.
 */
public final class CTracesPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7222540107360547882L;

  /**
   * Provides the active debugger.
   */
  private final CDebugPerspectiveModel m_model;

  /**
   * Graph to trace.
   */
  private final ZyGraph m_graph;

  /**
   * Provides the previously recorded traces.
   */
  private final ITraceListProvider m_traceProvider;

  /**
   * Panel for which the traces panel is shown.
   */
  private final CGraphPanel m_graphPanel;

  /**
   * Panel where the traces tables are shown.
   */
  private final CDebugEventListPanel m_innerPanel;

  /**
   * Table that shows debug traces.
   */
  private final CEventListTable m_traceTable;

  /**
   * Table that shows debug trace events.
   */
  private final CEventTable m_eventTable;

  /**
   * Creates a new panel object.
   *
   * @param model Provides the active debugger.
   * @param graph Graph to trace.
   * @param traceProvider Provides the previously recorded traces.
   * @param graphPanel Panel for which the traces panel is shown.
   */
  public CTracesPanel(final CDebugPerspectiveModel model, final ZyGraph graph,
      final ITraceListProvider traceProvider, final CGraphPanel graphPanel) {
    super(new BorderLayout());

    m_model = model;
    m_graph = graph;
    m_traceProvider = traceProvider;
    m_graphPanel = graphPanel;

    m_innerPanel = new CDebugEventListPanel(traceProvider);

    add(m_innerPanel);

    m_traceTable = m_innerPanel.getTracesTable();
    m_eventTable = m_innerPanel.getEventsTable();

    m_traceTable.addMouseListener(new InternalTraceTableListener());
    m_eventTable.addMouseListener(new InternalEventsTableListener());
  }

  @Override
  public void dispose() {
    m_innerPanel.dispose();
  }

  @Override
  public String getTitle() {
    return "Traces";
  }

  /**
   * Mouse handler for the debug trace events table.
   */
  private class InternalEventsTableListener extends MouseAdapter {
    /**
     * Converts row indices into trace objects.
     *
     * @param rows The row indices to convert.
     *
     * @return The corresponding trace object.
     */
    private List<ITraceEvent> getTraces(final int[] rows) {
      final List<ITraceEvent> events = new ArrayList<ITraceEvent>();

      for (final int row : rows) {
        events.add(m_eventTable.getTreeTableModel().getEvents().get(row));
      }

      return events;
    }

    /**
     * Shows a popup menu for a given mouse event.
     *
     * @param event The mouse event that triggered the popup menu.
     */
    private void showPopupMenu(final MouseEvent event) {
      final int[] rows = m_eventTable.getConvertedSelectedRows();

      final List<ITraceEvent> traces = getTraces(rows);

      final CEventTableMenu menu = new CEventTableMenu(m_eventTable, m_model, traces);

      menu.show(m_eventTable, event.getX(), event.getY());
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
        TableHelpers.selectClickedRow(m_eventTable, event);

        final int[] rows = m_eventTable.getConvertedSelectedRows();

        if (rows.length == 1) {
          final ITraceEvent debugEvent = m_eventTable.getTreeTableModel().getEvents().get(rows[0]);

          final BreakpointAddress address = debugEvent.getOffset();

          ZyZoomHelpers.zoomToAddress(
              m_graph, address.getAddress().getAddress(), address.getModule(), true);
        }
      }
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

  /**
   * Handles mouse-clicks on the table.
   */
  private class InternalTraceTableListener extends MouseAdapter {
    /**
     * Shows a popup menu for a mouse event.
     *
     * @param event The mouse event that triggered the popup menu.
     */
    private void showPopupMenu(final MouseEvent event) {
      final int mouseRow = m_traceTable.rowAtPoint(event.getPoint());
      if (mouseRow != -1) {
        final int[] rows = m_traceTable.getSelectedRows();
        if (Ints.asList(rows).indexOf(mouseRow) != -1) {
          m_traceTable.setRowSelectionInterval(mouseRow, mouseRow);
        }
      }

      // Make sure at least one row is selected
      final int minIndex = m_traceTable.getSelectionModel().getMinSelectionIndex();
      if (minIndex != -1) {
        final CTagsTree tagsTree = m_graphPanel.getTagsTree();

        final JPopupMenu popupMenu = new CEventListTableMenu(
            (JFrame) SwingUtilities.getWindowAncestor(CTracesPanel.this), m_traceTable, m_graph,
            m_traceProvider, tagsTree);
        popupMenu.show(m_traceTable, event.getX(), event.getY());
      }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
        // Double-Click == Select nodes where trace events happened
        TableHelpers.selectClickedRow(m_traceTable, event);
        final int[] rows = m_traceTable.getConvertedSelectedRows();
        if (rows.length == 1) {
          CTraceFunctions.selectList(m_graph, m_traceProvider.getList(rows[0]));
        }
      }
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
