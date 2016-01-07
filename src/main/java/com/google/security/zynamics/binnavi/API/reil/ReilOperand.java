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
package com.google.security.zynamics.binnavi.API.reil;


import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.reil.ReilHelpers;

// / Single REIL instruction operand
/**
 * Represents a single REIL operand.
 */
public final class ReilOperand implements ApiObject<com.google.security.zynamics.reil.ReilOperand> {
  // ! Empty Operand
  /**
   * This empty operand object can be used when creating new REIL instructions.
   */
  public static final ReilOperand EMPTY_OPERAND =
      new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, "");

  /**
   * The wrapped internal REIL operand.
   */
  private final com.google.security.zynamics.reil.ReilOperand m_operand;

  // / @cond INTERNAL
  /**
   * Creates a new API REIL operand.
   *
   * @param operand The wrapped internal REIL operand.
   */
  // / @endcond
  public ReilOperand(final com.google.security.zynamics.reil.ReilOperand operand) {
    m_operand = operand;
  }

  // ! Creates a new REIL operand.
  /**
   * Creates a new REIL operand.
   *
   * @param size Size of the new REIL operand.
   * @param value Value of the new REIL operand.
   */
  public ReilOperand(final OperandSize size, final String value) {
    this(ReilHelpers.createOperand(OperandSize.valueOf(size), value));
  }

  @Override
  public com.google.security.zynamics.reil.ReilOperand getNative() {
    return m_operand;
  }

  // ! Size of the operand.
  /**
   * Returns the size of the operand.
   *
   * @return The size of the operand.
   */
  public OperandSize getSize() {
    return OperandSize.valueOf(m_operand.getSize());
  }

  // ! Type of the operand.
  /**
   * Returns the type of the operand.
   *
   * @return The type of the operand.
   */
  public OperandType getType() {
    return OperandType.valueOf(m_operand.getType());
  }

  // ! Value of the operand.
  /**
   * Returns the string value of the operand.
   *
   * @return The string value of the operand.
   */
  public String getValue() {
    return m_operand.getValue();
  }

  // ! Printable representation of the operand.
  /**
   * Returns the string representation of the operand.
   *
   * @return The string representation of the operand.
   */
  @Override
  public String toString() {
    return getValue();
  }
}
