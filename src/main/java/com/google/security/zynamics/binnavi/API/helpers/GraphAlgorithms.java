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
package com.google.security.zynamics.binnavi.API.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.security.zynamics.binnavi.API.disassembly.IDirectedGraph;
import com.google.security.zynamics.binnavi.API.disassembly.IGraphNode;
import com.google.security.zynamics.binnavi.API.disassembly.ITreeNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.algorithms.LengauerTarjan;



// / Standard graph algorithms.
/**
 * Provides a few standard graph algorithms that might be useful when working with graphs.
 */
public final class GraphAlgorithms {
  /**
   * Do no create objects of this class.
   */
  private GraphAlgorithms() {
    // You are not supposed to instantiate this class.
  }

  // ! Calculates the back edges of the current graph.
  /**
   * Calculates the back edges of the current graph.
   * 
   * @param graph The input graph.
   * @param rootNode The root node of the graph.
   * @return A HashMap which contains the relation of nodes and their respective back edges.
   * @throws MalformedGraphException Thrown if the graph has more then one entry node.
   */
  public static <T extends IGraphNode<T>> HashMap<T, ArrayList<T>> getBackEdges(
      final IDirectedGraph<T, ?> graph, final T rootNode) throws MalformedGraphException {
    try {
      return com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms.getBackEdges(graph,
          rootNode);
    } catch (final com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException e) {
      throw new MalformedGraphException(e);
    }
  }

  /**
   * 
   * @param graph The input graph.
   * @param rootNode The root node of the graph.
   * @param mapping Output parameter which will contain the mapping between API object and internal
   *        TreeType.
   * @return The generated dominator tree.
   * @throws MalformedGraphException
   */
  public static <T extends IGraphNode<T>> Tree<T> getDominatorTree(
      final IDirectedGraph<T, ?> graph, final T rootNode, final HashMap<T, ITreeNode<T>> mapping)
      throws MalformedGraphException {
    try {
      final Pair<com.google.security.zynamics.zylib.types.trees.Tree<T>, HashMap<T, com.google.security.zynamics.zylib.types.trees.ITreeNode<T>>> resultPair =
          LengauerTarjan.calculate(graph, rootNode);
      if (mapping != null) {
        for (final T t : resultPair.second().keySet()) {
          mapping.put(t, (ITreeNode<T>) resultPair.second().get(t));
        }
      }
      return new Tree<T>(resultPair.first());
    } catch (final com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException e) {
      throw new MalformedGraphException(e);
    }
  }

  // ! Calculates the loops contained in a graph.
  /**
   * Calculates the loops contained in a graph.
   * 
   * @param graph the input graph
   * @return A List of Sets where each set contains the nodes of one loop.
   * @throws MalformedGraphException Thrown if the graph has more than one entry node.
   */
  public static <T extends IGraphNode<T>> ArrayList<Set<T>> getGraphLoops(
      final IDirectedGraph<T, ?> graph) throws MalformedGraphException {
    try {
      return com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms.getGraphLoops(graph);
    } catch (final com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException e) {
      throw new MalformedGraphException(e);
    }
  }

  // ! Gets the nodes of a loop
  /**
   * Gets the nodes of a loop
   * 
   * @param srcNode The source node of the loop where the back edge originates from.
   * @param dstNode The destination node of the loop where the back edge points to.
   * @return The Set of nodes which belong to the loop.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getLoopNodes(
      final NodeType srcNode, final NodeType dstNode) {
    return new HashSet<NodeType>(
        com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms.getLoopNodes(srcNode,
            dstNode));
  }

  // ! Returns the predecessors of a graph node.
  /**
   * Returns all predecessors of a graph node.
   * 
   * @param node The node which is the starting point of the predecessor search.
   * 
   * @return The predecessors of the node in the graph.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getPredecessors(
      final NodeType node) {
    return new HashSet<NodeType>(
        com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms.getPredecessors(node));
  }

  // ! Returns the successors of a graph node.
  /**
   * Returns all successors of a graph node.
   * 
   * @param node The node which is the starting point of the successor search.
   * 
   * @return The successors of the node in the graph.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getSuccessors(
      final NodeType node) {
    return new HashSet<NodeType>(
        com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms.getSuccessors(node));
  }
}
