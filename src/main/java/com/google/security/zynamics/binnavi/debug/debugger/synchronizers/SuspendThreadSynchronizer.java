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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SuspendThreadReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Resume replies with the debugger.
 */
public final class SuspendThreadSynchronizer extends ReplySynchronizer<SuspendThreadReply> {
  /**
   * Creates a new Resume synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public SuspendThreadSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleError(final SuspendThreadReply reply) {
    try {
      final TargetProcessThread thread = getDebugger().getProcessManager().getThread(reply.getThreadId());

      // TODO: In case we can not suspend the thread, we assume that it is really running.
      // This is not correct, however. What really needs to happen is that the debug client
      // sends the real state of the thread in the error message.

      thread.setState(ThreadState.RUNNING);
    } catch (final MaybeNullException exception) {
      // Note: This is not necessary an error situation. Imagine the following
      //
      // 1. Send SuspendThread to the debug client
      // 2. While the command is sent, the thread is closed
      // 3. Debug client can not suspend the thread
      // 4. We land here

      NaviLogger.severe("Error: Tried to suspend unknown thread '%d'", reply.getThreadId());
    }
  }

  @Override
  protected void handleSuccess(final SuspendThreadReply reply) {
    try {
      final TargetProcessThread thread = getDebugger().getProcessManager().getThread(reply.getThreadId());

      thread.setState(ThreadState.SUSPENDED);
    } catch (final MaybeNullException exception) {
      // Unlike in the error case, this is really a bug.

      NaviLogger.severe("Error: Tried to suspend unknown thread '%d'", reply.getThreadId());
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final SuspendThreadReply reply) {
    listener.receivedReply(reply);
  }
}
