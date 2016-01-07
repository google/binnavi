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
 * Enumeration that is used to describe the type of breakpoints.
 */
public enum BreakpointType {
  /**
   * Regular breakpoints are the breakpoints the user can set manually.
   *
   *  Regular breakpoints have the highest breakpoint priority level. This means when a regular
   * breakpoint is set, breakpoints of lower priority level are automatically removed from the
   * address where the regular breakpoint is set. Furthermore existing regular breakpoints can not
   * be overwritten by any other breakpoints.
   */
  REGULAR,

  /**
   * Echo breakpoints are the breakpoints used in trace mode to automatically create event lists of
   * hit breakpoints. If an echo breakpoint is hit, it is automatically removed from the target
   * process and the target process is resumed automatically.
   *
   *  Echo breakpoints have the lowest breakpoint priority level. This means that echo breakpoints
   * can be overwritten by any other kind of breakpoint and echo breakpoints themselves can not
   * overwrite existing breakpoints of any other type.
   */
  ECHO,

  /**
   * Step breakpoints are used to simulate the step operations "Step Over" and "Step to Next Block".
   * Once a step breakpoint is hit, all active step breakpoints are automatically removed from the
   * target process.
   *
   *  Step breakpoints have an intermediate breakpoint priority level. They can overwrite echo
   * breakpoints but not regular breakpoints and they can be overwritten by regular breakpoints but
   * not echo breakpoints.
   */
  STEP
}
