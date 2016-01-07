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
package com.google.security.zynamics.reil.translators;

import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ReilGraphGenerator {

  /**
   * Creates a REIL graph from a pair that contains REIL blocks and REIL edges.
   * 
   * @return The created graph object.
   */
  public static ReilGraph createGraph(final Collection<List<ReilInstruction>> instructionList,
      final Collection<IAddress> nativeJumpTargets) {
    final Pair<List<ReilBlock>, List<ReilEdge>> pair =
        createGraphElements(instructionList, nativeJumpTargets);

    return new ReilGraph(pair.first(), pair.second());
  }

  /**
   * Creates REIL basic blocks and edges from a list of REIL instructions.
   * 
   * @param instructionList A list of REIL instructions.
   * @param nativeJumpTargets Additional jump targets for the algorithm to consider.
   * 
   * @return A pair containing the blocks and edges created from the REIL instructions.
   */
  public static Pair<List<ReilBlock>, List<ReilEdge>> createGraphElements(
      final Collection<List<ReilInstruction>> instructionList,
      final Collection<IAddress> nativeJumpTargets) {
    final BasicBlockGenerator generator =
        new BasicBlockGenerator(instructionList, nativeJumpTargets);

    final List<ReilBlock> blocks = generator.getBlocks();
    final ArrayList<Triple<ReilBlock, IAddress, EdgeType>> edgepairs = generator.getEdges();

    final List<ReilEdge> edges = new ArrayList<ReilEdge>();

    for (final Triple<ReilBlock, IAddress, EdgeType> p : edgepairs) {
      final ReilBlock source = p.first();
      final IAddress target = p.second();
      final EdgeType edgeType = p.third();

      if (target != null) {
        for (final ReilBlock block : blocks) {
          for (final ReilInstruction instruction : block.getInstructions()) {
            if (target.equals(instruction.getAddress())) {
              final ReilEdge edge = new ReilEdge(source, block, edgeType);

              edges.add(edge);

              ReilBlock.link(source, block, edge);
            }
          }
        }
      } else {
        // Unknown target address

        final int index = blocks.indexOf(source);

        if (blocks.size() > (index + 1)) {
          final ReilEdge edge = new ReilEdge(source, blocks.get(index + 1), edgeType);

          edges.add(edge);

          ReilBlock.link(source, blocks.get(index + 1), edge);
        }
      }
    }

    return new Pair<List<ReilBlock>, List<ReilEdge>>(blocks, edges);
  }

}
