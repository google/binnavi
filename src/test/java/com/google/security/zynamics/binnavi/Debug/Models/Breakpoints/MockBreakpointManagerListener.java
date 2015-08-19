/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Debug.Models.Breakpoints;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;

public class MockBreakpointManagerListener implements BreakpointManagerListener {
  private final List<String> m_events = Lists.newArrayList();

  @Override
  public void breakpointsAdded(final List<Breakpoint> breakpoints) {
    for (final Breakpoint breakpoint : breakpoints) {
      m_events.add(String.format("Add: %s", breakpoint.getAddress().getAddress().getAddress()
          .toHexString()));
    }
  }

  @Override
  public void breakpointsConditionChanged(final Set<Breakpoint> breakpoints) {
  }

  @Override
  public void breakpointsDescriptionChanged(final Set<Breakpoint> breakpoints) {
  }

  @Override
  public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
    for (final Breakpoint breakpoint : breakpoints) {
      m_events.add(String.format("Remove: %s", breakpoint.getAddress().getAddress().getAddress()
          .toHexString()));
    }
  }

  @Override
  public void breakpointsStatusChanged(
      final Map<Breakpoint, BreakpointStatus> breakpointsToStatus, final BreakpointStatus status) {
  }

  public String getEvent(final int index) {
    if ((index < m_events.size()) && (index >= 0)) {
      return m_events.get(index);
    }
    return "No such event";
  }

  public int size() {
    return m_events.size();
  }
}
