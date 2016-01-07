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

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Echo Breakpoint Hit events from the debug client with the simulated target process.
 */
public final class EchoBreakpointHitSynchronizer extends
    ReplySynchronizer<EchoBreakpointHitReply> {
  /**
   * Creates a new Echo Breakpoint Hit synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public EchoBreakpointHitSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final EchoBreakpointHitReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();

    final long tid = reply.getThreadId();

    for (final ThreadRegisters threadRegisters : reply.getRegisterValues()) {
      if (tid == threadRegisters.getTid()) {
        for (final RegisterValue registerValue : threadRegisters) {
          if (registerValue.isPc()) {
            final RelocatedAddress address =
                new RelocatedAddress(new CAddress(registerValue.getValue()));

            manager.setBreakpointStatus(
                Sets.newHashSet(DebuggerHelpers.getBreakpointAddress(getDebugger(), address)),
                BreakpointType.ECHO, BreakpointStatus.BREAKPOINT_HIT);

            break;
          }
        }
      }
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final EchoBreakpointHitReply reply) {
    listener.receivedReply(reply);
  }
}
