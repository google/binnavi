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


// / Represents a single callgraph.

/**
 * The Call graph of a module is the graph that shows how the individual functions call each other.
 * The nodes of a Call graph are function block objects which encapsulate functions. The edges are
 * function edges, directed edges that lead from the source of a function call to the called
 * function.
 */
public final class Callgraph extends DirectedGraph<FunctionBlock, FunctionEdge> implements
    IDirectedGraph<FunctionBlock, FunctionEdge> {
  // / @cond INTERNAL
  /**
   * Creates a new API call graph object.
   * 
   * @param nodes Nodes of the call graph.
   * @param edges Edges of the call graph.
   */
  // / @endcond
  public Callgraph(final List<FunctionBlock> nodes, final List<FunctionEdge> edges) {
    super(nodes, edges);

    for (final Object block : nodes) {
      // This check is necessary because of type erasure of generic type parameters
      // when called from Python/Ruby/... scripts.

      if (!(block instanceof FunctionBlock)) {
        throw new IllegalArgumentException(
            "Error: Block list contains objects that are not FunctionBlock objects");
      }
    }

    for (final Object block : edges) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof FunctionEdge)) {
        throw new IllegalArgumentException(
            "Error: Edge list contains objects that are not FunctionEdge objects");
      }
    }
  }

  // ! The edges of the Call graph.
  /**
   * Returns the edges of the callgraph.
   * 
   * @return The edges of the callgraph.
   */
  @Override
  public List<FunctionEdge> getEdges() {
    return super.getEdges();
  }

  // ! The nodes of the Call graph.
  /**
   * Returns the nodes of the Call graph.
   * 
   * @return The nodes of the Call graph.
   */
  @Override
  public List<FunctionBlock> getNodes() {
    return super.getNodes();
  }

  // ! Printable representation of the Call graph.
  /**
   * Returns a string representation of the Call graph.
   * 
   * @return A string representation of the Call graph.
   */
  @Override
  public String toString() {
    return String.format("Callgraph [%d function nodes, %d edges]", nodeCount(), edgeCount());
  }
}
