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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes incoming Breakpoint Hit replies by interpreting the effects of the reply and
 * applying these effects to the state of the debugger that sent the Attach request.
 */
public final class BreakpointHitSynchronizer extends ReplySynchronizer<BreakpointHitReply> {
  /**
   * Creates a new Breakpoint Hit synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public BreakpointHitSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  /**
   * Handles incoming Breakpoint Hit replies.
   *
   * @param reply The incoming Breakpoint Hit reply to handle.
   */
  @Override
  protected void handleSuccess(final BreakpointHitReply reply) {
    final ProcessManager processManager = getDebugger().getProcessManager();

    // When the debug client notifies BinNavi that a
    // breakpoint was hit, it is necessary to mark the
    // breakpoint as hit.

    // TODO: Check for success

    RelocatedAddress eventAddress = null;

    final RegisterValues registerValues = reply.getRegisterValues();
    final long tid = reply.getThreadId();

    for (final ThreadRegisters threadRegisters : registerValues) {
      if (tid == threadRegisters.getTid()) {
        for (final RegisterValue registerValue : threadRegisters) {
          if (registerValue.isPc()) {
            eventAddress = new RelocatedAddress(new CAddress(registerValue.getValue()));
          }
        }
      }
    }

    if (eventAddress != null) {
      updateHitBreakpoints(DebuggerHelpers.getBreakpointAddress(getDebugger(), eventAddress));
    } else {
      throw new IllegalStateException("IE00173: register reply did not include program counter");
    }

    try {
      final TargetProcessThread thread = processManager.getThread(reply.getThreadId());

      // Update the thread with the new register values.
      for (final ThreadRegisters threadRegisters : registerValues) {
        if (tid == threadRegisters.getTid()) {
          thread.setRegisterValues(threadRegisters.getRegisters());
          break;
        }
      }

      processManager.setActiveThread(thread);
      thread.setCurrentAddress(eventAddress);
    } catch (final MaybeNullException exception) {
      NaviLogger.info("Error: there is no thread with the specified thread id %d Exception: %s",
          reply.getThreadId(), exception);
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final BreakpointHitReply reply) {
    listener.receivedReply(reply);
  }
}
