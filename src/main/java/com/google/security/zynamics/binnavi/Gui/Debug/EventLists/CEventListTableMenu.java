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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CActionCombineTraces;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CActionDelete;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CActionDifferenceTraces;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CActionIntersectTraces;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CActionSelect;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CTagEventNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTaggingFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.tables.CTableSearcher;

/**
 * Context menu that is shown in the table where the event lists are shown.
 */
public final class CEventListTableMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 945728009591780085L;

  /**
   * Creates a new menu object.
   *
   * @param parent Parent window used for dialogs.
   * @param table Table for which the menu is created.
   * @param traceProvider Provides the available traces.
   */
  public CEventListTableMenu(
      final JFrame parent, final CEventListTable table, final ITraceListProvider traceProvider) {
    Preconditions.checkNotNull(parent, "IE01289: Parent argument can not be null");
    Preconditions.checkNotNull(table, "IE01290: Table argument can not be null");
    Preconditions.checkNotNull(traceProvider, "IE01291: Trace provider argument can not be null");

    final int[] selectedRows = table.getConvertedSelectedRows();

    add(CActionProxy.proxy(new CActionDelete(parent, traceProvider, selectedRows)));

    addSeparator();

    add(CActionProxy.proxy(new CSearchTableAction(parent, table)));

    new CTableSearcher(parent, "Search", table, 0);

    if (selectedRows.length >= 2) {
      addCombineMenu(parent, traceProvider, selectedRows);
    }
  }

  /**
   * Creates a new context menu object.
   *
   * @param parent Parent window used for dialogs.
   * @param table Table where the context menu is shown.
   * @param graph Graph that is shown in the window the table belongs to.
   * @param traceProvider Trace provider that provides the trace information shown in the table.
   * @param tree The tags tree that provides the node tags.
   */
  public CEventListTableMenu(final JFrame parent, final CEventListTable table, final ZyGraph graph,
      final ITraceListProvider traceProvider, final CTagsTree tree) {
    Preconditions.checkNotNull(parent, "IE01371: Parent argument can not be null");
    Preconditions.checkNotNull(table, "IE01372: Table argument can not be null");
    Preconditions.checkNotNull(graph, "IE01373: Graph argument can not be null");
    Preconditions.checkNotNull(traceProvider, "IE01374: Trace provider argument can not be null");

    final int[] selectedRows = table.getConvertedSelectedRows();

    if (selectedRows.length == 1) {
      final TraceList entry = traceProvider.getList(selectedRows[0]);

      add(CActionProxy.proxy(new CActionSelect(graph, entry)));
    }

    add(CActionProxy.proxy(new CActionDelete(parent, traceProvider, selectedRows)));

    addSeparator();

    add(CActionProxy.proxy(new CSearchTableAction(parent, table)));

    new CTableSearcher(parent, "Search", table, 0);

    if (selectedRows.length == 1) {
      try {
        final CTag selectedTag = CTaggingFunctions.getSelectedTag(tree);

        addSeparator();

        final TraceList entry = traceProvider.getList(selectedRows[0]);

        add(CActionProxy.proxy(new CTagEventNodes(parent, graph, entry, selectedTag)));
      } catch (final MaybeNullException exception) {
        // If there is no selected tag, then don't offer this menu.
      }
    } else {
      addCombineMenu(parent, traceProvider, selectedRows);
    }
  }

  /**
   * Adds the sub menu that is makes it possible to combine multiple traces into one trace.
   *
   * @param parent Parent window used for dialogs.
   * @param traceProvider Trace provider that provides the trace information shown in the table.
   * @param selectedRows Table row indices of the selected traces.
   */
  private void addCombineMenu(
      final JFrame parent, final ITraceListProvider traceProvider, final int[] selectedRows) {
    addSeparator();

    final JMenu combineMenu = new JMenu("Combine Traces");

    final List<TraceList> traces = getTraces(traceProvider, selectedRows);

    combineMenu.add(CActionProxy.proxy(new CActionCombineTraces(parent, traceProvider, traces)));
    combineMenu.add(CActionProxy.proxy(new CActionIntersectTraces(parent, traceProvider, traces)));

    if (traces.size() == 2) {
      combineMenu.addSeparator();

      combineMenu.add(CActionProxy.proxy(
          new CActionDifferenceTraces(parent, traceProvider, traces.get(0), traces.get(1))));
      combineMenu.add(CActionProxy.proxy(
          new CActionDifferenceTraces(parent, traceProvider, traces.get(1), traces.get(0))));
    }

    add(combineMenu);
  }

  /**
   * Returns the traces that belong to a given array of indices.
   *
   * @param traceProvider Trace provider that provides the trace information shown in the table.
   * @param indices Table row indices of the selected traces.
   *
   * @return The traces that correspond to the indices.
   */
  private List<TraceList> getTraces(final ITraceListProvider traceProvider, final int[] indices) {
    final List<TraceList> traces = new ArrayList<TraceList>();

    for (final int index : indices) {
      traces.add(traceProvider.getList(index));
    }

    return traces;
  }
}
