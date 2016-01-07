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

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.CNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Or.COrCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;



/**
 * Class that represents single nodes of the visible criterium tree.
 */
public final class JCriteriumTreeNode extends IconNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5235703104912059226L;

  /**
   * Icon shown in the tree for this node.
   */
  private static final ImageIcon DEFAULT_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/default_condition.png"));

  /**
   * Criterium represented by this node.
   */
  private final ICriterium m_criterium;

  /**
   * Builds the context menu of this node.
   */
  private final CNodeMenuBuilder m_menuBuilder;

  /**
   * Creates a new tree node object.
   *
   * @param ctree Criterium tree that backs the visible tree.
   * @param criterium Criterium represented by this node.
   * @param criteria List of available criteria.
   */
  public JCriteriumTreeNode(final CCriteriumTree ctree, final ICriterium criterium,
      final List<ICriteriumCreator> criteria) {
    m_criterium = criterium;

    m_menuBuilder = new CNodeMenuBuilder(this, ctree, criteria);
  }

  /**
   * Checks whether a NOT node can be inserted after the current node.
   *
   * @return True, if a NOT node can be inserted. False, otherwise.
   */
  private boolean allowNotInsert() {
    final int childCount = getChildCount();

    if (!(getCriterium() instanceof CNotCriterium) && childCount == 1
        && !(((JCriteriumTreeNode) children.get(0)).getCriterium() instanceof CNotCriterium)) {
      return true;
    }

    return false;
  }

  /**
   * Checks whether a node with the given type can be appended to this node.
   *
   * @param appendType The type of the criterium to append.
   *
   * @return True, if the criterium can be appended. False, otherwise.
   */
  public boolean allowAppend(final Class<?> appendType) {
    final int count = getChildCount();

    if (isRoot() && getChildCount() > 0) {
      return false;
    }

    if (getCriterium() instanceof CConditionCriterium && !isRoot()) {
      return false;
    }

    if (appendType == CConditionCriterium.class) {
      return (getCriterium() instanceof CConditionCriterium && isRoot() && count == 0)
          || !(getCriterium() instanceof CNotCriterium) || count == 0;
    }

    if (appendType == CAndCriterium.class || appendType == COrCriterium.class) {
      return (!(getCriterium() instanceof CNotCriterium) || getChildCount() == 0);
    }

    if (appendType == CNotCriterium.class) {
      return !(getCriterium() instanceof CNotCriterium);
    }

    return false;
  }

  /**
   * Checks whether a node with the given type can be inserted after this node.
   *
   * @param insertType The type of the criterium to insert.
   *
   * @return True, if the criterium can be inserted. False, otherwise.
   */
  public boolean allowInsert(final Class<?> insertType) {
    if (getCriterium() instanceof CConditionCriterium && !isRoot()) {
      return false;
    }

    if (insertType == CConditionCriterium.class) {
      return false;
    }

    if (insertType == CAndCriterium.class || insertType == COrCriterium.class) {
      return getChildCount() > 0;
    }

    if (insertType == CNotCriterium.class) {
      return allowNotInsert();
    }

    return true;
  }

  /**
   * Returns the criterium represented by this node.
   *
   * @return The criterium represented by this node.
   */
  public ICriterium getCriterium() {
    return m_criterium;
  }

  @Override
  public Icon getIcon() {
    final Icon icon = getCriterium().getIcon();

    if (icon == null) {
      return DEFAULT_ICON;
    }

    return icon;
  }

  /**
   * Returns the popup menu to display for this node.
   *
   * @return The context menu of this node.
   */
  public JPopupMenu getPopupMenu() {
    return m_menuBuilder.getPopup();
  }

  @Override
  public String toString() {
    return getCriterium().getCriteriumDescription();
  }
}
