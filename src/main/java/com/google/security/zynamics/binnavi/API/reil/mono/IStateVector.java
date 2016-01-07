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
package com.google.security.zynamics.binnavi.API.reil.mono;

// ! Interface for state vectors.
/**
 * Interface that must be implemented by all classes to be used as state vectors in the context of
 * MonoREIL.
 *
 * @param <GraphNode> Type of the nodes in the lattice graph.
 * @param <LatticeElement> Type of the lattice elements.
 */
public interface IStateVector<GraphNode, LatticeElement> extends
    com.google.security.zynamics.reil.algorithms.mono.IStateVector<GraphNode, LatticeElement>, Iterable<GraphNode> {
  // ! Returns the state of a given node.
  /**
   * Returns the state of a given node.
   *
   * @param node The node for which the state is returned.
   *
   * @return The state of a given node.
   */
  @Override
  LatticeElement getState(GraphNode node);

  // ! Checks if the state vector knows the state of a node.
  /**
   * Checks whether the state vector knows the current state of a given node or not.
   *
   * @param node The node to check.
   *
   * @return True, if the state of the node is known. False, otherwise.
   */
  @Override
  boolean hasState(GraphNode node);

  // ! Updates the state of a graph node.
  /**
   * Updates the state of a graph node.
   *
   * @param node The graph node whose state is updated.
   * @param element The new state of the graph node.
   */
  @Override
  void setState(GraphNode node, LatticeElement element);

  // ! Number of entries in the state vector.
  /**
   * Returns the number of entries in the state vector.
   *
   * @return The number of entries in the state vector.
   */
  @Override
  int size();
}
