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

import java.math.BigInteger;

import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;


// / Single register value of a thread.
/**
 * Represents a single register of a thread of the target process.
 */
public final class Register {
  /**
   * The wrapped internal register object.
   */
  private final RegisterValue m_register;

  // / @cond INTERNAL
  /**
   * Creates a new API register object.
   *
   * @param register The wrapped internal register object.
   */
  // / @endcond
  public Register(final RegisterValue register) {
    m_register = register;
  }

  // ! Name of the register.
  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  public String getName() {
    return m_register.getName();
  }

  // ! Value of the register.
  /**
   * Returns the value of the register.
   *
   * @return The value of the register.
   */
  public BigInteger getValue() {
    return m_register.getValue();
  }

  // ! Checks if the register is the PC register.
  /**
   * Tells whether the register is the program counter / instruction pointer register of the target
   * thread.
   *
   * @return True, if the register is the program counter. False, otherwise.
   */
  public boolean isProgramCounter() {
    return m_register.isPc();
  }

  // ! Checks if the register is the stack pointer register.
  /**
   * Tells whether the register is the stack pointer register of the target thread.
   *
   * @return True, if the register is the stack pointer. False, otherwise.
   */
  public boolean isStackPointer() {
    return m_register.isSp();
  }

  // ! Printable representation of the register.
  /**
   * Returns a string representation of the register.
   *
   * @return A string representation of the register.
   */
  @Override
  public String toString() {
    return String.format("%s - %08X", getName(), getValue());
  }
}
