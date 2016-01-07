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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CBreakpointModuleSynchronizer;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.storage.DebuggerEventSettingsStorage;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Simulates Module Unloaded events from the debug client with the simulated target process.
 */
public final class ModuleUnloadedSynchronizer extends ReplySynchronizer<ModuleUnloadedReply> {
  /**
   * Creates a new Module Unloaded synchronizer.
   *
   * @param debugger The debug client synchronize.
   * @param listeners Listeners that are notified about relevant events.
   */
  public ModuleUnloadedSynchronizer(final IDebugger debugger,
      final ListenerProvider<IDebugEventListener> listeners) {
    super(debugger, listeners);
  }

  private boolean needsResume() {
    DebuggerEventSettings settings = null;
    try {
      settings = new DebuggerEventSettingsStorage(getDebugger(),
          getDebugger().getDebugTargetSettings()).deserialize();
    } catch (final CouldntLoadDataException e) {
    }

    return ((settings == null) || !settings.getBreakOnDllUnload()) &&
        getDebugger()
        .getProcessManager()
        .getTargetInformation()
        .getDebuggerOptions()
        .canBreakOnModuleUnload();
  }

  @Override
  protected void handleSuccess(final ModuleUnloadedReply reply) {
    final ProcessManager processManager = getDebugger().getProcessManager();

    final MemoryModule replyModule = reply.getModule();
    final MemoryModule existingModule = processManager.getModule(replyModule.getBaseAddress());

    if (existingModule == null) {
      processManager.removeNonExistingModule(replyModule);
    } else {
      processManager.removeModule(existingModule);
      CBreakpointModuleSynchronizer.disableEchoBreakpoints(getDebugger(), existingModule);
      CBreakpointModuleSynchronizer.disableRegularBreakpoints(getDebugger(), existingModule);
    }

    if (needsResume()) {
      try {
        getDebugger().resume();
      } catch (final DebugExceptionWrapper e) {
        NaviLogger.severe("Error: Could not resume the debugger. Exception: %s", e);
      }
    }
  }

  @Override
  protected void notifyListener(final IDebugEventListener listener,
      final ModuleUnloadedReply reply) {
    listener.receivedReply(reply);
  }
}
