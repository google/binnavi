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

// ! Used to listen on breakpoint managers.
/**
 * Interface that must be implemented by classes that want to be notified about changes in
 * breakpoint managers.
 */
public interface IBreakpointManagerListener {
  // ! Signals new breakpoints.
  /**
   * Invoked after a new breakpoint was added to a breakpoint manager.
   *
   * @param breakpointManager The manager where the breakpoint was set.
   * @param breakpoint The new breakpoint.
   */
  void addedBreakpoint(BreakpointManager breakpointManager, Breakpoint breakpoint);

  // ! Signals new echo breakpoints.
  /**
   * Invoked after a new echo breakpoint was added to a breakpoint manager.
   *
   * @param breakpointManager The manager where the echo breakpoint was set.
   * @param breakpoint The new echo breakpoint.
   */
  void addedEchoBreakpoint(BreakpointManager breakpointManager, Breakpoint breakpoint);

  // ! Signals the removal of breakpoints.
  /**
   * Invoked after a breakpoint was removed from the breakpoint manager.
   *
   * @param breakpointManager The breakpoint manager from where the breakpoint was removed.
   * @param breakpoint The removed breakpoint.
   */
  void removedBreakpoint(BreakpointManager breakpointManager, Breakpoint breakpoint);

  // ! Signals the removal of echo breakpoints.
  /**
   * Invoked after an echo breakpoint was removed from the breakpoint manager.
   *
   * @param breakpointManager The breakpoint manager from where the echo breakpoint was removed.
   * @param breakpoint The removed echo breakpoint.
   */
  void removedEchoBreakpoint(BreakpointManager breakpointManager, Breakpoint breakpoint);
}
