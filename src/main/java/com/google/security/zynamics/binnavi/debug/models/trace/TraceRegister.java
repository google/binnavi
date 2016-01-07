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
package com.google.security.zynamics.binnavi.debug.models.trace;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Describes the value of a register at a trace event.
 */
public final class TraceRegister {
  /**
   * The name of the register.
   */
  private final String registerName;

  /**
   * The value of the register.
   */
  private final IAddress registerValue;

  /**
   * The memory at the location the register points to.
   */
  private final byte[] memoryPointedToByRegister;

  /**
   * Creates a new trace register object.
   *
   * @param name The name of the register.
   * @param value The value of the register.
   * @param memory The memory at the location the register points to.
   */
  public TraceRegister(final String name, final IAddress value, final byte[] memory) {
    registerName = Preconditions.checkNotNull(name, "IE00730: name argument can not be null");
    registerValue = Preconditions.checkNotNull(value, "IE00731: value argument can not be null");
    memoryPointedToByRegister = Preconditions.checkNotNull(memory.clone(),
        "Error: memory.clone() argument can not be null");
  }

  /**
   * Returns the memory at the address the register is pointing to.
   *
   * @return The memory at the address the register is pointing to.
   */
  public byte[] getMemory() {
    return memoryPointedToByRegister.clone();
  }

  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  public String getName() {
    return registerName;
  }

  /**
   * Returns the value of the register.
   *
   * @return The value of the register.
   */
  public IAddress getValue() {
    return registerValue;
  }
}
