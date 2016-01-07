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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Represents basic blocks in functions.
 */
public final class CBlockNode implements IBlockNode {
  /**
   * The basic block the block node represents.
   */
  private final INaviBasicBlock m_block;

  /**
   * Parents of the block node in the function it belongs to.
   */
  private final List<IBlockNode> m_parents = new ArrayList<IBlockNode>();

  /**
   * Children of the block node in the function it belongs to.
   */
  private final List<IBlockNode> m_children = new ArrayList<IBlockNode>();

  /**
   * The edges that lead from the block node to its children.
   */
  private final List<CFunctionEdge> m_outgoingEdges = new ArrayList<CFunctionEdge>();

  /**
   * Creates a new block node object.
   * 
   * @param block The basic block the node represents.
   */
  public CBlockNode(final INaviBasicBlock block) {
    m_block = Preconditions.checkNotNull(block, "IE01231: Block argument can not be null");
  }

  /**
   * Links two basic blocks through an edge.
   * 
   * @param edge The edge that leads from the parent to the child.
   * @param parent The parent node to link.
   * @param child The child node to link.
   */
  public static void link(final CFunctionEdge edge, final CBlockNode parent, final CBlockNode child) {
    parent.m_outgoingEdges.add(Preconditions.checkNotNull(edge,
        "IE01232: Edge argument can not be null"));
    parent.m_children.add(Preconditions.checkNotNull(parent,
        "IE01233: Parent argument can not be null"));
    child.m_parents.add(Preconditions
        .checkNotNull(child, "IE01234: Child argument can not be null"));
  }

  @Override
  public IAddress getAddress() {
    return m_block.getAddress();
  }

  @Override
  public INaviBasicBlock getBlock() {
    return m_block;
  }

  @Override
  public List<IBlockNode> getChildren() {
    return new ArrayList<IBlockNode>(m_children);
  }

  @Override
  public Iterable<INaviInstruction> getInstructions() {
    return m_block.getInstructions();
  }

  @Override
  public INaviInstruction getLastInstruction() {
    final List<INaviInstruction> instructions = m_block.getInstructions();
    return instructions.isEmpty() ? null : instructions.get(instructions.size() - 1);
  }

  @Override
  public List<CFunctionEdge> getOutgoingEdges() {
    return new ArrayList<CFunctionEdge>(m_outgoingEdges);
  }

  @Override
  public List<IBlockNode> getParents() {
    return new ArrayList<IBlockNode>(m_parents);
  }

  @Override
  public boolean hasInstruction(final INaviInstruction instruction) {
    return m_block.getInstructions().contains(instruction);
  }
}
