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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CDeleteAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CDisableAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CEnableAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CZoomBreakpointAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.Pair;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Used to display a list of all breakpoints in the breakpoint management dialog. Note that the
 * breakpoints displayed in this table can come from more than one debugger. A Debugger column
 * indicates from by debugger the breakpoint is handled.
 */
public final class CBreakpointTable extends JTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7406128305807276782L;

  /**
   * Provides all the debuggers that manage breakpoints which are displayed in the table.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Graph shown in the window of the breakpoint table.
   */
  private final ZyGraph m_graph;

  /**
   * View container of the graph.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Table model of the breakpoint table.
   */
  private final CBreakpointTableModel m_tableModel;

  /**
   * Listener that updates the table when the breakpoints change.
   */
  private final InternalBreakpointManagerListener m_breakpointManagerListener =
      new InternalBreakpointManagerListener();

  /**
   * Listens on changing debuggers.
   */
  private final DebuggerProviderListener m_debuggerListener = new InternalDebuggerListener();

  /**
   * Creates a new breakpoint management table.
   * 
   * @param debuggerProvider Provides all the debuggers that manage breakpoints which are displayed
   *        in the table.
   * @param graph Graph shown in the window of the breakpoint table.
   * @param viewContainer View container of the graph.
   */
  public CBreakpointTable(final BackEndDebuggerProvider debuggerProvider, final ZyGraph graph,
      final IViewContainer viewContainer) {
    m_debuggerProvider =
        Preconditions.checkNotNull(debuggerProvider, "IE01335: Debugger provider can't be null");
    m_graph = Preconditions.checkNotNull(graph, "IE02093: Graph argument can not be null");
    m_viewContainer =
        Preconditions.checkNotNull(viewContainer,
            "IE02099: View container argument can not be null");

    m_tableModel = new CBreakpointTableModel(debuggerProvider);

    setModel(m_tableModel);

    // Since each debugger that is available in the view can be used to set
    // breakpoints, it is necessary to add listeners to the breakpoint
    // managers of all debuggers.
    for (final IDebugger debugger : debuggerProvider.getDebuggers()) {
      addDebuggerListeners(debugger);
    }

    debuggerProvider.addListener(m_debuggerListener);

    getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    // Make sure that the status cells are colored.
    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_STATUS).setCellRenderer(
        new CBreakpointStatusRenderer());

    // Center some fields in the table
    final DefaultTableCellRenderer tcrColumn = new DefaultTableCellRenderer();
    tcrColumn.setHorizontalAlignment(SwingConstants.CENTER);

    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_UNRELOCATED_ADDRESS).setCellRenderer(
        tcrColumn);
    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_RELOCATED_ADDRESS).setCellRenderer(
        tcrColumn);
    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_MODULE_NAME).setCellRenderer(tcrColumn);
    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_DEBUGGER).setCellRenderer(tcrColumn);

    getColumnModel().getColumn(CBreakpointTableModel.COLUMN_CONDITION).setCellEditor(
        new CConditionEditor());

    addMouseListener(new InternalMouseListener());
  }

  /**
   * Initializes all listeners for a debugger.
   * 
   * @param debugger The new debugger.
   */
  private void addDebuggerListeners(final IDebugger debugger) {
    final BreakpointManager manager = debugger.getBreakpointManager();

    manager.addListener(m_breakpointManagerListener);
  }

  /**
   * Removes all listeners from a debugger.
   * 
   * @param debugger The debugger.
   */
  private void removeDebuggerListeners(final IDebugger debugger) {
    final BreakpointManager manager = debugger.getBreakpointManager();

    manager.removeListener(m_breakpointManagerListener);
  }

  /**
   * Displays the popup menu at the location specified by the click event.
   * 
   * @param event The click event.
   */
  private void showPopupMenu(final MouseEvent event) {
    final int row = rowAtPoint(event.getPoint());
    final int column = columnAtPoint(event.getPoint());

    int[] rows = getSelectedRows();

    if ((rows.length == 0) || (rows.length == 1)) {
      changeSelection(row, column, false, false);
      rows = getSelectedRows();
    }

    final JPopupMenu menu = new JPopupMenu();

    menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteAction(m_debuggerProvider, rows))));

    if (CBreakpointFunctions.allDisabled(m_debuggerProvider, rows)) {
      menu.add(new JMenuItem(CActionProxy.proxy(new CEnableAction(m_debuggerProvider, rows))));
    } else if (CBreakpointFunctions.allNotDisabled(m_debuggerProvider, rows)) {
      menu.add(new JMenuItem(CActionProxy.proxy(new CDisableAction(m_debuggerProvider, rows))));
    }

    if (rows.length == 1) {
      menu.addSeparator();

      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, rows[0]);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      final BreakpointAddress address =
          manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress();

      menu.add(new JMenuItem(CActionProxy.proxy(new CZoomBreakpointAction(SwingUtilities
          .windowForComponent(this), m_graph, m_viewContainer, address))));
    }

    menu.show(event.getComponent(), event.getX(), event.getY());
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    for (final IDebugger debugger : m_debuggerProvider.getDebuggers()) {
      removeDebuggerListeners(debugger);
    }

    m_debuggerProvider.removeListener(m_debuggerListener);
  }

  /**
   * Responsible for updating the table if the state of the breakpoint manager changed.
   */
  private class InternalBreakpointManagerListener implements BreakpointManagerListener {
    // Filter to only update the table if there has been at least one regular
    // breakpoint in the set of breakpoints.
    private void handleTableChangeTrigger(final Collection<Breakpoint> breakpoints) {
      boolean needsTableChange = false;
      for (final Breakpoint breakpoint : breakpoints) {
        if (breakpoint.getType() == BreakpointType.REGULAR) {
          needsTableChange = true;
          break;
        }
      }
      if (needsTableChange) {
        m_tableModel.fireTableDataChanged();
      }
    }

    @Override
    public void breakpointsAdded(final List<Breakpoint> breakpoints) {
      handleTableChangeTrigger(breakpoints);
    }

    @Override
    public void breakpointsConditionChanged(final Set<Breakpoint> breakpoints) {
      handleTableChangeTrigger(breakpoints);
    }

    @Override
    public void breakpointsDescriptionChanged(final Set<Breakpoint> breakpoints) {
      handleTableChangeTrigger(breakpoints);
    }

    @Override
    public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
      handleTableChangeTrigger(breakpoints);
    }

    @Override
    public void breakpointsStatusChanged(
        final Map<Breakpoint, BreakpointStatus> breakpointsToStatus, final BreakpointStatus status) {
      handleTableChangeTrigger(breakpointsToStatus.keySet());
    }
  }

  /**
   * Listens on changing debuggers.
   */
  private class InternalDebuggerListener implements DebuggerProviderListener {
    @Override
    public void debuggerAdded(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      addDebuggerListeners(debugger);
    }

    @Override
    public void debuggerRemoved(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      removeDebuggerListeners(debugger);
    }
  }

  /**
   * Responsible for showing the context menu if the user right-clicked.
   */
  private class InternalMouseListener extends MouseAdapter {
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
