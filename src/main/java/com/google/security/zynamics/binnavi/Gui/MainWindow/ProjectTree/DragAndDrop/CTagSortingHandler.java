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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CNodeExpander;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.CTagNode;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import javax.swing.tree.DefaultMutableTreeNode;



/**
 * Drag & Drop handler for view tags.
 */
public final class CTagSortingHandler extends CAbstractDropHandler {
  /**
   * Creates a new tag D&D handler object.
   */
  public CTagSortingHandler() {
    super(CTagTransferable.TAG_FLAVOR);
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    return (parentNode instanceof CTagNode) && (draggedNode instanceof CTagNode)
        && (parentNode != draggedNode);
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode, final Object data) {
    return false;
  }

  @Override
  public void drop(final DefaultMutableTreeNode parentNode, final Object data) {
    // TODO: Why is this empty?
  }

  @Override
  public void drop(final DNDTree target, final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    final ITagManager tagManager = ((CTagNode) parentNode).getTagManager();

    final TreeNode<CTag> parentNodeNode = ((CTagNode) parentNode).getObject();
    final TreeNode<CTag> draggedNodeNode = ((CTagNode) draggedNode).getObject();

    try {
      tagManager.moveTag(parentNodeNode, draggedNodeNode);
      CNodeExpander.expandNode(target, parentNodeNode);
    } catch (final CouldntSaveDataException e) {
      // TODO: Improve this

      CUtilityFunctions.logException(e);
    }
  }
}
