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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.enums;

/**
 * Enumeration of possible breakpoint states
 */
public enum BreakpointStatus {
  /**
   * Active breakpoints are those that are set in the breakpoint manager and in the target process.
   */
  BREAKPOINT_ACTIVE,

  /**
   * Inactive breakpoints are those that are set in the breakpoint manager but not in the target
   * process.
   */
  BREAKPOINT_INACTIVE,

  /**
   * Disabled breakpoints are those that the user intentionally disabled in the breakpoint manager.
   * Those breakpoints are also disabled in the target process if a target process is active.
   */
  BREAKPOINT_DISABLED,

  /**
   * Hit breakpoints are those that are hit in the target process.
   */
  BREAKPOINT_HIT,

  /**
   * Enabled breakpoints are those that were disabled in the breakpoint manager until the user
   * intentionally enabled them again. An enabled breakpoint quickly changes its status depending on
   * whether a process is active or not and - if a process is active - depending on whether the
   * breakpoint could be set in the target process or not.
   */
  BREAKPOINT_ENABLED,

  /**
   * Invalid breakpoints are those that could not be set in the target process.
   */
  BREAKPOINT_INVALID,

  /**
   * Deleting breakpoints are those that are deleted in BinNavi but not yet in the debug client.
   * Deleting breakpoints quickly change their state depending on the result of the breakpoint
   * deletion in the target process.
   */
  BREAKPOINT_DELETING,
}
