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
package com.google.security.zynamics.binnavi.ZyGraph.helpers;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * Contains code for collecting all node colors of a graph.
 */
public final class CNodeColorCollector {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeColorCollector() {
  }

  /**
   * Returns a list of all node colors in the graph.
   *
   * @param graph The graph whose node colors are found.
   *
   * @return The node colors of the graph.
   *
   *         TODO: Change return value to Set.
   */
  public static List<Color> getNodeColors(final ZyGraph graph) {
    final Set<Color> colors = new HashSet<Color>();

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode item) {
        colors.add(item.getRawNode().getColor());

        return IterationMode.CONTINUE;
      }
    });

    return new ArrayList<Color>(colors);
  }
}
