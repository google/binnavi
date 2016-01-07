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

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Implementations.CCriteriumFunctions;


/**
 * Action class used for appending a criterium to a criterium tree.
 */
public final class CAddConditionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 932075293123020152L;

  /**
   * Tree where the criterium is appended.
   */
  private final CCriteriumTree m_tree;

  /**
   * Parent node of the new criterium node.
   */
  private final CCriteriumTreeNode m_parent;

  /**
   * Creates the criterium of the new node.
   */
  private final ICriteriumCreator m_condition;

  /**
   * Creates a new action object.
   *
   * @param tree Tree where the criterium is inserted.
   * @param parent Parent node of the new criterium node.
   * @param condition Creates the criterium of the new node.
   */
  public CAddConditionAction(final CCriteriumTree tree, final CCriteriumTreeNode parent,
      final ICriteriumCreator condition) {
    super(condition.getCriteriumDescription());

    m_tree = tree;
    m_parent = parent;
    m_condition = condition;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CCriteriumFunctions.appendCriterium(m_tree, m_parent, m_condition.createCriterium());
  }
}
