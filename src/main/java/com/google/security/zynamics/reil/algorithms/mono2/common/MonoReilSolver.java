/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.algorithms.mono2.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraph;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILattice;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILatticeElement;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.IMonoReilSolver;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ITransformationProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The solver operates on a REIL instruction graph and is parameterized with a lattice element. The
 * lattice element values are propagated either upwards or downwards with regards to the graph
 * edges. The iteration stops once a fixed point is reached.
 *
 * @param <LatticeElementType> The lattice element type used in the graph.
 */
public class MonoReilSolver<LatticeElementType extends ILatticeElement<LatticeElementType>>
    implements IMonoReilSolver<LatticeElementType> {
  /**
   * Associates a lattice element with each graph edge.
   */
  private final Map<IInstructionGraphEdge, LatticeElementType> m_stateMap =
      new HashMap<IInstructionGraphEdge, LatticeElementType>();

  /**
   * Determines the order in which nodes are traversed during fixed point iteration.
   */
  private final MinMaxPriorityQueue<CComparableInstructionGraphNode> m_workList;

  private final AnalysisDirection m_direction;
  private final ILattice<LatticeElementType> m_lattice;
  private final IInstructionGraph m_graph;

  /**
   * The set of edges which have already been visited during iteration. When generating the final
   * result we skip edges which have not been visited in order to omit artificially inserted entry
   * and exit edges.
   */
  private final Set<IInstructionGraphEdge> m_traversedEdges = new HashSet<>();

  public MonoReilSolver(final IInstructionGraph instructionGraph,
      final AnalysisDirection analysisDirection, final ILattice<LatticeElementType> lattice) {
    m_graph = Preconditions.checkNotNull(instructionGraph,
        "Error: instruction graph argument can not be null");
    m_direction = Preconditions.checkNotNull(analysisDirection,
        "Error: analysis direction argument can not be null");
    m_lattice = Preconditions.checkNotNull(lattice, "Error: latice argument can not be null");

    m_workList = MinMaxPriorityQueue.expectedSize(m_graph.size()).create();
  }

  /**
   * Retrieves the set of edges along which the dataflow values should be propagated.
   *
   * @param node The node for which to determine the relevant edges.
   * @return An iterator for all relevant edges.
   */
  private Iterable<IInstructionGraphEdge> getRelevantEdges(
      final CComparableInstructionGraphNode node) {
    if (m_direction == AnalysisDirection.DOWN) {
      return m_graph.getIncomingEdges(node.getNode());
    }
    return m_graph.getOutgoingEdges(node.getNode());
  }

  // TODO: document: is the first entry of the pair always the true edge? verify!
  private void setOutgoingState(final IInstructionGraphNode n,
      final Pair<LatticeElementType, LatticeElementType> states) {
    // If we are running upward, things are easy: No true/false edge needs to be respected
    if (m_direction == AnalysisDirection.UP) {
      for (final IInstructionGraphEdge instructionGraphEdge : m_graph.getIncomingEdges(n)) {
        setStateInternal(instructionGraphEdge, states.first().copy());
      }
    } else {
      for (final IInstructionGraphEdge instructionGraphEdge : m_graph.getOutgoingEdges(n)) {
        setStateInternal(instructionGraphEdge,
            instructionGraphEdge.isTrue() ? states.first().copy() : states.second().copy());
      }
    }
  }

  private void setState(final IInstructionGraphEdge edge, final LatticeElementType state) {

    if (edge.isInstructionExit()) {
      state.onInstructionExit();
    }

    if (m_stateMap.containsKey(edge)) {
      if (m_lattice.isSmallerEqual(state, m_stateMap.get(edge))) {
        return;
      }
      final List<LatticeElementType> combines = new ArrayList<LatticeElementType>();
      combines.add(state);
      combines.add(m_stateMap.get(edge));
      m_stateMap.put(edge, m_lattice.combine(combines));
    } else {
      m_stateMap.put(edge, state);
    }

    if (!m_lattice.isSmallerEqual(state, m_lattice.getMinimalElement())) {
      if (m_direction == AnalysisDirection.DOWN) {
        m_workList.add(new CComparableInstructionGraphNode(m_graph.getDestination(edge)));
      } else if (m_direction == AnalysisDirection.UP) {
        m_workList.add(new CComparableInstructionGraphNode(m_graph.getSource(edge)));
      }
    }
  }

  private void setStateInternal(final IInstructionGraphEdge edge, final LatticeElementType state) {
    m_traversedEdges.add(edge);
    setState(edge, state);
  }

  @Override
  public MonoReilSolverResult<LatticeElementType> solve(
      final ITransformationProvider<LatticeElementType> transformationProvider,
      final Iterable<Pair<IInstructionGraphEdge, LatticeElementType>> initialStates,
      int maximumIteration) {
    Preconditions.checkNotNull(transformationProvider,
        "Error: transformation provider argument can not be null");
    Preconditions.checkNotNull(initialStates, "Error: initialStates argument can not be null");

    for (final Pair<IInstructionGraphEdge, LatticeElementType> initialState : initialStates) {
      setState(initialState.first(), initialState.second());
    }

    while (m_workList.size() > 0) {
      if (--maximumIteration == 0) {
        throw new IllegalStateException("Solver could not generate a sane result");
      }

      final CComparableInstructionGraphNode comparableInstructionGraphNode =
          m_workList.removeFirst();

      final List<LatticeElementType> statesToCombine = new ArrayList<LatticeElementType>();

      for (final IInstructionGraphEdge edge : getRelevantEdges(comparableInstructionGraphNode)) {
        if (m_stateMap.containsKey(edge)) {
          statesToCombine.add(m_stateMap.get(edge));
        }
      }

      final LatticeElementType combinedState = m_lattice.combine(statesToCombine);
      Pair<LatticeElementType, LatticeElementType> newStates;

      newStates =
          transformationProvider.transform(comparableInstructionGraphNode.getNode(), combinedState);

      setOutgoingState(comparableInstructionGraphNode.getNode(), newStates);
    }
    return new MonoReilSolverResult<LatticeElementType>(m_graph, m_direction, m_lattice, m_stateMap,
        m_traversedEdges);
  }

  // This class only exists because the MinMaxPriorityQueue needs comparable
  // objects for the priority comparison. It simply wraps the IInstructionGraphNode
  private class CComparableInstructionGraphNode implements
      Comparable<CComparableInstructionGraphNode> {
    private final IInstructionGraphNode m_node;
    private final int m_priority;

    CComparableInstructionGraphNode(final IInstructionGraphNode n) {
      m_node = n;
      m_priority = 1;
    }

    private int getPriority() {
      return m_priority;
    }

    @Override
    public int compareTo(final CComparableInstructionGraphNode o) {
      return o.getPriority() - m_priority;
    }

    public IInstructionGraphNode getNode() {
      return m_node;
    }
  }
}
