/*
Copyright 2015 Google Inc. All Rights Reserved.

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

/**
 * Enumeration of possible operand types.
 */
public enum OperandType {

  /**
   * Operand does not exist
   */
  EMPTY,

  /**
   * Operand is an integer literal.
   */
  INTEGER_LITERAL,

  /**
   * Operand is a subaddress.
   */
  SUB_ADDRESS,

  /**
   * Operand is a register.
   */
  REGISTER;

  /**
   * Checks whether a string is a valid integer number.
   * 
   * @param value The string to check.
   * @return True or false, depending on whether the string is a number or not.
   */
  private static boolean isInteger(final String value) {
    for (final char character : value.toCharArray()) {
      if ((character != '-') && ((character < '0') || (character > '9'))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Converts the string representation of an operand to an OperandType value.
   * 
   * @param value The string representation of an operand.
   * @return The converted OperandType value.
   */
  public static OperandType getOperandType(final String value) {

    if ("".equals(value)) {
      return OperandType.EMPTY;
    }
    if (value.contains(".")) {
      return OperandType.SUB_ADDRESS;
    } else if (isInteger(value)) {
      return OperandType.INTEGER_LITERAL;
    } else {
      return OperandType.REGISTER;
    }
  }
}
