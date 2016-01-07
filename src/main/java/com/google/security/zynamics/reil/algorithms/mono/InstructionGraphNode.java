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
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;


/**
 * Node of an instruction graph.
 */
public final class InstructionGraphNode implements IGraphNode<InstructionGraphNode> {
  /**
   * The REIL instruction that is represented by the node.
   */
  private final ReilInstruction instruction;

  /**
   * Outgoing edges of the node.
   */
  private final List<InstructionGraphEdge> outgoingEdges = new ArrayList<>();

  /**
   * Incoming edges of the node.
   */
  private final List<InstructionGraphEdge> incomingEdges = new ArrayList<>();

  /**
   * Creates a new instruction graph node.
   * 
   * @param instruction The instruction represented by the node.
   */
  public InstructionGraphNode(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null");
    this.instruction = instruction;
  }

  /**
   * Links two instruction graph nodes with an edge.
   * 
   * @param source The source node.
   * @param target The target node.
   * @param edge The edge between the two nodes.
   */
  public static void link(final InstructionGraphNode source, final InstructionGraphNode target,
      final InstructionGraphEdge edge) {
    Preconditions.checkNotNull(source, "Error: Source argument can not be null");
    Preconditions.checkNotNull(target, "Error: Target argument can not be null");
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");

    source.outgoingEdges.add(edge);
    target.incomingEdges.add(edge);
  }

  @Override
  public List<InstructionGraphNode> getChildren() {
  
    return outgoingEdges
            .stream()
            .map(InstructionGraphEdge::getTarget)
            .collect(Collectors.toList());
  }

  /**
   * Returns the incoming edges of the node.
   * 
   * @return The incoming edges of the node.
   */
  public List<InstructionGraphEdge> getIncomingEdges() {
    // TODO return an unmodifiable iterator.
    return new ArrayList<InstructionGraphEdge>(incomingEdges);
  }

  /**
   * Returns the instruction represented by the node.
   * 
   * @return The instruction represented by the node.
   */
  public ReilInstruction getInstruction() {
    return instruction;
  }

  /**
   * Returns the outgoing edges of the node.
   * 
   * @return The outgoing edges of the node.
   */
  public List<InstructionGraphEdge> getOutgoingEdges() {
    // TODO return an unmodifiable iterator.
    return new ArrayList<InstructionGraphEdge>(outgoingEdges);
  }

  @Override
  public List<InstructionGraphNode> getParents() {
 
    return incomingEdges
            .stream()
            .map(InstructionGraphEdge::getSource)
            .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return instruction.toString();
  }
}
