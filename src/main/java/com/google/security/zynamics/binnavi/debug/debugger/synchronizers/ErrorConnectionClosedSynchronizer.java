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

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Error Connection Closed events sent by the debug client with the simulated target
 * process.
 */
public final class ErrorConnectionClosedSynchronizer extends
    ReplySynchronizer<DebuggerClosedUnexpectedlyReply> {
  /**
   * Creates a new Error Connection Closed synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ErrorConnectionClosedSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final DebuggerClosedUnexpectedlyReply reply) {
    resetTargetProcess();
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final DebuggerClosedUnexpectedlyReply reply) {
    listener.receivedReply(reply);
    listener.debuggerClosed(reply.getErrorCode());
  }
}
