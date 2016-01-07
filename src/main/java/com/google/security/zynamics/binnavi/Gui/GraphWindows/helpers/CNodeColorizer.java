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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.helpers;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.awt.Color;



/**
 * Contains code for changing the color of nodes in a graph.
 */
public final class CNodeColorizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeColorizer() {
  }

  /**
   * Changes the color of the selected nodes of a graph.
   *
   * @param graph Graph whose selected nodes are changed.
   * @param color The new color of the selected nodes.
   */
  public static void colorizeSelectedNodes(final ZyGraph graph, final Color color) {
    IteratorFunctions.iterateSelected(graph, new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        node.getRawNode().setColor(color);

        return IterationMode.CONTINUE;
      }
    });
  }
}
