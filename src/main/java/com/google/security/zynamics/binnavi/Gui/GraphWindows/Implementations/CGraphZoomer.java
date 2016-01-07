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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.SelectedVisibleFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

/**
 * Contains functions for graph zooming operations.
 */
public final class CGraphZoomer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphZoomer() {
  }

  /**
   * Checks whether the graph argument is null and throws if it is.
   *
   * @param graph The graph to check.
   */
  private static void checkArguments(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01762: Graph argument can not be null");
  }

  /**
   * Centers a node in the graph.
   *
   * @param graph The graph where the operation happens.
   * @param node The node to center.
   */
  public static void centerNode(final ZyGraph graph, final NaviNode node) {
    checkArguments(graph);

    MoveFunctions.centerNode(graph, node);
  }

  /**
   * Center and zoom to a node in the graph.
   *
   * @param graph The graph where the operation happens.
   * @param node The node to be centered and zoomed to.
   */
  public static void centerNodeZoomed(final ZyGraph graph, final NaviNode node) {
    checkArguments(graph);

    MoveFunctions.centerNode(graph, node);
    ZoomFunctions.zoomToNode(graph, node);
  }

  /**
   * Zooms into the graph.
   *
   * @param graph The graph to zoom in to.
   */
  public static void zoomIn(final ZyGraph graph) {
    checkArguments(graph);

    graph.zoomIn();
  }

  /**
   * Zooms to a node.
   *
   * @param graph The graph where the zooming operation happens.
   * @param node The node to zoom to.
   */
  public static void zoomNode(final ZyGraph graph, final NaviNode node) {
    checkArguments(graph);

    // Defer more argument checking
    ZoomFunctions.zoomToNode(graph, node);
  }

  /**
   * Zooms to a list of nodes.
   *
   * @param graph The graph where the zooming operation happens.
   * @param nodes The nodes to zoom to.
   */
  public static void zoomNode(final ZyGraph graph, final List<NaviNode> nodes) {
    checkArguments(graph);

    // Defer more argument checking
    ZoomFunctions.zoomToNodes(graph, nodes);
  }

  /**
   * Zooms out of a graph.
   *
   * @param graph The graph to zoom out of.
   */
  public static void zoomOut(final ZyGraph graph) {
    checkArguments(graph);

    graph.zoomOut();
  }

  /**
   * Zooms to the selected nodes of a graph.
   *
   * @param graph The graph that provides the nodes.
   */
  public static void zoomSelected(final ZyGraph graph) {
    checkArguments(graph);

    if (!graph.getSelectedNodes().isEmpty()) {
      ZoomFunctions.zoomToNodes(graph, SelectedVisibleFilter.filter(graph.getSelectedNodes()));
    }
  }

  /**
   * Zooms out to make the whole graph visible.
   *
   * @param graph The graph to be made visible.
   */
  public static void zoomToScreen(final ZyGraph graph) {
    checkArguments(graph);

    ZoomFunctions.zoomToScreen(graph);
  }
}
