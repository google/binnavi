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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.IAbstractAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.IAbstractNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Or.IAbstractOrCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Root.IAbstractRootCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.IAbstractCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.IAbstractCriteriumTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Used to evaluate a criteria formula and to select all nodes of a graph that match the criteria
 * formula.
 */
public final class CCriteriumExecuter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CCriteriumExecuter() {
  }

  /**
   * Selects all nodes of a graph that match a given criterium.
   * 
   * @param tree The tree that specifies the formula to match.
   * @param graph The graph whose nodes are selected.
   */
  public static void execute(final IAbstractCriteriumTree tree, final ZyGraph graph) {
    // Use a temporary variable to work around OpenJDK build problem. Original code is:
    // graph.selectNodes(GraphHelpers.getNodes(graph), false);
    final List<NaviNode> nodes = GraphHelpers.filter(graph, new CriteriumFilter(tree));
    final List<NaviNode> allNodes = GraphHelpers.getNodes(graph);

    graph.selectNodes(allNodes, false);
    graph.selectNodes(nodes, true);
  }

  /**
   * Filter class that filters all nodes of a graph that match a criteria tree.
   */
  private static class CriteriumFilter implements ICollectionFilter<NaviNode> {
    /**
     * The criterium tree to match.
     */
    private final IAbstractCriteriumTree m_tree;

    /**
     * Creates a new filter object.
     * 
     * @param tree The criterium tree to match.
     */
    public CriteriumFilter(final IAbstractCriteriumTree tree) {
      m_tree = tree;
    }

    /**
     * Checks whether a given node matches a formula.
     * 
     * @param node The root node of the formula.
     * @param naviNode The node to match.
     * 
     * @return True, if the node matches the formula. False, otherwise.
     */
    private boolean qualifies(final IAbstractCriteriumTreeNode node, final NaviNode naviNode) {
      if (node.getCriterium() instanceof IAbstractRootCriterium) {
        return qualifiesRootNode(node, naviNode);
      } else if (node.getCriterium() instanceof IAbstractAndCriterium) {
        return qualifiesAndNode(node, naviNode);
      } else if (node.getCriterium() instanceof IAbstractOrCriterium) {
        return qualifiesOrNode(node, naviNode);
      } else if (node.getCriterium() instanceof IAbstractNotCriterium) {
        return qualifiesNotNode(node, naviNode);
      }

      return node.getCriterium().matches(naviNode);
    }

    /**
     * Checks whether a given AND node matches a formula.
     * 
     * @param node The root node of the formula.
     * @param naviNode The node to match.
     * 
     * @return True, if the node matches the formula. False, otherwise.
     */
    private boolean qualifiesAndNode(final IAbstractCriteriumTreeNode node, final NaviNode naviNode) {
      final List<? extends IAbstractCriteriumTreeNode> children = node.getChildren();

      if (children.size() < 2) {
        throw new IllegalStateException("IE01120: AND operator has less than two child criteria.");
      }

      for (final IAbstractCriteriumTreeNode child : node.getChildren()) {
        if (!qualifies(child, naviNode)) {
          return false;
        }
      }

      return true;
    }

    /**
     * Checks whether a given NOT node matches a formula.
     * 
     * @param node The root node of the formula.
     * @param naviNode The node to match.
     * 
     * @return True, if the node matches the formula. False, otherwise.
     */
    private boolean qualifiesNotNode(final IAbstractCriteriumTreeNode node, final NaviNode naviNode) {
      final List<? extends IAbstractCriteriumTreeNode> children = node.getChildren();

      if (children.size() != 1) {
        throw new IllegalStateException(
            "IE01142: NOT operator has more or less than one child criterium.");
      }

      return !qualifies(children.get(0), naviNode);
    }

    /**
     * Checks whether a given OR node matches a formula.
     * 
     * @param node The root node of the formula.
     * @param naviNode The node to match.
     * 
     * @return True, if the node matches the formula. False, otherwise.
     */
    private boolean qualifiesOrNode(final IAbstractCriteriumTreeNode node, final NaviNode naviNode) {
      final List<? extends IAbstractCriteriumTreeNode> children = node.getChildren();

      if (children.size() < 2) {
        throw new IllegalStateException("IE01141: OR operator has less than two child criteria.");
      }

      for (final IAbstractCriteriumTreeNode child : node.getChildren()) {
        if (qualifies(child, naviNode)) {
          return true;
        }
      }

      return false;
    }

    /**
     * Checks whether a given root node matches a formula.
     * 
     * @param node The root node of the formula.
     * @param naviNode The node to match.
     * 
     * @return True, if the node matches the formula. False, otherwise.
     */
    private boolean qualifiesRootNode(final IAbstractCriteriumTreeNode node,
        final NaviNode naviNode) {
      final List<? extends IAbstractCriteriumTreeNode> children = node.getChildren();

      if (children.isEmpty()) {
        throw new IllegalStateException("IE00469: Root node no child criterion.");
      }

      if (children.size() > 1) {
        throw new IllegalStateException("IE01112: Root node has more than one child criterion.");
      }

      return qualifies(children.get(0), naviNode);
    }

    @Override
    public boolean qualifies(final NaviNode item) {
      return qualifies(m_tree.getRoot(), item);
    }
  }
}
