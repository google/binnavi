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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.CNodesDisplayString;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import javax.swing.ImageIcon;



/**
 * Node of the selection history tree that represents a single graph node.
 */
public final class CNodeNode extends IconNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2768699124043290036L;

  /**
   * Icon shown if the represented graph node is selected.
   */
  private static final ImageIcon ICON_SELECTED_GRAPHNODE = new ImageIcon(CMain.class.getResource(
      "data/undoselectionchoosericons/graphnode_selection.png"));

  /**
   * Icon shown if the represented graph node is visible but not selected.
   */
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/no_selected_graphnodes.png"));

  /**
   * Icon shown if the represented graph node is invisible.
   */
  private static final ImageIcon ICON_NO_SELECTED_GRAPHNODES_GRAY = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/no_selected_graphnodes_gray.png"));

  /**
   * The graph node represented by this node.
   */
  private final NaviNode m_node;

  /**
   * Creates a new node object.
   *
   * @param node The graph node represented by this node.
   */
  public CNodeNode(final NaviNode node) {
    super(CNodesDisplayString.getDisplayString(node));

    m_node = node;
  }

  @Override
  public ImageIcon getIcon() {
    if (!m_node.getRawNode().isVisible()) {
      return ICON_NO_SELECTED_GRAPHNODES_GRAY;
    }

    if (m_node.getRawNode().isSelected()) {
      return ICON_SELECTED_GRAPHNODE;
    }

    return ICON_NO_SELECTED_GRAPHNODES;
  }

  /**
   * Returns the graph node represented by this node.
   *
   * @return The graph node represented by this node.
   */
  public NaviNode getNode() {
    return m_node;
  }
}
