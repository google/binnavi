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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * Used to describe value and special properties of a single CPU register.
 */
public final class RegisterValue {
  /**
   * The name of the register.
   */
  private final String name;

  /**
   * The value of the register.
   */
  private final BigInteger value;

  /**
   * Memory at the location the register points to.
   */
  private final byte[] memoryPointedTo;

  /**
   * Flag that determines whether the register is the program counter.
   */
  private final boolean isProgramCounter;

  /**
   * Flag that determines whether register is the stack pointer.
   */
  private final boolean isStackPointer;

  /**
   * Creates a new register value object.
   *
   * @param name The name of the register.
   * @param value The value of the register.
   * @param memory Memory at the location the register points to.
   * @param isPc Flag that determines whether the register is the program counter.
   * @param isSp Flag that determines whether register is the stack pointer.
   */
  public RegisterValue(final String name, final BigInteger value, final byte[] memory,
      final boolean isPc, final boolean isSp) {
    this.name = Preconditions.checkNotNull(name, "IE01047: Name argument can not be null");
    this.value = Preconditions.checkNotNull(value, "IE01033: Value argument can not be null");
    Preconditions.checkArgument(!(isPc && isSp),
        "IE01034: A register can not be both program counter register and stack register");
    memoryPointedTo = memory.clone();
    isProgramCounter = isPc;
    isStackPointer = isSp;
  }

  /**
   * Returns the pointed-to memory.
   *
   * @return The pointed-to memory.
   */
  public byte[] getMemory() {
    return memoryPointedTo.clone();
  }

  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the value of the register.
   *
   * @return The value of the register.
   */
  public BigInteger getValue() {
    return value;
  }

  /**
   * Returns whether the register is the program counter register or not.
   *
   * @return True, if the value is the program counter register. False, otherwise.
   */
  public boolean isPc() {
    return isProgramCounter;
  }

  /**
   * Returns whether the register is the stack pointer register or not.
   *
   * @return True, if the value is the stack pointer register. False, otherwise.
   */
  public boolean isSp() {
    return isStackPointer;
  }
}
