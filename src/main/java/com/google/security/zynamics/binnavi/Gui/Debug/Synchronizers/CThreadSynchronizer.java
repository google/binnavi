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
package com.google.security.zynamics.binnavi.Gui.Debug.Synchronizers;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;

/**
 * Synchronizes the GUI with changes in the number of threads.
 */
public final class CThreadSynchronizer {
  /**
   * Process manager that is synchronized with the GUI.
   */
  private final ProcessManager m_processManager;

  /**
   * Thread event synchronizer for new threads.
   */
  private final ThreadListener m_threadEventListener;

  /**
   * Keeps track of the thread manager and updates the GUI on relevant events.
   */
  private final InternalProcessListener m_internalThreadListener = new InternalProcessListener();

  /**
   * Creates a new thread synchronizer object.
   *
   * @param processManager Process manager that is synchronized with the GUI.
   * @param threadEventListener Thread event synchronizer for new threads.
   */
  public CThreadSynchronizer(
      final ProcessManager processManager, final ThreadListener threadEventListener) {
    Preconditions.checkNotNull(processManager, "IE01516: Process manager argument can not be null");

    Preconditions.checkNotNull(
        threadEventListener, "IE01517: Thread event listener argument can not be null");

    m_processManager = processManager;
    m_threadEventListener = threadEventListener;

    processManager.addListener(m_internalThreadListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_processManager.removeListener(m_internalThreadListener);
  }

  /**
   * Keeps track of the thread manager and updates the GUI on relevant events.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void addedThread(final TargetProcessThread thread) {
      thread.addListener(m_threadEventListener);
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      thread.removeListener(m_threadEventListener);
    }
  }
}
