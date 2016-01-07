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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.IndexedBreakpointStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * The GUI needs to be able to retrieve regular breakpoints via an index (for the breakpoints table)
 * so we store them in a list as well.
 */
public class RegularBreakpointStorage extends DefaultBreakpointStorage implements
    IndexedBreakpointStorage {

  private final List<Breakpoint> breakpoints = new ArrayList<>();

  @Override
  public void add(final Breakpoint breakpoint, final BreakpointStatus status) {
    super.add(breakpoint, status);
    breakpoints.add(breakpoint);
  }

  @Override
  public void clear() {
    super.clear();
    breakpoints.clear();
  }

  @Override
  public Breakpoint get(final int index) {
    Preconditions.checkArgument((index >= 0) && (index < breakpoints.size()),
        "IE01302: Index out of bounds");
    return breakpoints.get(index);
  }

  @Override
  public void remove(final Breakpoint breakpoint) {
    super.remove(breakpoint);
    breakpoints.remove(breakpoint);
  }
}
