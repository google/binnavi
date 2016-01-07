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

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointSetReply;
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
 * Synchronizes incoming replies to Set Echo Breakpoint requests by interpreting the effects of the
 * reply and applying these effects to the state of the debugger that sent the request.
 */
public final class EchoBreakpointSetSynchronizer extends
    ReplySynchronizer<EchoBreakpointSetReply> {
  /**
   * Creates a new Echo Breakpoint Set synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public EchoBreakpointSetSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final EchoBreakpointSetReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();
    final Set<BreakpointAddress> addressesToActivate = new HashSet<>();
    final Set<BreakpointAddress> addressesToRemove = new HashSet<>();

    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      final RelocatedAddress address = resultPair.first();
      if (resultPair.second() == 0) {
        addressesToActivate.add(DebuggerHelpers.getBreakpointAddress(getDebugger(), address));
      } else {
        addressesToRemove.add(DebuggerHelpers.getBreakpointAddress(getDebugger(), address));
      }
    }

    manager.setBreakpointStatus(addressesToActivate, BreakpointType.ECHO,
        BreakpointStatus.BREAKPOINT_ACTIVE);
    manager.removeBreakpointsPassive(BreakpointType.ECHO, addressesToRemove);
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final EchoBreakpointSetReply reply) {
    listener.receivedReply(reply);
  }
}
