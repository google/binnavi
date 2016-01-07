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

// / Used to listen on threads of a target process.
/**
 * This interface must be implemented by all classes that want to be notified about changes in a
 * thread of the target process.
 */
public interface IThreadListener {
  // ! Signals a change in the PC register.
  /**
   * Invoked after the program counter of the thread changed.
   *
   * @param thread The thread whose program counter changed.
   */
  void changedProgramCounter(Thread thread);

  // ! Signals changing register values.
  /**
   * Invoked after the register values of the thread changed.
   *
   * @param thread The thread whose register values changed.
   */
  void changedRegisters(Thread thread);

  // ! Signals changing thread states.
  /**
   * Invoked after the state of the thread changed.
   *
   * @param thread The thread whose state changed.
   */
  void changedState(Thread thread);
}
