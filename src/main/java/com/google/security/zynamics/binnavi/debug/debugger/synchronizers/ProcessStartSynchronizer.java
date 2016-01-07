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

import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CBreakpointModuleSynchronizer;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CRelocationNotifier;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.zylib.general.ListenerProvider;

public class ProcessStartSynchronizer extends ReplySynchronizer<ProcessStartReply> {
  /**
   * Creates a new Process Start synchronizer.
   *
   * @param debugger The currently active debugger.
   * @param listeners Listeners that are notified about events.
   */
  public ProcessStartSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  @Override
  protected void handleSuccess(final ProcessStartReply reply) {
    final TargetProcessThread thread = reply.getProcessStart().getThread();
    final MemoryModule module = reply.getProcessStart().getModule();

    getDebugger().getProcessManager().addThread(thread);
    getDebugger().getProcessManager().setActiveThread(thread);

    refreshRegisters();

    CRelocationNotifier.relocateModule(getDebugger(), module);
    getDebugger().getProcessManager().addModule(module);
    CBreakpointModuleSynchronizer.enableRegularBreakpoints(getDebugger(), module);

    try {
      getDebugger().resume();
    } catch (final DebugExceptionWrapper e) {
      NaviLogger.severe("Error: Could not resume debugger. Exception: %s", e);
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final ProcessStartReply reply) {
    listener.receivedReply(reply);
  }
}
