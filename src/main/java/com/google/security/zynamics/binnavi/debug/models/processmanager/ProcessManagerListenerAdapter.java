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

import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;

/**
 * Adapter class for classes that want to listen on just a few process events.
 */
public class ProcessManagerListenerAdapter implements ProcessManagerListener {
  @Override
  public void addedModule(final MemoryModule module) {}

  @Override
  public void addedThread(final TargetProcessThread thread) {}

  @Override
  public void attached() {}

  @Override
  public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {}

  @Override
  public void changedMemoryMap() {}

  @Override
  public void changedTargetInformation(final TargetInformation information) {}

  @Override
  public void detached() {}

  @Override
  public void raisedException(final DebuggerException exception) {}

  @Override
  public void removedModule(final MemoryModule module) {}

  @Override
  public void removedNonExistingModule(final MemoryModule module) {}

  @Override
  public void removedThread(final TargetProcessThread thread) {}
}
