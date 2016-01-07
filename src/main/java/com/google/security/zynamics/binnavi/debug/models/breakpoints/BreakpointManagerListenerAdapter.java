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
package com.google.security.zynamics.binnavi.debug.models.breakpoints;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BreakpointManagerListenerAdapter implements BreakpointManagerListener {
  @Override
  public void breakpointsAdded(final List<Breakpoint> breakpoints) {}

  @Override
  public void breakpointsConditionChanged(final Set<Breakpoint> breakpoints) {}

  @Override
  public void breakpointsDescriptionChanged(final Set<Breakpoint> breakpoints) {}

  @Override
  public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {}

  @Override
  public void breakpointsStatusChanged(final Map<Breakpoint, BreakpointStatus> breakpointsToStatus,
      final BreakpointStatus newStatus) {}
}
