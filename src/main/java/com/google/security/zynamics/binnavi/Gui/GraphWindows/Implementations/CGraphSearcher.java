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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Contains methods for working with the search of a graph panel.
 */
public final class CGraphSearcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphSearcher() {
  }

  /**
   * Toggles the state of case sensitive search in a graph.
   *
   * @param graph The graph where the search state is toggled.
   */
  public static void toggleCaseSensitiveSearch(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01756: Graph argument can not be null");

    graph.getSettings().getSearchSettings()
        .setSearchCaseSensitive(!graph.getSettings().getSearchSettings().getSearchCaseSensitive());
  }

  /**
   * Toggles the state of regular expression search in a graph.
   *
   * @param graph The graph where the search state is toggled.
   */
  public static void toggleRegularExpressionSearch(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01757: Graph argument can not be null");

    graph.getSettings().getSearchSettings()
        .setSearchRegEx(!graph.getSettings().getSearchSettings().getSearchRegEx());
  }

  /**
   * Toggles the state of Search only Selected Nodes.
   *
   * @param graph The graph where the search state is toggled.
   */
  public static void toggleRestrictSearchToSelectedNodes(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01758: Graph argument can not be null");

    graph.getSettings().getSearchSettings().setSearchSelectedNodesOnly(
        !graph.getSettings().getSearchSettings().getSearchSelectedNodesOnly());
  }

  /**
   * Toggles the state of Restrict Search to Visible Nodes.
   *
   * @param graph The graph where the search state is toggled.
   */
  public static void toggleRestrictSearchToVisibleNodes(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01759: Graph argument can not be null");

    graph.getSettings().getSearchSettings().setSearchVisibleNodesOnly(
        !graph.getSettings().getSearchSettings().getSearchVisibleNodesOnly());
  }
}
