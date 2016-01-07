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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;



/**
 * Contains code for counting nodes in different situations.
 */
public final class CNodeTypeCounter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeTypeCounter() {
  }

  /**
   * Counts nodes according to different criteria.
   *
   * @param nodes The nodes to count.
   *
   * @return A pair of selected node count and visible node count.
   */
  public static Pair<Integer, Integer> count(final List<NaviNode> nodes) {
    int selected = 0;
    int invisible = 0;

    for (final NaviNode graphNode : nodes) {
      if (graphNode.getRawNode().isSelected()) {
        selected++;
      }

      if (!graphNode.getRawNode().isVisible()) {
        invisible++;
      }
    }

    return new Pair<Integer, Integer>(selected, invisible);
  }
}
