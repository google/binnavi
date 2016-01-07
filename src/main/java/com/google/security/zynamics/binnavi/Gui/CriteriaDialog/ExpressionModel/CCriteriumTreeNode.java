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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;


/**
 * Represents a single node in criterium trees.
 */
public final class CCriteriumTreeNode implements ICriteriumTreeNode {
  /**
   * Parent of the node.
   */
  private ICriteriumTreeNode m_parent;

  /**
   * Children of the node.
   */
  private final List<CCriteriumTreeNode> m_children = new ArrayList<CCriteriumTreeNode>();

  /**
   * Criterium represented by this node.
   */
  private final ICriterium m_criterium;

  /**
   * Creates a new node object.
   *
   * @param criterium Criterium represented by this node.
   */
  public CCriteriumTreeNode(final ICriterium criterium) {
    m_criterium = criterium;
  }

  /**
   * Appends a new node to a parent node.
   *
   * @param parent Parent node of the new node.
   * @param child The child node to append.
   */
  public static void append(final CCriteriumTreeNode parent, final CCriteriumTreeNode child) {
    parent.getChildren().add(child);
    child.m_parent = parent;
  }

  /**
   * Inserts a node into the criterium tree.
   *
   * @param parent The parent tree of the inserted node.
   * @param child The node to insert.
   */
  public static void insert(final CCriteriumTreeNode parent, final CCriteriumTreeNode child) {
    for (final CCriteriumTreeNode grandchild : parent.getChildren()) {
      child.getChildren().add(grandchild);
      grandchild.m_parent = child;
    }

    parent.getChildren().clear();

    parent.getChildren().add(child);
    child.m_parent = parent;
  }

  /**
   * Removes a single node from the criterium tree.
   *
   * @param node The node to remove.
   */
  public static void remove(final ICriteriumTreeNode node) {
    for (final ICriteriumTreeNode child : node.getChildren()) {
      remove(child);
    }

    node.getChildren().clear();
  }

  /**
   * Creates a cached tree node with fixed input data.
   *
   * @return The created cached node.
   */
  public CCachedExpressionTreeNode createCachedNode() {
    return new CCachedExpressionTreeNode(m_criterium.createCachedCriterium());
  }

  @Override
  public List<CCriteriumTreeNode> getChildren() {
    return m_children;
  }

  @Override
  public ICriterium getCriterium() {
    return m_criterium;
  }

  @Override
  public ICriteriumTreeNode getParent() {
    return m_parent;
  }
}
