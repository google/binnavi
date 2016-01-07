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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CNodeDeleter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class for deleting the selected nodes of a graph and to connect their parent nodes with
 * their child nodes..
 */
public final class CActionDeleteSelectedNodesKeep extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 164849002308832548L;

  /**
   * Graph whose selected nodes are deleted.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param graph Graph whose selected nodes are deleted.
   */
  public CActionDeleteSelectedNodesKeep(final ZyGraph graph) {
    super("Delete Selected Nodes (Keep Edges)");

    m_graph = Preconditions.checkNotNull(graph, "IE02817: graph argument can not be null");

    putValue(SMALL_ICON,
        new ImageIcon(CMain.class.getResource("data/deleteselectednodeskeepedges_up.png")));
    putValue(SHORT_DESCRIPTION,
        HotKeys.GRAPH_DELETE_SELECTED_NODES_KEEP_EDGES_HK.getDescription());
    putValue(ACCELERATOR_KEY,
        HotKeys.GRAPH_DELETE_SELECTED_NODES_KEEP_EDGES_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CNodeDeleter.removeSelectedNodesKeepEdges(m_graph);
  }
}
