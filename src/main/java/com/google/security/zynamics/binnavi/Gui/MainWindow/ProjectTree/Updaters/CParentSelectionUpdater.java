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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;

/**
 * Project tree updater that selects the parent of a node.
 */
public final class CParentSelectionUpdater implements ITreeUpdater {
  /**
   * The project tree.
   */
  private final JTree m_tree;

  /**
   * Parent node to select.
   */
  private final DefaultMutableTreeNode m_parent;

  /**
   * Creates a new updater object.
   * 
   * @param tree The project tree.
   * @param parent Parent node to select.
   */
  public CParentSelectionUpdater(final JTree tree, final DefaultMutableTreeNode parent) {
    Preconditions.checkNotNull(tree, "IE01293: Tree argument can not be null");

    Preconditions.checkNotNull(parent, "IE01294: Parent argument can not be null");

    m_tree = tree;
    m_parent = parent;
  }

  @Override
  public void update() {
    m_tree.setSelectionPath(new TreePath(m_parent.getPath()));
  }
}
