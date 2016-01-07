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
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTaggedNodesContainerNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTreeModel;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.Filter.CGraphNodeTaggedFilter;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviGraphListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

/**
 * Tags tree node that represents a set of graph nodes.
 */
public final class CTaggedGraphNodesContainerNode extends CAbstractTagTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4590660106065351427L;

  /**
   * Icon shown when all nodes in the container are unselected but visible.
   */
  private static final ImageIcon ICON_ALL_UNSELECTED_GRAPHNODES = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/graph_selection_folder_all_unselected.png"));

  /**
   * Icon shown when all nodes in the container are selected.
   */
  private static final ImageIcon ICON_ALL_SELECTED_GRAPHNODES = new ImageIcon(
      CMain.class.getResource("data/undoselectionchoosericons/graph_selection_folder_open.png"));

  /**
   * Icon shown when all nodes in the container are invisible.
   */
  private static final
      ImageIcon ICON_ALL_INVISIBLE_GRAPHNODES = new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_all_unselected_gray.png"));

  /**
   * Icon shown when all nodes in the container are unselected but some are visible while others are
   * invisible.
   */
  private static final ImageIcon ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_all_unselected_halfgray.png"));

  /**
   * Icon shown when all nodes in the container are visible but some are selected while others are
   * unselected.
   */
  private static final ImageIcon ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_some_unselected.png"));

  /**
   * Icon shown when the nodes in the container are partly visible and partly selected.
   */
  private static final ImageIcon ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_some_unselected_halfgray.png"));

  /**
   * Icon shown when no nodes belong to this folder.
   */
  private static final ImageIcon ICON_EMPTY_FOLDER = new ImageIcon(
      CMain.class.getResource("data/undoselectionchoosericons/graph_selection_folder_empty.png"));

  /**
   * The model of the tags tree the node belongs to.
   */
  private final CTagsTreeModel m_model;

  /**
   * Tag represented by the container.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Creates the context menu of the node.
   */
  private final CTaggedNodesContainerNodeMenuBuilder m_menuBuilder;

  /**
   * Updates the node on changes to tagging.
   */
  private final InternalNaviGraphListener m_graphListener = new InternalNaviGraphListener();

  /**
   * Updates the GUI on changes to the view.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Creates a new node object.
   *
   * @param graph The graph whose nodes are tagged.
   * @param model The model of the tags tree the node belongs to.
   * @param tag Tag represented by the container.
   */
  public CTaggedGraphNodesContainerNode(
      final ZyGraph graph, final CTagsTreeModel model, final ITreeNode<CTag> tag) {
    super(-tag.getObject().getId(), graph);

    Preconditions.checkNotNull(graph, "IE01800: Graph can not be null.");
    m_model = Preconditions.checkNotNull(model, "IE01801: Model can not be null.");
    m_tag = Preconditions.checkNotNull(tag, "IE02327: Tag argument can not be null");

    m_menuBuilder = new CTaggedNodesContainerNodeMenuBuilder(graph, m_tag);

    createChildren();

    graph.addListener(m_graphListener);
    graph.getRawView().addListener(m_viewListener);
  }

  /**
   * Creates the children of the node.
   */
  private void createChildren() {
    final List<NaviNode> nodes = GraphHelpers.filter(
        getGraph(), new CGraphNodeTaggedFilter(Sets.newHashSet(m_tag.getObject())));

    for (final NaviNode node : nodes) {
      add(new CTaggedGraphNodeNode(getGraph(), node));
    }
  }

  @Override
  public void dispose() {
    getGraph().getRawView().removeListener(m_viewListener);
    getGraph().removeListener(m_graphListener);
  }

  /**
   * Returns the node represented by this container.
   *
   * @return The node represented by this container.
   */
  public List<NaviNode> getGraphNodes() {
    final List<NaviNode> graphNode = new ArrayList<NaviNode>();
    final Enumeration<?> enumeration = children();

    while (enumeration.hasMoreElements()) {
      graphNode.add(((CTaggedGraphNodeNode) enumeration.nextElement()).getGraphNode());
    }

    return graphNode;
  }

  @Override
  public Icon getIcon() {
    int selected = 0;
    int unselected = 0;
    int invisible = 0;

    final List<NaviNode> nodes = getGraphNodes();

    for (final NaviNode n : nodes) {
      if (n.getRawNode().isSelected()) {
        selected++;
      } else {
        unselected++;
      }

      if (!n.getRawNode().isVisible()) {
        invisible++;
      }
    }

    if (nodes.isEmpty()) {
      return ICON_EMPTY_FOLDER;
    } else if (invisible == nodes.size()) {
      return ICON_ALL_INVISIBLE_GRAPHNODES;
    } else if (selected == nodes.size()) {
      return ICON_ALL_SELECTED_GRAPHNODES;
    } else if ((unselected == nodes.size()) && (invisible == 0)) {
      return ICON_ALL_UNSELECTED_GRAPHNODES;
    } else if (selected == 0) {
      return ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    } else if (invisible == 0) {
      return ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES;
    } else if ((invisible != 0) && (selected != 0)) {
      return ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    }

    throw new IllegalStateException("IE00669: Unknown container state");
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return m_menuBuilder.getPopupMenu();
  }

  @Override
  public ITreeNode<CTag> getTag() {
    return m_tag;
  }

  @Override
  public String toString() {
    int selected = 0;
    int visible = 0;
    int invisible = 0;

    for (final NaviNode n : getGraphNodes()) {
      if (n.getRawNode().isSelected()) {
        selected++;
      }

      if (n.getRawNode().isVisible()) {
        visible++;
      } else {
        invisible++;
      }
    }
    return String.format(
        "Tagged Nodes (%d/%d/%d/%d)", selected, visible, invisible, visible + invisible);
  }

  /**
   * Updates the node on changes to tagging.
   */
  private class InternalNaviGraphListener extends NaviGraphListenerAdapter {
    @Override
    public void addedNode(final ZyGraph graph, final NaviNode node) {
      final CGraphNodeTaggedFilter filter =
          new CGraphNodeTaggedFilter(Sets.newHashSet(m_tag.getObject()));

      if (filter.qualifies(node)) {
        add(new CTaggedGraphNodeNode(getGraph(), node));

        m_model.nodeStructureChanged(m_model.getRoot());
      }
    }

    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      oldView.removeListener(m_viewListener);
      newView.addListener(m_viewListener);
    }

    @Override
    public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
      if (m_tag.getObject() == tag) {
        m_model.getRoot().addGraphNodeNodeToTree(node, CTaggedGraphNodesContainerNode.this);

        m_model.nodeStructureChanged(m_model.getRoot());
      }
    }

    @Override
    public void untaggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
      if (m_tag.getObject() == tag) {
        m_model.getRoot().refreshTree(m_tag);

        m_model.nodeStructureChanged(parent);
      }
    }
  }

  /**
   * Updates the GUI on changes to the view.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    /**
     * Updates the tree if a node was deleted.
     *
     * @param node The deleted node.
     */
    private void deleteNode(final INaviViewNode node) {
      for (int i = 0; i < getChildCount(); i++) {
        if (((CTaggedGraphNodeNode) getChildAt(i)).getGraphNode().getRawNode() == node) {
          remove(i);
          break;
        }
      }
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      deleteNode(node);

      m_model.nodeStructureChanged(m_model.getRoot());
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      for (final INaviViewNode node : nodes) {
        deleteNode(node);
      }

      m_model.nodeStructureChanged(m_model.getRoot());
    }
  }
}
