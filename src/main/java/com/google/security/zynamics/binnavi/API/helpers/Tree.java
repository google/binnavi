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
package com.google.security.zynamics.binnavi.API.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// ! Class used to create trees.
/**
 * Class that can be used to create trees.
 *
 * @param <ObjectType> Type of the objects stored in the tree.
 */
public final class Tree<ObjectType> {
  /**
   * Root node of the tree.
   */
  private final TreeNode<ObjectType> m_rootNode;

  // / @cond INTERNAL
  /**
   * Creates a new API tree object.
   *
   * @param tree The wrapped internal tree object.
   */
  // / @endcond
  public Tree(final com.google.security.zynamics.zylib.types.trees.Tree<ObjectType> tree) {
    m_rootNode = new TreeNode<ObjectType>(tree.getRootNode().getObject());

    generate(m_rootNode, tree.getRootNode());
  }

  /**
   * Copies an internal tree object into an API tree object.
   *
   * @param <ObjectType> Type of the objects stored in the tree.
   *
   * @param apiParent The API tree to extend.
   * @param nativeNode The native tree that provides the data.
   */
  private static <ObjectType> void generate(final TreeNode<ObjectType> apiParent,
      final com.google.security.zynamics.zylib.types.trees.ITreeNode<ObjectType> nativeNode) {
    for (final com.google.security.zynamics.zylib.types.trees.ITreeNode<ObjectType> child :
        nativeNode.getChildren()) {
      final TreeNode<ObjectType> newChild = new TreeNode<ObjectType>(child.getObject());

      TreeNode.link(apiParent, newChild);

      generate(newChild, child);
    }
  }

  // ! The root node of the tree.
  /**
   * Returns the root node of the tree.
   *
   * @return The root node of the tree.
   */
  public TreeNode<ObjectType> getRootNode() {
    return m_rootNode;
  }

  // ! The nodes of the tree
  /**
   * Returns a list of all nodes in the tree.
   *
   * @return A list of all nodes in the tree.
   */
  public List<TreeNode<ObjectType>> getTreeNodes() {
    TreeNode<ObjectType> currentNode = m_rootNode;
    final List<TreeNode<ObjectType>> nodeList = new ArrayList<TreeNode<ObjectType>>();
    nodeList.add(m_rootNode);

    final Stack<TreeNode<ObjectType>> nodesToVisit = new Stack<TreeNode<ObjectType>>();

    for (final TreeNode<ObjectType> treeNode : m_rootNode.getChildren()) {
      nodesToVisit.push(treeNode);
    }

    while (!nodesToVisit.isEmpty()) {
      currentNode = nodesToVisit.pop();
      nodeList.add(currentNode);
      for (final TreeNode<ObjectType> treeNode : currentNode.getChildren()) {
        nodesToVisit.push(treeNode);
      }
    }

    return nodeList;
  }

  // ! Printable representation of the tree.
  /**
   * Returns a string representation of the tree.
   *
   * @return A string representation of the tree.
   */
  @Override
  public String toString() {
    return "Tree with root node " + m_rootNode.toString();
  }
}
