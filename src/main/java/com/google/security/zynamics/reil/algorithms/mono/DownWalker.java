/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.IGraphWalker;

/**
 * InstructionGraph walker that can be used to walk downwards through InstructionGraph nodes.
 * 
 */
public final class DownWalker implements IGraphWalker<InstructionGraphNode, WalkInformation> {
  @Override
  public List<InstructionGraphNode> getInfluenced(final InstructionGraphNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");

    // When walking downwards, the influenced nodes of a node
    // are its children.

    final List<InstructionGraphNode> nodes = new ArrayList<InstructionGraphNode>();

    for (final InstructionGraphEdge edge : node.getOutgoingEdges()) {
      nodes.add(edge.getTarget());
    }

    return nodes;
  }

  @Override
  public List<InfluencingInstructionNode> getInfluencing(final InstructionGraphNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");

    // When walking downwards, the influencing nodes of a node
    // are its parents.

    final List<InfluencingInstructionNode> nodes = new ArrayList<InfluencingInstructionNode>();

    for (final InstructionGraphEdge edge : node.getIncomingEdges()) {
      nodes.add(new InfluencingInstructionNode(edge.getSource(), new WalkInformation(edge)));
    }

    return nodes;
  }
}
