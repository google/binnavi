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
package com.google.security.zynamics.reil.algorithms.mono;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeGraph;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OperandGraph extends DirectedGraph<OperandGraphNode, OperandGraphEdge> implements
    ILatticeGraph<OperandGraphNode> {
  public OperandGraph(final Set<OperandGraphNode> nodes,
      final Set<OperandGraphEdge> edges) {
    super(new ArrayList<OperandGraphNode>(nodes), new ArrayList<OperandGraphEdge>(edges));
  }

  private static OperandGraph connectParts(final ReilGraph graph,
      final Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> graphMap) {
    final Pair<Collection<List<OperandGraphNode>>, Collection<List<OperandGraphEdge>>> unzipped =
        CollectionHelpers.unzip(graphMap.values());

    final Set<OperandGraphNode> allNodes =
        Sets.newHashSet(CollectionHelpers.flatten(unzipped.first()));
    final Set<OperandGraphEdge> allEdges =
        Sets.newHashSet(CollectionHelpers.flatten(unzipped.second()));

    for (final ReilBlock block : graph) {
      final Pair<List<OperandGraphNode>, List<OperandGraphEdge>> p = graphMap.get(block);

      final List<OperandGraphNode> nodes = p.first();

      final List<ReilBlock> parents = getParents(block);

      for (final OperandGraphNode node : nodes) {
        if (node.getIndex() == 2) {
          // We only have to link input operands
          continue;
        }

        final OperandGraphNode definition = findDefinition(node, block, graphMap, false);

        if (definition != null) {
          final OperandGraphEdge edge = new OperandGraphEdge(definition, node);

          OperandGraphNode.link(definition, node);

          allEdges.add(edge);
        } else {
          final Set<OperandGraphNode> definitions =
              findDefinitions(parents, node, graphMap, new HashSet<ReilBlock>());

          for (final OperandGraphNode innerDefinition : definitions) {
            final OperandGraphEdge edge = new OperandGraphEdge(innerDefinition, node);

            OperandGraphNode.link(innerDefinition, node);

            allEdges.add(edge);
          }
        }
      }
    }

    return new OperandGraph(allNodes, allEdges);
  }

  private static OperandGraphNode create(final ReilInstruction instruction, final int index,
      final List<OperandGraphNode> nodes, final List<OperandGraphEdge> edges,
      final Map<String, OperandGraphNode> defines) {
    final OperandGraphNode node = new OperandGraphNode(instruction, index);

    nodes.add(node);

    final String operandString = node.getValue();

    if (defines.containsKey(node)) {
      final OperandGraphNode source = defines.get(operandString);

      final OperandGraphEdge edge = new OperandGraphEdge(source, node);

      edges.add(edge);

      OperandGraphNode.link(source, node);
    }

    return node;
  }

  private static Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> createInitialMap(
      final ReilGraph graph) {
    final Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> graphMap =
        new HashMap<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>>();

    for (final ReilBlock block : graph) {
      final List<OperandGraphNode> nodes = new ArrayList<OperandGraphNode>();
      final List<OperandGraphEdge> edges = new ArrayList<OperandGraphEdge>();

      graphMap.put(block, new Pair<List<OperandGraphNode>, List<OperandGraphEdge>>(nodes, edges));

      final Map<String, OperandGraphNode> defines = new HashMap<String, OperandGraphNode>();

      for (final ReilInstruction instruction : block) {
        final Integer mnemonic = instruction.getMnemonicCode();

        OperandGraphNode firstNode = null;
        OperandGraphNode secondNode = null;

        if (ReilHelpers.usesFirstOperand(mnemonic)) {
          firstNode = create(instruction, 0, nodes, edges, defines);
        }

        if (ReilHelpers.usesSecondOperand(mnemonic)) {
          secondNode = create(instruction, 1, nodes, edges, defines);
        }

        if (ReilHelpers.writesThirdOperand(mnemonic)) {
          final OperandGraphNode node = new OperandGraphNode(instruction, 2);

          nodes.add(node);

          defines.put(instruction.getThirdOperand().getValue(), node);

          if (firstNode != null) {
            final OperandGraphEdge edge = new OperandGraphEdge(firstNode, node);

            edges.add(edge);

            OperandGraphNode.link(firstNode, node);
          }

          if (secondNode != null) {
            final OperandGraphEdge edge = new OperandGraphEdge(secondNode, node);

            edges.add(edge);

            OperandGraphNode.link(secondNode, node);
          }
        }
      }
    }

    return graphMap;
  }

  private static OperandGraphNode findDefinition(final OperandGraphNode search,
      final ReilBlock block,
      final Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> graphMap,
      boolean found) {
    final String value = search.getValue();

    final List<ReilInstruction> instructions = Lists.newArrayList(block.getInstructions());

    for (int i = instructions.size() - 1; i >= 0; i--) {
      final ReilInstruction instruction = instructions.get(i);

      if (search.getInstruction() == instruction) {
        found = true;
      }

      if (!found) {
        continue;
      }

      if (ReilHelpers.writesThirdOperand(instruction.getMnemonicCode())
          && instruction.getThirdOperand().getValue().equals(value)) {
        final List<OperandGraphNode> nodes = graphMap.get(block).first();

        for (final OperandGraphNode node : nodes) {
          if ((node.getInstruction() == instruction) && (node.getIndex() == 2)) {
            return node;
          }
        }
      }
    }

    return null;
  }

  private static Set<OperandGraphNode> findDefinitions(final List<ReilBlock> parents,
      final OperandGraphNode node,
      final Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> graphMap,
      final HashSet<ReilBlock> visited) {
    final Set<OperandGraphNode> definitions = new HashSet<OperandGraphNode>();

    for (final ReilBlock reilBlock : parents) {
      if (visited.contains(reilBlock)) {
        continue;
      }

      visited.add(reilBlock);

      final OperandGraphNode definition = findDefinition(node, reilBlock, graphMap, true);

      if (definition != null) {
        definitions.add(definition);
      } else {
        definitions.addAll(findDefinitions(getParents(reilBlock), node, graphMap, visited));
      }
    }

    return definitions;
  }

  private static List<ReilBlock> getParents(final ReilBlock block) {
    
    return block.getIncomingEdges()
            .stream()
            .map(ReilEdge::getSource)
            .collect(Collectors.toList());
  }

  public static OperandGraph create(final ReilGraph graph) {
    final Map<ReilBlock, Pair<List<OperandGraphNode>, List<OperandGraphEdge>>> graphMap =
        createInitialMap(graph);

    return connectParts(graph, graphMap);
  }
}
