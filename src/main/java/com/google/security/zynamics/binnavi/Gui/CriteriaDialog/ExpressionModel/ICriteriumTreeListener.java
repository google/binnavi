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

/**
 * Listener interface for classes that want to be notified about changes in criterium trees.
 */
public interface ICriteriumTreeListener {
  /**
   * Invoked after a node was appended to a criterium tree.
   *
   * @param criteriumTree The criterium tree where the node was appended.
   * @param parent The parent node of the new node.
   * @param child The appended node.
   */
  void appendedNode(
      CCriteriumTree criteriumTree, ICriteriumTreeNode parent, ICriteriumTreeNode child);

  /**
   * Invoked after a node was inserter into a criterium tree.
   *
   * @param criteriumTree The criterium tree where the node was inserted.
   * @param parent The parent node of the new node.
   * @param child The inserted node.
   */
  void insertedNode(
      CCriteriumTree criteriumTree, ICriteriumTreeNode parent, ICriteriumTreeNode child);

  /**
   * Invoked after all nodes of a criterium tree were removed.
   *
   * @param criteriumTree The criterium tree whose nodes were removed.
   */
  void removedAll(CCriteriumTree criteriumTree);

  /**
   * Invoked after a single tree of a criterium tree was removed.
   *
   * @param criteriumTree The tree from which the node was removed.
   * @param node The removed node.
   */
  void removedNode(CCriteriumTree criteriumTree, ICriteriumTreeNode node);
}
