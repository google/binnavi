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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



/**
 * Contains helper functions for working with group nodes.
 */
public final class CGraphGrouper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphGrouper() {
  }

  /**
   * Determines whether all elements of a group node are selected.
   * 
   * @param group The group node to check.
   * 
   * @return True, if all elements of the group node are selected. False, otherwise.
   */
  private static boolean allElementsSelected(final INaviGroupNode group) {
    return !CollectionHelpers.any(group.getElements(), new ICollectionFilter<INaviViewNode>() {
      @Override
      public boolean qualifies(final INaviViewNode node) {
        return !node.isSelected();
      }
    });
  }

  /**
   * Determines the text to be displayed in a group node, if the given node is inside a collapsed
   * group node.
   * 
   * @param node The node whose text is determined.
   * 
   * @return The string to be displayed for the given node.
   */
  private static String determineNodeText(final NaviNode node) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();

      return String.format("Basic Block: %s", cnode.getAddress().toHexString());
    } else if (node.getRawNode() instanceof INaviFunctionNode) {
      final INaviFunctionNode fnode = (INaviFunctionNode) node.getRawNode();

      return String.format("Function: %s (%s)", fnode.getFunction().getName(), fnode.getFunction()
          .getAddress().toHexString());
    } else if (node.getRawNode() instanceof INaviTextNode) {
      // Display up to 15 characters of the first line of
      // the comment for comment nodes.

      final INaviTextNode tnode = (INaviTextNode) node.getRawNode();

      final List<IComment> comment = tnode.getComments();

      final String firstLine = (comment.isEmpty()) ? "" : comment.get(1).getComment();

      final int firstLineBreak = Math.min(firstLine.indexOf('\n'), firstLine.indexOf('\r'));

      final int toDisplay =
          Math.min(Math.min(15, firstLineBreak == -1 ? Integer.MAX_VALUE : firstLineBreak),
              firstLine.length());

      return String.format("Text: %s", firstLine.substring(0, toDisplay));
    } else if (node.getRawNode() instanceof INaviGroupNode) {
      // Display up to 15 characters of the first line of
      // the comment for group nodes.

      final INaviGroupNode gnode = (INaviGroupNode) node.getRawNode();

      final List<IComment> comment = gnode.getComments();

      final String firstLine = (comment.isEmpty()) ? "" : comment.get(0).getComment();

      final int firstLineBreak = Math.min(firstLine.indexOf('\n'), firstLine.indexOf('\r'));

      final int toDisplay =
          Math.min(Math.min(15, firstLineBreak == -1 ? Integer.MAX_VALUE : firstLineBreak),
              firstLine.length());

      return String.format("Group: %s", firstLine.substring(0, toDisplay));
    } else {
      throw new IllegalStateException("IE01150: Unknown node type");
    }
  }

  /**
   * Returns all nodes from a list that either do not have a parent group or are in a group where
   * not all nodes are selected.
   * 
   * @param nodes The nodes to filter.
   * 
   * @return The nodes of the input list that match the condition.
   */
  private static List<NaviNode> filterSelectedNodes(final Collection<NaviNode> nodes) {
    return CollectionHelpers.filter(nodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode node) {
        return (node.getRawNode().getParentGroup() == null)
            || !allElementsSelected(node.getRawNode().getParentGroup());
      }
    });
  }

  /**
   * Finds the common parent group of a list of nodes.
   * 
   * @param nodes The list of nodes whose parent group is determined.
   * 
   * @return The common parent group of the nodes or null if there is no such group.
   */
  private static INaviGroupNode getCommonParent(final List<NaviNode> nodes) {
    INaviGroupNode parent = null;
    boolean first = true;

    for (final NaviNode node : nodes) {
      if (first) {
        parent = node.getRawNode().getParentGroup();
        first = false;
      } else {
        if (parent != node.getRawNode().getParentGroup()) {
          return null;
        }
      }
    }

    return parent;
  }

  /**
   * Creates a new group node from a list of nodes.
   * 
   * @param graph The graph where the group node is created.
   * @param nodes The nodes to be grouped.
   */
  private static void groupNodes(final ZyGraph graph, final List<NaviNode> nodes) {
    final StringBuilder stringBuilder = new StringBuilder();

    final List<INaviViewNode> rawNodes = new ArrayList<INaviViewNode>();

    // ATTENTION: DO NOT MOVE THIS LINE BELOW THE REMOVEELEMENT LINE
    final INaviGroupNode commonParent = getCommonParent(nodes);

    for (final NaviNode node : nodes) {
      if (node.getRawNode().getParentGroup() != null) {
        node.getRawNode().getParentGroup().removeElement(node.getRawNode());
      }

      rawNodes.add(node.getRawNode());

      stringBuilder.append(determineNodeText(node));

      stringBuilder.append('\n');
    }

    final CGroupNode groupNode = graph.getRawView().getContent().createGroupNode(rawNodes);

    if (commonParent != null) {
      commonParent.addElement(groupNode);
    }

    try {
      groupNode.appendComment(stringBuilder.toString());
    } catch (CouldntSaveDataException | CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Creates a group node that contains all selected nodes of a graph.
   * 
   * @param graph The graph where the group node is created.
   */
  public static void groupSelectedNodes(final ZyGraph graph) {
    final List<NaviNode> nodes = filterSelectedNodes(graph.getSelectedNodes());

    if (!nodes.isEmpty()) {
      groupNodes(graph, nodes);
    }
  }

  /**
   * Removes a node from a group.
   * 
   * @param node The node to remove from its parent group.
   */
  public static void removeFromGroup(final NaviNode node) {
    final INaviGroupNode group = node.getRawNode().getParentGroup();

    if (group != null) {
      group.removeElement(node.getRawNode());

      if (group.getParentGroup() != null) {
        group.getParentGroup().addElement(node.getRawNode());
      }
    }
  }

  /**
   * Removes all selected group nodes from a graph.
   * 
   * @param graph Graph from which the group nodes are removed.
   */
  public static void removeSelectedGroups(final ZyGraph graph) {
    for (final NaviNode node : graph.getSelectedNodes()) {
      if (node.getRawNode() instanceof INaviGroupNode) {
        graph.getRawView().getContent().deleteNode(node.getRawNode());
      }
    }
  }

  /**
   * Toggles the state of all selected group nodes in a graph.
   * 
   * @param graph The graph whose group nodes are toggled.
   */
  public static void toggleSelectedGroups(final ZyGraph graph) {
    for (final NaviNode node : graph.getSelectedNodes()) {
      if (node.getRawNode() instanceof INaviGroupNode) {
        final INaviGroupNode gnode = (INaviGroupNode) node.getRawNode();

        gnode.setCollapsed(!gnode.isCollapsed());
      }
    }
  }
}
