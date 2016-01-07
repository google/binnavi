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


// ! Recorded register value.
/**
 * Represents a single recorded register value that was recorded for a debug trace event.
 */
public class TraceRegister {
  /**
   * The wrapped trace register.
   */
  private final com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister register;

  // / @cond INTERNAL
  /**
   * Creates a new API trace register object.
   *
   * @param register The wrapped trace register.
   */
  // / @endcond
  public TraceRegister(
      final com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister register) {
    this.register = register;
  }

  // ! Memory content at the register address.
  /**
   * Returns the memory content at the memory address pointed to by the register.
   *
   * @return The memory content.
   */
  public byte[] getMemory() {
    return register.getMemory();
  }

  // ! Name of the register.
  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  public String getName() {
    return register.getName();
  }

  // ! Register value.
  /**
   * Returns the value of the register.
   *
   * @return The value of the register.
   */
  public Address getValue() {
    return new Address(register.getValue().toBigInteger());
  }
}
