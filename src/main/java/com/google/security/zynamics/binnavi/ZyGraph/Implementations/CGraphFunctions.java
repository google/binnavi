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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model.GraphSearcher;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityRangeCalculator;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Contains a few graph helper functions.
 */
public final class CGraphFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphFunctions() {
  }

  /**
   * Counts the number of invisible nodes in a list of nodes.
   *
   * @param nodes The nodes to count.
   *
   * @return The number of invisible nodes in the list.
   */
  private static int countInvisibleNodes(final Collection<NaviNode> nodes) {
    return CollectionHelpers.countIf(nodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return !item.isVisible();
      }
    });
  }

  /**
   * If the visibility threshold is reached, this function prompts the user whether he really wants
   * to make more nodes visible.
   *
   * @param parent Parent used for dialogs.
   * @param graph The graph where the nodes are shown and hidden.
   * @param invisibleNodes Number of nodes to be made visible.
   *
   * @return True, if the user cancels. False, if he wants to show the nodes.
   */
  private static boolean userCancelsMakingVisible(
      final Window parent, final ZyGraph graph, final int invisibleNodes) {
    return (invisibleNodes
        >= graph.getSettings().getLayoutSettings().getVisibilityWarningTreshold()) && (
        JOptionPane.YES_OPTION != CMessageBox.showYesNoCancelQuestion(parent, String.format(
            "The selected operation makes %d more nodes visible. Do you want to continue?",
            invisibleNodes)));
  }

  /**
   * Selects all nodes of a graph that contain a given search string.
   *
   * @param graph The graph to search through and select.
   * @param searchString The search string to search for.
   */
  public static void selectNodesWithString(final ZyGraph graph, final String searchString) {
    Preconditions.checkNotNull(graph, "IE02117: Graph argument can not be null");
    Preconditions.checkNotNull(searchString, "IE02118: Search string argument can not be null");

    final GraphSearcher searcher = new GraphSearcher();

    searcher.search(GraphHelpers.getNodes(graph), new ArrayList<NaviEdge>(), searchString);

    final List<SearchResult> results = searcher.getResults();

    final List<NaviNode> resultNodes = new ArrayList<NaviNode>();

    for (final SearchResult searchResult : results) {
      resultNodes.add((NaviNode) searchResult.getObject());
    }

    graph.selectNodes(resultNodes, true);

    searcher.dispose();
  }

  /**
   * Shows and hides nodes of a graph in one step.
   *
   * @param parent Parent used for dialogs.
   * @param graph The graph where the nodes are shown and hidden.
   * @param toShow List of nodes to show.
   * @param toHide List of nodes to hide.
   */
  public static void showNodes(final JFrame parent, final ZyGraph graph,
      final Collection<NaviNode> toShow, final Collection<NaviNode> toHide) {
    Preconditions.checkNotNull(graph, "IE02120: Graph argument can not be null");
    Preconditions.checkNotNull(toShow, "IE02121: toShow argument can not be null");
    Preconditions.checkNotNull(toHide, "IE02122: toHide argument can not be null");

    final ZyGraphViewSettings settings = graph.getSettings();

    final Set<NaviNode> neighbours = ProximityRangeCalculator.getNeighbors(
        graph, toShow, settings.getProximitySettings().getProximityBrowsingChildren(),
        settings.getProximitySettings().getProximityBrowsingParents());
    neighbours.addAll(toShow);

    final int invisibleNodes = countInvisibleNodes(neighbours);

    if (userCancelsMakingVisible(parent, graph, invisibleNodes)) {
      return;
    }

    graph.showNodes(toShow, toHide);
  }

  /**
   * Shows or hides nodes of a graph in one step.
   *
   * @param parent Parent used for dialogs.
   * @param graph The graph where the nodes are shown or hidden.
   * @param nodes List of nodes to show or hide.
   * @param visible True, to show the nodes. False, to hide the nodes.
   */
  public static void showNodes(final Window parent, final ZyGraph graph,
      final Collection<NaviNode> nodes, final boolean visible) {
    Preconditions.checkNotNull(parent, "IE02123: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE02124: Graph argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02125: Nodes argument can not be null");

    if (visible) {
      final ZyGraphViewSettings settings = graph.getSettings();

      final Set<NaviNode> neighbours = ProximityRangeCalculator.getNeighbors(
          graph, nodes, settings.getProximitySettings().getProximityBrowsingChildren(),
          settings.getProximitySettings().getProximityBrowsingParents());
      neighbours.addAll(nodes);

      final int invisibleNodes = countInvisibleNodes(neighbours);

      if (userCancelsMakingVisible(parent, graph, invisibleNodes)) {
        return;
      }
    }

    graph.showNodes(nodes, visible);
  }
}
