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

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import java.util.List;


/**
 * Helper class for searching through nodes.
 */
public final class CNodeSearcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeSearcher() {
  }

  /**
   * Searches through a node.
   *
   * @param node The node to search through.
   * @param searchString The string to search for.
   * @param regEx True, to signal regular expression search.
   * @param caseSensitive True, to signal case sensitive search.
   *
   * @return Search results found in the node.
   */
  public static List<SearchResult> search(final NaviNode node, final String searchString,
      final boolean regEx, final boolean caseSensitive) {
    return CElementSearcher.search(
        node, node.getRealizer().getNodeContent(), searchString, regEx, caseSensitive);
  }
}
