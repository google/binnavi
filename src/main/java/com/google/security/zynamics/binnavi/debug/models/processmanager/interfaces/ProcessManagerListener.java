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
package com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces;

import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;

/**
 * Interface that must be implemented by classes that want to be notified about changes in the
 * process manager.
 */
public interface ProcessManagerListener {
  /**
   * Invoked after a new module was added to the address space of the target process.
   *
   * @param module The new module.
   */
  void addedModule(MemoryModule module);

  /**
   * Called after a new thread was added to the thread manager.
   *
   * @param thread The thread that was added to the thread manager.
   */
  void addedThread(TargetProcessThread thread);

  /**
   * Invoked when a debugger attached to the target process and the process manager and the real
   * target process are synchronized.
   */
  void attached();

  /**
   * Invoked after the thread that received the debug commands changed.
   *
   * @param oldThread The previously active thread.
   * @param newThread The newly active thread.
   */
  void changedActiveThread(TargetProcessThread oldThread, TargetProcessThread newThread);

  /**
   * This method is called after the memory map of the process was updated.
   */
  void changedMemoryMap();

  /**
   * Invoked when information about the target process changed.
   *
   * @param information The new target process information.
   */
  void changedTargetInformation(TargetInformation information);

  /**
   * Invoked when the debugger detached from the target process. The process manager is not
   * synchronized with the target process anymore.
   */
  void detached();

  /**
   * Invoked when the debuggee raises an exception.
   *
   * @param exception The exception which was raised.
   */
  void raisedException(DebuggerException exception);

  /**
   * Invoked after a module was removed from the address space of a target process.
   *
   * @param module The module that was removed.
   */
  void removedModule(MemoryModule module);

  /**
   * Invoked after a module was removed from the address space of a target process which was not
   * already known to the process manager. This is not an error condition. The case occurs in Win7
   * X64 during process creation.
   *
   * @param module The module that was removed.
   */
  void removedNonExistingModule(MemoryModule module);

  /**
   * Called after a thread was removed from the thread manager.
   *
   * @param thread The thread that was removed from the thread manager.
   */
  void removedThread(TargetProcessThread thread);
}
