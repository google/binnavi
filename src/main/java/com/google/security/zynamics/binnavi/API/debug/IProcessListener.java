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
package com.google.security.zynamics.binnavi.API.debug;

// / Used to listen on a target process.
/**
 * This interface must be implemented by all classes that want to be notified about changes in the
 * target process.
 */
public interface IProcessListener {



  // ! Signals that a new module was loaded.
  /**
   * Invoked after a new module was loaded into the address space of the target process.
   * 
   * @param process The target process.
   * @param module The loaded module.
   */
  void addedModule(Process process, MemoryModule module);

  // ! Signals the creation of a new thread.
  /**
   * Invoked after the target process created a new thread.
   * 
   * @param process The target process.
   * @param thread The new thread.
   */
  void addedThread(Process process, Thread thread);

  // ! Signals successful attaching to the target process.
  /**
   * Invoked after the debugger attached to the target process.
   * 
   * @param process The target process.
   */
  void attached(Process process);

  // ! Signals new information about the target process memory.
  /**
   * Invoked after the known memory map of the target process changed. This event is generally
   * invoked after the debugger sent an updated memory map to com.google.security.zynamics.binnavi.
   * 
   * @param process The target process.
   * @param memoryMap The new memory map of the target process.
   */
  void changedMemoryMap(Process process, MemoryMap memoryMap);

  // ! Signals new information about the target process.
  /**
   * Invoked after the target information of the target process changed. This event is generally
   * only invoked once, shortly after the debugger attached to the target process.
   * 
   * @param process The target process.
   */
  void changedTargetInformation(Process process);

  // ! Signals successful detaching from the target process.
  /**
   * Invoked after the debugger detached from the target process.
   * 
   * @param process The target process.
   */
  void detached(Process process);

  // ! Signals the unloading of a loaded module.
  /**
   * Invoked after a module was unloaded from the address space of the target process.
   * 
   * @param process The target process.
   * @param module The module that was unloaded.
   */
  void removedModule(Process process, MemoryModule module);

  // ! Signals the destruction of an existing thread.
  /**
   * Invoked after a thread of the target process was closed.
   * 
   * @param process The target process.
   * @param thread The closed thread.
   */
  void removedThread(Process process, Thread thread);
}
