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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointStorage;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DefaultBreakpointStorage implements BreakpointStorage {
  /**
   * Hash map that provides quick tests whether an offset has a breakpoint or not.
   */
  private final Map<BreakpointAddress, Breakpoint> breakpointAddressToBreakpoint =
      new HashMap<>();

  private final Map<BreakpointAddress, BreakpointStatus> breakpointAddressToStatus =
      new HashMap<>();

  /**
   * Multimap that allows for quick lookups of all breakpoints in a given module.
   */
  private final SetMultimap<INaviModule, Breakpoint> moduleToBreakpoint = HashMultimap.create();

  @Override
  public void add(final Breakpoint breakpoint, final BreakpointStatus status) {
    moduleToBreakpoint.put(breakpoint.getAddress().getModule(), breakpoint);
    breakpointAddressToBreakpoint.put(breakpoint.getAddress(), breakpoint);
    breakpointAddressToStatus.put(breakpoint.getAddress(), status);
  }

  @Override
  public void clear() {
    moduleToBreakpoint.clear();
    breakpointAddressToBreakpoint.clear();
    breakpointAddressToStatus.clear();
  }

  @Override
  public Breakpoint get(final BreakpointAddress address) {
    return breakpointAddressToBreakpoint.get(address);
  }

  @Override
  public Set<BreakpointAddress> getBreakPointAddresses() {
    return breakpointAddressToBreakpoint.keySet();
  }

  @Override
  public Iterable<Breakpoint> getBreakpoints() {
    return moduleToBreakpoint.isEmpty() ? Collections.<Breakpoint>emptySet()
        : Collections.unmodifiableCollection(moduleToBreakpoint.values());
  }

  @Override
  public Set<Breakpoint> getBreakPointsByAddress(final Set<BreakpointAddress> addresses) {
    final Set<Breakpoint> breakpoints = new HashSet<Breakpoint>();

    for (final BreakpointAddress breakpointAddress : addresses) {
      final Breakpoint breakpoint = breakpointAddressToBreakpoint.get(breakpointAddress);
      if (breakpoint != null) {
        breakpoints.add(breakpointAddressToBreakpoint.get(breakpointAddress));
      }
    }

    return breakpoints;
  }

  @Override
  public BreakpointStatus getBreakpointStatus(final BreakpointAddress address) {
    return breakpointAddressToStatus.get(address);
  }

  @Override
  public Set<Breakpoint> getByModule(final MemoryModule module) {
    for (final INaviModule naviModule : moduleToBreakpoint.keySet()) {
      if (naviModule.getConfiguration().getName().equalsIgnoreCase(module.getName())) {
        return moduleToBreakpoint.get(naviModule);
      }
    }
    return new HashSet<Breakpoint>();
  }

  @Override
  public void remove(final Breakpoint breakpoint) {
    // TODO: check if this can be optimized.
    moduleToBreakpoint.get(breakpoint.getAddress().getModule()).remove(breakpoint);
    breakpointAddressToBreakpoint.remove(breakpoint.getAddress());
    breakpointAddressToStatus.remove(breakpoint.getAddress());
  }

  @Override
  public void removeBreakpoints(final Set<BreakpointAddress> addresses) {
    for (final BreakpointAddress breakpointAddres : addresses) {
      final Breakpoint breakpoint = breakpointAddressToBreakpoint.remove(breakpointAddres);
      moduleToBreakpoint.get(breakpointAddres.getModule()).remove(breakpoint);
      breakpointAddressToStatus.remove(breakpointAddres);
    }
  }

  @Override
  public void setBreakpointStatus(final BreakpointAddress address, final BreakpointStatus status) {
    if (breakpointAddressToBreakpoint.get(address) != null) {
      breakpointAddressToStatus.put(address, status);
    }
  }

  @Override
  public int size() {
    return moduleToBreakpoint.values().size();
  }

  @Override
  public int sizeByModuleFilter(final IFilledList<MemoryModule> memoryModules) {
    int size = 0;

    for (final MemoryModule memoryModule : memoryModules) {
      for (final INaviModule naviModule : moduleToBreakpoint.keySet()) {
        if (naviModule.getConfiguration().getName().equalsIgnoreCase(memoryModule.getName())) {
          size += moduleToBreakpoint.get(naviModule).size();
        }
      }
    }
    return size;
  }
}
