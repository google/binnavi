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
package com.google.security.zynamics.binnavi.debug.helpers;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Class used for counting the nodes of the graph where breakpoints can be set and where there are
 * not yet any breakpoints set.
 */
public final class BreakpointableNodeCounter implements INodeCallback<NaviNode> {
  /**
   * Breakpoint manager used to set the breakpoints.
   */
  private final BreakpointManager breakpointManager;

  /**
   * Number of nodes where breakpoints can be set.
   */
  private int breakpointAbleNodeCount = 0;

  /**
   * Creates a new node counter object.
   *
   * @param manager Breakpoint manager used to set the breakpoints.
   */
  public BreakpointableNodeCounter(final BreakpointManager manager) {
    breakpointManager = manager;
  }

  /**
   * Returns the number of nodes where breakpoints can be set.
   *
   * @return The number of nodes where breakpoints can be set.
   */
  public int getCount() {
    return breakpointAbleNodeCount;
  }

  @Override
  public IterationMode next(final NaviNode node) {
    final INaviViewNode viewNode = node.getRawNode();

    if (viewNode instanceof INaviCodeNode) {
      final INaviCodeNode codeNode = (INaviCodeNode) viewNode;
      final INaviInstruction instruction = Iterables.getFirst(codeNode.getInstructions(), null);
      final INaviModule module = instruction.getModule();
      final BreakpointAddress address =
          new BreakpointAddress(module, new UnrelocatedAddress(instruction.getAddress()));
      if (EchoBreakpointCollector.isBlocked(breakpointManager, address)) {
        return IterationMode.CONTINUE;
      }
      ++breakpointAbleNodeCount;
    } else if (viewNode instanceof INaviFunctionNode) {
      final INaviFunctionNode functionNode = (INaviFunctionNode) viewNode;
      final INaviModule module = functionNode.getFunction().getModule();
      final BreakpointAddress address = new BreakpointAddress(module,
          new UnrelocatedAddress(functionNode.getFunction().getAddress()));
      if (EchoBreakpointCollector.isBlocked(breakpointManager, address)) {
        return IterationMode.CONTINUE;
      }
      ++breakpointAbleNodeCount;
    }
    return IterationMode.CONTINUE;
  }
}
