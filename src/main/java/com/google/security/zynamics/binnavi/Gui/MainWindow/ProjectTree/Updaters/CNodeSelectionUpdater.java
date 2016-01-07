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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters;

import javax.swing.JTree;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;

/**
 * Updater object that is used to select nodes that hold a given object.
 */
public final class CNodeSelectionUpdater implements INodeSelectionUpdater {
  /**
   * The project tree.
   */
  private final JTree m_tree;

  /**
   * The parent node of the subtree to search for.
   */
  private final CProjectTreeNode<?> m_parent;

  /**
   * The object to search for.
   */
  private Object m_object;

  /**
   * Creates a new updater object.
   * 
   * @param tree The project tree.
   * @param parent The parent node of the subtree to search for.
   */
  public CNodeSelectionUpdater(final JTree tree, final CProjectTreeNode<?> parent) {
    Preconditions.checkNotNull(tree, "IE01221: Tree argument can not be null");

    Preconditions.checkNotNull(parent, "IE01292: Parent argument can not be null");

    m_tree = tree;
    m_parent = parent;
  }

  @Override
  public void setObject(final Object object) {
    m_object = object;
  }

  @Override
  public void update() {
    for (int i = 0; i < m_parent.getChildCount(); i++) {
      final CProjectTreeNode<?> child = (CProjectTreeNode<?>) m_parent.getChildAt(i);

      if (child.getObject() == m_object) {
        m_tree.setSelectionPath(new TreePath(child.getPath()));
        return;
      }
    }
  }
}
