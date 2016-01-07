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
package com.google.security.zynamics.reil.algorithms.mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeGraph;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;


/**
 * Graph class that describes graphs with nodes that contain exactly one REIL instruction.
 */
public final class InstructionGraph extends
    DirectedGraph<InstructionGraphNode, InstructionGraphEdge> implements
    ILatticeGraph<InstructionGraphNode> {
  /**
   * Creates a new instruction graph.
   * 
   * @param nodes The nodes of the instruction graph.
   * @param edges The edges of the instruction graph.
   */
  public InstructionGraph(final List<InstructionGraphNode> nodes,
      final List<InstructionGraphEdge> edges) {
    super(nodes, edges);
  }

  /**
   * Creates an instruction graph from a REIL graph.
   * 
   * @param graph The REIL graph to convert.
   * 
   * @return The created instruction graph.
   */
  public static InstructionGraph create(final ReilGraph graph) {
    Preconditions.checkNotNull(graph, "Error: graph argument can not be null");

    final List<InstructionGraphNode> nodes = new ArrayList<InstructionGraphNode>();
    final List<InstructionGraphEdge> edges = new ArrayList<InstructionGraphEdge>();

    final HashMap<ReilBlock, InstructionGraphNode> firstNodeMapping =
        new HashMap<ReilBlock, InstructionGraphNode>();
    final HashMap<ReilBlock, InstructionGraphNode> lastNodeMapping =
        new HashMap<ReilBlock, InstructionGraphNode>();

    for (final ReilBlock block : graph) {
      InstructionGraphNode lastNode = null;
      final ReilInstruction lastInstruction = Iterables.getLast(block.getInstructions());

      for (final ReilInstruction instruction : block) {
        final InstructionGraphNode currentNode = new InstructionGraphNode(instruction); // NOPMD by
                                                                                        // sp on
                                                                                        // 04.11.08
                                                                                        // 14:17

        nodes.add(currentNode);

        if (instruction == lastInstruction) {
          lastNodeMapping.put(block, currentNode);
        }

        if (!firstNodeMapping.containsKey(block)) {
          firstNodeMapping.put(block, currentNode);
        }

        if (lastNode != null) {
          final InstructionGraphEdge edge =
              new InstructionGraphEdge(lastNode, currentNode, EdgeType.JUMP_UNCONDITIONAL); // NOPMD
                                                                                            // by sp
                                                                                            // on
                                                                                            // 04.11.08
                                                                                            // 14:18

          edges.add(edge);
          InstructionGraphNode.link(lastNode, currentNode, edge);
        }

        lastNode = currentNode;
      }
    }

    for (final ReilBlock block : graph) {
      for (final ReilEdge edge : block.getOutgoingEdges()) {
        final InstructionGraphEdge newEdge =
            new InstructionGraphEdge(lastNodeMapping.get(block), firstNodeMapping.get(edge
                .getTarget()), edge.getType()); // NOPMD by sp on 04.11.08 14:18

        edges.add(newEdge);
        InstructionGraphNode.link(lastNodeMapping.get(block),
            firstNodeMapping.get(edge.getTarget()), newEdge);
      }
    }

    return new InstructionGraph(nodes, edges);
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("{\n");

    for (final InstructionGraphNode node : this) {
      for (final InstructionGraphEdge edge : node.getOutgoingEdges()) {
        stringBuilder.append(node + " -> " + edge.getTarget());
        stringBuilder.append("\n");
      }
    }

    stringBuilder.append("}\n");

    return stringBuilder.toString();
  }
}
