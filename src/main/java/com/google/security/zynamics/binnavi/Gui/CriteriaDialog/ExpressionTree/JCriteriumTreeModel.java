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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumListener;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeListener;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeNode;


/**
 * Model that backs the visible criterium tree.
 */
public final class JCriteriumTreeModel extends DefaultTreeModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2472212228826158436L;

  /**
   * Tree the model belongs to.
   */
  private final JTree m_jtree;

  /**
   * Criterium tree that backs the visible tree.
   */
  private final CCriteriumTree m_criteriumTree;

  /**
   * List of available criteria.
   */
  private final List<ICriteriumCreator> m_criteria;

  /**
   * Updates the visible tree on changes in the criterium tree.
   */
  private final ICriteriumTreeListener m_internalTreeListener = new CriteriumTreeListener();

  /**
   * Updates the tree on changes in the criteria.
   */
  private final InternalCriteriumListener m_internalCriteriumListener =
      new InternalCriteriumListener();

  /**
   * Creates a new tree model.
   * 
   * @param jtree Tree the model belongs to.
   * @param criteriumTree Criterium tree that backs the visible tree.
   * @param criteria List of available criteria.
   */
  public JCriteriumTreeModel(final JTree jtree, final CCriteriumTree criteriumTree,
      final List<ICriteriumCreator> criteria) {
    super(new JCriteriumTreeNode(criteriumTree, criteriumTree.getRoot().getCriterium(), criteria));

    m_jtree = jtree;

    m_criteriumTree = criteriumTree;

    m_criteria = criteria;

    m_criteriumTree.addListener(m_internalTreeListener);
  }

  /**
   * Finds a visible tree node that represents a given criterium.
   * 
   * @param node Root node where the search begins.
   * @param criterium Criterium to search for.
   * 
   * @return The visible tree node that represents the given criterium.
   */
  private JCriteriumTreeNode findParentNode(final JCriteriumTreeNode node,
      final ICriterium criterium) {
    if (node.getCriterium() == criterium) {
      return node;
    }

    for (int i = 0; i < node.getChildCount(); i++) {
      final JCriteriumTreeNode child = (JCriteriumTreeNode) node.getChildAt(i);

      final JCriteriumTreeNode parent = findParentNode(child, criterium);

      if (parent != null) {
        return parent;
      }
    }

    return null;
  }

  /**
   * Sorts visible tree nodes.
   * 
   * @param parentNode Parent nodes whose children are sorted.
   */
  private void sortChildren(final JCriteriumTreeNode parentNode) {
    final List<JCriteriumTreeNode> operators = new ArrayList<JCriteriumTreeNode>();
    final List<JCriteriumTreeNode> conditions = new ArrayList<JCriteriumTreeNode>();
    final List<JCriteriumTreeNode> minus = new ArrayList<JCriteriumTreeNode>();

    final Enumeration<?> children = parentNode.children();

    while (children.hasMoreElements()) {
      final JCriteriumTreeNode child = (JCriteriumTreeNode) children.nextElement();
      final ICriterium type = child.getCriterium();

      if (type instanceof CConditionCriterium) {
        conditions.add(child);
      } else {
        operators.add(child);
      }
    }

    parentNode.removeAllChildren();

    for (final JCriteriumTreeNode child : operators) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
    for (final JCriteriumTreeNode child : conditions) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
    for (final JCriteriumTreeNode child : minus) {
      parentNode.add(child);
      child.setParent(parentNode);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_criteriumTree.removeListener(m_internalTreeListener);
  }

  @Override
  public void nodeStructureChanged(final TreeNode node) {
    final Set<ICriterium> criteriumSet = new HashSet<ICriterium>();

    final Enumeration<TreePath> expandedPaths =
        m_jtree.getExpandedDescendants(new TreePath(getRoot()));
    if (expandedPaths != null) {
      while (expandedPaths.hasMoreElements()) {
        final TreePath path = expandedPaths.nextElement();
        final JCriteriumTreeNode expandedNode = (JCriteriumTreeNode) path.getLastPathComponent();
        criteriumSet.add(expandedNode.getCriterium());
      }
    }

    super.nodeStructureChanged(node);

    final Enumeration<?> nodes = ((JCriteriumTreeNode) getRoot()).breadthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      final JCriteriumTreeNode nextNode = (JCriteriumTreeNode) nodes.nextElement();
      if (criteriumSet.contains(nextNode.getCriterium())) {
        m_jtree.expandPath(new TreePath(nextNode.getPath()));
      }
    }
  }

  /**
   * Updates the visible tree on changes in the criterium tree.
   */
  private class CriteriumTreeListener implements ICriteriumTreeListener {
    @Override
    public void appendedNode(final CCriteriumTree criteriumTree, final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      final JCriteriumTreeNode parentNode =
          findParentNode((JCriteriumTreeNode) getRoot(), parent.getCriterium());

      final JCriteriumTreeNode childNode =
          new JCriteriumTreeNode(criteriumTree, child.getCriterium(), m_criteria);

      parentNode.add(childNode);
      childNode.setParent(parentNode);

      sortChildren(parentNode);

      nodeStructureChanged(parentNode);

      m_jtree.setSelectionPath(new TreePath(childNode.getPath()));

      child.getCriterium().addListener(m_internalCriteriumListener);
    }

    @Override
    public void insertedNode(final CCriteriumTree criteriumTree, final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      final JCriteriumTreeNode parentNode =
          findParentNode((JCriteriumTreeNode) getRoot(), parent.getCriterium());

      final JCriteriumTreeNode newNode =
          new JCriteriumTreeNode(criteriumTree, child.getCriterium(), m_criteria);

      final List<JCriteriumTreeNode> grandChildren = new ArrayList<JCriteriumTreeNode>();

      final Enumeration<?> enumeration = parentNode.children();

      // has to be cached, other wise hasMoreElements returns false when child count / 2 is reached
      while (enumeration.hasMoreElements()) {
        grandChildren.add((JCriteriumTreeNode) enumeration.nextElement());
      }

      // can't be done within the above while loop
      for (final JCriteriumTreeNode grandChild : grandChildren) {
        newNode.add(grandChild);
        grandChild.setParent(newNode);
      }

      parentNode.removeAllChildren();

      parentNode.add(newNode);
      newNode.setParent(parentNode);

      nodeStructureChanged(parentNode);

      m_jtree.setSelectionPath(new TreePath(newNode.getPath()));

      child.getCriterium().addListener(m_internalCriteriumListener);
    }

    @Override
    public void removedAll(final CCriteriumTree criteriumTree) {
      // TODO: Remove listeners

      final JCriteriumTreeNode rootNode = (JCriteriumTreeNode) getRoot();

      rootNode.removeAllChildren();

      nodeStructureChanged(rootNode);

      m_jtree.setSelectionPath(new TreePath(rootNode.getPath()));
    }

    @Override
    public void removedNode(final CCriteriumTree criteriumTree,
        final ICriteriumTreeNode criteriumNode) {
      final JCriteriumTreeNode treeNode =
          findParentNode((JCriteriumTreeNode) getRoot(), criteriumNode.getCriterium());

      treeNode.removeAllChildren();

      final JCriteriumTreeNode parent = (JCriteriumTreeNode) treeNode.getParent();

      parent.remove(treeNode);

      nodeStructureChanged(treeNode);

      m_jtree.setSelectionPath(new TreePath(parent.getPath()));

      criteriumNode.getCriterium().removeListener(m_internalCriteriumListener);
    }
  }

  /**
   * Updates the tree on changes in the criteria.
   */
  private class InternalCriteriumListener implements ICriteriumListener {
    @Override
    public void criteriumChanged() {
      JCriteriumTreeModel.this.reload();
    }
  }
}
