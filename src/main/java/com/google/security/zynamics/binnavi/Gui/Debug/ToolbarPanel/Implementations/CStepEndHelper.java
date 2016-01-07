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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Contains helper functions for stepping to the end of a function.
 */
public final class CStepEndHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CStepEndHelper() {
  }

  /**
   * Returns the addresses of all final instructions of a graph.
   *
   * @param graph The graph whose final addresses are returned.
   *
   * @return The final addresses of the graph.
   */
  public static Set<BreakpointAddress> getEndAddresses(final ZyGraph graph) {
    final Set<BreakpointAddress> instructions = new HashSet<BreakpointAddress>();

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if ((node.getRawNode() instanceof INaviCodeNode) && node.getChildren().isEmpty()) {
          final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();

          final INaviInstruction lastInstruction = Iterables.getLast(cnode.getInstructions());

          instructions.add(new BreakpointAddress(
              lastInstruction.getModule(), new UnrelocatedAddress(lastInstruction.getAddress())));
        }

        return IterationMode.CONTINUE;
      }
    });

    return instructions;
  }
}
