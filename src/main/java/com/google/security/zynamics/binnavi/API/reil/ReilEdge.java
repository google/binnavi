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
package com.google.security.zynamics.binnavi.API.reil;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;

// / Edge between REIL blocks
/**
 * Represents an edge between two REIL blocks.
 */
public final class ReilEdge extends DefaultEdge<ReilBlock> {
  /**
   * Type of the edge.
   */
  private final EdgeType m_type;

  /**
   * Creates a new REIL edge.
   *
   * @param source Source block.
   * @param target Target block.
   * @param type Edge type.
   */
  public ReilEdge(final ReilBlock source, final ReilBlock target, final EdgeType type) {
    super(source, target);

    m_type = Preconditions.checkNotNull(type, "Error: Type argument can not be null");

    ReilBlock.link(source, target);
  }

  // ESCA-JAVA0059: Method stays for the JavaDoc comment
  // ! Source block of the edge.
  /**
   * Returns the source block of the edge.
   *
   * @return The source block of the edge.
   */
  @Override
  public ReilBlock getSource() {
    return super.getSource();
  }

  // ESCA-JAVA0059: Method stays for the JavaDoc comment
  // ! Target block of the edge.
  /**
   * Returns the target block of the edge.
   *
   * @return The target block of the edge.
   */
  @Override
  public ReilBlock getTarget() {
    return super.getTarget();
  }

  // ! The type of the edge.
  /**
   * Returns the type of the edge.
   *
   * @return The type of the edge.
   */
  public EdgeType getType() {
    return m_type;
  }

  // ! Printable representation of the edge.
  /**
   * Returns the string representation of the edge.
   *
   * @return The string representation of the edge.
   */
  @Override
  public String toString() {
    return String.format("REIL Edge [%s -> %s]", super.getSource().getAddress().toHexString(),
        super.getTarget().getAddress().toHexString());
  }
}
