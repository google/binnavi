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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.util.List;



/**
 * Class for filtering all search results to account only for those results that match active search
 * settings.
 */
public final class CResultFilter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CResultFilter() {
  }

  /**
   * Returns only those edges that should be considered by the search.
   *
   * @param edges All edges.
   * @param onlySelected True, if selected nodes search only is enabled.
   * @param onlyVisible True, if visible nodes search only is enabled.
   *
   * @return The filtered edges.
   */
  public static List<NaviEdge> filteredEdges(
      final List<NaviEdge> edges, final boolean onlySelected, final boolean onlyVisible) {
    return CollectionHelpers.filter(edges, new ICollectionFilter<NaviEdge>() {
      @Override
      public boolean qualifies(final NaviEdge edge) {
        return (!onlySelected || edge.isSelected()) && (!onlyVisible || edge.isVisible());
      }
    });
  }

  /**
   * Returns only those nodes that should be considered by the search.
   *
   * @param nodes All nodes.
   * @param onlySelected True, if selected nodes search only is enabled.
   * @param onlyVisible True, if visible nodes search only is enabled.
   *
   * @return The filtered nodes.
   */
  public static List<NaviNode> filteredNodes(
      final List<NaviNode> nodes, final boolean onlySelected, final boolean onlyVisible) {
    return CollectionHelpers.filter(nodes, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode node) {
        // http://www-ihs.theoinf.tu-ilmenau.de/~sane/projekte/karnaugh/embed_karnaugh.html

        return (!onlySelected || node.isSelected()) && (!onlyVisible || node.isVisible());
      }
    });
  }

}
