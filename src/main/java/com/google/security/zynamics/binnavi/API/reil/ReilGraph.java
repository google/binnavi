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
package com.google.security.zynamics.binnavi.API.reil;

import com.google.security.zynamics.binnavi.API.disassembly.IDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.List;



// / REIL code flowgraph
/**
 * Graph of REIL nodes and REIL edges.
 */
public final class ReilGraph extends DirectedGraph<ReilBlock, ReilEdge>
    implements IDirectedGraph<ReilBlock, ReilEdge> {
  // ! Creates a new REIL graph.
  /**
   * Creates a new REIL graph.
   *
   * @param nodes Nodes of the REIL graph.
   * @param edges Edges of the REIL graph.
   */
  public ReilGraph(final List<ReilBlock> nodes, final List<ReilEdge> edges) {
    super(nodes, edges);

    for (final Object block : nodes) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof ReilBlock)) {
        throw new IllegalArgumentException(
            "Error: Block list contains objects that are not ReilBlock objects");
      }
    }

    for (final Object block : edges) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof ReilEdge)) {
        throw new IllegalArgumentException(
            "Error: Edge list contains objects that are not ReilEdge objects");
      }
    }
  }

  // ! The edges of the graph.
  /**
   * Returns the edges of the REIL graph.
   *
   * @return The edges of the REIL graph.
   */
  @Override
  public List<ReilEdge> getEdges() {
    return super.getEdges();
  }

  // ! The nodes of the graph.
  /**
   * Returns the nodes of the REIL graph.
   *
   * @return The nodes of the REIL graph.
   */
  @Override
  public List<ReilBlock> getNodes() {
    return super.getNodes();
  }

  // ! Printable representation of the graph.
  /**
   * Returns a string representation of the REIL graph.
   *
   * @return A string representation of the REIL graph.
   */
  @Override
  public String toString() {
    return String.format("REIL graph [%d function nodes, %d edges]", nodeCount(), edgeCount());
  }
}
