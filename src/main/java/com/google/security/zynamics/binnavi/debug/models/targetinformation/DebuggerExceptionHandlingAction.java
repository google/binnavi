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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

/**
 * This enum describes the possible actions that can be taken by the debugger to handle an
 * exception.
 */
public enum DebuggerExceptionHandlingAction {
  /**
   * Continue tells the debugger to pass the exception to the process and let it handle the
   * exception.
   */
  Continue(0),
  /**
   * Halt tells the debugger to stop the process and defer exception processing.
   */
  Halt(1),
  /**
   * Ignore tells the debugger to swallow the exception and continue the process.
   */
  Ignore(2);

  private int debugExceptionAction;

  DebuggerExceptionHandlingAction(final int actionId) {
    debugExceptionAction = actionId;
  }

  /**
   * Returns an enum value from the given integer.
   *
   * @param i The integer value to be converted to the enum.
   * @return The enum value.
   */
  public static DebuggerExceptionHandlingAction convertToHandlingAction(final int i) {
    return values()[i];
  }

  /**
   * Returns a unique integer identifier of the corresponding action
   *
   * @return The integer identifier of the action
   */
  public int getValue() {
    return debugExceptionAction;
  }
}
