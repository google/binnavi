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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.DragDrop;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CAbstractDropHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CTagTransferable;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.tree.DefaultMutableTreeNode;



/**
 * Drag & Drop handler for node tags.
 */
public final class CTagSortingHandler extends CAbstractDropHandler {
  /**
   * Creates a new tag D&D handler object.
   */
  public CTagSortingHandler() {
    super(CTagTransferable.TAG_FLAVOR);
  }

  @Override
  public boolean canHandle(
      final DefaultMutableTreeNode parentNode, final DefaultMutableTreeNode draggedNode) {
    return parentNode instanceof CTagTreeNode && draggedNode instanceof CTagTreeNode
        && parentNode != draggedNode;
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode, final Object data) {
    return false;
  }

  @Override
  public void drop(final DefaultMutableTreeNode parentNode, final Object data) {
    // Tags can not be dragged from tables
  }

  @Override
  public void drop(final DNDTree target, final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    final ITagManager tagManager = ((CTagTreeNode) parentNode).getTagManager();

    final ITreeNode<CTag> parentNodeNode = ((CTagTreeNode) parentNode).getTag();
    final ITreeNode<CTag> draggedNodeNode = ((CTagTreeNode) draggedNode).getTag();

    try {
      tagManager.moveTag(parentNodeNode, draggedNodeNode);
    } catch (final CouldntSaveDataException e) {
      // TODO: Improve this
      CUtilityFunctions.logException(e);
    }
  }
}
