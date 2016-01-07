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
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;

/**
 * Provides generic helper functions for stepping operations.
 */
public final class CSteppingHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CSteppingHelper() {
  }

  /**
   * Determines the start address of a node.
   *
   * @param node Node whose address is determined.
   *
   * @return The start address of the given node or null if the node does not have an address.
   */
  private static BreakpointAddress getAddress(final INaviViewNode node) {
    if (node instanceof INaviCodeNode) {
      final INaviCodeNode ccnode = (INaviCodeNode) node;

      final INaviInstruction instruction = Iterables.getFirst(ccnode.getInstructions(), null);

      return new BreakpointAddress(instruction.getModule(), new UnrelocatedAddress(instruction
          .getAddress()));
    } else if (node instanceof INaviFunctionNode) {
      final INaviFunction function = ((INaviFunctionNode) node).getFunction();
      final INaviModule module = function.getModule();

      return new BreakpointAddress(module, new UnrelocatedAddress(function.getAddress()));
    } else {
      // Node types we can not step to
      return null;
    }
  }

  /**
   * Determines the addresses of the successor blocks of a node.
   *
   * @param node Node whose successor addresses are added to the list.
   *
   * @return The list of addresses of the successor nodes.
   */
  public static Set<BreakpointAddress> getSuccessors(final INaviViewNode node) {
    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    final List<INaviEdge> edges = node.getOutgoingEdges();

    for (final INaviEdge edge : edges) {
      final BreakpointAddress childAddress = CSteppingHelper.getAddress(edge.getTarget());

      if (childAddress != null) {
        addresses.add(childAddress);
      }
    }

    return addresses;
  }
}
