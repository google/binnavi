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
package com.google.security.zynamics.binnavi.Gui.Debug.Notifier;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;


/**
 * Contains routines to synchronize breakpoints with newly loaded modules. For example, breakpoints
 * are enabled incrementally as new modules are loaded.
 */
public class CBreakpointModuleSynchronizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointModuleSynchronizer() {
  }

  /**
   * Checks whether the given breakpoint is within the boundaries of the specified module.
   * 
   * @param module The module against which the breakpoint is tested.
   * @param breakpoint The breakpoint to be tested.
   * @return True of the breakpoint is within the given module, false otherwise.
   */
  private static boolean isWithinModule(final IDebugger debugger, final MemoryModule module,
      final Breakpoint breakpoint) {
    final RelocatedAddress bpAddress =
        debugger.fileToMemory(breakpoint.getAddress().getModule(), breakpoint.getAddress()
            .getAddress());

    final boolean addressOk =
        (bpAddress.getAddress().toBigInteger()
            .compareTo(module.getBaseAddress().getAddress().toBigInteger()) >= 0)
            && (bpAddress
                .getAddress()
                .toBigInteger()
                .compareTo(
                    module.getBaseAddress().getAddress().toBigInteger()
                        .add(BigInteger.valueOf(module.getSize()))) <= 0);
    return addressOk
        && (module.getName().compareToIgnoreCase(
            breakpoint.getAddress().getModule().getConfiguration().getName()) == 0);
  }

  private static void processModuleBreakpoints(final IDebugger debugger,
      final MemoryModule module, final BreakpointType breakPointType,
      final IModuleBreakpointEnumerator callback) {
    final BreakpointManager manager = debugger.getBreakpointManager();
    final Set<BreakpointAddress> breakpointAddresses = new HashSet<BreakpointAddress>();

    for (final Breakpoint breakpoint : manager.getBreakpointsByModule(breakPointType, module)) {
      if ((manager.getBreakpointStatus(breakpoint.getAddress(), breakpoint.getType()) != BreakpointStatus.BREAKPOINT_ENABLED)
          && (manager.getBreakpointStatus(breakpoint.getAddress(), breakpoint.getType()) != BreakpointStatus.BREAKPOINT_DISABLED)
          && isWithinModule(debugger, module, breakpoint)) {
        breakpointAddresses.add(breakpoint.getAddress());
      }
    }
    callback.handleAddress(manager, breakpointAddresses);
  }

  public static void disableEchoBreakpoints(final IDebugger debugger, final MemoryModule module) {
    processModuleBreakpoints(debugger, module, BreakpointType.ECHO, new DisableBreakpointsAction(
        BreakpointType.ECHO));
  }

  public static void disableRegularBreakpoints(final IDebugger debugger, final MemoryModule module) {
    processModuleBreakpoints(debugger, module, BreakpointType.REGULAR,
        new DisableBreakpointsAction(BreakpointType.REGULAR));
  }

  public static void enableEchoBreakpoints(final IDebugger debugger, final MemoryModule module) {
    processModuleBreakpoints(debugger, module, BreakpointType.ECHO, new EnableBreakpointsAction(
        BreakpointType.ECHO));
  }

  public static void enableRegularBreakpoints(final IDebugger debugger, final MemoryModule module) {
    processModuleBreakpoints(debugger, module, BreakpointType.REGULAR, new EnableBreakpointsAction(
        BreakpointType.REGULAR));
  }

  private static class DisableBreakpointsAction implements IModuleBreakpointEnumerator {
    private final BreakpointType m_bpType;

    public DisableBreakpointsAction(final BreakpointType bpType) {
      m_bpType = bpType;
    }

    @Override
    public void handleAddress(final BreakpointManager manager,
        final Set<BreakpointAddress> breakpoints) {
      manager.setBreakpointStatus(breakpoints, m_bpType, BreakpointStatus.BREAKPOINT_INACTIVE);
    }
  }

  private static class EnableBreakpointsAction implements IModuleBreakpointEnumerator {
    private final BreakpointType m_bpType;

    public EnableBreakpointsAction(final BreakpointType bpType) {
      m_bpType = bpType;
    }

    @Override
    public void handleAddress(final BreakpointManager manager,
        final Set<BreakpointAddress> breakpoints) {
      manager.setBreakpointStatus(breakpoints, m_bpType, BreakpointStatus.BREAKPOINT_ENABLED);
    }
  }

  private interface IModuleBreakpointEnumerator {
    void handleAddress(final BreakpointManager manager, final Set<BreakpointAddress> breakpoints);
  }
}
