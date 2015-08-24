/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CDebugEventListPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CEventListTableMenu;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CEventTableMenu;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component.Help.CTracesViewsTableHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Help.CViewFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CViewFilterCreator;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Component that is shown on the right side of the main window when a Debug Traces node is
 * selected.
 */
public final class CTracesNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2169183737568464057L;

  /**
   * Provides the trace information shown in the component.
   */
  private final IViewContainer m_container;

  /**
   * Panel where the trace information is shown.
   */
  private final CDebugEventListPanel m_tracesPanel;

  /**
   * Model of the table that shows what views are relevant for a trace.
   */
  private final CArbitraryViewsModel m_model = new CArbitraryViewsModel();

  /**
   * Listens on changes in the trace list.
   */
  private final ListSelectionListener m_listener = new InternalSelectionListener();

  /**
   * Creates a new traces node component.
   *
   * @param projectTree Project tree of the main window.
   * @param container Provides the trace information shown in the component.
   */
  public CTracesNodeComponent(final JTree projectTree, final IViewContainer container) {
    super(new BorderLayout());
    Preconditions.checkNotNull(projectTree, "IE02007: Project tree argument can not be null");
    m_container = Preconditions.checkNotNull(container, "IE02008: Container argument can not be null");

    setBorder(new TitledBorder("Debug Traces"));

    m_tracesPanel = new CDebugEventListPanel(m_container.getTraceProvider());

    final CArbitraryViewsTable table =
        new CArbitraryViewsTable(projectTree, m_model, container, new CTracesViewsTableHelp());

    final JPanel lowerPanel =
        new CTablePanel<INaviView>(table, new CViewFilterCreator(container), new CViewFilterHelp());
    lowerPanel.setBorder(new TitledBorder("Views"));

    lowerPanel.add(new JScrollPane(table));

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_tracesPanel, lowerPanel);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setResizeWeight(0.5);

    m_tracesPanel.getTracesTable().getSelectionModel().addListSelectionListener(m_listener);
    m_tracesPanel.getTracesTable().addMouseListener(new InternalTraceTableListener());

    m_tracesPanel.getEventsTable().addMouseListener(new InternalEventsTableListener());
  }

  /**
   * Shows the views that belong to a trace in the table in the lower half of the component.
   *
   * @param trace The trace list.
   *
   * @throws CouldntLoadDataException
   */
  private void showRelevantViews(final TraceList trace) throws CouldntLoadDataException {
    final IFilledList<UnrelocatedAddress> addresses = new FilledList<UnrelocatedAddress>();

    for (final ITraceEvent traceEvent : trace) {
      addresses.add(traceEvent.getOffset().getAddress());
    }

    final List<INaviView> views = m_container.getViewsWithAddresses(addresses, false);

    if (m_container instanceof CProjectContainer) {
      for (final INaviModule module : m_container.getModules()) {
        if (module.isLoaded()) {
          views.addAll(module.getViewsWithAddresses(addresses, false));
        }
      }
    }

    m_model.setViews(views);
  }

  @Override
  public void dispose() {
    m_tracesPanel.dispose();
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
        events.add(m_tracesPanel.getEventsTable().getTreeTableModel().getEvents().get(row));
      }

      return events;
    }

    /**
     * Shows a popup menu for a given mouse event.
     *
     * @param event The mouse event that triggered the popup menu.
     */
    private void showPopupMenu(final MouseEvent event) {
      final int[] rows = m_tracesPanel.getEventsTable().getConvertedSelectedRows();

      final List<ITraceEvent> traces = getTraces(rows);

      final CEventTableMenu menu =
          new CEventTableMenu(m_tracesPanel.getEventsTable(), traces);

      menu.show(m_tracesPanel.getEventsTable(), event.getX(), event.getY());
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
   * Listens on changes in the trace list.
   */
  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final int first = m_tracesPanel.getTracesTable().getSelectionModel().getMinSelectionIndex();

      if (first == -1) {
        m_model.setViews(new FilledList<INaviView>());

        return;
      }

      final boolean single =
          first == m_tracesPanel.getTracesTable().getSelectionModel().getMaxSelectionIndex();

      if (single) {
        final JTable table = m_tracesPanel.getTracesTable();
        final TraceList list =
            m_tracesPanel.getTracesTable().getTreeTableModel().getTraces()
                .get(table.convertRowIndexToModel(first));

        final LoadRelevantViewsThread thread = new LoadRelevantViewsThread(list);

        CProgressDialog.showEndless(SwingUtilities.getWindowAncestor(getParent()),
            "Loading views that belong to the selected trace", thread);

        if (thread.getException() != null) {
          final String innerMessage = "E00042: " + "Could not load relevant views";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  String.format("BinNavi could not load the views that belong to the trace '%s'.",
                      list.getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The views that belong to the trace can not be shown."});

          NaviErrorDialog.show(SwingUtilities.getWindowAncestor(getParent()), innerMessage,
              innerDescription, thread.getException());
        }
      }
    }
  }

  /**
   * Handles mouse-clicks on the table.
   */
  private class InternalTraceTableListener extends MouseAdapter {
    /**
     * Shows a popup menu for a given mouse event.
     *
     * @param event The event that triggered the popup menu.
     */
    private void showPopupMenu(final MouseEvent event) {
      final JTable traceTable = m_tracesPanel.getTracesTable();
      final int mouseRow = traceTable.rowAtPoint(event.getPoint());
      if (mouseRow != -1) {
        final int[] rows = traceTable.getSelectedRows();
        if (Ints.asList(rows).indexOf(mouseRow) != -1) {
          traceTable.setRowSelectionInterval(mouseRow, mouseRow);
        }
      }

      // Make sure at least one row is selected
      final int minIndex =
          m_tracesPanel.getTracesTable().getSelectionModel().getMinSelectionIndex();
      if (minIndex != -1) {
        final JPopupMenu popupMenu =
            new CEventListTableMenu(
                (JFrame) SwingUtilities.getWindowAncestor(CTracesNodeComponent.this),
                m_tracesPanel.getTracesTable(), m_container.getTraceProvider());
        popupMenu.show(m_tracesPanel.getTracesTable(), event.getX(), event.getY());
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
   * Background thread for loading views while a progress dialog is active.
   */
  private class LoadRelevantViewsThread extends CEndlessHelperThread {
    /**
     * Trace list for which the relevant views are loaded.
     */
    private final TraceList m_list;

    /**
     * Creates a new thread object.
     *
     * @param list Trace list for which the relevant views are loaded.
     */
    public LoadRelevantViewsThread(final TraceList list) {
      m_list = list;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      showRelevantViews(m_list);
    }

    @Override
    public void closeRequested() {
      finish();
    }
  }
}
