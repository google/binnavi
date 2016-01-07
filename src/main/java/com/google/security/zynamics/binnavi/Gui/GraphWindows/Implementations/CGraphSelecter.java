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

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.ZyGraph.Filter.CInvisibleNodeFilter;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.gui.zygraph.functions.SelectionFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.SelectableGraph;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;

/**
 * Class that contains functions for selecting and deselecting parts of graphs.
 */
// TODO(cblichmann): Fix spelling to GraphSelector
public final class CGraphSelecter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphSelecter() {
  }

  /**
   * Inverts the selection in a graph. The formerly selected nodes are unselected while the formerly
   * unselected nodes are selected.
   * 
   * @param graph The graph where the selection operation happens.
   */
  public static void invertSelection(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01741: Graph argument can not be null");

    SelectionFunctions.invertSelection(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      graph.selectNodes(GraphHelpers.filter(graph, new CInvisibleNodeFilter()), false);
    }
  }

  /**
   * Selects all child nodes of the currently selected nodes in a graph.
   * 
   * @param graph The graph where the selection operation happens.
   */
  public static void selectChildrenOfSelection(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01935: Graph argument can not be null");

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.selectSuccessorsOfSelection(selectableGraph);
    } else {
      final Collection<INaviViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());

      graph.selectNodes(GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes)),
          true);
    }
  }

  /**
   * Selects all function nodes of a graph that have a given type.
   * 
   * @param graph The graph whose nodes are selected.
   * @param type The type of the function.
   */
  public static void selectNodesWithFunctionType(final ZyGraph graph, final FunctionType type) {
    final List<NaviNode> nodes = GraphHelpers.filter(graph, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return item.getRawNode() instanceof INaviFunctionNode
            && ((INaviFunctionNode) item.getRawNode()).getFunction().getType().equals(type);
      }
    });

    graph.selectNodes(nodes, true);
  }

  /**
   * Selects all code nodes of a graph that have a given parent function.
   * 
   * @param graph The graph whose nodes are selected.
   * @param function The function to search for.
   */
  public static void selectNodesWithParentFunction(final ZyGraph graph, final INaviFunction function) {
    final List<NaviNode> nodes = GraphHelpers.filter(graph, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        try {
          return item.getRawNode() instanceof INaviCodeNode
              && ((INaviCodeNode) item.getRawNode()).getParentFunction() == function;
        } catch (final MaybeNullException e) {
          return false;
        }
      }
    });

    graph.selectNodes(nodes, true);
  }

  /**
   * Selects all parent nodes of the currently selected nodes in a graph.
   * 
   * @param graph The graph where the selection operation happens.
   */
  public static void selectParentsOfSelection(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01742: Graph argument can not be null");

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.selectPredecessorsOfSelection(selectableGraph);
    } else {
      final Collection<INaviViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());

      graph.selectNodes(GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes)),
          true);
    }
  }

  /**
   * Selects all predecessors of a given node.
   * 
   * @param graph The graph where the selection operation happens.
   * @param node The node whose predecessors are selected.
   */
  public static void selectPredecessors(final ZyGraph graph, final NaviNode node) {
    Preconditions.checkNotNull(graph, "IE01838: Graph argument can not be null");
    Preconditions.checkNotNull(node, "IE01839: Node argument can not be null");

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.selectPredecessors(selectableGraph, node);
    } else {
      graph.selectNodes(
          GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(node.getRawNode())), true);
    }
  }

  /**
   * Selects all successors of a given node.
   * 
   * @param graph The graph where the selection operation happens.
   * @param node The node whose successors are selected.
   */
  public static void selectSuccessors(final ZyGraph graph, final NaviNode node) {
    Preconditions.checkNotNull(graph, "IE01483: Graph argument can not be null");

    Preconditions.checkNotNull(node, "IE01761: Node argument can not be null");

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.selectSuccessors(selectableGraph, node);
    } else {
      graph.selectNodes(
          GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(node.getRawNode())), true);
    }
  }
}
