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

// ! Interface for object that transform lattice elements.
/**
 * Must be implemented by objects that want to transform lattice elements.
 *
 * @param <GraphNode> Type of the nodes in the input graph.
 * @param <LatticeElement> Type of the lattice elements.
 */
public interface ITransformationProvider<
    GraphNode, LatticeElement extends ILatticeElement<LatticeElement>>
    extends com.google.security.zynamics.reil.algorithms.mono.ITransformationProvider<GraphNode, LatticeElement> {
  // ! Transforms a lattice element into a new element.
  /**
   * Transforms a lattice element into a new element.
   *
   * @param node The node that controls the transformation.
   * @param currentState The current state of the node.
   * @param inputState The combined state of the influencing nodes.
   *
   * @return The transformed lattice element.
   */
  @Override
  LatticeElement transform(GraphNode node, LatticeElement currentState, LatticeElement inputState);
}
