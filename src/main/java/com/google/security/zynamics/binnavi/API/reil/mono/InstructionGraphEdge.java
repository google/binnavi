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

import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;



// ! Edge of an instruction graph.
/**
 * Edge of an instruction graph.
 */
public final class InstructionGraphEdge extends DefaultEdge<InstructionGraphNode> {
  /**
   * Type of the edge.
   */
  private final EdgeType edgeType;

  // ! Creates a new instruction graph edge.
  /**
   * Creates a new instruction graph edge.
   *
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   * @param edgeType Type of the edge.
   */
  public InstructionGraphEdge(final InstructionGraphNode source, final InstructionGraphNode target,
      final EdgeType edgeType) {
    super(source, target);

    if (edgeType == null) {
      throw new IllegalArgumentException("Error: Edge type argument can not be null");
    }

    this.edgeType = edgeType;
  }

  // ESCA-JAVA0059: Required for the documentation.
  // ! The source node of the edge.
  /**
   * Returns the source node of the edge.
   *
   * @return The source node of the edge.
   */
  @Override
  public InstructionGraphNode getSource() {
    return super.getSource();
  }

  // ESCA-JAVA0059: Required for the documentation.
  // ! The target node of the edge.
  /**
   * Returns the target node of the edge.
   *
   * @return The target node of the edge.
   */
  @Override
  public InstructionGraphNode getTarget() {
    return super.getTarget();
  }

  // ! Type of the edge.
  /**
   * Returns the type of the edge.
   *
   * @return The type of the edge.
   */
  public EdgeType getType() {
    return edgeType;
  }

  @Override
  public String toString() {
    return "[ " + getSource() + " -> " + getTarget() + " ]";
  }
}
