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
package com.google.security.zynamics.reil;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

/**
 * This class can be used to store information about the operands of REIL instructions.
 */
public class ReilOperand implements IOperandTree {
  private final ReilOperandNode m_root;

  /**
   * Creates a new ReilOperand object.
   * 
   * @param root The root operand node of the operand.
   */
  public ReilOperand(final ReilOperandNode root) {
    m_root = Preconditions.checkNotNull(root);
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof ReilOperand)) {
      return false;
    }

    final ReilOperand rhsOperand = (ReilOperand) rhs;

    return getType().equals(rhsOperand.getType()) && getValue().equals(rhsOperand.getValue())
        && getSize().equals(rhsOperand.getSize());
  }

  @Override
  public ReilOperandNode getRootNode() {
    return m_root;
  }

  /**
   * Returns the size of the operand.
   * 
   * @return The size of the operand.
   */
  public OperandSize getSize() {
    return OperandSize.sizeStringToValue(m_root.getValue());
  }

  /**
   * Returns the type of the operand.
   * 
   * @return The type of the operand.
   */
  public OperandType getType() {
    return OperandType.getOperandType(getValue());
  }

  /**
   * Returns the value of the operand.
   * 
   * @return The value of the operand.
   */
  public String getValue() {
    return m_root.getChildren().get(0).getValue();
  }

  @Override
  public int hashCode() {
    return getType().hashCode() * getValue().hashCode() * getSize().hashCode();
  }

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
