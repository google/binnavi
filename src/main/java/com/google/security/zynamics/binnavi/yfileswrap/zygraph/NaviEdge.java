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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph;

import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

import java.awt.Color;

/**
 * Edge class that can be used for all edges in ZyGraph objects.
 */
public final class NaviEdge extends ZyGraphEdge<NaviNode, NaviEdge, INaviEdge> {
  /**
   * Creates a new edge object.
   *
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   * @param edge The yFiles edge that is used to display the edge in a Graph2D.
   * @param realizer Edge realizer of the edge.
   * @param rawEdge The raw edge that provides the raw data for the edge.
   */
  public NaviEdge(final NaviNode source, final NaviNode target, final Edge edge,
      final ZyEdgeRealizer<NaviEdge> realizer, final INaviEdge rawEdge) {
    super(source, target, edge, realizer, rawEdge);
  }

  public ZyLabelContent getLabelContent() {
    return getRealizer().getEdgeLabelContent();
  }

  public void initializeDrawingMode(ZyGraph graph, boolean drawSloppyEdges) {
    final ZyEdgeRealizer<NaviEdge> realizer = getRealizer();

    realizer.setDrawBends(graph.getSettings().getEdgeSettings().getDrawSelectedBends());
    realizer.setDrawSloppyEdges(drawSloppyEdges);
  }

  public void setDrawSloppyEdges(boolean draw) {
    getRealizer().setDrawSloppyEdges(draw);
  }

  public Color getRealizerLineColor() {
    return getRealizer().getLineColor();
  }

  public int getRealizerLabelCount() {
    return getRealizer().labelCount();
  }
}
