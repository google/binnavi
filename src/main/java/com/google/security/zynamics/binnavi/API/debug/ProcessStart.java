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

import com.google.common.base.Preconditions;

public class ProcessStart {

  private final com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart
      processStart;

  public ProcessStart(
      final com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart processStart) {
    this.processStart =
        Preconditions.checkNotNull(processStart, "Error: processStart argument can not be null");
  }

  public MemoryModule getModule() {
    return new MemoryModule(processStart.getModule());
  }

  public Thread getThread() {
    return new Thread(processStart.getThread());
  }
}
