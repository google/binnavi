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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

/**
 * Contains helper functions for debugging a graph.
 */
public final class CGraphDebugger {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphDebugger() {
  }

  /**
   * Disables or enables a breakpoint at a given address.
   *
   * @param manager The breakpoint manager that is used to toggle the breakpoint status.
   * @param address The address where the breakpoint is set.
   */
  private static void toggleBreakpoint(
      final BreakpointManager manager, final BreakpointAddress address) {
    if (manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
      final BreakpointStatus status = manager.getBreakpointStatus(address, BreakpointType.REGULAR);

      if (status == BreakpointStatus.BREAKPOINT_DISABLED) {
        manager.setBreakpointStatus(
            Sets.newHashSet(address), BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ENABLED);
      } else if ((status == BreakpointStatus.BREAKPOINT_ACTIVE)
          || (status == BreakpointStatus.BREAKPOINT_INACTIVE)
          || (status == BreakpointStatus.BREAKPOINT_ENABLED)
          || (status == BreakpointStatus.BREAKPOINT_HIT)) {
        manager.setBreakpointStatus(
            Sets.newHashSet(address), BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);
      }
    }
  }

  /**
   * Determines the breakpoint status of a breakpoint at a given address.
   *
   * @param manager The breakpoint manager that set the breakpoint.
   * @param module The module where the breakpoint was set.
   * @param unrelocatedAddress The unrelocated address of the breakpoint.
   *
   * @return The status of the breakpoint.
   */
  public static BreakpointStatus getBreakpointStatus(final BreakpointManager manager,
      final INaviModule module, final UnrelocatedAddress unrelocatedAddress) {
    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    return manager.getBreakpointStatus(address, BreakpointType.REGULAR);
  }

  /**
   * Determines the debugger that is used with a given function node.
   *
   * @param debuggerProvider Debugger provider that contains all potential debuggers.
   * @param functionNode Function node whose debugger is determined.
   *
   * @return The debugger associated with the code node or null if there is no such debugger.
   */
  public static IDebugger getDebugger(
      final BackEndDebuggerProvider debuggerProvider, final INaviFunctionNode functionNode) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01706: Debugger provider argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE01707: Function node argument can not be null");

