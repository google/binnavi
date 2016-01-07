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

/* ! \file OperandSize.java \brief Contains the OperandSize enumeration * */

/**
 * Describes the size of REIL operands.
 */
public enum OperandSize {
  /**
   * Operand is an Address
   */
  OPERAND_SIZE_ADDRESS,

  /**
   * Operand is 1 byte large
   */
  OPERAND_SIZE_BYTE,

  /**
   * Operand is 2 bytes large
   */
  OPERAND_SIZE_WORD,

  /**
   * Operand is 4 bytes large
   */
  OPERAND_SIZE_DWORD,

  /**
   * Operand does not exist
   */
  OPERAND_SIZE_EMPTY,

  /**
   * Operand is 8 bytes large
   */
  OPERAND_SIZE_QWORD,

  /**
   * Operand is 16 bytes large.
   */
  OPERAND_SIZE_OWORD;

  // / @cond INTERNAL
  /**
   * Converts an internal operand size value into an API operand type value.
   *
   * @param size The operand size value to convert.
   *
   * @return The converted operand size value.
   */
  public static OperandSize valueOf(final com.google.security.zynamics.reil.OperandSize size) {
    switch (size) {
      case ADDRESS:
        return OPERAND_SIZE_ADDRESS;
      case EMPTY:
        return OPERAND_SIZE_EMPTY;
      case BYTE:
        return OPERAND_SIZE_BYTE;
      case WORD:
        return OPERAND_SIZE_WORD;
      case DWORD:
        return OPERAND_SIZE_DWORD;
      case QWORD:
        return OPERAND_SIZE_QWORD;
      case OWORD:
        return OPERAND_SIZE_OWORD;
      default:
        throw new IllegalArgumentException("Error: Unknown REIL operand type");
    }
  }

  /**
   * Converts an API operand size value into an internal operand size value.
   *
   * @param size The size value to convert.
   *
   * @return The converted size value.
   */
  // / @endcond
  public static com.google.security.zynamics.reil.OperandSize valueOf(final OperandSize size) {
    switch (size) {
      case OPERAND_SIZE_ADDRESS:
        return com.google.security.zynamics.reil.OperandSize.ADDRESS;
      case OPERAND_SIZE_EMPTY:
        return com.google.security.zynamics.reil.OperandSize.EMPTY;
      case OPERAND_SIZE_BYTE:
        return com.google.security.zynamics.reil.OperandSize.BYTE;
      case OPERAND_SIZE_WORD:
        return com.google.security.zynamics.reil.OperandSize.WORD;
      case OPERAND_SIZE_DWORD:
        return com.google.security.zynamics.reil.OperandSize.DWORD;
      case OPERAND_SIZE_QWORD:
        return com.google.security.zynamics.reil.OperandSize.QWORD;
      case OPERAND_SIZE_OWORD:
        return com.google.security.zynamics.reil.OperandSize.OWORD;
      default:
        throw new IllegalArgumentException("Error: Unknown API operand type");
    }
  }

}
