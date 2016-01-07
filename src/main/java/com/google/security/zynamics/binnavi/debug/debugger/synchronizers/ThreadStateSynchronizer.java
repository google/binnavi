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
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;

/**
 * Synchronizes debug events with modeled thread states.
 */
public class ThreadStateSynchronizer {
  /**
   * Debugger used for synchronization.
   */
  private final IDebugger debugger;

  /**
   * Keeps track of changes in thread states.
   */
  private final ThreadListener m_threadListener = new ThreadListenerAdapter() {
    @Override
    public void stateChanged(final TargetProcessThread thread) {
      if (thread.getState() == ThreadState.RUNNING) {
        try {
          debugger.resumeThread(thread.getThreadId());
        } catch (final DebugExceptionWrapper exception) {
          NaviLogger.severe("Error: Debugger could not resume thread. Exception %s", exception);
        }
      } else {
        try {
          debugger.suspendThread(thread.getThreadId());
        } catch (final DebugExceptionWrapper exception) {
          NaviLogger.severe("Error: Debugger could not suspend thread. Exception %s", exception);
        }
      }
    }
  };

  /**
   * Keeps track of relevant events in the synchronized process.
   */
  private final ProcessManagerListener m_processListener = new ProcessManagerListenerAdapter() {
    @Override
    public void addedThread(final TargetProcessThread thread) {
      thread.addListener(m_threadListener);
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      thread.removeListener(m_threadListener);
    }
  };

  /**
   * Creates a new synchronizer object.
   *
   * @param debugger Debugger used for synchronization.
   */
  public ThreadStateSynchronizer(final IDebugger debugger) {
    this.debugger = debugger;
    for (final TargetProcessThread thread : this.debugger.getProcessManager().getThreads()) {
      thread.addListener(m_threadListener);
    }
    this.debugger.getProcessManager().addListener(m_processListener);
  }
}
