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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Resume replies with the debugger.
 */
public final class ResumeSynchronizer extends ReplySynchronizer<ResumeReply> {
  /**
   * Creates a new Resume synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ResumeSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final ResumeReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();
    final ProcessManager processManager = getDebugger().getProcessManager();

    // TODO: At this point it is assumed that there can never be more
    // than one hit breakpoint at the same time. When a thread is resumed,
    // this one breakpoint is set to ACTIVE again.

    for (final Breakpoint breakpoint : manager.getBreakpoints(BreakpointType.REGULAR)) {
      if (manager.getBreakpointStatus(breakpoint.getAddress(), breakpoint.getType())
          == BreakpointStatus.BREAKPOINT_HIT) {
        manager.setBreakpointStatus(Sets.newHashSet(breakpoint.getAddress()), breakpoint.getType(),
            BreakpointStatus.BREAKPOINT_ACTIVE);
      }
    }
    processManager.setActiveThread(null);
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final ResumeReply reply) {
    listener.receivedReply(reply);
  }
}
