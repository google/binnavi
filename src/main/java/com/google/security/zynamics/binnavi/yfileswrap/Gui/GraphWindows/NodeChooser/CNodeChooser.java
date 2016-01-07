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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeChooser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * The node chooser panel is used to display the nodes of a graph. Furthermore the user can select
 * nodes here.
 */
public final class CNodeChooser extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8233404984719936506L;

  private final CNodeChooserTable m_table;

  /**
   * Creates a new node chooser object.
   *
   * @param graph The graph that contains the nodes that can be chosen.
   * @param searchField The search field whose results are considered when drawing the node chooser.
   */
  public CNodeChooser(final ZyGraph graph, final CGraphSearchField searchField) {
    super(new BorderLayout());

    Preconditions.checkNotNull(graph, "IE01764: Graph argument can not be null");

    setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Graph Nodes"));

    setDoubleBuffered(true);
    setMinimumSize(new Dimension(0, 0)); // required to restore collapsed split panes later

    m_table = new CNodeChooserTable(graph, searchField);

    final JScrollPane scrollPane = new JScrollPane(m_table);

    add(scrollPane, BorderLayout.CENTER);
  }

  public void dispose() {
    m_table.dispose();
  }
}
