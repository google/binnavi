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

import com.google.security.zynamics.binnavi.disassembly.CNodesDisplayString;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTree;



/**
 * Renders all nodes of the selection history chooser tree.
 */
public final class CSelectionTreeCellRenderer extends IconNodeRenderer {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3075353420167960940L;

  /**
   * Font color used for visible unselected nodes.
   */
  private static final Color NORMAL_FONT_COLOR = new Color(0, 0, 0);

  /**
   * Font color used for invisible nodes.
   */
  private static final Color INVISIBLE_FONT_COLOR = new Color(128, 128, 128);

  /**
   * Font color used for selected visible nodes.
   */
  private static final Color SELECTED_FONT_COLOR = new Color(160, 0, 0);

  /**
   * Font color used for node groups without a unified state.
   */
  private static final Color MIXED_STATE_GROUP_NODE_COLOR = new Color(160, 120, 120);

  /**
   * Builds the tooltip used for nodes that represent a single graph node.
   *
   * @param node The graph node that provides the tooltip content.
   *
   * @return The generated tooltip.
   */
  private String buildToolTip(final NaviNode node) {
    final ZyLabelContent content = node.getRealizer().getNodeContent();

    return HtmlGenerator.getHtml(content, GuiHelper.getMonospaceFont(), true);
  }

  /**
   * Builds the tooltip for used for nodes that represent multiple graph nodes.
   *
   * @param nodes The graph nodes that provide the tooltip content.
   *
   * @return The generated tooltip.
   */
  private String buildToolTip(final List<NaviNode> nodes) {
    final StringBuilder tooltip = new StringBuilder("<html>");

    boolean first = true;
    for (final NaviNode graphNode : nodes) {
      if (!first) {
        tooltip.append("<br>");
      }

      tooltip.append(CNodesDisplayString.getDisplayString(graphNode));

      first = false;
    }

    return tooltip + "</html>";
  }

  /**
   * Renders a history tree node.
   *
   * @param treeNode The tree node to render.
   */
  private void renderHistoryTreeNode(final CSelectionHistoryTreeNode treeNode) {
    if (!treeNode.isRoot()) {
      final List<NaviNode> nodes = treeNode.getSnapshot().getSelection();

      final Pair<Integer, Integer> result = CNodeTypeCounter.count(nodes);

      final int countAll = nodes.size();
      final int selected = result.first();
      final int unselected = countAll - selected;
      final int invisible = result.second();

      if (countAll == selected) {
        setForeground(SELECTED_FONT_COLOR);
      } else if (countAll == unselected) {
        setForeground(NORMAL_FONT_COLOR);
      } else if (countAll == invisible) {
        setForeground(INVISIBLE_FONT_COLOR);
      } else {
        setForeground(MIXED_STATE_GROUP_NODE_COLOR);
      }

      setToolTipText(buildToolTip(nodes));
    }
  }

  /**
   * Renders a leaf node.
   *
   * @param treeNode The leaf node to render.
   */
  private void renderLeafNode(final CNodeNode treeNode) {
    final NaviNode graphNode = treeNode.getNode();

    if (graphNode.isSelected() && graphNode.isVisible()) {
      setForeground(SELECTED_FONT_COLOR);
    } else if (!graphNode.isVisible()) {
      setForeground(INVISIBLE_FONT_COLOR);
    }

    setToolTipText(buildToolTip(graphNode));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    setBackgroundSelectionColor(Color.WHITE);
    setBorderSelectionColor(Color.WHITE);

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    setForeground(NORMAL_FONT_COLOR);

    if (value instanceof CSelectionHistoryTreeNode) {
      renderHistoryTreeNode((CSelectionHistoryTreeNode) value);
    } else if (value instanceof CNodeNode) {
      renderLeafNode((CNodeNode) value);
    }

    return this;
  }
}
