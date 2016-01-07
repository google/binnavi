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

import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Read Memory replies with the debugger.
 */
public final class ReadMemorySynchronizer extends ReplySynchronizer<ReadMemoryReply> {
  /**
   * Creates a new Read Memory synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ReadMemorySynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleError(final ReadMemoryReply reply) {
    // if we encounter an error while reading memory, an invalid memory range must have been used
    // to solve this error we simply refresh the memory map of the target process
    try {
      getDebugger().getMemoryMap();
    } catch (final DebugExceptionWrapper exception) {
      NaviLogger.severe("Error: Debugger could not ger memory map. Exception %s", exception);
    }
  }

  @Override
  protected void handleSuccess(final ReadMemoryReply reply) {
    getDebugger().getProcessManager().getMemory()
        .store(reply.getAddress().toLong(), reply.getData());
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final ReadMemoryReply reply) {
    listener.receivedReply(reply);
  }
}
