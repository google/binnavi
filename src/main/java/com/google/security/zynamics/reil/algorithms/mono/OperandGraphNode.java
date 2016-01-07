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

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;

import java.util.ArrayList;
import java.util.List;


public final class OperandGraphNode implements IGraphNode<OperandGraphNode> {
  private final ReilInstruction m_instruction;
  private final int m_index;

  private final List<OperandGraphNode> m_children = new ArrayList<OperandGraphNode>();
  private final List<OperandGraphNode> m_parents = new ArrayList<OperandGraphNode>();

  public OperandGraphNode(final ReilInstruction instruction, final int index) {
    m_instruction = instruction;
    m_index = index;
  }

  public static void link(final OperandGraphNode parent, final OperandGraphNode child) {
    parent.m_children.add(child);
    child.m_parents.add(parent);
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof OperandGraphNode)) {
      return false;
    }

    final OperandGraphNode rhsNode = (OperandGraphNode) rhs;

    return (m_index == rhsNode.getIndex()) && m_instruction.equals(rhsNode.getInstruction());
  }

  @Override
  public List<OperandGraphNode> getChildren() {
    return new ArrayList<OperandGraphNode>(m_children);
  }

  public int getIndex() {
    return m_index;
  }

  public ReilInstruction getInstruction() {
    return m_instruction;
  }

  @Override
  public List<OperandGraphNode> getParents() {
    return new ArrayList<OperandGraphNode>(m_parents);
  }

  public String getValue() {
    switch (m_index) {
      case 0:
        return m_instruction.getFirstOperand().getValue();
      case 1:
        return m_instruction.getSecondOperand().getValue();
      case 2:
        return m_instruction.getThirdOperand().getValue();
      default:
        throw new IllegalStateException("Error: Unknown operand index");
    }
  }

  @Override
  public int hashCode() {
    return m_index * m_instruction.hashCode();
  }

  @Override
  public String toString() {
    return m_instruction.toString() + "/" + m_index;
  }
}
