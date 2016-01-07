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

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.util.HashSet;
import java.util.Set;



/**
 * Provides helper functions for block stepping operations.
 */
public final class CStepBlockHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CStepBlockHelper() {
  }

  /**
   * Determines whether a node contains a given address.
   *
   * @param node The node in question.
   * @param address The address in questions.
   *
   * @return True, if the node contains the address. False, otherwise.
   */
  private static boolean containsAddress(
      final INaviViewNode node, final UnrelocatedAddress address) {
    return (node instanceof INaviCodeNode
        && CCodeNodeHelpers.containsAddress((INaviCodeNode) node, address.getAddress())) || (
        node instanceof INaviFunctionNode
        && ((INaviFunctionNode) node).getFunction().getAddress().equals(address.getAddress()));
  }

  /**
   * Determines the blocks that can be reached from the blocks that contain a given address.
   *
   * @param graph Graph where the block step happens.
   * @param address The address in question.
   *
   * @return A list of block addresses.
   */
  public static Set<BreakpointAddress> getNextBlocks(
      final ZyGraph graph, final UnrelocatedAddress address) {
    final Set<BreakpointAddress> instructions = new HashSet<BreakpointAddress>();

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if (containsAddress(node.getRawNode(), address)) {
          instructions.addAll(CSteppingHelper.getSuccessors(node.getRawNode()));
        }

        return IterationMode.CONTINUE;
      }
    });

    return instructions;
  }
}
