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

import com.google.security.zynamics.binnavi.API.disassembly.Address;

// ! Represents a single breakpoint.
/**
 * Represents a single breakpoint that is set in the target process.
 */
public final class Breakpoint {

  /**
   * Wrapped internal breakpoint object.
   */
  private final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint
      m_breakpoint;

  // / @cond INTERNAL
  /**
   * Creates a new API breakpoint object.
   *
   * @param breakpoint Wrapped internal breakpoint object.
   */
  // / @endcond
  public Breakpoint(
      final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint breakpoint) {
    m_breakpoint = breakpoint;
  }

  // ! Disabled the breakpoint.
  /**
   * Disables the breakpoint if it is enabled.
   */
  public void disable() {
    // m_breakpoint.setStatus(com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_DISABLED);
  }

  // ! Enables the breakpoint.
  /**
   * Enables the breakpoint if it is disabled.
   */
  public void enable() {
    // m_breakpoint.setStatus(com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_ENABLED);
  }

  // ! The address of the breakpoint.
  /**
   * Returns the address of the breakpoint.
   *
   * @return The address of the breakpoint.
   */
  public Address getAddress() {
    return new Address(m_breakpoint.getAddress().getAddress().getAddress().toBigInteger());
  }

  // ! The description of the breakpoint.
  /**
   * Returns the description of the breakpoint.
   *
   * @return The description of the breakpoint.
   */
  public String getDescription() {
    return m_breakpoint.getDescription();
  }

  // ! Changes the breakpoint description.
  /**
   * Changes the description of the breakpoint.
   *
   * @param description The description of the breakpoint.
   */
  public void setDescription(final String description) {
    m_breakpoint.setDescription(description);
  }

  @Override
  public String toString() {
    return String.format("Breakpoint %s", getAddress());
  }
}
