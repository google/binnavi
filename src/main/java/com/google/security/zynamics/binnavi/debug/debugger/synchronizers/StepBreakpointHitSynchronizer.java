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

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Step Breakpoint Hit events from the debug client with the simulated target process.
 */
public final class StepBreakpointHitSynchronizer extends ReplySynchronizer<StepBreakpointHitReply> {
  /**
   * Creates a new Step Breakpoint Hit synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public StepBreakpointHitSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final StepBreakpointHitReply reply) {
    final BreakpointManager manager = getDebugger().getBreakpointManager();
    final ProcessManager processManager = getDebugger().getProcessManager();
    RelocatedAddress breakpointAddress = null;
    final RegisterValues registerValues = reply.getRegisterValues();
    final long tid = reply.getThreadId();

    for (final ThreadRegisters threadRegisters : registerValues) {
      if (tid == threadRegisters.getTid()) {
        for (final RegisterValue registerValue : threadRegisters) {
          if (registerValue.isPc()) {
            breakpointAddress = new RelocatedAddress(new CAddress(registerValue.getValue()));
            break;
          }
        }
      }
    }

    manager.clearBreakpointsPassive(BreakpointType.STEP);

    try {
      final TargetProcessThread thread = processManager.getThread(tid);

      for (final ThreadRegisters threadRegisters : registerValues) {
        if (tid == threadRegisters.getTid()) {
          // Update the thread with the new register values.
          thread.setRegisterValues(threadRegisters.getRegisters());
        }
      }

      processManager.setActiveThread(thread);
      thread.setCurrentAddress(breakpointAddress);
    } catch (final MaybeNullException exception) {
      // Apparently there is no thread with the specified TID.
      // This is not necessarily an error because the thread might have
      // been closed while this handler was active.
      // Nevertheless this should be logged.
      NaviLogger.info("Error: Process manager could not get thread. Exception %s", exception);
      return;
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final StepBreakpointHitReply reply) {
    listener.receivedReply(reply);
  }
}
