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
package com.google.security.zynamics.binnavi.ZyGraph;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CGraphFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.settings.CProximitySettingsAdapter;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;

/**
 * Default proximity browsing updater class that is used to make nodes visible after relevant
 * proximity browsing events.
 */
public final class CDefaultProximityUpdater extends ProximityUpdater<NaviNode> {
  /**
   * Parent window used to show graph size warning dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph whose nodes are made visible.
   */
  private final ZyGraph m_graph;

  /**
   * Keeps track of relevant changes in the graph settings.
   */
  private final IProximitySettingsListener m_proximityListener = new InternalProximityListener();

  /**
   * Creates a new proximity updater.
   *
   * @param parent Parent window used to show graph size warning dialogs.
   * @param graph Graph whose nodes are made visible.
   */
  public CDefaultProximityUpdater(final JFrame parent, final ZyGraph graph) {
    super(graph);

    m_parent = Preconditions.checkNotNull(parent, "IE02100: Parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE02362: graph argument can not be null");

    m_graph.getSettings().getProximitySettings().addListener(m_proximityListener);
  }

  @Override
  protected void showNodes(
      final Collection<NaviNode> selectedNodes, final Collection<NaviNode> unselectedNodes) {
    CGraphFunctions.showNodes(m_parent, m_graph, selectedNodes, unselectedNodes);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graph.getSettings().getProximitySettings().removeListener(m_proximityListener);
  }

  /**
   * Keeps track of relevant changes in the graph settings.
   */
  private class InternalProximityListener extends CProximitySettingsAdapter {
    /**
     * Shows all selected nodes of the graph while hiding all other nodes.
     */
    private void showSelectedNodes() {
      final Set<NaviNode> selectedNodes = m_graph.getSelectedNodes();

      final List<NaviNode> allNodes = GraphHelpers.getNodes(m_graph);
      allNodes.removeAll(selectedNodes);

      showNodes(selectedNodes, allNodes);
    }

    @Override
    public void changedProximityBrowsingFrozen(final boolean frozen) {
      if (m_graph.getSettings().getProximitySettings().getProximityBrowsing() && !frozen) {
        // When frozen mode is turned off, all selected nodes must become visible.

        showSelectedNodes();
      }
    }
  }
}
