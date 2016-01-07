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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * Helper class used to highlight edges that belong to a loop.
 */
public final class CLoopHighlighter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CLoopHighlighter() {
  }

  /**
   * Returns the loopy edge type of a given non-loopy edge type.
   * 
   * @param type The non-loopy edge type.
   * 
   * @return The loopy edge type.
   */
  private static EdgeType loopify(final EdgeType type) {
    switch (type) {
      case JUMP_CONDITIONAL_FALSE:
        return EdgeType.JUMP_CONDITIONAL_FALSE_LOOP;
      case JUMP_CONDITIONAL_TRUE:
        return EdgeType.JUMP_CONDITIONAL_TRUE_LOOP;
      case JUMP_UNCONDITIONAL:
        return EdgeType.JUMP_UNCONDITIONAL_LOOP;
      default:
        return type;
    }
  }

  /**
   * Changes the edge type of edges that belong to a loop.
   * 
   * @param graph The graph whose loop edges are highlighted.
   * @param entryNode The entry node of the graph.
   * 
   * @throws MalformedGraphException Thrown if the graph does not have the expected format.
   */
  public static void colorLoops(final IDirectedGraph<INaviViewNode, INaviEdge> graph,
      final INaviViewNode entryNode) throws MalformedGraphException {
    final Collection<INaviEdge> edges = graph.getEdges();

    if (entryNode != null) {
      final HashMap<INaviViewNode, ArrayList<INaviViewNode>> backedges =
          GraphAlgorithms.getBackEdges(graph, entryNode);
      for (final INaviEdge edge : edges) {
        if (edge.getSource() == edge.getTarget()) {
          edge.setEdgeType(loopify(edge.getType()));
        }
        if (backedges.containsKey(edge.getSource())) {
          if (backedges.get(edge.getSource()).contains(edge.getTarget())) {
            edge.setEdgeType(loopify(edge.getType()));
          }
        }
      }
    }
  }
}
