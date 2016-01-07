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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;

// / Represents a single operand of a disassembled instruction.
/**
 * Represents an operand of a disassembled instruction. Each operand is stored as a tree of operand
 * expressions with a root node and a number of child nodes that form the operand.
 */
public final class Operand implements ApiObject<COperandTree> {
  /**
   * Root node of the operand tree.
   */
  private final OperandExpression m_root;

  /**
   * Wrapped internal operand object.
   */
  private final COperandTree m_operand;

  // / @cond INTERNAL
  /**
   * Creates a new API operand object.
   *
   * @param operand Wrapped internal operand object.
   */
  // / @endcond
  public Operand(final COperandTree operand) {
    m_operand = Preconditions.checkNotNull(operand, "Error: Operand argument can't be null");

    m_root = clone(operand.getRootNode(), null);
  }

  // ! Creates a new operand.
  /**
   * Creates a new operand.
   *
   * @param module The module the operand belongs to.
   * @param root The root node of the operand.
   *
   * @return The created operand.
   */
  public static Operand create(final Module module, final OperandExpression root) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    Preconditions.checkNotNull(root, "Error: Root argument can not be null");

    final COperandTree opTree = module.getNative().createOperand((COperandTreeNode) root.getNative());

    return new Operand(opTree);
  }

  /**
   * Converts an internal operand tree into an API operand tree.
   *
   * @param currentNode Node to convert.
   * @param parentExpression Parent API node the converted node is attached to.
   *
   * @return The converted node.
   */
  private OperandExpression clone(
      final INaviOperandTreeNode currentNode, final OperandExpression parentExpression) {
    final OperandExpression childExpression = new OperandExpression(currentNode);

    if (parentExpression != null) {
      OperandExpression.link(parentExpression, childExpression);
    }

    for (final INaviOperandTreeNode child : currentNode.getChildren()) {
      clone(child, childExpression);
    }

    return childExpression;
  }

  @Override
  public COperandTree getNative() {
    return m_operand;
  }

  // ! Root node of the expression tree.
  /**
   * Returns the the root expression of the operand tree.
   *
   * @return The root expression of the operand tree.
   */
  public OperandExpression getRootNode() {
    return m_root;
  }

  // ! Printable representation of the operand.
  /**
   * Returns the string representation of the operand.
   *
   * @return The string representation of the operand.
   */
  @Override
  public String toString() {
    return m_operand.toString();
  }
}
