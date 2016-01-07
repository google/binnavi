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

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;


/**
 * Node class for nodes in cached Select by Criteria trees.
 */
public final class CCachedExpressionTreeNode implements IAbstractCriteriumTreeNode {
  /**
   * Children of the node.
   */
  private final List<CCachedExpressionTreeNode> m_children =
      new ArrayList<CCachedExpressionTreeNode>();

  /**
   * Criterium represented by this node.
   */
  private final ICachedCriterium m_criterium;

  /**
   * Creates a new node object.
   *
   * @param criterium Criterium represented by this node.
   */
  public CCachedExpressionTreeNode(final ICachedCriterium criterium) {
    m_criterium = criterium;
  }

  /**
   * Appends a new node to a parent node.
   *
   * @param parent Parent node of the new node.
   * @param child The child node to append.
   */
  public static void append(
      final CCachedExpressionTreeNode parent, final CCachedExpressionTreeNode child) {
    parent.getChildren().add(child);
  }

  @Override
  public List<CCachedExpressionTreeNode> getChildren() {
    return m_children;
  }

  @Override
  public ICachedCriterium getCriterium() {
    return m_criterium;
  }
}
