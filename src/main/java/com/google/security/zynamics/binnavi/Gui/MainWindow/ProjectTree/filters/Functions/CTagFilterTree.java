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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.Functions;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;


import com.jidesoft.swing.CheckBoxTree;

/**
 * Tree class where the user can select from tags for filtering views.
 */
public final class CTagFilterTree extends CheckBoxTree {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7766617210776874627L;

  /**
   * Icon to be used to leaf nodes in the tree.
   */
  private static final ImageIcon ICON_TAG = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/tag.png"));

  /**
   * Icon to be used for non-leaf nodes in the tree.
   */
  private static final ImageIcon ICON_CONTAINER_TAG = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/tag_folder2.png"));

  /**
   * Updates the tree model when the underlying tag model changes.
   */
  private final ITagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Provides the tags the user can select.
   */
  private final ITagManager m_tagManager;

  /**
   * Creates a new filter tree object.
   * 
   * @param tagManager Provides the tags the user can select.
   */
  public CTagFilterTree(final ITagManager tagManager) {
    super(new CFilterTreeModel(tagManager.getRootTag()));

    m_tagManager = tagManager;

    setDigIn(false);

    final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setOpenIcon(ICON_CONTAINER_TAG);
    renderer.setClosedIcon(ICON_CONTAINER_TAG);
    renderer.setLeafIcon(ICON_TAG);
    setCellRenderer(renderer);

    setRootVisible(false);
    TreeHelpers.expandAll(this, true);

    tagManager.addListener(m_tagManagerListener);
  }


  /**
   * Updates the tree model when the underlying tag model changes.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      setModel(new CFilterTreeModel(m_tagManager.getRootTag()));
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      setModel(new CFilterTreeModel(m_tagManager.getRootTag()));
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      setModel(new CFilterTreeModel(m_tagManager.getRootTag()));
    }

    @Override
    public void insertedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      setModel(new CFilterTreeModel(m_tagManager.getRootTag()));
    }
  }
}
