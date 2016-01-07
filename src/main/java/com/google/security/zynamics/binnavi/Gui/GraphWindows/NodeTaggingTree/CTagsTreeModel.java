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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree;

import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeTaggingTree.Nodes.CRootTagTreeNode;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * Tree model of the node tags tree.
 */
public final class CTagsTreeModel extends DefaultTreeModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4972223351434760580L;

  /**
   * The node tags tree.
   */
  private final JTree m_tree;

  /**
   * Creates a new model object.
   *
   * @param tree The node tags tree.
   */
  public CTagsTreeModel(final JTree tree) {
    super(null);

    m_tree = tree;
  }

  @Override
  public CRootTagTreeNode getRoot() {
    return (CRootTagTreeNode) super.getRoot();
  }

  @Override
  public void nodeStructureChanged(final TreeNode node) {
    // ensures the reconstruction of the nodes expansion state (which gets normally lost - which
    // means that the nodes collapse)
    final Enumeration<TreePath> expandedPaths = m_tree.getExpandedDescendants(
        new TreePath(super.getRoot()));

    super.nodeStructureChanged(node);

    if (expandedPaths != null) {
      while (expandedPaths.hasMoreElements()) {
        m_tree.expandPath(expandedPaths.nextElement());
      }
    }

    m_tree.validate();
  }
}
