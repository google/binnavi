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

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.API.disassembly.IGraphNode;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;


// ! Node of an instruction graph.
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
  private final List<InstructionGraphEdge> outgoingEdges = new ArrayList<InstructionGraphEdge>();

  /**
   * Incoming edges of the node.
   */
  private final List<InstructionGraphEdge> incomingEdges = new ArrayList<InstructionGraphEdge>();

  // ! Creates a new instruction graph node.
  /**
   * Creates a new instruction graph node.
   *
   * @param instruction The REIL instruction to put into the instruction graph node.
   */
  public InstructionGraphNode(final ReilInstruction instruction) {
    if (instruction == null) {
      throw new IllegalArgumentException("Error: Instruction argument can not be null");
    }

    this.instruction = instruction;
  }

  // ! Links two nodes.
  /**
   * Links two nodes. Calling this function is necessary after creating an edge.
   *
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   * @param edge Edge that connects the two nodes.
   */
  public static void link(final InstructionGraphNode source, final InstructionGraphNode target,
      final InstructionGraphEdge edge) {
    if (source == null) {
      throw new IllegalArgumentException("Error: Source argument can not be null");
    }

    if (target == null) {
      throw new IllegalArgumentException("Error: Target argument can not be null");
    }

    if (edge == null) {
      throw new IllegalArgumentException("Error: Edge argument can not be null");
    }

    source.outgoingEdges.add(edge);
    target.incomingEdges.add(edge);
  }

  // ! Unlinks two nodes.
  /**
   * Unlinks two nodes. Calling this function is necessary after removing an edge from a graph.
   *
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   * @param edge Edge that connects the two nodes.
   */
  public static void unlink(final InstructionGraphNode source, final InstructionGraphNode target,
      final InstructionGraphEdge edge) {
    source.outgoingEdges.remove(edge);
    target.incomingEdges.remove(edge);
  }

  // ! Children of the node.
  /**
   * Returns a list of all child nodes of the node.
   *
   * @return A list of all child nodes of the node.
   */
  @Override
  public List<InstructionGraphNode> getChildren() {
    final List<InstructionGraphNode> children = new ArrayList<InstructionGraphNode>();

    for (final InstructionGraphEdge edge : outgoingEdges) {
      children.add(edge.getTarget());
    }

    return children;
  }

  // ! Incoming edges of the node.
  /**
   * Returns the incoming edges of the node.
   *
   * @return The incoming edges of the node.
   */
  public List<InstructionGraphEdge> getIncomingEdges() {
    return new ArrayList<InstructionGraphEdge>(incomingEdges);
  }

  // ! REIL instruction represented by the node.
  /**
   * Returns the instruction represented by the node.
   *
   * @return The instruction represented by the node.
   */
  public ReilInstruction getInstruction() {
    return instruction;
  }

  // ! Outgoing edges of the node.
  /**
   * Returns the outgoing edges of the node.
   *
   * @return The outgoing edges of the node.
   */
  public List<InstructionGraphEdge> getOutgoingEdges() {
    return new ArrayList<InstructionGraphEdge>(outgoingEdges);
  }

  // ! Parents of the node.
  /**
   * Returns a list of all parent nodes of the node.
   *
   * @return A list of all parent nodes of the node.
   */
  @Override
  public List<InstructionGraphNode> getParents() {
    final List<InstructionGraphNode> parents = new ArrayList<InstructionGraphNode>();

    for (final InstructionGraphEdge edge : incomingEdges) {
      parents.add(edge.getSource());
    }

    return parents;
  }

  // ! Printable representation of the node.
  /**
   * Returns a string representation of the node.
   *
   * @return A string representation of the node.
   */
  @Override
  public String toString() {
    return instruction.toString();
  }
}
