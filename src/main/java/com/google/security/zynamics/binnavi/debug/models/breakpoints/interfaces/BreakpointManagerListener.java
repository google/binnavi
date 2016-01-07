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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for classes that want to be notified about changes in the breakpoint manager.
 */
public interface BreakpointManagerListener {
  /**
   * This function is called when new breakpoints are added to the breakpoints managed by the
   * breakpoint manager.
   *
   * @param breakpoints The added breakpoints.
   */
  void breakpointsAdded(List<Breakpoint> breakpoints);

  /**
   * This function is called when existing breakpoints have changed their conditions.
   *
   * @param breakpoints The breakpoints whose conditions have been changed.
   */
  void breakpointsConditionChanged(Set<Breakpoint> breakpoints);

  /**
   * This function is called when the description of existing breakpoints has been changed.
   *
   * @param breakpoints The breakpoints whose descriptions have been changed.
   */
  void breakpointsDescriptionChanged(Set<Breakpoint> breakpoints);

  /**
   * This function is called when existing breakpoints were removed from the collection of
   * breakpoints managed by the breakpoint manager.
   *
   * @param breakpoints The breakpoints that were removed from the breakpoint manager.
   */
  void breakpointsRemoved(Set<Breakpoint> breakpoints);

  /**
   * This function is called when existing breakpoints change their status.
   *
   * @param breakpoints The breakpoints where the status has changed.
   */
  void breakpointsStatusChanged(Map<Breakpoint, BreakpointStatus> breakpoints,
      BreakpointStatus newStatus);
}
