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

// / Used to listen on target process memory.
/**
 * Interface that must be implemented by classes that want to be notified about changes in the
 * available target process memory.
 */
public interface IMemoryListener {
  // ! Signals changes in the process memory.
  /**
   * Invoked after a part of the target process memory changed. This event is generally invoked when
   * BinNavi receives memory data from the debug client.
   *
   * @param memory The memory which changed.
   * @param address Start address of the changed memory section.
   * @param size Number of bytes that were changed.
   */
  void changedMemory(Memory memory, long address, int size);

  // ! Signals resets in the process memory.
  /**
   * Invoked after the target process memory was cleared. This event is generally invoked when the
   * debug client detaches from the target process and the known target process memory is reset.
   *
   * @param memory The memory which was cleared.
   */
  void clearedMemory(Memory memory);
}
