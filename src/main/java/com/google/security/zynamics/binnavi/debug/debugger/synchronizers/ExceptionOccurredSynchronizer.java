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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ExceptionOccurredReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerExceptionHandlingAction;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Exception Occurred events from the debug client with the simulated target process.
 */
public final class ExceptionOccurredSynchronizer extends ReplySynchronizer<ExceptionOccurredReply> {
  /**
   * Creates a new Exception Occurred synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ExceptionOccurredSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final ExceptionOccurredReply reply) {
    final ProcessManager processManager = getDebugger().getProcessManager();
    try {
      final TargetProcessThread thread = processManager.getThread(reply.getThreadId());
      processManager.setActiveThread(thread);
      thread.setCurrentAddress(reply.getAddress());
      processManager.addExceptionEvent(new DebuggerException(reply.getExceptionName(),
          reply.getExceptionCode(), DebuggerExceptionHandlingAction.Continue));
      refreshRegisters();
    } catch (final MaybeNullException exception) {
      NaviLogger.severe("Exception occured in unknown thread %d", reply.getThreadId());
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final ExceptionOccurredReply reply) {
    listener.receivedReply(reply);
  }
}
