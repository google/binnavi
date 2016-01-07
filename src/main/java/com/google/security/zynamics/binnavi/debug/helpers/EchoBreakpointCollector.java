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
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.util.HashSet;
import java.util.Set;

/**
 * Collects all addresses where echo breakpoints can be set at the beginning of nodes in a graph.
 */
public final class EchoBreakpointCollector implements INodeCallback<NaviNode> {
  /**
   * Breakpoint manager that sets the echo breakpoints.
   */
  private final BreakpointManager manager;

  /**
   * Addresses where echo breakpoints can be set.
   */
  private final Set<BreakpointAddress> echoBreakpointAbleAddresses = new HashSet<>();

  /**
   * Creates a new echo breakpoint collector.
   *
   * @param manager Breakpoint manager that sets the echo breakpoints.
   */
  public EchoBreakpointCollector(final BreakpointManager manager) {
    this.manager = manager;
  }

  /**
   * Determines whether a breakpoint address is already blocked by a higher breakpoint.
   *
   * @param manager The breakpoint manager.
   * @param address The address to check.
   *
   * @return True, if the address is blocked. False, otherwise.
   */
  public static boolean isBlocked(final BreakpointManager manager,
      final BreakpointAddress address) {
    return (manager.hasBreakpoint(BreakpointType.REGULAR, address) && (
        manager.getBreakpointStatus(address, BreakpointType.REGULAR)
        != BreakpointStatus.BREAKPOINT_DISABLED))
        || manager.hasBreakpoint(BreakpointType.ECHO, address)
        || manager.hasBreakpoint(BreakpointType.STEP, address);
  }

  /**
   * Returns the addresses where echo breakpoints can be set.
   *
   * @return The addresses where echo breakpoints can be set.
   */
  public Set<BreakpointAddress> getBreakpoints() {
    return echoBreakpointAbleAddresses;
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
      if (isBlocked(manager, address)) {
        return IterationMode.CONTINUE;
      }
      NaviLogger.info("Adding Echo breakpoint %s to the active list",
          address.getAddress().getAddress().toHexString());

      // Add the echo breakpoint to the list of active echo breakpoints
      echoBreakpointAbleAddresses.add(address);
    } else if (viewNode instanceof INaviFunctionNode) {
      final INaviFunctionNode functionNode = (INaviFunctionNode) viewNode;
      final INaviModule module = functionNode.getFunction().getModule();
      final BreakpointAddress address = new BreakpointAddress(module,
          new UnrelocatedAddress(functionNode.getFunction().getAddress()));
      if (isBlocked(manager, address)) {
        return IterationMode.CONTINUE;
      }
      NaviLogger.info("Adding Echo breakpoint %s to the active list",
          address.getAddress().getAddress().toHexString());

      // Add the echo breakpoint to the list of active echo breakpoints
      echoBreakpointAbleAddresses.add(address);
    }
    return IterationMode.CONTINUE;
  }
}
