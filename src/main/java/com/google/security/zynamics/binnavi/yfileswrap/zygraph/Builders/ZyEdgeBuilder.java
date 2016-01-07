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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders;

import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.CEdgeUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;
import y.view.Graph2D;

/**
 * Used to build the edges that are displayed in ZyGraphs.
 */
public final class ZyEdgeBuilder {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyEdgeBuilder() {}

  /**
   * Builds the content of a graph edge label.
   *
   * @param edge The edge whose label content is built.
   *
   * @return The created edge label content.
   */
  public static ZyLabelContent buildContent(final INaviEdge edge) {
    final ZyLabelContent content = new ZyLabelContent(null);
    ZyNodeBuilder.addCommentLines(content, edge, edge.getLocalComment(), edge.getGlobalComment());
    return content;
  }

  /**
   * Creates a graph node from a raw edge.
   *
   * @param edge The raw edge that provides the underlying data.
   * @param sourceNode Source node of the edge.
   * @param targetNode Target node of the edge.
   * @param graph2D The graph object where the edge is created.
   * @param adjustColors Flag that indicates whether the initial color of all edges should be
   *        recalculated according to their type.
   *
   * @return The created YNode/NaviNode pair.
   */
  public static Pair<Edge, NaviEdge> convertEdge(final INaviEdge edge, final NaviNode sourceNode,
      final NaviNode targetNode, final Graph2D graph2D, final boolean adjustColors) {
    // Build the edge label if necessary
    final ZyLabelContent content = ZyEdgeBuilder.buildContent(edge);

    // Create the edge realizer of the new edge
    final ZyEdgeRealizer<NaviEdge> realizer =
        new ZyEdgeRealizer<NaviEdge>(content, new CEdgeUpdater(edge));

    // Create the edge
    final Edge g2dEdge = graph2D.createEdge(sourceNode.getNode(), targetNode.getNode(), realizer);

    // ATTENTION: If you change the switch below, you also have to update
    // a comparable switch in ZyGraph.

    if (adjustColors) {
      EdgeInitializer.adjustColor(edge);
    }

    EdgeInitializer.initializeEdgeType(edge, realizer);

    graph2D.getRealizer(g2dEdge).setLineColor(edge.getColor());

    // Associate user data with the edge
    final NaviEdge zyEdge = new NaviEdge(sourceNode, targetNode, g2dEdge, realizer, edge);
    NaviNode.link(sourceNode, targetNode);

    final ZyEdgeData<NaviEdge> data = new ZyEdgeData<NaviEdge>(zyEdge);
    realizer.setUserData(data);

    return new Pair<Edge, NaviEdge>(g2dEdge, zyEdge);
  }
}
