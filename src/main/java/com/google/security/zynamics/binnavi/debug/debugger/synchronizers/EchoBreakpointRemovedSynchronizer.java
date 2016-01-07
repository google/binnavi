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
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Synchronizes incoming replies to Remove Echo Breakpoint requests by interpreting the effects of
 * the reply and applying these effects to the state of the debugger that sent the request.
 */
public final class EchoBreakpointRemovedSynchronizer extends
    ReplySynchronizer<EchoBreakpointsRemovedReply> {
  /**
   * Creates a new Echo Breakpoint Removed synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public EchoBreakpointRemovedSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final EchoBreakpointsRemovedReply reply) {
    // Assumption: Before this event can be received, the breakpoint was
    // already removed from the breakpoint manager so we do not have to
    // do it again.

    final BreakpointManager manager = getDebugger().getBreakpointManager();

    for (final Pair<RelocatedAddress, Integer> resultPair : reply.getAddresses()) {
      // TODO: This needs to be rewritten as soon as breakpoint lifecycling gets clearer.
      if (resultPair.second() == 0) {
        final BreakpointAddress address =
            DebuggerHelpers.getBreakpointAddress(getDebugger(), resultPair.first());

        try {
          if (manager.hasBreakpoint(BreakpointType.ECHO, address)) {
            manager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(address));
          }
        } catch (final IllegalArgumentException ex) {
          NaviLogger.severe("Error: Manager could not remove breakpoint. Exception %s", ex);
        }
      }
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final EchoBreakpointsRemovedReply reply) {
    listener.receivedReply(reply);
  }
}
