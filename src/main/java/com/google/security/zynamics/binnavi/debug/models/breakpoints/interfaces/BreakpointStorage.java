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
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.Set;

/**
 * Every breakpoint type can possibly have a different internal storage mechanism. Every such
 * storage mechanism needs to implement this interface.
 */
public interface BreakpointStorage {
  void add(Breakpoint breakpoint, BreakpointStatus status);

  /**
   * Remove all breakpoints from the storage.
   */
  void clear();

  /**
   * Retrieve a breakpoint for the given address from the storage.
   *
   * @param address The address to retrieve the breakpoint from.
   * @return The retried breakpoint or null if no breakpoints exists at the given address.
   */
  Breakpoint get(BreakpointAddress address);

  Set<BreakpointAddress> getBreakPointAddresses();

  /**
   * Create a list of all breakpoints in the storage.
   *
   * @return A list of all breakpoints.
   */
  Iterable<Breakpoint> getBreakpoints();

  /**
   * Return the set of breakpoints which belong to the given module.
   *
   * @param module The module for which the breakpoints should be determined.
   * @return The set of breakpoints belonging to the given module.
   */
  Set<Breakpoint> getByModule(final MemoryModule module);

  /**
   * Removes the given breakpoint from the storage.
   *
   * @param breakpoint The breakpoint to be removed.
   */
  void remove(Breakpoint breakpoint);

  void setBreakpointStatus(BreakpointAddress address, BreakpointStatus status);

  /**
   * Returns the number of stored breakpoints.
   *
   * @return The number of stored breakpoints.
   */
  int size();

  /**
   * Returns the number of stored breakpoints for the given modules.
   *
   * @param memoryModules list of memory modules which could contain breakpoints.
   * @return The number of stored breakpoints for the given modules
   */
  int sizeByModuleFilter(IFilledList<MemoryModule> memoryModules);

  public abstract Set<Breakpoint> getBreakPointsByAddress(Set<BreakpointAddress> addresses);

  public abstract BreakpointStatus getBreakpointStatus(final BreakpointAddress address);

  public abstract void removeBreakpoints(final Set<BreakpointAddress> addresses);

}
