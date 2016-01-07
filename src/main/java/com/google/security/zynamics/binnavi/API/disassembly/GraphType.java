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

/* ! \file GraphType.java \brief Contains the GraphType enumeration * */

// / Describes the type of graphs.
/**
 * Describes the type of graphs.
 */
public enum GraphType {
  /**
   * Graph that contains only function nodes.
   */
  Callgraph,

  /**
   * Graph that contains only code nodes.
   */
  Flowgraph,

  /**
   * Graph that contains both code nodes and function nodes.
   */
  MixedGraph;

  // / @cond INTERNAL
  /**
   * Converts an internal graph type to an API graph type.
   *
   * @param type The graph type to convert.
   *
   * @return The converted graph type.
   */
  public static GraphType convert(final com.google.security.zynamics.zylib.disassembly.GraphType type) {
    switch (type) {
      case CALLGRAPH:
        return Callgraph;
      case FLOWGRAPH:
        return Flowgraph;
      case MIXED_GRAPH:
        return MixedGraph;
      default:
        throw new IllegalArgumentException("Error: Unknown graph type");
    }
  }

  /**
   * Converts an API graph type to an internal graph type.
   *
   * @return The internal graph type.
   */
  // / @endcond
  public com.google.security.zynamics.zylib.disassembly.GraphType getNative() {
    switch (this) {
      case Callgraph:
        return com.google.security.zynamics.zylib.disassembly.GraphType.CALLGRAPH;
      case Flowgraph:
        return com.google.security.zynamics.zylib.disassembly.GraphType.FLOWGRAPH;
      case MixedGraph:
        return com.google.security.zynamics.zylib.disassembly.GraphType.MIXED_GRAPH;
      default:
        throw new IllegalArgumentException("Error: Unknown graph type");
    }
  }

}
