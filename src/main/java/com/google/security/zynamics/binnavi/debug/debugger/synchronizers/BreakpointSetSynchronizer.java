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
package com.google.security.zynamics.binnavi.debug.debugger.synchronizers;

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * Synchronizes incoming replies to Set Breakpoint requests by interpreting the effects of the reply
 * and applying these effects to the state of the debugger that sent the request.
 */
public final class BreakpointSetSynchronizer extends ReplySynchronizer<BreakpointSetReply> {
  /**
   * Creates a new Breakpoint Set synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public BreakpointSetSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final BreakpointSetReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();

    final Set<BreakpointAddress> addressesToActivate = new HashSet<>();
    final Set<BreakpointAddress> addressesToInvalidate = new HashSet<>();

    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      if (resultPair.second() == 0) {
        // If a breakpoint was successfully set in the target process, its status is
        // set to ACTIVE in the breakpoint manager.

        final BreakpointAddress breakpointAddress =
            DebuggerHelpers.getBreakpointAddress(getDebugger(), resultPair.first());

        if (manager.getBreakpointStatus(breakpointAddress, BreakpointType.REGULAR)
            != BreakpointStatus.BREAKPOINT_DISABLED) {
          addressesToActivate.add(breakpointAddress);
        }
      } else {
        // If a breakpoint could not be set in the target process, its status in the
        // breakpoint manager is set to INVALID. The real status of the breakpoint
        // is unknown though.

        addressesToInvalidate.add(
            DebuggerHelpers.getBreakpointAddress(getDebugger(), resultPair.first()));
      }
    }
    manager.setBreakpointStatus(addressesToInvalidate, BreakpointType.REGULAR,
        BreakpointStatus.BREAKPOINT_INVALID);
    manager.setBreakpointStatus(addressesToActivate, BreakpointType.REGULAR,
        BreakpointStatus.BREAKPOINT_ACTIVE);
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final BreakpointSetReply reply) {
    listener.receivedReply(reply);
  }
}
