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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.List;

import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;


// / Represents a single flowgraph.
/**
 * Flow graph objects describe the control flow inside a function. The nodes of a Flow graph are
 * {@link BasicBlock} objects, the edges between the {@link BasicBlock} nodes are {@link BlockEdge}
 * objects.
 */
public final class FlowGraph extends DirectedGraph<BasicBlock, BlockEdge> implements
    IDirectedGraph<BasicBlock, BlockEdge> {
  // / @cond INTERNAL
  /**
   * Creates a new flow graph object.
   * 
   * @param nodes The nodes of the flow graph.
   * @param edges The edges of the flow graph.
   */
  // / @endcond
  public FlowGraph(final List<BasicBlock> nodes, final List<BlockEdge> edges) {
    super(nodes, edges);

    for (final Object block : nodes) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof BasicBlock)) {
        throw new IllegalArgumentException(
            "Error: Block list contains objects that are not BasicBlock objects");
      }
    }

    for (final Object block : edges) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof BlockEdge)) {
        throw new IllegalArgumentException(
            "Error: Edge list contains objects that are not BlockEdge objects");
      }
    }
  }

  // ! The edges of the flowgraph.
  /**
   * Returns all edges that are part of this flowgraph.
   * 
   * @return A list of edges.
   */
  @Override
  public List<BlockEdge> getEdges() {
    return super.getEdges();
  }

  // ! The nodes of the flowgraph.
  /**
   * Returns all nodes that are part of this flowgraph.
   * 
   * @return A list of nodes.
   */
  @Override
  public List<BasicBlock> getNodes() {
    return super.getNodes();
  }

  // ! Printable representation of the flowgraph.
  /**
   * Returns a string representation of the flowgraph.
   * 
   * @return A string representation of the flowgraph.
   */
  @Override
  public String toString() {
    return String.format("Flow graph [%d nodes, %d edges]", nodeCount(), edgeCount());
  }
}
