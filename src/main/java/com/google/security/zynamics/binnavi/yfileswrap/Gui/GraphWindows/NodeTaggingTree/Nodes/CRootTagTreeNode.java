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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeTaggingTree.Nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CAbstractTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodesContainerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ITagContainerNode;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Represents root tags of the node tags tree.
 */
public final class CRootTagTreeNode extends CAbstractTagTreeNode implements ITagContainerNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1594300346621455654L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tree the node belongs to.
   */
  private final CTagsTree m_tagsTree;

  /**
   * Provides tagging information.
   */
  private final ITagManager m_tagManager;

  /**
   * Root node tag.
   */
  private final ITreeNode<CTag> m_rootTag;

  /**
   * Updates the tree on changes to the tag manager.
   */
  private final ITagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Creates a new node object.
   *
   * @param parent Parent window used for dialogs.
   * @param tagsTree Tree the node belongs to.
   * @param graph Graph whose nodes are tagged.
   * @param tagManager Provides tagging information.
   */
  public CRootTagTreeNode(final JFrame parent, final CTagsTree tagsTree, final ZyGraph graph,
      final ITagManager tagManager) {
    super(0, graph); // id of root node

    m_parent = Preconditions.checkNotNull(parent, "IE02325: Parent argument can not be null");
    m_tagsTree =
        Preconditions.checkNotNull(tagsTree, "IE02326: Tags tree argument can not be null");
    m_tagManager = Preconditions.checkNotNull(tagManager, "IE01796: Tag manager can not be null");

    m_rootTag = tagManager.getRootTag();

    m_tagManager.addListener(m_tagManagerListener);

    createChildren();
  }

  /**
   * Creates the children of the node.
   */
  private void createChildren() {
    for (final ITreeNode<CTag> child : m_rootTag.getChildren()) {
      add(new CTagTreeNode(m_parent, getGraph(), m_tagManager, m_tagsTree.getModel(), child));
    }
  }

  public void addGraphNodeNodeToTree(final INaviViewNode node,
      final CTaggedGraphNodesContainerNode cTaggedGraphNodesContainerNode) {
    cTaggedGraphNodesContainerNode.add(
        new CTaggedGraphNodeNode(getGraph(), getGraph().getNode(node)));
  }

  @Override
  public void dispose() {
    m_tagManager.removeListener(m_tagManagerListener);

    deleteChildren();
  }

  @Override
  public JPopupMenu getPopupMenu() {
    // root tag isn't visible
    return null;
  }

  @Override
  public ITreeNode<CTag> getTag() {
    return m_rootTag;
  }

  /**
   * Rebuilds the entire tree from scratch by filtering over all elements in the graph. This is
   * pretty damn slow for any reasonably-sized graph, and should hence be done only rarely.
   * @deprecated
   */
  @Deprecated
  @Override
  public void refreshTree(final ITreeNode<CTag> tag) {
    final List<DefaultMutableTreeNode> lastNodes = TreeHelpers.getLastExpandedNodes(m_tagsTree);

    final List<Integer> lastNodeIds = new ArrayList<Integer>();

    if ((tag != null) && (tag.getParent() != null)) {
      lastNodeIds.add(tag.getParent().getObject().getId());
    }

    for (final DefaultMutableTreeNode lastNode : lastNodes) {
      if (lastNode.getUserObject() instanceof Integer) {
        final int tagId = (Integer) lastNode.getUserObject();
        lastNodeIds.add(tagId);
      }
    }

    deleteChildren();

    createChildren();

    final Enumeration<?> bfn = breadthFirstEnumeration();
    while (bfn.hasMoreElements()) {
      final Object node = bfn.nextElement();

      int tagId = -1;
      TreePath path = null;
      if (node instanceof CRootTagTreeNode) {
        final CRootTagTreeNode rootNode = (CRootTagTreeNode) node;
        tagId = m_rootTag.getObject().getId();
        path = new TreePath(rootNode.getPath());
      } else if (node instanceof CTagTreeNode) {
        final CTagTreeNode treeNode = (CTagTreeNode) node;
        tagId = treeNode.getTag().getObject().getId();
        path = new TreePath(treeNode.getPath());
      } else if (node instanceof CTaggedGraphNodesContainerNode) {
        final CTaggedGraphNodesContainerNode containerNode = (CTaggedGraphNodesContainerNode) node;
        tagId = -containerNode.getTag().getObject().getId();
        path = new TreePath(containerNode.getPath());
      }

      if (lastNodeIds.contains(tagId)) {
        m_tagsTree.expandPath(path);
      }
    }
  }

  /**
   * Updates the tree on changes to the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      if (m_rootTag.getObject().getId() == tag.getParent().getObject().getId()) {
        refreshTree(tag);

        m_tagsTree.getModel().nodeStructureChanged(CRootTagTreeNode.this);
      }
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (m_rootTag.getObject().getId() == parent.getObject().getId()) {
        refreshTree(parent);

        m_tagsTree.getModel().nodeStructureChanged(getRoot());
      }
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (m_rootTag.getObject().getId() == parent.getObject().getId()) {
        refreshTree(parent);

        m_tagsTree.getModel().nodeStructureChanged(getRoot());
      }
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      if (m_rootTag.getObject().getId() == tag.getParent().getObject().getId()) {
        refreshTree(tag);

        m_tagsTree.getModel().nodeStructureChanged(getRoot());
      }
    }
  }
}
