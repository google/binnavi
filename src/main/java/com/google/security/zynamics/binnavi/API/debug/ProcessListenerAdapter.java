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

// / Adapter class for target processes
/**
 * Adapter class that can be used by objects that want to listen on processes but only need to
 * process few events.
 */
public class ProcessListenerAdapter implements IProcessListener {
  @Override
  public void addedModule(final Process process, final MemoryModule module) {
    // Adapter method
  }

  @Override
  public void addedThread(final Process process, final Thread thread) {
    // Adapter method
  }

  @Override
  public void attached(final Process process) {
    // Adapter method
  }

  @Override
  public void changedMemoryMap(final Process process, final MemoryMap memoryMap) {
    // Adapter method
  }

  @Override
  public void changedTargetInformation(final Process process) {
    // Adapter method
  }

  @Override
  public void detached(final Process process) {
    // Adapter method
  }

  @Override
  public void removedModule(final Process process, final MemoryModule module) {
    // Adapter method
  }

  @Override
  public void removedThread(final Process process, final Thread thread) {
    // Adapter method
  }
}
