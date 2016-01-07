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
package com.google.security.zynamics.reil.interpreter;

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;

/**
 * Used by the interpreter to keep track of the values of the registers during interpretation of
 * REIL code.
 */
public class ReilRegister {
  /**
   * Name of the register
   */
  private final String register;

  /**
   * Size of the register
   */
  private final OperandSize size;

  /**
   * Value of the register
   */
  private final BigInteger value;

  /**
   * Creates a new REIL register object.
   * 
   * @param register Name of the register
   * @param size Size of the register
   * @param value Value of the register
   */
  public ReilRegister(final String register, final OperandSize size, final BigInteger value) {
    this.register = Preconditions.checkNotNull(register, "Error: Argument register can't be null");
    this.size = Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    this.value = value;
  }

  /**
   * Returns the name of the register.
   * 
   * @return The name of the register
   */
  public String getRegister() {
    return register;
  }

  /**
   * Returns the size of the register.
   * 
   * @return The size of the register
   */
  public OperandSize getSize() {
    return size;
  }

  /**
   * Returns the value of the register.
   * 
   * @return The value of the register
   */
  public BigInteger getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s: %s", register, value);
  }
}
