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
package com.google.security.zynamics.reil;

import java.util.List;

import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;


public class ReilGraph extends DirectedGraph<ReilBlock, ReilEdge> {
  public ReilGraph(final List<ReilBlock> nodes, final List<ReilEdge> edges) {
    super(nodes, edges);
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("{\n");

    for (final ReilBlock block : this) {
      stringBuilder.append(block + "\n");

      for (final ReilEdge edge : block.getOutgoingEdges()) {
        stringBuilder.append(block.getAddress());
        stringBuilder.append(" [");
        stringBuilder.append(Enum.valueOf(EdgeType.class, edge.getType().toString()));
        stringBuilder.append(edge.getTarget().getAddress());
        stringBuilder.append("\n");
      }
    }

    stringBuilder.append("}\n");

    return stringBuilder.toString();
  }
}
