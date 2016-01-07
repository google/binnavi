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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Root.CRootCriterium;
import com.google.security.zynamics.zylib.general.ListenerProvider;



/**
 * Class that represents the non-visible model of a criterium tree.
 */
public final class CCriteriumTree implements IAbstractCriteriumTree {
  /**
   * Listeners that are notified about changes in the criterium tree.
   */
  private final ListenerProvider<ICriteriumTreeListener> m_listeners =
      new ListenerProvider<ICriteriumTreeListener>();

  /**
   * Root node of the criterium tree.
   */
  private final CCriteriumTreeNode m_rootNode = new CCriteriumTreeNode(new CRootCriterium());

  /**
   * Fills a cached tree.
   *
   * @param originalParent The current original node to process.
   * @param cachedExpressionTreeNode The current cache tree node to process.
   */
  private void createCachedTree(final CCriteriumTreeNode originalParent,
      final CCachedExpressionTreeNode cachedExpressionTreeNode) {
    for (final CCriteriumTreeNode originalChild : originalParent.getChildren()) {
      final CCachedExpressionTreeNode clonedChild = originalChild.createCachedNode();

      CCachedExpressionTreeNode.append(cachedExpressionTreeNode, clonedChild);

      createCachedTree(originalChild, clonedChild);
    }
  }

  /**
   * Adds a listener object that is notified about changes in the criterium tree.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ICriteriumTreeListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Appends a new node to a parent node.
   *
   * @param parent Parent node of the new node.
   * @param child The child node to append.
   */
  public void appendNode(final CCriteriumTreeNode parent, final CCriteriumTreeNode child) {
    CCriteriumTreeNode.append(parent, child);

    for (final ICriteriumTreeListener listener : m_listeners) {
      try {
        listener.appendedNode(this, parent, child);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

  }

  /**
   * Removes all nodes from the tree.
   */
  public void clear() {
    if (m_rootNode.getChildren().size() == 1) {
      remove(m_rootNode.getChildren().get(0));
    }

    for (final ICriteriumTreeListener listener : m_listeners) {
      try {
        listener.removedAll(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Returns the cached version of this tree.
   *
   * @return The cached version of this tree.
   */
  public CCachedExpressionTree createCachedTree() {
    final CCachedExpressionTree newTree = new CCachedExpressionTree();

    createCachedTree(m_rootNode, newTree.getRoot());

    return newTree;
  }

  /**
   * Returns the root node of the criterium tree.
   *
   * @return The root node of the criterium tree.
   */
  @Override
  public CCriteriumTreeNode getRoot() {
    return m_rootNode;
  }

  /**
   * Inserts a node into the criterium tree.
   *
   * @param parent The parent tree of the inserted node.
   * @param child The node to insert.
   */
  public void insertNode(final CCriteriumTreeNode parent, final CCriteriumTreeNode child) {
    CCriteriumTreeNode.insert(parent, child);

    for (final ICriteriumTreeListener listener : m_listeners) {
      try {
        listener.insertedNode(this, parent, child);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a single node from the criterium tree.
   *
   * @param node The node to remove.
   */
  public void remove(final ICriteriumTreeNode node) {
    CCriteriumTreeNode.remove(node);

    final ICriteriumTreeNode parent = node.getParent();
    parent.getChildren().remove(node);

    for (final ICriteriumTreeListener listener : m_listeners) {
      try {
        listener.removedNode(this, node);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener that was previously notified about changes in the tree.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final ICriteriumTreeListener listener) {
    m_listeners.removeListener(listener);
  }
}
