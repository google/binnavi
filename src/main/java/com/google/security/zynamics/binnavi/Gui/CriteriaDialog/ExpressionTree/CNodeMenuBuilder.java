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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.CNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Or.COrCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CAddConditionAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CAppendAndOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CAppendNotOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CAppendOrOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CInsertAndOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CInsertNotOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CInsertOrOperatorAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CRemoveAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.Actions.CRemoveAllAction;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Implementations.CCriteriumFunctions;


/**
 * Menu builder that builds the context menus for each node of the criterium tree.
 */
public final class CNodeMenuBuilder {
  /**
   * Node for which the menu item is built.
   */
  private final JCriteriumTreeNode m_criteriumNode;

  /**
   * The context menu built by the menu builder.
   */
  private final JPopupMenu m_popup = new JPopupMenu();

  /**
   * Menu item used to insert AND conditions.
   */
  private final JMenuItem m_insertAnd;

  /**
   * Menu item used to insert OR conditions.
   */
  private final JMenuItem m_insertOr;

  /**
   * Menu item used to insert NOT conditions.
   */
  private final JMenuItem m_insertNot;

  /**
   * Menu item used to append AND conditions.
   */
  private final JMenuItem m_appendAnd;

  /**
   * Menu item used to append OR conditions.
   */
  private final JMenuItem m_appendOr;

  /**
   * Menu item used to append NOT conditions.
   */
  private final JMenuItem m_appendNot;

  /**
   * Sub menu where the individual conditions are listed.
   */
  private final JMenu m_conditionSubmenu;

  /**
   * Menu item used to remove one node from the tree.
   */
  private final JMenuItem m_remove;

  /**
   * Menu item used to reset the tree.
   */
  private final JMenuItem m_removeAll;

  /**
   * Creates a new menu builder object.
   *
   * @param node Node for which the menu item is built.
   * @param ctree Criterium tree to modify by the menu.
   * @param criteria List of available criteria.
   */
  public CNodeMenuBuilder(final JCriteriumTreeNode node, final CCriteriumTree ctree,
      final List<ICriteriumCreator> criteria) {
    m_criteriumNode = node;

    final CCriteriumTreeNode clickedCriterium = CCriteriumFunctions.findNode(
        ctree.getRoot(), node.getCriterium());

    m_appendAnd = new JMenuItem(new CAppendAndOperatorAction(ctree, clickedCriterium));
    m_appendOr = new JMenuItem(new CAppendOrOperatorAction(ctree, clickedCriterium));
    m_appendNot = new JMenuItem(new CAppendNotOperatorAction(ctree, clickedCriterium));

    m_popup.add(m_appendAnd);
    m_popup.add(m_appendOr);
    m_popup.add(m_appendNot);

    m_popup.add(new JSeparator());

    m_insertAnd = new JMenuItem(new CInsertAndOperatorAction(ctree, clickedCriterium));
    m_insertOr = new JMenuItem(new CInsertOrOperatorAction(ctree, clickedCriterium));
    m_insertNot = new JMenuItem(new CInsertNotOperatorAction(ctree, clickedCriterium));

    m_popup.add(m_insertAnd);
    m_popup.add(m_insertOr);
    m_popup.add(m_insertNot);

    m_popup.add(new JSeparator());

    m_conditionSubmenu = new JMenu("Create Condition");

    for (final ICriteriumCreator condition : criteria) {
      m_conditionSubmenu.add(
          new JMenuItem(new CAddConditionAction(ctree, clickedCriterium, condition)));
    }

    m_popup.add(m_conditionSubmenu);

    m_popup.add(new JSeparator());

    m_remove = new JMenuItem(new CRemoveAction(ctree, clickedCriterium));
    m_popup.add(m_remove);

    m_popup.add(new JSeparator());

    m_removeAll = new JMenuItem(new CRemoveAllAction(ctree));
    m_popup.add(m_removeAll);

  }

  /**
   * Updates the state of the context menu depending on the state of the criterium tree.
   */
  private void updateMenuState() {
    m_appendAnd.setEnabled(m_criteriumNode.allowAppend(CAndCriterium.class));
    m_appendOr.setEnabled(m_criteriumNode.allowAppend(COrCriterium.class));
    m_appendNot.setEnabled(m_criteriumNode.allowAppend(CNotCriterium.class));

    m_insertAnd.setEnabled(m_criteriumNode.allowInsert(CAndCriterium.class));
    m_insertOr.setEnabled(m_criteriumNode.allowInsert(COrCriterium.class));
    m_insertNot.setEnabled(m_criteriumNode.allowInsert(CNotCriterium.class));

    m_conditionSubmenu.setEnabled(m_criteriumNode.allowAppend(CConditionCriterium.class));

    m_remove.setEnabled(!m_criteriumNode.isRoot());

    m_removeAll.setEnabled(m_criteriumNode.getChildCount() != 0);
  }

  /**
   * Returns the popup menu built by the menu builder.
   *
   * @return The built popup menu.
   */
  public JPopupMenu getPopup() {
    updateMenuState();

    return m_popup;
  }
}
