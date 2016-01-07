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

import com.google.security.zynamics.binnavi.API.disassembly.IDirectedGraph;
import com.google.security.zynamics.binnavi.API.reil.ReilGraph;
import com.google.security.zynamics.binnavi.REIL.CInstructionGraphConverter;
import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;



// ! Lattice graph class where each node contains one REIL instructions.
/**
 * Graph class that describes graphs with nodes that contain exactly one REIL instruction.
 */
public final class InstructionGraph extends
    MutableDirectedGraph<InstructionGraphNode, InstructionGraphEdge> implements
    ILatticeGraph<InstructionGraphNode>, IDirectedGraph<InstructionGraphNode, InstructionGraphEdge> {
  // ! Creates a new instruction graph.
  /**
   * Creates a new instruction graph.
   * 
   * @param nodes List of nodes to put into the graph.
   * @param edges List of edges to put into the graph.
   */
  public InstructionGraph(final List<InstructionGraphNode> nodes,
      final List<InstructionGraphEdge> edges) {
    super(nodes, edges);
  }

  // ! Creates an instruction graph from a REIL graph.
  /**
   * Creates an instruction graph from a REIL graph.
   * 
   * @param graph The REIL graph to convert.
   * 
   * @return The created instruction graph.
   */
  public static InstructionGraph create(final ReilGraph graph) {
    return CInstructionGraphConverter
        .convert(com.google.security.zynamics.reil.algorithms.mono.InstructionGraph
            .create(ReilGraphConverter.convert(graph)));
  }

  // ! Adds an edge to the graph.
  /**
   * Adds an instruction edge to the instruction graph.
   * 
   * @param edge The edge to add to the graph.
   */
  @Override
  public void addEdge(final InstructionGraphEdge edge) {
    super.addEdge(edge);
  }

  // ! Adds a node to the graph.
  /**
   * Adds an instruction node to the instruction graph.
   * 
   * @param node The node to add to the graph.
   */
  @Override
  public void addNode(final InstructionGraphNode node) {
    super.addNode(node);
  }

  // ! Removes an edge from the graph.
  /**
   * Removes an instruction edge from the instruction graph.
   * 
   * @param edge The edge to remove.
   */
  @Override
  public void removeEdge(final InstructionGraphEdge edge) {
    super.removeEdge(edge);
  }

  // ! Removes a node from the graph.
  /**
   * Removes an instruction node from the instruction graph.
   * 
   * @param node The node to remove.
   */
  @Override
  public void removeNode(final InstructionGraphNode node) {
    super.removeNode(node);
  }

  // ! A printable representation of the instruction graph.
  /**
   * Returns a string representation of the instruction graph.
   * 
   * @return A string representation of the instruction graph.
   */
  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("{\n");

    for (final InstructionGraphNode node : getNodes()) {
      for (final InstructionGraphEdge edge : node.getOutgoingEdges()) {
        stringBuilder.append(node + " -> " + edge.getTarget());
        stringBuilder.append('\n');
      }
    }

    stringBuilder.append("}\n");

    return stringBuilder.toString();
  }
}
