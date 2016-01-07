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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left.Standard;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo.CSelectionHistory;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo.CSelectionHistoryChooser;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.CGraphOverview;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeChooser.CNodeChooser;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Panel that is shown on the left side of the graph window.
 */
public final class CStandardLeftPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5140318386690988967L;

  /**
   * Used to select previous selections.
   */
  private final CSelectionHistoryChooser m_undoHistory;

  private CNodeChooser m_nodeChooser;

  /**
   * Creates a new panel object.
   *
   * @param graph Graph that provides the data for the components in this panel.
   * @param selectionHistory Shows the selection history of the given graph.
   * @param searchField Search field that is used to search through the graph.
   */
  public CStandardLeftPanel(final ZyGraph graph, final CSelectionHistory selectionHistory,
      final CGraphSearchField searchField) {
    super(new BorderLayout());

    Preconditions.checkNotNull(searchField, "IE01810: Search field argument can not be null");

    m_undoHistory = new CSelectionHistoryChooser(graph, selectionHistory);

    final JSplitPane bottomSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
        m_nodeChooser = new CNodeChooser(graph, searchField), m_undoHistory);
    final JSplitPane topSplitter = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT, true, new CGraphOverview(graph), bottomSplitter);

    topSplitter.setDividerLocation(200);

    bottomSplitter.setDoubleBuffered(true);
    bottomSplitter.setResizeWeight(0.75);
    bottomSplitter.setOneTouchExpandable(true);
    bottomSplitter.setMinimumSize(new Dimension(0, 0));

    topSplitter.setDoubleBuffered(true);
    topSplitter.setOneTouchExpandable(true);
    topSplitter.setMinimumSize(new Dimension(0, 0));
    topSplitter.setDividerLocation(200);

    add(topSplitter);
  }

  /**
   * Frees allocated resources.
   */
  public void delete() {
    m_nodeChooser.dispose();
    m_undoHistory.dispose();
  }
}
