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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * A breakpoint address is a combination of a module and an unrelocated address. Breakpoint
 * addresses can therefore be automatically converted between unrelocated addresses and relocated
 * addresses.
 */
public class BreakpointAddress {
  /**
   * Module component of the breakpoint address. This value can be null.
   */
  private final INaviModule module;

  /**
   * Unrelocated address of the breakpoint.
   */
  private final UnrelocatedAddress unrelocatedBreakpointAddress;

  /**
   * Creates a new breakpoint object.
   *
   * @param module Module component of the breakpoint address. This value can be null.
   * @param address Unrelocated address of the breakpoint.
   */
  public BreakpointAddress(final INaviModule module, final UnrelocatedAddress address) {
    unrelocatedBreakpointAddress =
        Preconditions.checkNotNull(address, "IE00234: Address argument can not be null");
    this.module = Preconditions.checkNotNull(module, "IE01693: Module arguemnt can not be null");
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof BreakpointAddress)
        && (((BreakpointAddress) rhs).module == module)
        && ((BreakpointAddress) rhs).unrelocatedBreakpointAddress.equals(
            unrelocatedBreakpointAddress);
  }

  /**
   * Returns the address component.
   *
   * @return The address component.
   */
  public UnrelocatedAddress getAddress() {
    return unrelocatedBreakpointAddress;
  }

  /**
   * Returns the module component. This method can return null.
   *
   * @return The module component.
   */
  public INaviModule getModule() {
    return module;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(unrelocatedBreakpointAddress.hashCode(), module.hashCode());
  }

  @Override
  public String toString() {
    return module + "!" + unrelocatedBreakpointAddress;
  }
}
