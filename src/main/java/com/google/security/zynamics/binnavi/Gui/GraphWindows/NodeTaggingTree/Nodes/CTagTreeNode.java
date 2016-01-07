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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagTreeNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTreeModel;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeTaggingTree.Nodes.CRootTagTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * Tags tree node that represents a single tag.
 */
public final class CTagTreeNode extends CAbstractTagTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -274077764881287409L;

  /**
   * Icon used for non-root tags.
   */
  private static final ImageIcon ICON_GREEN_TAG =
      new ImageIcon(CMain.class.getResource("data/nodetaggingtreeicons/green_tag.png"));

  /**
   * Icon used for root tags.
   */
  private static final ImageIcon ICON_GREEN_ROOTTAG =
      new ImageIcon(CMain.class.getResource("data/nodetaggingtreeicons/green_roottag.png"));

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides tagging information.
   */
  private final ITagManager m_tagManager;

  /**
   * Tree model of the tree the node belongs to.
   */
  private final CTagsTreeModel m_model;

  /**
   * Tag represented by the tree node.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Builds the context menu of the tree node.
   */
  private final CTagTreeNodeMenuBuilder m_menuBuilder;

  /**
   * Updates the node on changes to the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Updates the node on changes to the tag.
   */
  private final InternalTagListener m_tagListener = new InternalTagListener();

  /**
   * Creates a new node object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph whose nodes are tagged.
   * @param tagManager Provides tagging information.
   * @param model Tree model of the tree the node belongs to.
   * @param tag Tag represented by the tree node.
   */
  public CTagTreeNode(final JFrame parent, final ZyGraph graph, final ITagManager tagManager,
      final CTagsTreeModel model, final ITreeNode<CTag> tag) {
    super(tag.getObject().getId(), graph);

    m_parent = Preconditions.checkNotNull(parent, "IE02328: Parent argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE02329: Tag argument can not be null");
    m_tagManager = Preconditions.checkNotNull(tagManager, "IE01802: Tag manager can not be null");
    m_model = Preconditions.checkNotNull(model, "IE01803: Treemodel can't be null.");

    m_menuBuilder = new CTagTreeNodeMenuBuilder(parent, graph, tagManager, m_tag);

    m_tagManager.addListener(m_tagManagerListener);

    m_tag.getObject().addListener(m_tagListener);

    createChildren();
  }

  /**
   * Creates the children of the node.
   */
  private void createChildren() {
    add(new CTaggedGraphNodesContainerNode(getGraph(), m_model, m_tag));

    for (final ITreeNode<CTag> child : m_tag.getChildren()) {
      add(new CTagTreeNode(m_parent, getGraph(), m_tagManager, m_model, child));
    }
  }

  /**
   * Counts the number of children of the node.
   *
   * @return The number of children of the node.
   */
  private int getDeepChildCount() {
    int count = 0;

    final Enumeration<?> enumeration = breadthFirstEnumeration();

    while (enumeration.hasMoreElements()) {
      if (enumeration.nextElement() instanceof CTagTreeNode) {
        count++;
      }
    }

    return count == 0 ? 0 : count - 1;
  }

  /**
   * Determines whether the tag represented by this node is a root tag.
   *
   * @return True, if the node is a root tag. False, otherwise.
   */
  private boolean isRootTag() {
    return m_model.getPathToRoot(this).length == 2;
  }

  @Override
  public void dispose() {
    m_tagManager.removeListener(m_tagManagerListener);

    m_tag.getObject().removeListener(m_tagListener);

    deleteChildren();
  }

  @Override
  public Icon getIcon() {
    return isRootTag() ? ICON_GREEN_ROOTTAG : ICON_GREEN_TAG;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return m_menuBuilder.getPopupMenu();
  }

  @Override
  public ITreeNode<CTag> getTag() {
    return m_tag;
  }

  /**
   * Returns the tag manager that manages the wrapped tag.
   *
   * @return The tag manager that manages the wrapped tag.
   */
  public ITagManager getTagManager() {
    return m_tagManager;
  }

  @Override
  public String toString() {
    return String.format(
        "%s (%s/%s)", m_tag.getObject().getName(), getChildCount() - 1, getDeepChildCount());
  }

  /**
   * Updates the node on changes to the tag.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      // do nothing here
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      m_model.nodeChanged(CTagTreeNode.this);
    }

    @Override
    public void deletedTag(final CTag tag) {
      // do nothing here
    }
  }

  /**
   * Updates the node on changes to the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      if (m_tag.getObject().getId() == tag.getParent().getObject().getId()) {
        ((CRootTagTreeNode) getRoot()).refreshTree(tag);

        m_model.nodeStructureChanged(CTagTreeNode.this);
      }
    }

    @Override
    public void deletedTag(
        final CTagManager manager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      if (m_tag.getObject().getId() == parent.getObject().getId()) {
        ((CRootTagTreeNode) getRoot()).refreshTree(parent);

        m_model.nodeStructureChanged(getRoot());
      }
    }

    @Override
    public void deletedTagSubtree(
        final CTagManager manager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      if (m_tag.getObject().getId() == parent.getObject().getId()) {
        ((CRootTagTreeNode) getRoot()).refreshTree(parent);

        m_model.nodeStructureChanged(getRoot());
      }
    }

    @Override
    public void insertedTag(
        final CTagManager tagManager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      if (m_tag.getObject().getId() == tag.getParent().getObject().getId()) {
        ((CRootTagTreeNode) getRoot()).refreshTree(tag);

        m_model.nodeStructureChanged(getRoot());
      }
    }
  }

}
