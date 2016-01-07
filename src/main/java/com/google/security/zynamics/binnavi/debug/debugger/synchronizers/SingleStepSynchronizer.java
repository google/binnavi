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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Single Step replies with the debugger.
 */
public final class SingleStepSynchronizer extends ReplySynchronizer<SingleStepReply> {
  /**
   * Creates a new Single Step synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public SingleStepSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final SingleStepReply reply) {
    final ProcessManager processManager = getDebugger().getProcessManager();

    final long tid = reply.getThreadId();

    try {
      // Find the thread object with the specified TID
      final TargetProcessThread thread = processManager.getThread(tid);

      // At the end of a single step event, a thread is automatically suspended.
      processManager.setActiveThread(thread);

      // Update the thread object with the values from the event.
      setRegisterValues(reply.getRegisterValues());

      updateHitBreakpoints(
          DebuggerHelpers.getBreakpointAddress(getDebugger(), thread.getCurrentAddress()));

    } catch (final MaybeNullException e) {
      // Apparently there is no thread with the specified TID.
      // This is not necessarily an error because the thread might have
      // been closed while this handler was active.

      NaviLogger.info("Error: Process manager could not get thread. Exception %s", e);
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final SingleStepReply reply) {
    listener.receivedReply(reply);
  }
}