    return debuggerProvider.getDebugger(functionNode.getFunction().getModule());
  }

  /**
   * Determines the debugger that is used with a given code node.
   *
   * @param debuggerProvider Debugger provider that contains all potential debuggers.
   * @param instruction Instruction whose debugger is determined.
   *
   * @return The debugger associated with the code node or null if there is no such debugger.
   */
  public static IDebugger getDebugger(
      final BackEndDebuggerProvider debuggerProvider, final INaviInstruction instruction) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01704: Debugger provider argument can not be null");
    Preconditions.checkNotNull(instruction, "IE01705: Instruction argument can not be null");

    return debuggerProvider.getDebugger(instruction.getModule());
  }

  /**
   * Checks whether a breakpoint exists at the given address.
   *
   * @param manager The breakpoint manager that set the breakpoint.
   * @param module The module where the breakpoint was set.
   * @param unrelocatedAddress The unrelocated address of the breakpoint.
   *
   * @return True, if a breakpoint exists at that address. False, otherwise.
   */
  public static boolean hasBreakpoint(final BreakpointManager manager, final INaviModule module,
      final UnrelocatedAddress unrelocatedAddress) {
    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    return manager.hasBreakpoint(BreakpointType.REGULAR, address);
  }

  /**
   * Removes a breakpoint from the given address.
   *
   * @param manager The breakpoint manager that sets the breakpoint.
   * @param module The module where the breakpoint was set.
   * @param unrelocatedAddress The unrelocated address of the breakpoint.
   */
  public static void removeBreakpoint(final BreakpointManager manager, final INaviModule module,
      final UnrelocatedAddress unrelocatedAddress) {
    Preconditions.checkNotNull(manager, "IE01710: Breakpoint manager argument can not be null");
    Preconditions.checkNotNull(module, "IE01711: Module argument can not be null");
    Preconditions.checkNotNull(unrelocatedAddress, "IE01712: Address argument can not be null");

    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    if (manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
      removeBreakpoints(Sets.newHashSet(address), manager);
    }
  }

  /**
   * Removes a breakpoint from a breakpoint manager.
   *
   * @param addresses The address of the breakpoint to remove.
   * @param manager The breakpoint manager from where the breakpoint is removed.
   */
  public static void removeBreakpoints(
      final Set<BreakpointAddress> addresses, final BreakpointManager manager) {
    Preconditions.checkNotNull(manager, "IE01708: Manager argument can not be null");
    Preconditions.checkNotNull(addresses, "IE01709: Address argument can not be null");

    final Set<BreakpointAddress> addressesToRemoveFromManager = new HashSet<BreakpointAddress>();
    final Set<BreakpointAddress> addressesToRemoveFromDebugger = new HashSet<BreakpointAddress>();

    for (final BreakpointAddress address : addresses) {
      final BreakpointStatus status = manager.getBreakpointStatus(address, BreakpointType.REGULAR);
      if ((status == BreakpointStatus.BREAKPOINT_DISABLED)
          || (status == BreakpointStatus.BREAKPOINT_INACTIVE)) {
        addressesToRemoveFromManager.add(address);
      }
      if (status != BreakpointStatus.BREAKPOINT_DELETING) {
        addressesToRemoveFromDebugger.add(address);
      }
    }

    if (addressesToRemoveFromManager.size() != 0) {
      manager.removeBreakpoints(BreakpointType.REGULAR, addressesToRemoveFromManager);
    }
    if (addressesToRemoveFromDebugger.size() != 0) {
      manager.setBreakpointStatus(addressesToRemoveFromDebugger, BreakpointType.REGULAR,
          BreakpointStatus.BREAKPOINT_DELETING);
    }
  }

  /**
   * Sets a breakpoint at a given address.
   *
   * @param manager The breakpoint manager that sets the breakpoint.
   * @param module The module where the breakpoint is set.
   * @param unrelocatedAddress The unrelocated address of the breakpoint.
   */
  public static void setBreakpoint(final BreakpointManager manager, final INaviModule module,
      final UnrelocatedAddress unrelocatedAddress) {
    Preconditions.checkNotNull(manager, "IE01713: Manager argument can not be null");
    Preconditions.checkNotNull(module, "IE01714: Module argument can not be null");
    Preconditions.checkNotNull(unrelocatedAddress, "IE01715: Address argument can not be null");

    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    if (!manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
      manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(address));
    }
  }

  /**
   * Sets a breakpoint on an address or removes a breakpoint from an address.
   *
   * @param manager The breakpoint manager used to set or remove the breakpoint.
   * @param module The module where the breakpoint is set or removed.
   * @param unrelocatedAddress The unrelocated address where the breakpoint is set or removed.
   */
  public static void toggleBreakpoint(final BreakpointManager manager, final INaviModule module,
      final UnrelocatedAddress unrelocatedAddress) {
    Preconditions.checkNotNull(manager, "IE01716: Manager argument can not be null");
    Preconditions.checkNotNull(module, "IE01717: Module argument can not be null");
    Preconditions.checkNotNull(unrelocatedAddress, "IE01718: Address argument can not be null");

    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    if (manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
      removeBreakpoints(Sets.newHashSet(address), manager);
    } else {

      manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(address));
    }
  }

  /**
   * Sets or removes a breakpoint at a given line of a code node.
   *
   * @param debuggerProvider Debugger provider that contains all possible debuggers for the code
   *        node.
   * @param codeNode The code node where the breakpoint is set.
   * @param row The code node row where the breakpoint is set.
   */
  public static void toggleBreakpoint(final BackEndDebuggerProvider debuggerProvider,
      final INaviCodeNode codeNode, final int row) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01719: Debugger provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE01720: Code node argument can not be null");

    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, row);

    if (instruction == null) {
      return;
    }

    final IDebugger debugger = getDebugger(debuggerProvider, instruction);

    if (debugger == null) {
      return;
    }

    toggleBreakpoint(debugger.getBreakpointManager(), instruction.getModule(),
        new UnrelocatedAddress(instruction.getAddress()));
  }

  /**
   * Sets or removes a breakpoint from a function node.
   *
   * @param debuggerProvider Debugger provider that contains all possible debuggers for the function
   *        node.
   * @param functionNode Function node object where the breakpoint is set or removed.
   */
  public static void toggleBreakpoint(
      final BackEndDebuggerProvider debuggerProvider, final INaviFunctionNode functionNode) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01721: Debugger provider argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE01722: Function node argument can not be null");

    if (functionNode.getFunction().getType() == FunctionType.IMPORT) {
      // We can not set breakpoints on imported functions
      return;
    }

    final IDebugger debugger = getDebugger(debuggerProvider, functionNode);

    if (debugger == null) {
      return;
    }

    final INaviModule module = functionNode.getFunction().getModule();

    CGraphDebugger.toggleBreakpoint(debugger.getBreakpointManager(), module,
        new UnrelocatedAddress(functionNode.getFunction().getAddress()));
  }

  /**
   * Disables or enables a breakpoint at a given address.
   *
   * @param manager The breakpoint manager that is used to toggle the breakpoint status.
   * @param module The module where the breakpoint is set.
   * @param unrelocatedAddress The unrelocated address where the breakpoint is set.
   */
  public static void toggleBreakpointStatus(final BreakpointManager manager,
      final INaviModule module, final UnrelocatedAddress unrelocatedAddress) {
    Preconditions.checkNotNull(manager, "IE01723: Manager argument can not be null");
    Preconditions.checkNotNull(module, "IE01724: Module argument can not be null");
    Preconditions.checkNotNull(unrelocatedAddress, "IE01725: Address argument can not be null");

    final BreakpointAddress address = new BreakpointAddress(module, unrelocatedAddress);

    toggleBreakpoint(manager, address);
  }
}
