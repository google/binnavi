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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.common.base.Preconditions;

// ! Describes trace events for trace logging.
/**
 * This class is used to specify the addresses where the trace logger puts echo breakpoints.
 */
public class TracePoint {
  /**
   * Module of the trace point. This value can be null.
   */
  private final Module m_module;

  /**
   * Address of the trace point.
   */
  private final Address m_address;

  // ! Creates a new trace point object.
  /**
   * Creates a new trace point object. The module argument can be null while the address argument
   * must not be null.
   *
   *  If the module argument is null, the address is the relocated address where the breakpoint is
   * set in memory. If the module is not null, the address is the unrelocated address.
   *
   * @param module Module where the breakpoint is set. This argument can be null.
   * @param address The address of the breakpoint.
   */
  public TracePoint(final Module module, final Address address) {
    m_address = Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    m_module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
  }

  // ! Address of the trace point.
  /**
   * Returns the address of the trace point.
   *
   * @return The address of the trace point.
   */
  public Address getAddress() {
    return m_address;
  }

  // ! Module of the trace point.
  /**
   * Returns the module of the trace point. The return value of this function can be null.
   *
   * @return The module of the trace point.
   */
  public Module getModule() {
    return m_module;
  }
}
