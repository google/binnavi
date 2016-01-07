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

import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;

// / Function of REIL code
/**
 * Represents a single REIL function.
 */
public final class ReilFunction {
  /**
   * REIL graph of the function.
   */
  private final ReilGraph m_graph;

  /**
   * The wrapped internal REIL function object.
   */
  private final com.google.security.zynamics.reil.ReilFunction m_function;

  // / @cond INTERNAL
  /**
   * Creates a new API REIL function object.
   *
   * @param function The wrapped internal REIL function object.
   */
  // / @endcond
  public ReilFunction(final com.google.security.zynamics.reil.ReilFunction function) {
    m_function = function;

    m_graph = ReilGraphConverter.createReilGraph(function.getGraph());
  }

  // ! The graph of the function.
  /**
   * Returns the graph of the REIL function.
   *
   * @return The graph of the REIL function.
   */
  public ReilGraph getGraph() {
    return m_graph;
  }

  // ! The name of the function.
  /**
   * Returns the name of the REIL function.
   *
   * @return The name of the REIL function.
   */
  public String getName() {
    return m_function.getName();
  }

  // ! Printable representation of the REIL function.
  /**
   * Returns the string representation of the REIL function.
   *
   * @return The string representation of the REIL function.
   */
  @Override
  public String toString() {
    return String.format("REIL function %s", getName());
  }
}
