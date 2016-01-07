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

import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;

// / Represents a single edge that connects the blocks of a function.
/**
 * Represents an edge in a callgraph.
 */
public final class FunctionEdge extends DefaultEdge<FunctionBlock> {
  // / @cond INTERNAL
  /**
   * Creates a new function edge object.
   *
   * @param source Source block of the edge.
   * @param target Target block of the edge.
   */
  // / @endcond
  public FunctionEdge(final FunctionBlock source, final FunctionBlock target) {
    super(source, target);

    FunctionBlock.link(source, target);
  }

  // ESCA-JAVA0059: We need this override for the documentation.
  // ! Source block of the edge.
  /**
   * Returns the source block of the edge.
   *
   * @return The source block of the edge.
   */
  @Override
  public FunctionBlock getSource() {
    return super.getSource();
  }

  // ESCA-JAVA0059: We need this override for the documentation.
  // ! Target block of the edge.
  /**
   * Returns the target block of the edge.
   *
   * @return The target block of the edge.
   */
  @Override
  public FunctionBlock getTarget() {
    return super.getTarget();
  }

  // ! Printable representation of the edge.
  /**
   * Returns the string representation of the function edge.
   *
   * @return The string representation of the function edge.
   */
  @Override
  public String toString() {
    return String.format("Function Edge [%s -> %s]", super.getSource().getFunction().getName(),
        super.getTarget().getFunction().getName());
  }
}
