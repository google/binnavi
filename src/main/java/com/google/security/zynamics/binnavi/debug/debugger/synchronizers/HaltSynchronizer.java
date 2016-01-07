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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.HaltReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Halt replies with the debugger.
 */
public final class HaltSynchronizer extends ReplySynchronizer<HaltReply> {
  /**
   * Creates a new halt reply synchronizer.
   *
   * @param debugger The debugger to be synchronized.
   * @param listeners Listeners that are notified about the received halt reply.
   */
  public HaltSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final HaltReply reply) {
    final ProcessManager processManager = getDebugger().getProcessManager();
    try {
      final TargetProcessThread thread = processManager.getThread(reply.getTid());
      processManager.setActiveThread(thread);
    } catch (final MaybeNullException e) {
      NaviLogger.severe("Error: Process manager could not set active threads. Exception %s", e);
      // Apparently there is no thread with the specified TID.
      // This is not necessarily an error because the thread might have
      // been closed while this handler was active.
      // Nevertheless this should be logged.
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final HaltReply reply) {
    listener.receivedReply(reply);
  }
}
