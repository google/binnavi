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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Tree model for the project tree of the main window.
 */
public final class CProjectTreeModel extends DefaultTreeModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2718167459894265781L;

  /**
   * Project tree.
   */
  private final JTree m_tree;

  /**
   * Creates a new project tree model.
   * 
   * @param tree The project tree.
   */
  public CProjectTreeModel(final JTree tree) {
    super(null);

    m_tree = tree;
  }

  @Override
  public void nodeStructureChanged(final TreeNode node) {
    // ensures the reconstruction of the nodes expansion state (which gets normally lost - which
    // means that the nodes collapse)
    final Enumeration<TreePath> expandedPaths =
        m_tree.getExpandedDescendants(new TreePath(getRoot()));

    super.nodeStructureChanged(node);

    if (expandedPaths != null) {
      while (expandedPaths.hasMoreElements()) {
        final TreePath path = expandedPaths.nextElement();
        m_tree.expandPath(path);
      }
    }

    m_tree.validate();
  }
}
