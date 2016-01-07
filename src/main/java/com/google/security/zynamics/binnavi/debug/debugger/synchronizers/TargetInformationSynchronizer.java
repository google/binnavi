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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Target Information messages from the debug client with the simulated target process.
 */
public final class TargetInformationSynchronizer extends ReplySynchronizer<TargetInformationReply> {
  /**
   * Creates a new Target Information synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public TargetInformationSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final TargetInformationReply reply) {
    final IDebugger debugger = getDebugger();
    final ProcessManager processManager = getDebugger().getProcessManager();
    final TargetInformation info = reply.getTargetInformation();
    processManager.setTargetInformation(info);
    if (info.getDebuggerOptions().canMemmap()) {
      try {
        // As soon as we are attached to the target process, we
        // try to find out the memory structure of the target
        // process.
        debugger.getMemoryMap();
      } catch (final DebugExceptionWrapper e) {
        NaviLogger.severe("Error: Debugger could not get memory map. Exception %s", e);
        issueDebugException(e);
      }
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final TargetInformationReply reply) {
    listener.receivedReply(reply);
  }
}
