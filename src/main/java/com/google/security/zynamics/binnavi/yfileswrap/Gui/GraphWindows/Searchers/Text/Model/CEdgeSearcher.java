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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CElementSearcher;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper class for searching through the label of an edge.
 *
 * TODO: Factor out the common parts with CNodeSearcher.
 */
public final class CEdgeSearcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CEdgeSearcher() {
  }

  /**
   * Searches through the label of an edge.
   *
   * @param edge The edge to search through.
   * @param searchString The string to search for.
   * @param regEx True, to signal regular expression search.
   * @param caseSensitive True, to signal case sensitive search.
   *
   * @return Search results found in the edge label.
   */
  public static List<SearchResult> search(final NaviEdge edge, final String searchString,
      final boolean regEx, final boolean caseSensitive) {
    if (edge.getRealizerLabelCount() != 1) {
      return new ArrayList<SearchResult>();
    }

    return CElementSearcher.search(
        edge, edge.getLabelContent(), searchString, regEx, caseSensitive);
  }
}
