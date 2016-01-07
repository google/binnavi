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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.SelectableGraph;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;

/**
 * Contains code for expanding and shrinking graph selections.
 */
public final class CGraphSelectionExpander {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphSelectionExpander() {
  }

  /**
   * Checks whether the graph argument is null and throws if it is.
   *
   * @param graph The graph to check.
   */
  private static void checkArguments(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01760: Graph argument can not be null");
  }

  /**
   * Does an Expand Selection operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void expandSelection(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    GraphHelpers.expandSelectionUp(selectableGraph);
    GraphHelpers.expandSelectionDown(selectableGraph);
  }

  /**
   * Does an Expand Selection Down operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void expandSelectionDown(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.expandSelectionDown(selectableGraph);
    } else {
      final Collection<INaviViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());

      graph.selectNodes(
          GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes, 1)), true);
    }
  }

  /**
   * Does an Export Selection Up operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void expandSelectionUp(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    if (graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      GraphHelpers.expandSelectionUp(selectableGraph);
    } else {
      final Collection<INaviViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());

      graph.selectNodes(
          GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes, 1)), true);
    }
  }

  /**
   * Does a Shrink Selection operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void shrinkSelection(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    GraphHelpers.shrinkSelectionUp(selectableGraph);
    GraphHelpers.shrinkSelectionDown(selectableGraph);
  }

  /**
   * Does a Shrink Selection Down operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void shrinkSelectionDown(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    GraphHelpers.shrinkSelectionDown(selectableGraph);
  }

  /**
   * Does a Shrink Selection Up operation on a graph.
   *
   * @param graph The graph where the selection operation happens.
   */
  public static void shrinkSelectionUp(final ZyGraph graph) {
    checkArguments(graph);

    final SelectableGraph<NaviNode> selectableGraph = SelectableGraph.wrap(graph);

    GraphHelpers.shrinkSelectionUp(selectableGraph);
  }
}
