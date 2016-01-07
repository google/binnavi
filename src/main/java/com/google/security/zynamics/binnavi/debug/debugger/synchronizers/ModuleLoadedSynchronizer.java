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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CBreakpointModuleSynchronizer;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CRelocationNotifier;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleLoadedReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.storage.DebuggerEventSettingsStorage;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes Module Loaded events from the debug client with the simulated target process.
 */
public final class ModuleLoadedSynchronizer extends ReplySynchronizer<ModuleLoadedReply> {
  /**
   * Creates a new Module Loaded synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ModuleLoadedSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  /**
   * Sets the currently active thread in the process manager.
   *
   * @param threadId The thread id to be activated.
   */
  private static void setActiveThreadById(final ProcessManager processManager,
      final long threadId) {
    try {
      final TargetProcessThread thread = processManager.getThread(threadId);
      processManager.setActiveThread(thread);
    } catch (final MaybeNullException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  private boolean needsResume() {
    DebuggerEventSettings settings = null;
    try {
      settings = new DebuggerEventSettingsStorage(getDebugger(),
          getDebugger().getDebugTargetSettings()).deserialize();
    } catch (final CouldntLoadDataException e) {
    }

    return (settings == null) || !settings.getBreakOnDllLoad();
  }

  @Override
  protected void handleSuccess(final ModuleLoadedReply reply) {
    getDebugger().getProcessManager().addModule(reply.getModule());
    CRelocationNotifier.relocateModule(getDebugger(), reply.getModule());

    CBreakpointModuleSynchronizer.enableRegularBreakpoints(getDebugger(), reply.getModule());
    CBreakpointModuleSynchronizer.enableEchoBreakpoints(getDebugger(), reply.getModule());

    final TargetInformation targetInformation =
        getDebugger().getProcessManager().getTargetInformation();
    if ((targetInformation != null)
        && targetInformation.getDebuggerOptions().canBreakOnModuleLoad()) {
      if (needsResume()) {
        try {
          getDebugger().resume();
        } catch (final DebugExceptionWrapper e) {
          NaviLogger.severe("Error: Could not resume debugger. Exception: %s", e);
        }
      } else {
        refreshRegisters();
        setActiveThreadById(getDebugger().getProcessManager(), reply.getThread().getThreadId());
      }
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener, final ModuleLoadedReply reply) {
    listener.receivedReply(reply);
  }
}
