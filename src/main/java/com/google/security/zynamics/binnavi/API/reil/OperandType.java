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

/* ! \file OperandType.java \brief Contains the OperandType enumeration * */

/**
 * Describes the type of an operand.
 */
public enum OperandType {
  /**
   * Operand is unused.
   */
  EMPTY,

  /**
   * Operand is an integer literal.
   */
  INTEGER_LITERAL,

  /**
   * Operand is a register.
   */
  REGISTER,

  /**
   * Operand is a sub-address
   */
  SUB_ADDRESS;

  // / @cond INTERNAL
  /**
   * Converts an internal operand type value into an API operand type value.
   *
   * @param type The operand type value to convert.
   *
   * @return The converted operand type value.
   */
  // / @endcond
  public static OperandType valueOf(final com.google.security.zynamics.reil.OperandType type) {
    switch (type) {
      case EMPTY:
        return EMPTY;
      case INTEGER_LITERAL:
        return INTEGER_LITERAL;
      case REGISTER:
        return REGISTER;
      case SUB_ADDRESS:
        return SUB_ADDRESS;
      default:
        throw new IllegalArgumentException("Error: Unknown type");
    }
  }

}
