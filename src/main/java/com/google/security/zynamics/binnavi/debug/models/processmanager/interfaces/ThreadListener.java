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
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

/**
 * Interface that must be implemented by classes that want to be notified about changes in threads.
 */
public interface ThreadListener {
  /**
   * Called after the instruction pointer of a thread changed.
   *
   * @param thread The thread whose instruction pointer changed.
   * @param oldAddress The previous instruction pointer address. This can be null.
   */
  void instructionPointerChanged(TargetProcessThread thread, RelocatedAddress oldAddress);

  /**
   * Called after the register values of a thread changed.
   *
   * @param thread The thread whose register values changed.
   */
  void registersChanged(TargetProcessThread thread);

  /**
   * Called after the state a thread changed.
   *
   * @param thread The thread whose state changed.
   */
  void stateChanged(TargetProcessThread thread);
}
