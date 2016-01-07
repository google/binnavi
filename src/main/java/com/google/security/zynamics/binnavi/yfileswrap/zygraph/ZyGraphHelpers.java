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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

import java.awt.geom.Point2D;

/**
 * Contains a few helper classes for use with ZyGraph objects.
 */
public final class ZyGraphHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyGraphHelpers() {
  }

  /**
   * Zooms and centers a view.
   * 
   * @param graph The graph to zoom and center.
   * @param zoom The new zoom level.
   * @param point2D The new center world coordinate.
   */
  private static void focusView(final ZyGraph graph, final double zoom, final Point2D point2D) {
    graph.getView().focusView(zoom, point2D,
        graph.getSettings().getLayoutSettings().getAnimateLayout());

    graph.updateViews();
  }

  /**
   * Centers an edge label on the screen.
   * 
   * @param graph The graph where the center operation takes place.
   * @param edge The edge that provides the label.
   * @param zoom Flag that indicates whether the graph should be zoomed too.
   */
  public static void centerEdgeLabel(final ZyGraph graph, final NaviEdge edge, final boolean zoom) {
    Preconditions.checkNotNull(graph, "IE02101: Graph argument can not be null");
    Preconditions.checkNotNull(edge, "IE02102: Edge argument can not be null");

    final double oldZoom = graph.getView().getZoom();

    if (!edge.isVisible()) {

      final NaviNode sourceNode = edge.getSource();
      final NaviNode targetNode = edge.getTarget();

      final boolean autoLayout = graph.getSettings().getLayoutSettings().getAutomaticLayouting();
      graph.getSettings().getLayoutSettings().setAutomaticLayouting(false);

      graph.showNode(sourceNode, true);
      graph.showNode(targetNode, true);

      graph.getSettings().getLayoutSettings().setAutomaticLayouting(autoLayout);
    }

    if (zoom) {
      ZoomFunctions.zoomToEdgeLabel(graph, edge);
    } else {
      MoveFunctions.centerEdgeLable(graph, edge);

      focusView(graph, oldZoom, graph.getView().getCenter());
    }
  }

  /**
   * Centers a node on the screen.
   * 
   * @param graph The graph where the center operation takes place.
   * @param node The node to center.
   * @param zoom Flag that indicates whether the graph should be zoomed too.
   */
  public static void centerNode(final ZyGraph graph, final NaviNode node, final boolean zoom) {
    Preconditions.checkNotNull(graph, "IE02103: Graph argument can not be null");
    Preconditions.checkNotNull(node, "IE02104: Node argument can not be null");

    final double oldZoom = graph.getView().getZoom();

    graph.showNode(node, true);

    if (zoom) {
      ZoomFunctions.zoomToNode(graph, node);
    } else {
      MoveFunctions.centerNode(graph, node);

      if (Double.compare(oldZoom, graph.getView().getZoom()) != 0) {
        focusView(graph, oldZoom, graph.getView().getCenter());
      }
    }
  }
}
