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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CNodesDisplayString;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Tags tree node that represents a single graph node.
 */
public final class CTaggedGraphNodeNode extends CAbstractTagTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6116067600251118589L;

  /**
   * Icon used in the tree if the node is selected.
   */
  private static final ImageIcon ICON_SELECTED_GRAPHNODE = new ImageIcon(CMain.class.getResource(
      "data/undoselectionchoosericons/graphnode_selection.png"));

  /**
   * Icon used in the tree if the node is not selected.
   */
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/no_selected_graphnodes.png"));

  /**
   * Icon used in the tree if the node is not visible.
   */
  private static final ImageIcon ICON_NODE_INVISIBLE = new ImageIcon(CMain.class.getResource(
      "data/undoselectionchoosericons/no_selected_graphnodes_gray.png"));

  /**
   * The graph node represented by the tree node.
   */
  private final NaviNode m_node;

  /**
   * Creates a new node object.
   *
   * @param graph The graph whose nodes are tagged.
   * @param node The graph node represented by the tree node.
   */
  public CTaggedGraphNodeNode(final ZyGraph graph, final NaviNode node) {
    super(0, graph); // here 0 is OK because this is always a leaf tree node

    m_node = Preconditions.checkNotNull(node, "IE01799: Node can't be null.");
  }

  @Override
  public void dispose() {
    // Nothing to dispose
  }

  /**
   * Returns the graph node represented by this tree node.
   *
   * @return The graph node represented by this tree node.
   */
  public NaviNode getGraphNode() {
    return m_node;
  }

  @Override
  public Icon getIcon() {
    if (!m_node.getRawNode().isVisible()) {
      return ICON_NODE_INVISIBLE;
    } else if (m_node.getRawNode().isSelected()) {
      return ICON_SELECTED_GRAPHNODE;
    } else {
      return ICON_NO_SELECTED_GRAPHNODES;
    }
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return null;
  }

  @Override
  public ITreeNode<CTag> getTag() {
    return null;
  }

  @Override
  public String toString() {
    return CNodesDisplayString.getDisplayString(m_node);
  }
}
