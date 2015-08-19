/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import com.google.security.zynamics.binnavi.API.debug.IProcessListener;
import com.google.security.zynamics.binnavi.API.debug.MemoryMap;
import com.google.security.zynamics.binnavi.API.debug.MemoryModule;
import com.google.security.zynamics.binnavi.API.debug.Process;
import com.google.security.zynamics.binnavi.API.debug.Thread;

public final class MockProcessListener implements IProcessListener {
  public String events = "";


  @Override
  public void addedModule(final Process process, final MemoryModule module) {
    events += String.format("addedModule/%s;", module.getName());
  }

  @Override
  public void addedThread(final Process process, final Thread thread) {
    events += String.format("addedThread/%d;", thread.getThreadId());
  }

  @Override
  public void attached(final Process process) {
    events += "attached;";
  }

  @Override
  public void changedMemoryMap(final Process process, final MemoryMap memoryMap) {
    events += "changedMemoryMap;";
  }

  @Override
  public void changedTargetInformation(final Process process) {
    events += "changedTargetInformation;";
  }

  @Override
  public void detached(final Process process) {
    events += "detached;";
  }

  @Override
  public void removedModule(final Process process, final MemoryModule module) {
    events += String.format("removedModule/%s;", module.getName());
  }

  @Override
  public void removedThread(final Process process, final Thread thread) {
    events += String.format("removedThread/%d;", thread.getThreadId());
  }

}
