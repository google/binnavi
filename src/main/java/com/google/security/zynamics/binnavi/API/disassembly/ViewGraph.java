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

import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;

import java.util.List;


// ! Represents the graph of a view.
/**
 * Represents the graph of a view.
 */
public final class ViewGraph extends MutableDirectedGraph<ViewNode, ViewEdge>
    implements IDirectedGraph<ViewNode, ViewEdge> {
  // / @cond INTERNAL
  /**
   * Creates a new view graph object.
   *
   * @param nodes Nodes of the view graph.
   * @param edges Edges of the view graph.
   */
  // / @endcond
  public ViewGraph(final List<ViewNode> nodes, final List<ViewEdge> edges) {
    super(nodes, edges);

    for (final Object block : nodes) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof ViewNode)) {
        throw new IllegalArgumentException(
            "Error: Node list contains objects that are not ViewNode objects");
      }
    }

    for (final Object block : edges) {
      // This check is necessary because of type erasure of generic type parameters.

      if (!(block instanceof ViewEdge)) {
        throw new IllegalArgumentException(
            "Error: Edge list contains objects that are not ViewEdge objects");
      }
    }
  }

  @Override
  public void addEdge(final ViewEdge edge) {
    super.addEdge(edge);
  }

  @Override
  public void addNode(final ViewNode node) {
    super.addNode(node);
  }

  // ! Edges of the graph.
  /**
   * Returns all edges of the graph.
   *
   * @return The edges of the graph.
   */
  @Override
  public List<ViewEdge> getEdges() {
    return super.getEdges();
  }

  // ! Nodes of the graph.
  /**
   * Returns all nodes of the graph.
   *
   * @return The nodes of the graph.
   */
  @Override
  public List<ViewNode> getNodes() {
    return super.getNodes();
  }

  // ! Printable representation of the graph.
  /**
   * Returns the string representation of the graph.
   *
   * @return The string representation of the graph.
   */
  @Override
  public String toString() {
    return String.format("View Graph [%d nodes, %d edges]", nodeCount(), edgeCount());
  }
}
