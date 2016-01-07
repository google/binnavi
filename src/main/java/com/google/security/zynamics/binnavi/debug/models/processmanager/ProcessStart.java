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
package com.google.security.zynamics.binnavi.debug.models.processmanager;

/**
 * Class used to describe the initial thread and the process module of the new target process before
 * the process is running.
 */
public class ProcessStart {
  /**
   * The initial thread.
   */
  private final TargetProcessThread thread;

  /**
   * The initial target process module.
   */
  private final MemoryModule module;

  public ProcessStart(final TargetProcessThread thread, final MemoryModule module) {
    this.thread = thread;
    this.module = module;
  }

  /**
   * Returns the module object representing the process image of the target process.
   *
   * @return The corresponding process object.
   */
  public MemoryModule getModule() {
    return module;
  }

  /**
   * Returns the thread object representing the initial thread of the target process.
   *
   * @return The corresponding thread object.
   */
  public TargetProcessThread getThread() {
    return thread;
  }
}
