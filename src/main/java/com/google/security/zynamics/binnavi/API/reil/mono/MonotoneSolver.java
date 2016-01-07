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

import java.util.List;

import com.google.security.zynamics.binnavi.REIL.mono.apiwrappers.LatticeWrapper;


// ! Main class of MonoREIL.
/**
 * The MonotoneSolver class is the base class of the monotone framework. Given a lattice, an
 * instruction graph, an initial state vector, and some other objects the monotone framework tries
 * to propagate information from node to node of the instruction graph until no further information
 * can be deduced from the current program state and the transforming instructions.
 *
 * @param <GraphNode> Type of the graph nodes in the given graph.
 * @param <Lattice> Type of the lattice the monotone framework is using.
 * @param <LatticeElement> Type of the elements of the lattice.
 * @param <ObjectType> Type of the additional value objects.
 */
public class MonotoneSolver<GraphNode, LatticeElement extends ILatticeElement<LatticeElement>,
    ObjectType, Lattice extends ILattice<LatticeElement, ObjectType>> {
  /**
   * Graph that is being worked on.
   */
  private final ILatticeGraph<GraphNode> graph;

  /**
   * Lattice structure that provides the method to merge states.
   */
  private final Lattice lattice;

  /**
   * Initial state vector.
   */
  private final IStateVector<GraphNode, LatticeElement> state;

  /**
   * Transforms the state vector during each step of the algorithm.
   */
  private final ITransformationProvider<GraphNode, LatticeElement> transformationList;

  /**
   * Specifies how to walk through the graph.
   */
  private final IGraphWalker<GraphNode, ObjectType> walker;

  // ! Creates a new instance of a monotone solver.
  /**
   * Creates a new instance of a monotone solver.
   *
   * @param graph Graph that is being worked on.
   * @param lattice Lattice structure that provides the method to merge states.
   * @param startVector Initial state vector.
   * @param transformationProvider Transforms the state vector during each step of the algorithm.
   * @param walker Specifies how to walk through the graph.
   */
  public MonotoneSolver(final ILatticeGraph<GraphNode> graph, final Lattice lattice,
      final IStateVector<GraphNode, LatticeElement> startVector,
      final ITransformationProvider<GraphNode, LatticeElement> transformationProvider,
      final IGraphWalker<GraphNode, ObjectType> walker) {
    if (graph == null) {
      throw new IllegalArgumentException("Error: Graph argument can not be null");
    }

    if (lattice == null) {
      throw new IllegalArgumentException("Error: Lattice argument can not be null");
    }

    if (startVector == null) {
      throw new IllegalArgumentException("Error: Start vector argument can not be null");
    }

    if (transformationProvider == null) {
      throw new IllegalArgumentException("Error: Transformation list argument can not be null");
    }

    final List<GraphNode> nodes = graph.getNodes();

    if (nodes.size() != startVector.size()) {
      throw new IllegalArgumentException(String.format(
          "Error: Invalid start vector (%d states for %d nodes)", startVector.size(),
          nodes.size()));
    }

    for (final GraphNode node : nodes) {
      if (!startVector.hasState(node)) {
        throw new IllegalArgumentException("Error: Node " + node
            + " does not have a state in the initial state vector");
      }
    }

    this.graph = graph;
    this.lattice = lattice;
    this.state = startVector;
    this.transformationList = transformationProvider;
    this.walker = walker;
  }

  /**
   * Converts an internal state vector object into an API state vector object.
   *
   * @param vector The state vector object to convert.
   *
   * @return The converted state vector object.
   */
  private IStateVector<GraphNode, LatticeElement> convert(
      final com.google.security.zynamics.reil.algorithms.mono.IStateVector<GraphNode, LatticeElement> vector) {
    final DefaultStateVector<GraphNode, LatticeElement> convertedVector =
        new DefaultStateVector<GraphNode, LatticeElement>();

    for (final GraphNode node : graph.getNodes()) {
      convertedVector.setState(node, vector.getState(node));
    }

    return convertedVector;
  }

  // ! Runs the code analysis algorithm.
  /**
   * Run the actual code analysis.
   *
   *  Starting from the initial state vector, iterate the transformation until a fixpoint is
   * reached.
   *
   *  Depending on your analysis, your transformations and the program you are analyzing, it is
   * conceivable that this will iterate for a long while. If your lattice is infinite and not
   * noetherian (e.g. doesn't fulfill the 'all ascending chains stabilize'), this might not
   * terminate.
   *
   * @return The fixpoint state.
   */
  public final IStateVector<GraphNode, LatticeElement> solve() {
    return convert(new com.google.security.zynamics.reil.algorithms.mono.MonotoneSolver<GraphNode, LatticeElement, ObjectType, LatticeWrapper<LatticeElement, ObjectType>>(graph,
        new LatticeWrapper<LatticeElement, ObjectType>(lattice),
        state,
        transformationList,
        walker,
        null).solve());
  }
}
