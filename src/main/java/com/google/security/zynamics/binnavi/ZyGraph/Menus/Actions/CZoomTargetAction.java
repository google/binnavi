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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphZoomer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used for zooming to the target node of an edge.
 */
public final class CZoomTargetAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5601407116652406618L;

  /**
   * Graph where the zoom operation takes place.
   */
  private final ZyGraph m_graph;

  /**
   * Edge that provides the target node to which the graph is zoomed.
   */
  private final NaviEdge m_edge;

  /**
   * Creates a new action object.
   *
   * @param graph Graph where the zoom operation takes place.
   * @param edge Edge that provides the target node to which the graph is zoomed.
   */
  public CZoomTargetAction(final ZyGraph graph, final NaviEdge edge) {
    super("Zoom to Target Node");

    Preconditions.checkNotNull(graph, "IE00965: Graph argument can't be null");

    Preconditions.checkNotNull(edge, "IE00966: Edge argument can't be null");

    m_graph = graph;
    m_edge = edge;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphZoomer.zoomNode(m_graph, m_edge.getTarget());
  }
}
