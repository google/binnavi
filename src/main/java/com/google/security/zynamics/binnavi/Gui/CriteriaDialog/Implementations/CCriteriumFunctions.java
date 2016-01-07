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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Implementations;

import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.CConditionBox;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CCriteriumWrapper;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTreeNode;


/**
 * Contains the implementations of the actions available in the criterium tree.
 */
public final class CCriteriumFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CCriteriumFunctions() {
  }

  private static void disposeTree(final CCriteriumTreeNode root) {
    root.getCriterium().dispose();

    for (final CCriteriumTreeNode child : root.getChildren()) {
      disposeTree(child);
    }
  }

  /**
   * Appends a criterium to a criterium tree.
   *
   * @param tree Tree where the criterium is appended.
   * @param parent Parent node of the appended criterium.
   * @param criterium The criterium to append.
   */
  public static void appendCriterium(
      final CCriteriumTree tree, final CCriteriumTreeNode parent, final ICriterium criterium) {
    tree.appendNode(parent, new CCriteriumTreeNode(criterium));
  }

  /**
   * Appends a criterium to the criterium tree. The criterium is selected through the given combo
   * box.
   *
   * @param jtree Visible criteria tree.
   * @param ctree Backs the visible criteria tree.
   * @param conditionBox Provides the criterium to add.
   */
  public static void appendCriterium(
      final JCriteriumTree jtree, final CCriteriumTree ctree, final CConditionBox conditionBox) {
    final TreePath path = jtree.getSelectionPath();

    if (path != null) {
      final JCriteriumTreeNode criteriumTreeNode = (JCriteriumTreeNode) path.getLastPathComponent();

      final CCriteriumTreeNode parent =
          CCriteriumFunctions.findNode(ctree.getRoot(), criteriumTreeNode.getCriterium());

      final CCriteriumWrapper selectedItem = (CCriteriumWrapper) conditionBox.getSelectedItem();

      if (selectedItem != null) {
        final ICriterium criterium = selectedItem.getObject().createCriterium();

        if (criterium != null) {
          appendCriterium(ctree, parent, criterium);
        }
      }
    }
  }

  /**
   * Searches for a criterium tree node that represents a given criterium.
   *
   * @param node Root node where the search starts.
   * @param criterium The criterium to search for.
   *
   * @return The node that represents the criterium.
   */
  public static CCriteriumTreeNode findNode(
      final CCriteriumTreeNode node, final ICriterium criterium) {
    if (node.getCriterium() == criterium) {
      return node;
    }

    for (final CCriteriumTreeNode child : node.getChildren()) {
      final CCriteriumTreeNode childNode = findNode(child, criterium);

      if (childNode != null) {
        return childNode;
      }
    }

    return null;
  }

  /**
   * Inserts a criterium into a criterium tree.
   *
   * @param tree Tree where the criterium is inserted.
   * @param parent Parent node of the inserted criterium.
   * @param criterium The criterium to insert.
   */
  public static void insertCriterium(
      final CCriteriumTree tree, final CCriteriumTreeNode parent, final ICriterium criterium) {
    tree.insertNode(parent, new CCriteriumTreeNode(criterium));
  }

  /**
   * Removes a single node from the criterium tree.
   *
   * @param tree The tree from which the node is removed.
   * @param node The removed node.
   */
  public static void remove(final CCriteriumTree tree, final ICriteriumTreeNode node) {
    node.getCriterium().dispose();

    tree.remove(node);
  }

  /**
   * Removes all nodes from a criterium tree.
   *
   * @param tree The criterium tree to reset.
   */
  public static void removeAll(final CCriteriumTree tree) {
    disposeTree(tree.getRoot());

    tree.clear();
  }
}
