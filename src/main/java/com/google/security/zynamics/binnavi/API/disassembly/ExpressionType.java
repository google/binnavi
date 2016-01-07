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

/* ! \file ExpressionType.java \brief Contains the ExpressionType enumeration * */

// / Describes the potential types of operand tree expressions.
/**
 * Enumeration of potential operand tree expression types.
 */
public enum ExpressionType {
  /**
   * The expression is a symbol.
   */
  Symbol,

  /**
   * The expression is an immediate integer value.
   */
  ImmediateInteger,

  /**
   * The expression is an immediate float value.
   */
  ImmediateFloat,

  /**
   * The expression is an operand.
   */
  Operator,

  /**
   * The expression is a register.
   */
  Register,

  /**
   * The expression is a size prefix.
   */
  SizePrefix,

  /**
   * The expression is a memory dereferencing operation.
   */
  MemDeref,

  /**
   * The expression is an expression list.
   */
  ExpressionList;

  // / @cond INTERNAL
  /**
   * Converts an internal expression type to an API expression type.
   *
   * @param type The expression type to convert.
   *
   * @return The converted expression type.
   */
  public static ExpressionType convert(final com.google.security.zynamics.zylib.disassembly.ExpressionType type) {
    switch (type) {
      case SYMBOL:
        return Symbol;
      case IMMEDIATE_FLOAT:
        return ImmediateFloat;
      case IMMEDIATE_INTEGER:
        return ImmediateInteger;
      case MEMDEREF:
        return MemDeref;
      case OPERATOR:
        return Operator;
      case REGISTER:
        return Register;
      case SIZE_PREFIX:
        return SizePrefix;
      case EXPRESSION_LIST:
        return ExpressionList;
      default:
        throw new IllegalArgumentException("Error: Unknown expression type");
    }
  }

  /**
   * Converts an API expression type to an internal expression type.
   *
   * @return The internal expression type.
   */
  // / @endcond
  public com.google.security.zynamics.zylib.disassembly.ExpressionType getNative() {
    switch (this) {
      case ExpressionList:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.EXPRESSION_LIST;
      case ImmediateFloat:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.IMMEDIATE_FLOAT;
      case ImmediateInteger:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.IMMEDIATE_INTEGER;
      case MemDeref:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.MEMDEREF;
      case Operator:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.OPERATOR;
      case Register:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.REGISTER;
      case SizePrefix:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.SIZE_PREFIX;
      case Symbol:
        return com.google.security.zynamics.zylib.disassembly.ExpressionType.SYMBOL;
      default:
        throw new IllegalArgumentException("Error: Unknown expression type");
    }
  }
}
