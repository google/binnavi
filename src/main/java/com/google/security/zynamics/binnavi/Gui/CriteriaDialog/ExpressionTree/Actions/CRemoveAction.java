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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Implementations.CCriteriumFunctions;


/**
 * Action class used for removing a criterium from a criterium tree.
 */
public final class CRemoveAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -960630402266055154L;

  /**
   * The tree from which the node is removed.
   */
  private final CCriteriumTree m_tree;

  /**
   * The criterium tree node to remove.
   */
  private final ICriteriumTreeNode m_node;

  /**
   * Creates a new action object.
   *
   * @param tree The tree from which the node is removed.
   * @param node The criterium tree node to remove.
   */
  public CRemoveAction(final CCriteriumTree tree, final ICriteriumTreeNode node) {
    super("Remove");

    m_tree = tree;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CCriteriumFunctions.remove(m_tree, m_node);
  }
}
