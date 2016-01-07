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
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.strings.Commafier;

/**
 * Represents the operand tree where the individual operand expressions of a single operand are
 * stored.
 */
public final class COperandTree implements INaviOperandTree {
  /**
   * The root node of the operand tree.
   */
  private final COperandTreeNode root;

  /**
   * Used to synchronize the operand tree with the database.
   */
  private final SQLProvider provider;

  /**
   * Instruction the operand tree belongs to.
   */
  private INaviInstruction instruction;

  private final TypeManager typeManager;
  private final TypeInstanceContainer instanceContainer;

  /**
   * Creates a new operand tree node.
   * 
   * @param rootNode The root node of the operand tree.
   * @param provider Used to synchronize the operand tree with the database.
   */
  public COperandTree(final COperandTreeNode rootNode, final SQLProvider provider,
      final TypeManager typeManager, final TypeInstanceContainer instanceContainer) {
    Preconditions.checkNotNull(rootNode, "IE00212: Root node can't be null.");
    this.typeManager = Preconditions.checkNotNull(typeManager, "Type manager can not be null.");
    this.instanceContainer =
        Preconditions.checkNotNull(instanceContainer, "Type instance container can not be null.");
    this.provider = Preconditions.checkNotNull(provider, "Sql provider can not be null.");
    if (rootNode.getChildren().size() == 0) {
      final COperandTreeNode realParent = new COperandTreeNode(
          -1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null, new ArrayList<IReference>(),
          provider, typeManager, instanceContainer);
      COperandTreeNode.link(realParent, rootNode);
      root = realParent;
    } else {
      root = rootNode;
    }
    initializeTree(root);
  }

  /**
   * Collects all nodes of an operand tree.
   * 
   * @param currentNode The current node to process.
   * @param nodes The processed nodes.
   */
  private static void collect(final INaviOperandTreeNode currentNode,
      final List<INaviOperandTreeNode> nodes) {
    nodes.add(currentNode);

    for (final INaviOperandTreeNode child : currentNode.getChildren()) {
      collect(child, nodes);
    }
  }

  /**
   * Initializes a tree node and all of its children.
   * 
   * @param node The tree node to initialize.
   */
  private void initializeTree(final COperandTreeNode node) {
    node.setOperand(this);

    for (final IOperandTreeNode child : node.getChildren()) {
      initializeTree((COperandTreeNode) child);
    }
  }

  /**
   * Generates the string representation of a tree node.
   * 
   * @param node The tree node.
   * 
   * @return The generated string.
   */
  private String toString(final IOperandTreeNode node) {
    final ArrayList<String> flattenedChildren = new ArrayList<String>();

    for (final IOperandTreeNode child : node.getChildren()) {
      flattenedChildren.add(toString(child));
    }

    if (flattenedChildren.isEmpty()) {
      return node.toString();
    } else if (flattenedChildren.size() == 1) {
      final ExpressionType type = node.getType();

      if (type == ExpressionType.MEMDEREF) {
        return "[" + flattenedChildren.get(0) + "]";
      } else if (type == ExpressionType.EXPRESSION_LIST) {
        return "{" + Commafier.commafy(flattenedChildren) + "}";
      } else {
        return node.toString() + (node.toString().isEmpty() ? "" : " ") + flattenedChildren.get(0);
      }
    } else {
      final String value = node.toString();

      final StringBuffer stringBuffer = new StringBuffer();

      for (int i = 0; i < flattenedChildren.size(); i++) {
        stringBuffer.append(flattenedChildren.get(i));

        if (i != (flattenedChildren.size() - 1)) {
          stringBuffer.append(' ');
          stringBuffer.append(value);
          stringBuffer.append(' ');
        }
      }

      return stringBuffer.toString();
    }
  }

  /**
   * Assigns the instruction the operand tree belongs to.
   * 
   * @param instruction The instruction the operand tree belongs to.
   */
  protected void setNaviInstruction(final INaviInstruction instruction) {
    Preconditions.checkArgument(
        this.instruction == null, "IE00213: Instruction already initialized");
    this.instruction = instruction;
  }

  public COperandTree cloneTree() {
    return new COperandTree(root.cloneNode(), provider, typeManager, instanceContainer);
  }

  /**
   * Closes the operand tree.
   */
  public void close() {
    for (final INaviOperandTreeNode node : getNodes()) {
      node.close();
    }
  }

  @Override
  public INaviInstruction getInstruction() {
    return instruction;
  }

  /**
   * Returns all nodes that belong to the tree.
   * 
   * @return A list of all nodes in the tree.
   */
  public List<INaviOperandTreeNode> getNodes() {
    final List<INaviOperandTreeNode> nodes = new ArrayList<INaviOperandTreeNode>();
    collect(root, nodes);
    return nodes;
  }

  @Override
  public COperandTreeNode getRootNode() {
    return root;
  }

  @Override
  public String toString() {
    return toString(root);
  }
}
