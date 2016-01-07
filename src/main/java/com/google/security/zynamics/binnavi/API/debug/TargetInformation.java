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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;

import java.util.Collection;
import java.util.Collections;

// / Describes the target architecture.
/**
 * Gives information about the target architecture and the target process.
 */
public final class TargetInformation {
  /**
   * The wrapped internal target information object.
   */
  private final
      com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation
      targetInformation;

  // / @cond INTERNAL
  /**
   * Creates a new API target information object.
   *
   * @param targetInformation The wrapped internal target information object.
   */
  // / @endcond
  public TargetInformation(
      final com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation targetInformation) {
    Preconditions.checkNotNull(targetInformation,
        "Error: Target information argument can not be null");
    this.targetInformation = targetInformation;
  }

  // ! Checks whether the debugger supports attaching.
  /**
   * Returns whether the debugger can attach to a target process.
   *
   * @return True, if the debugger supports attaching to a target process.
   */
  public boolean canAttach() {
    return targetInformation.getDebuggerOptions().canAttach();
  }

  // ! Checks whether the debugger supports detaching.
  /**
   * Checks whether the debugger can detach from the target process.
   *
   * @return True, if the debugger can detach from the target process. False, otherwise.
   */
  public boolean canDetach() {
    return targetInformation.getDebuggerOptions().canDetach();
  }

  // ! Checks whether the debugger supports halting.
  /**
   * Checks whether the debugger can halt the target process.
   *
   * @return True, if the debugger can halt the target process. False, otherwise.
   */
  public boolean canHalt() {
    return targetInformation.getDebuggerOptions().canHalt();
  }

  // ! Checks whether the debugger supports memory mapping.
  /**
   * Checks whether the debugger can determine the memory layout of the target process.
   *
   * @return True, if the debugger can determine the memory layout of the target process. False,
   *         otherwise.
   */
  public boolean canMapMemory() {
    return targetInformation.getDebuggerOptions().canMemmap();
  }

  // ! Checks whether the debugger supports multithreading.
  /**
   * Checks whether the target architecture supports multithreading.
   *
   * @return True, if the target architecture supports multithreading. False, otherwise.
   */
  public boolean canMultithread() {
    return targetInformation.getDebuggerOptions().canMultithread();
  }

  // ! Checks whether the debugger supports software breakpoints.
  /**
   * Checks whether the target architecture supports software breakpoints.
   *
   * @return True, if the target architecture supports software breakpoints. False, otherwise.
   */
  public boolean canSoftwareBreakpoint() {
    return targetInformation.getDebuggerOptions().canSoftwareBreakpoint();
  }

  // ! Checks whether the debugger supports process termination.
  /**
   * Checks whether the debugger can terminate the target process.
   *
   * @return True, if the debugger can terminate the target process. False, otherwise.
   */
  public boolean canTerminate() {
    return targetInformation.getDebuggerOptions().canTerminate();
  }

  /**
   * Checks whether the debugger is able to handle breakpoint hit counts which are bigger than one.
   *
   * @return True, if the debugger supports breakpoint hit counts bigger than one. False otherwise.
   */
  public boolean canTracecount() {
    return targetInformation.getDebuggerOptions().canTraceCount();
  }

  // ! Checks whether the debugger supports memory validation.
  /**
   * Checks whether the debugger can validate memory of the target process.
   *
   * @return True, if the debugger can validate memory of the target process. False, otherwise.
   */
  public boolean canValidateMemory() {
    return targetInformation.getDebuggerOptions().canValidMemory();
  }

  // ! Typical address size of the target architecture.
  /**
   * Returns the typical register size of the target architecture. For a 32bit architecture the
   * return value is 32 for example.
   *
   * @return The typical register size of the target architecture.
   */
  public int getAddressSize() {
    return targetInformation.getAddressSize();
  }

  // ! Printable representation of the target information.
  /**
   * Returns a string representation of the target information object.
   *
   * @return A string representation of the target information object.
   */
  @Override
  public String toString() {
    return "Target Information Object";
  }

  // ! Gets the exceptions settings.
  /**
   * Returns the Collection of exception settings for the debugger.
   *
   * @return the Collection of exception settings for the debugger.
   */
  public Collection<DebuggerDebugException> getExceptionSettings() {
    final Collection<DebuggerDebugException> exceptions = Collections.emptyList();
    for (final DebuggerException exception :
        targetInformation.getDebuggerOptions().getExceptions()) {
      exceptions.add(new DebuggerDebugException(exception));
    }
    return exceptions;
  }
}
