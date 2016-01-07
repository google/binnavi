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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.disassembly.IBlockEdge;
import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;



// / Represents edges between basic blocks in flowgraphs.
/**
 * Block edges are used to connect basic blocks in flowgraphs.
 */
public final class BlockEdge extends DefaultEdge<BasicBlock> {
  /**
   * Wrapped internal block edge object.
   */
  private final IBlockEdge m_edge;

  // / @cond INTERNAL
  /**
   * Creates a new API block edge object.
   *
   * @param edge Wrapped internal block edge object.
   * @param source Source block of the edge.
   * @param target Target block of the edge.
   */
  // / @endcond
  public BlockEdge(final IBlockEdge edge, final BasicBlock source, final BasicBlock target) {
    super(source, target);

    m_edge = edge;

    BasicBlock.link(source, target);
  }

  // ESCA-JAVA0059:
  // ! Source block of the edge.
  /**
   * Returns the source block of the edge.
   *
   * @return The source block of the edge.
   */
  @Override
  public BasicBlock getSource() {
    // Method stays for the javadoc comment
    return super.getSource();
  }

  // ESCA-JAVA0059:
  // ! Target block of the edge.
  /**
   * Returns the target block of the edge.
   *
   * @return The target block of the edge.
   */
  @Override
  public BasicBlock getTarget() {
    // Method stays for the javadoc comment
    return super.getTarget();
  }

  // ! Type of the edge.
  /**
   * Returns the type of the edge.
   *
   * @return The type of the edge.
   */
  public EdgeType getType() {
    return EdgeType.convert(m_edge.getType());
  }

  // ! Printable representation of the edge.
  /**
   * Returns a string representation of the edge.
   *
   * @return A string representation of the edge.
   */
  @Override
  public String toString() {
    return String.format("Block Edge [%s -> %s]", super.getSource().getAddress().toHexString(),
        super.getTarget().getAddress().toHexString());
  }
}
