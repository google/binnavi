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

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerErrorCodes;
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
 * Synchronizes Set Step Breakpoint replies with the debugger.
 */
public final class StepBreakpointSetSynchronizer extends ReplySynchronizer<StepBreakpointSetReply> {
  /**
   * Creates a new Step Breakpoint Set synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public StepBreakpointSetSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  /**
   * When a stepping breakpoint was set in the target process, its state is set to ACTIVE in the
   * breakpoint manager.
   */
  @Override
  protected void handleSuccess(final StepBreakpointSetReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();
    final Set<BreakpointAddress> addressesToActivate = new HashSet<BreakpointAddress>();
    final Set<BreakpointAddress> addressesToRemove = new HashSet<BreakpointAddress>();

    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      final RelocatedAddress address = resultPair.first();

      if (resultPair.second() == DebuggerErrorCodes.SUCCESS) {
        addressesToActivate.add(DebuggerHelpers.getBreakpointAddress(getDebugger(), address));
      } else {
        addressesToRemove.add(DebuggerHelpers.getBreakpointAddress(getDebugger(), address));
      }
    }
    manager.setBreakpointStatus(addressesToActivate, BreakpointType.STEP,
        BreakpointStatus.BREAKPOINT_ACTIVE);
    manager.removeBreakpoints(BreakpointType.STEP, addressesToRemove);
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final StepBreakpointSetReply reply) {
    listener.receivedReply(reply);
  }
}
