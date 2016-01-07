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

/* ! \file BreakpointStatus.java \brief Contains the BreakpointStatus enumeration * */

// ! Enumeration of valid breakpoint states.
/**
 * Enumeration that contains all valid breakpoint states.
 */
public enum BreakpointStatus {
  /**
   * Active breakpoints are those that are set in the breakpoint manager and in the target process.
   */
  Active,

  /**
   * Inactive breakpoints are those that are set in the breakpoint manager but not in the target
   * process.
   */
  Inactive,

  /**
   * Disabled breakpoints are those that the user intentionally disabled in the breakpoint manager.
   * Those breakpoints are also disabled in the target process if a target process is active.
   */
  Disabled,

  /**
   * Hit breakpoints are those that are hit in the target process.
   */
  Hit,

  /**
   * Enabled breakpoints are those that were disabled in the breakpoint manager until the user
   * intentionally enabled them again. An enabled breakpoint quickly changes its status depending on
   * whether a process is active or not and - if a process is active - depending on whether the
   * breakpoint could be set in the target process or not.
   */
  Enabled,

  /**
   * Invalid breakpoints are those that could not be set in the target process.
   */
  Invalid,

  /**
   * Deleting breakpoints are those that are deleted in BinNavi but not yet in the debug client.
   * Deleting breakpoints quickly change their state depending on the result of the breakpoint
   * deletion in the target process.
   */
  Deleting;

  // / @cond INTERNAL

  /**
   * Converts an internal breakpoint status value to an API breakpoint status value.
   *
   * @param breakpointStatus The internal breakpoint status value.
   *
   * @return The corresponding API breakpoint status value.
   */
  public static BreakpointStatus convert(
      final com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus breakpointStatus) {
    switch (breakpointStatus) {
      case BREAKPOINT_ACTIVE:
        return BreakpointStatus.Active;
      case BREAKPOINT_DELETING:
        return BreakpointStatus.Deleting;
      case BREAKPOINT_DISABLED:
        return BreakpointStatus.Disabled;
      case BREAKPOINT_ENABLED:
        return BreakpointStatus.Enabled;
      case BREAKPOINT_HIT:
        return BreakpointStatus.Hit;
      case BREAKPOINT_INACTIVE:
        return BreakpointStatus.Inactive;
      case BREAKPOINT_INVALID:
        return BreakpointStatus.Invalid;
      default:
        throw new IllegalArgumentException("Error: Invalid breakpoint status");
    }
  }

  /**
   * Converts an API breakpoint status value into an internal breakpoint status value.
   *
   * @return The internal breakpoint status value.
   */
  public com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus getNative() {
    switch (this) {
      case Active:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_ACTIVE;
      case Deleting:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_DELETING;
      case Disabled:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_DISABLED;
      case Enabled:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_ENABLED;
      case Hit:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_HIT;
      case Inactive:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_INACTIVE;
      case Invalid:
        return com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_INVALID;
      default:
        throw new IllegalStateException("Error: Invalid breakpoint status");
    }
  }

  // / @endcond
}
