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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodesContainerNode;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CNodesDisplayString;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;



/**
 * This class is used to render the nodes in the tree that displayes the tagged nodes in the node
 * tagging tree that is shown on the right side of graph windows.
 *
 *  The way nodes are displayed depends on some of their characteristics, for example their
 * visibility and selection state.
 */
public final class CTagTreeCellRenderer extends IconNodeRenderer {
  /**
   * Used for serializion.
   */
  private static final long serialVersionUID = -2003280754368848624L;

  /**
   * Default foreground color used to render nodes.
   */
  private static final Color DEFAULT_FOREGROUND_COLOR = new Color(0, 0, 0);

  /**
   * Color used to render nodes if all subnodes of the hierarchy are selected.
   */
  private static final Color COLOR_TAGGED_SELECTED_ALL = new Color(160, 0, 0);

  /**
   * Color used to render nodes if any of the subnodes of the hierarchy are selected.
   */
  private static final Color COLOR_TAGGED_SELECTED_ANY = new Color(160, 120, 120);

  /**
   * Color used to render nodes if all subnodes of the hierarchy are invisible.
   */
  private static final Color COLOR_TAGGED_INVISIBLE = new Color(128, 128, 128);

  /**
   * The last node of the tree that was selected.
   */
  private DefaultMutableTreeNode m_lastSelectionNode = null;

  /**
   * Generates the tooltip shown when the cursor hovers over a tag tree node that represents a graph
   * node.
   *
   * @param node The node whose content is shown in the tooltip.
   *
   * @return The generated HTML tooltip.
   */
  private String buildToolTip(final NaviNode node) {
    final ZyLabelContent content = node.getRealizer().getNodeContent();

    return HtmlGenerator.getHtml(content, GuiHelper.getMonospaceFont(), true);
  }

  /**
   * Generates the tooltip shown when the cursor hovers over a tag tree node that represents a tag.
   *
   * @param tag The tag whose information is shown in the tooltip.
   *
   * @return The generated HTML tooltip.
   */
  private String buildToolTip(final CTag tag) {
    return "<html><b>" + tag.getName() + "</b><br><i>" + tag.getDescription().replace("\n", "<br>")
        + "</i></html>";
  }

  /**
   * Generates the tooltip shown when the cursor hovers over a tag tree node that represents a
   * container of graph nodes.
   *
   * @param node The node whose information is shown in the tooltip.
   *
   * @return The generated HTML tooltip.
   */
  private String buildToolTip(final CTaggedGraphNodesContainerNode node) {
    final StringBuilder tooltip = new StringBuilder("<html>");

    boolean first = true;

    for (final NaviNode graphnode : node.getGraphNodes()) {
      if (!first) {
        tooltip.append("<br>");
      }

      tooltip.append(CNodesDisplayString.getDisplayString(graphnode));

      first = false;
    }

    return tooltip + "</html>";
  }

  /**
   * Determines whether all the nodes in a list are both tagged and invisible.
   *
   * @param graphNodes The nodes to check.
   *
   * @return True, if all nodes in the list are both tagged and invisible.
   */
  private boolean isAllTaggedAndInvisible(final List<NaviNode> graphNodes) {
    return CollectionHelpers.all(graphNodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return item.getRawNode().isTagged() && !item.getRawNode().isVisible();
      }
    });
  }

  /**
   * Determines whether all the nodes in a list are both tagged and selected.
   *
   * @param graphNodes The nodes to check.
   *
   * @return True, if all nodes in the list are both tagged and selected.
   */
  private boolean isAllTaggedAndSelected(final List<NaviNode> graphNodes) {
    return CollectionHelpers.all(graphNodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return item.getRawNode().isTagged() && item.getRawNode().isSelected();
      }
    });
  }

  /**
   * Determines whether any of the nodes in a list are both tagged and selected.
   *
   * @param graphNodes The nodes to check.
   *
   * @return True, if any node in the list is both tagged and selected.
   */
  private boolean isAnyTaggedAndSelected(final List<NaviNode> graphNodes) {
    return CollectionHelpers.any(graphNodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return item.getRawNode().isTagged() && item.getRawNode().isSelected();
      }
    });
  }

  /**
   * Renders nodes that represent individual graph nodes.
   *
   * @param node The node to render.
   */
  private void renderTaggedGraphNodeNode(final CTaggedGraphNodeNode node) {
    final INaviViewNode rawNode = node.getGraphNode().getRawNode();

    if (rawNode.isTagged() && rawNode.isSelected()) {
      setForeground(COLOR_TAGGED_SELECTED_ALL);
    } else if (rawNode.isTagged() && !rawNode.isVisible()) {
      setForeground(COLOR_TAGGED_INVISIBLE);
    }

    setToolTipText(buildToolTip(node.getGraphNode()));
  }

  /**
   * Renders container nodes that display compact information about the tagged nodes. These nodes
   * are typically located below the visible root nodes of the tag tree.
   *
   * @param node The node to render.
   */
  private void renderTaggedGraphNodesContainerNode(final CTaggedGraphNodesContainerNode node) {
    final List<NaviNode> graphNodes = node.getGraphNodes();

    if (!graphNodes.isEmpty()) {
      if (isAllTaggedAndSelected(graphNodes)) {
        setForeground(COLOR_TAGGED_SELECTED_ALL);
      } else if (isAnyTaggedAndSelected(graphNodes)) {
        setForeground(COLOR_TAGGED_SELECTED_ANY);
      } else if (isAllTaggedAndInvisible(graphNodes)) {
        setForeground(COLOR_TAGGED_INVISIBLE);
      }

      setToolTipText(buildToolTip(node));
    }
  }

  /**
   * Renders tree nodes that display information about a tag. These are typically the visible root
   * nodes of the tag tree.
   *
   * @param node The node to render.
   */
  private void renderTagTreeNode(final CTagTreeNode node) {
    final CTag tag = node.getTag().getObject();

    setToolTipText(buildToolTip(tag));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    final boolean selected = m_lastSelectionNode != null &&
        ((DefaultMutableTreeNode) value).getUserObject() == m_lastSelectionNode.getUserObject();

    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, selected);

    setForeground(DEFAULT_FOREGROUND_COLOR);

    if (value instanceof CTagTreeNode) {
      renderTagTreeNode((CTagTreeNode) value);
    } else if (value instanceof CTaggedGraphNodeNode) {
      renderTaggedGraphNodeNode((CTaggedGraphNodeNode) value);
    } else if (value instanceof CTaggedGraphNodesContainerNode) {
      renderTaggedGraphNodesContainerNode((CTaggedGraphNodesContainerNode) value);
    }

    return this;
  }

  /**
   * Sets the last selected node.
   *
   * @param node The last selected node.
   */
  public void setSelectedNode(final DefaultMutableTreeNode node) {
    m_lastSelectionNode = node;
  }
}
