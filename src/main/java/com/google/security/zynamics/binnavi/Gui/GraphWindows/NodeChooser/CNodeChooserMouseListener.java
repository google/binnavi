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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeChooser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphZoomer;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeChooser.CNodeChooserTable;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * This class handles clicks on the node chooser table.
 */
public final class CNodeChooserMouseListener extends MouseAdapter {
  /**
   * The parent table of the listener.
   */
  private final CNodeChooserTable m_table;

  /**
   * The graph that provides the table content.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new node chooser mouse listener object.
   *
   * @param table The parent table of the listener.
   * @param graph The graph that provides the table content.
   */
  public CNodeChooserMouseListener(final CNodeChooserTable table, final ZyGraph graph) {
    Preconditions.checkNotNull(table, "IE01766: Table argument can not be null");
    Preconditions.checkNotNull(graph, "IE01767: Graph argument can not be null");

    m_table = table;
    m_graph = graph;
  }

  /**
   * Searches for a graph node backed by a given raw node.
   *
   * @param nodes The graph nodes to search through.
   * @param node The raw node to search for.
   *
   * @return The graph node backed by the raw node.
   */
  @Deprecated
  private static NaviNode searchNode(final List<NaviNode> nodes, final INaviViewNode node) {
    for (final NaviNode n : nodes) {
      if (n.getRawNode() == node) {
        return n;
      }
    }

    return null;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (event.getClickCount() == 1) {
        // Left clicking selects unselected nodes and deselects
        // selected nodes.

        final int nodeIndex = m_table.modelIndex(m_table.getSelectedRow());
        m_graph.makeRawNodeVisibleAndSelect(nodeIndex);
      }
    } else if (event.getButton() == MouseEvent.BUTTON3) {
      // Single right clicking centers the node on the screen
      // Double right clicking fits the node to the screen

      final int row = m_table.modelIndex(m_table.rowAtPoint(event.getPoint()));

      final NaviNode node = m_graph.getRawNodeFromIndex(row);

      if (!node.isVisible()) {
        return;
      }

      if (event.getClickCount() == 1) {
        CGraphZoomer.centerNode(m_graph, node);
      } else {
        CGraphZoomer.centerNodeZoomed(m_graph, node);
      }
    }
  }
}
