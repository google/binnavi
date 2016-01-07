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
package com.google.security.zynamics.binnavi.API.reil.mono;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.API.disassembly.IGraphNode;


// ! Used to walk instruction graphs downwards.
/**
 * InstructionGraph walker that can be used to walk downwards through InstructionGraph nodes.
 *
 * @param <GraphNode> Type of the nodes in the walked graph.
 */
public final class DownWalker<GraphNode extends IGraphNode<GraphNode>>
    implements IGraphWalker<GraphNode, Object> {
  @Override
  public List<GraphNode> getInfluenced(final GraphNode node) {
    if (node == null) {
      throw new IllegalArgumentException("Error: Node argument can not be null");
    }

    // When walking downwards, the influenced nodes of a node
    // are its children.

    final List<GraphNode> nodes = new ArrayList<GraphNode>();

    for (final GraphNode child : node.getChildren()) {
      nodes.add(child);
    }

    return nodes;
  }

  @Override
  public List<IInfluencingNode<GraphNode, Object>> getInfluencing(final GraphNode node) {
    if (node == null) {
      throw new IllegalArgumentException("Error: Node argument can not be null");
    }

    // When walking downwards, the influencing nodes of a node
    // are its parents.

    final List<IInfluencingNode<GraphNode, Object>> nodes =
        new ArrayList<IInfluencingNode<GraphNode, Object>>();

    for (final GraphNode parent : node.getParents()) {
      nodes.add(new DefaultInfluencingNode<GraphNode, Object>(parent));
    }

    return nodes;
  }
}
