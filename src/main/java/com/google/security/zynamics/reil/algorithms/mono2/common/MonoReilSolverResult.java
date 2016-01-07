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
package com.google.security.zynamics.reil.algorithms.mono2.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraph;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILattice;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILatticeElement;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.IMonoReilSolverResult;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;


public class MonoReilSolverResult<LatticeElementType extends ILatticeElement<LatticeElementType>>
    implements IMonoReilSolverResult<LatticeElementType> {

  /**
   * The instruction graph which has been used to get this result.
   */
  private final IInstructionGraph graph;

  /**
   * The analysis direction which has been used to get this result.
   */
  private final AnalysisDirection direction;

  /**
   * The lattice which has been used to get this result.
   */
  private final ILattice<LatticeElementType> lattice;

  /**
   * Associates a lattice element with each graph edge.
   */
  private final Map<IInstructionGraphEdge, LatticeElementType> stateMap;

  /**
   * The set of edges which have already been visited during iteration. When generating the final
   * result we skip edges which have not been visited in order to omit artificially inserted entry
   * and exit edges.
   */
  private final Set<IInstructionGraphEdge> traversedEdges;

  public MonoReilSolverResult(final IInstructionGraph graph, final AnalysisDirection direction,
      final ILattice<LatticeElementType> lattice,
      final Map<IInstructionGraphEdge, LatticeElementType> stateMap,
      final Set<IInstructionGraphEdge> traversedEdges) {
    this.graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    this.lattice = Preconditions.checkNotNull(lattice, "Error: lattice argument can not be null");
    this.direction = Preconditions.checkNotNull(direction, "Error: direction argument can not be null");
    this.stateMap = Preconditions.checkNotNull(stateMap, "Error: stateMap argument can not be null");
    this.traversedEdges =
        Preconditions.checkNotNull(traversedEdges, "Error: traversedEdges argument can not be null");
  }

  private Iterator<Pair<IInstructionGraphEdge, LatticeElementType>> resultIterator() {
    return new ProxyIterator();
  }

  /**
   * Collect lattice results and generate a map which associates a lattice result with each address.
   * 
   * @param startInstruction The instruction where collecting the results is started.
   * @param trackIncoming Flag whether to start collecting immediately before or after the start
   *        instruction.
   * 
   * @return The map which associates addresses with lattice results.
   */
  @Override
  public Map<IAddress, LatticeElementType> generateAddressToStateMapping(
      final IInstruction startInstruction, final boolean trackIncoming) {

    final Map<IAddress, LatticeElementType> addressToLatticeElementMap = new TreeMap<>();

    final Iterator<Pair<IInstructionGraphEdge, LatticeElementType>> iter = resultIterator();

    while (iter.hasNext()) {
      final Pair<IInstructionGraphEdge, LatticeElementType> edgeToLatticeElement = iter.next();

      if (edgeToLatticeElement.first().isInstructionExit()) {
        IAddress address;

        if (hasResult(edgeToLatticeElement.first())) {
          if (direction == AnalysisDirection.DOWN) {
            address =
                graph.getSource(edgeToLatticeElement.first()).getReilInstruction().getAddress();
          } else {
            address =
                graph.getDestination(edgeToLatticeElement.first()).getReilInstruction()
                    .getAddress();
          }

          if (addressToLatticeElementMap.containsKey(address)) {
            final ArrayList<LatticeElementType> combinelist = new ArrayList<>();
            combinelist.add(edgeToLatticeElement.second());
            combinelist.add(addressToLatticeElementMap.get(address));
            addressToLatticeElementMap.put(address, lattice.combine(combinelist));
          } else {
            addressToLatticeElementMap.put(address, edgeToLatticeElement.second());
          }
        } else if (ReilHelpers.toNativeAddress(
            graph.getSource(edgeToLatticeElement.first()).getReilInstruction().getAddress())
            .equals(startInstruction.getAddress())
            && (direction == AnalysisDirection.DOWN) && !trackIncoming) {
          address = graph.getSource(edgeToLatticeElement.first()).getReilInstruction().getAddress();
          addressToLatticeElementMap.put(address, edgeToLatticeElement.second());

        } else if (ReilHelpers.toNativeAddress(
            graph.getDestination(edgeToLatticeElement.first()).getReilInstruction().getAddress())
            .equals(startInstruction.getAddress())
            && (direction == AnalysisDirection.UP) && trackIncoming) {
          address =
              graph.getDestination(edgeToLatticeElement.first()).getReilInstruction().getAddress();
          addressToLatticeElementMap.put(address, edgeToLatticeElement.second());
        }
      }
    }
    return addressToLatticeElementMap;
  }

  @Override
  public LatticeElementType getResult(final IInstructionGraphEdge edge) {
    return hasResult(edge) ? stateMap.get(edge) : null;
  }

  @Override
  public boolean hasResult(final IInstructionGraphEdge instructionGraphEdge) {
    return traversedEdges.contains(instructionGraphEdge);
  }

  /**
   * 
   * An encapsulated iterator without remove operation to iterate over the
   * {@link MonoReilSolverResult}.
   * 
   * @author (timkornau@google.com)
   * 
   */
  class ProxyIterator implements Iterator<Pair<IInstructionGraphEdge, LatticeElementType>> {
    private final Iterator<Map.Entry<IInstructionGraphEdge, LatticeElementType>> m_internalIterator =
        stateMap.entrySet().iterator();

    @Override
    public boolean hasNext() {
      return m_internalIterator.hasNext();
    }

    @Override
    public Pair<IInstructionGraphEdge, LatticeElementType> next() {
      final Map.Entry<IInstructionGraphEdge, LatticeElementType> entry = m_internalIterator.next();
      return new Pair<IInstructionGraphEdge, LatticeElementType>(entry.getKey(), entry.getValue());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Unsupported operation on ProxyIterator: remove!");
    }
  }
}
