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
package com.google.security.zynamics.binnavi.REIL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.reil.ReilBlock;
import com.google.security.zynamics.binnavi.API.reil.ReilEdge;
import com.google.security.zynamics.binnavi.API.reil.ReilGraph;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;

/**
 * Converts between internal REIL graphs and API REIL graphs.
 */
public final class ReilGraphConverter {
  /**
   * You are not supposed to instantiate this class.
   */
  private ReilGraphConverter() {
  }

  /**
   * Converts a list of API instructions to a list of internal instructions.
   * 
   * @param instructions The API instructions to convert.
   * 
   * @return The converted internal instructions.
   */
  private static List<com.google.security.zynamics.reil.ReilInstruction> convert(
      final List<ReilInstruction> instructions) {
    final List<com.google.security.zynamics.reil.ReilInstruction> convertedInstructions =
        new ArrayList<com.google.security.zynamics.reil.ReilInstruction>();

    for (final ReilInstruction reilInstruction : instructions) {
      convertedInstructions.add(reilInstruction.getNative());
    }

    return convertedInstructions;
  }

  /**
   * Converts an API REIL graph to an internal REIL graph.
   * 
   * @param graph The API REIL graph to convert.
   * 
   * @return The converted internal REIL graph.
   */
  public static com.google.security.zynamics.reil.ReilGraph convert(final ReilGraph graph) {
    Preconditions.checkNotNull(graph, "IE01050: Graph argument can not be null");

    final List<com.google.security.zynamics.reil.ReilBlock> nodes =
        new ArrayList<com.google.security.zynamics.reil.ReilBlock>();

    final Map<ReilBlock, com.google.security.zynamics.reil.ReilBlock> blockMap =
        new HashMap<ReilBlock, com.google.security.zynamics.reil.ReilBlock>();

    for (final ReilBlock reilBlock : graph) {
      final com.google.security.zynamics.reil.ReilBlock convertedBlock =
          new com.google.security.zynamics.reil.ReilBlock(convert(reilBlock.getInstructions()));

      blockMap.put(reilBlock, convertedBlock);

      nodes.add(convertedBlock);
    }

    final List<com.google.security.zynamics.reil.ReilEdge> edges =
        new ArrayList<com.google.security.zynamics.reil.ReilEdge>();

    for (final ReilEdge edge : graph.getEdges()) {
      final com.google.security.zynamics.reil.ReilEdge convertedEdge =
          new com.google.security.zynamics.reil.ReilEdge(blockMap.get(edge.getSource()),
              blockMap.get(edge.getTarget()), edge.getType().getNative());

      edges.add(convertedEdge);

      com.google.security.zynamics.reil.ReilBlock.link(blockMap.get(edge.getSource()),
          blockMap.get(edge.getTarget()), convertedEdge);
    }

    return new com.google.security.zynamics.reil.ReilGraph(nodes, edges);
  }

  /**
   * Converts an internal REIL graph to an API REIL graph.
   * 
   * @param graph The internal REIL graph to convert.
   * 
   * @return The converted API REIL graph.
   */
  public static ReilGraph createReilGraph(final com.google.security.zynamics.reil.ReilGraph graph) {
    Preconditions.checkNotNull(graph, "IE01049: Graph argument can not be null");

    final List<ReilBlock> blocks = new ArrayList<ReilBlock>();
    final List<ReilEdge> edges = new ArrayList<ReilEdge>();

    final HashMap<com.google.security.zynamics.reil.ReilBlock, ReilBlock> blockMap =
        new HashMap<com.google.security.zynamics.reil.ReilBlock, ReilBlock>();

    for (final com.google.security.zynamics.reil.ReilBlock block : graph.getNodes()) {
      final ReilBlock newBlock = new ReilBlock(block);

      blockMap.put(block, newBlock);

      blocks.add(newBlock);
    }

    for (final com.google.security.zynamics.reil.ReilEdge edge : graph.getEdges()) {
      final ReilBlock source = blockMap.get(edge.getSource());
      final ReilBlock target = blockMap.get(edge.getTarget());

      edges.add(new ReilEdge(source, target, EdgeType.convert(edge.getType())));
    }

    return new ReilGraph(blocks, edges);
  }
}
