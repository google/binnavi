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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.functions.NodeFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Contains functions for deleting nodes from a graph.
 */
public final class CNodeDeleter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeDeleter() {
  }

  /**
   * Creates edges to connect the parents of a node with its children.
   *
   * @param view The view where the edges are created.
   * @param node The node at the center of edge creation.
   */
  private static void connectParentsWithChildren(final INaviView view, final INaviViewNode node) {
    final List<INaviEdge> incomingEdges = node.getIncomingEdges();
    final List<? extends INaviViewNode> children = node.getChildren();

    for (final INaviEdge incomingEdge : incomingEdges) {
      if (incomingEdge.getSource() == node) {
        continue;
      }

      for (final INaviViewNode child : children) {
        if (child == node) {
          continue;
        }

        // Avoid duplicate edges
        if (!hasEdge(incomingEdge.getSource(), child, incomingEdge.getType())) {
          view.getContent().createEdge(incomingEdge.getSource(), child, incomingEdge.getType());
        }
      }
    }
  }

  /**
   * Removes all hidden nodes from a list of nodes. Nodes are hidden when they are inside a
   * collapsed group node.
   *
   * @param nodes The nodes to filter.
   *
   * @return The filtered nodes.
   */
  private static List<NaviNode> filterHiddenNodes(final Collection<NaviNode> nodes) {
    return CollectionHelpers.filter(nodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode node) {
        return (node.getRawNode().getParentGroup() == null)
            || !node.getRawNode().getParentGroup().isCollapsed();
      }
    });
  }

  /**
   * Determines whether there is an edge of a given type going from a source node to a target node.
   *
   * @param source The source node.
   * @param target The target node.
   * @param type Edge type to look for.
   *
   * @return True, if such an edge exists. False, otherwise.
   */
  private static boolean hasEdge(
      final INaviViewNode source, final INaviViewNode target, final EdgeType type) {
    for (final INaviEdge edge : source.getOutgoingEdges()) {
      if ((edge.getTarget() == target) && (edge.getType() == type)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes an instruction from a code node.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph the node belongs to.
   * @param node Node to remove the instruction from.
   * @param instruction Instruction to remove.
   */
  public static void deleteInstruction(final JFrame parent, final ZyGraph graph,
      final NaviNode node, final INaviInstruction instruction) {
    if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(parent, String.format(
        "Do you really want to delete the instruction '%s' from the code node?",
        instruction.getInstructionString()))) {
      final INaviCodeNode rawNode = (INaviCodeNode) node.getRawNode();

      // final Iterable<INaviInstruction> instructions = rawNode.getInstructions();

      if (!rawNode.hasInstruction(instruction)) {
        CMessageBox.showError(parent, "The instruction is not part of the code node");
        return;
      }

      if (Iterables.size(rawNode.getInstructions()) == 1) {
        connectParentsWithChildren(graph.getRawView(), node.getRawNode());

        graph.deleteNodes(Lists.newArrayList(node));
      } else {
        ((INaviCodeNode) node.getRawNode()).removeInstruction(instruction);
      }
    }
  }

  /**
   * Removes the invisible nodes from the graph.
   *
   * @param graph The graph from which the invisible nodes are deleted.
   */
  public static void deleteInvisibleNodes(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01729: Graph argument can not be null");

    graph.deleteNodes(NodeFunctions.getInvisibleNodes(graph));
  }

  /**
   * Removes a node from a graph.
   *
   * @param view The view from which the node is removed.
   * @param node The node to remove from the graph.
   */
  public static void deleteNode(final INaviView view, final INaviViewNode node) {
    Preconditions.checkNotNull(view, "IE01730: View argument can not be null");
    Preconditions.checkNotNull(node, "IE01731: Node argument can not be null");

    view.getContent().deleteNode(node);
  }

  /**
   * Removes the selected nodes from the graph.
   *
   * @param graph The graph from which the selected nodes are deleted.
   */
  public static void removeSelectedNodes(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01732: Graph argument can not be null");

    final List<NaviNode> selectedNodes = filterHiddenNodes(graph.getSelectedNodes());

    graph.deleteNodes(selectedNodes);
  }

  /**
   * Removes the selected nodes from the graph and adds edges between the children of the selected
   * nodes and their parents.
   *
   * @param graph The graph from which the selected nodes are deleted.
   */
  public static void removeSelectedNodesKeepEdges(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01733: Graph argument can not be null");

    final List<NaviNode> selectedNodes = filterHiddenNodes(graph.getSelectedNodes());

    for (final NaviNode naviNode : selectedNodes) {
      connectParentsWithChildren(graph.getRawView(), naviNode.getRawNode());
    }

    graph.deleteNodes(selectedNodes);
  }

  /**
   * Removes the unselected nodes from the graph.
   *
   * @param graph The graph from which the unselected nodes are deleted.
   */
  public static void removeUnselectedNodes(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01734: Graph argument can not be null");

    final List<NaviNode> unselectedNodes =
        filterHiddenNodes(GraphHelpers.getUnselectedNodes(graph));

    graph.deleteNodes(unselectedNodes);
  }
}
