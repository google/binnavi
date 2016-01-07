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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.EdgeHidingMode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Contains helper functions for edge drawing.
 */
public final class CEdgeDrawingFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CEdgeDrawingFunctions() {
  }

  /**
   * Calculates whether edges are drawn in sloppy mode or normal node.
   *
   * @param graph The graph for which the result is calculated.
   *
   * @return True, if edges are drawn in sloppy mode.
   */
  public static boolean calcDrawSloppyEdges(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE02113: Graph argument can not be null");

    final EdgeHidingMode mode = graph.getSettings().getEdgeSettings().getEdgeHidingMode();

    return (mode == EdgeHidingMode.HIDE_NEVER) || ((mode == EdgeHidingMode.HIDE_ON_THRESHOLD) && (
        graph.getSettings().getEdgeSettings().getEdgeHidingThreshold()
        > graph.getEdgeCount()));
  }

  /**
   * Initializes all edges of a graph with their desired drawing mode.
   *
   * @param graph The graph whose edges are initialized.
   */
  public static void initializeEdgeDrawingMode(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE02114: Graph argument can not be null");

    final boolean drawSloppyEdges = CEdgeDrawingFunctions.calcDrawSloppyEdges(graph);

    graph.iterateEdges(new IEdgeCallback<NaviEdge>() {
      @Override
      public IterationMode nextEdge(final NaviEdge edge) {
        edge.initializeDrawingMode(graph, drawSloppyEdges);
        return IterationMode.CONTINUE;
      }
    });
  }
}
