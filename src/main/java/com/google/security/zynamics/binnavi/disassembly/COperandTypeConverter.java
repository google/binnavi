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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

/**
 * Used to convert expression types to numeric values for saving them to the database.
 */
public final class COperandTypeConverter {
  /**
   * You are not supposed to instantiate this class.
   */
  private COperandTypeConverter() {
  }

  /**
   * Converts an expression type enumeration value to a numeric value.
   * 
   * @param type The type to convert.
   * 
   * @return The converted type.
   */
  public static int convert(final ExpressionType type) {
    switch (type) {
      case EXPRESSION_LIST:
        return IOperandTree.NODE_TYPE_SYMBOL_ID;
      case IMMEDIATE_FLOAT:
        return IOperandTree.NODE_TYPE_IMMEDIATE_FLOAT_ID;
      case IMMEDIATE_INTEGER:
        return IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID;
      case MEMDEREF:
        return IOperandTree.NODE_TYPE_DEREFERENCE_ID;
      case OPERATOR:
        return IOperandTree.NODE_TYPE_OPERATOR_ID;
      case REGISTER:
        return IOperandTree.NODE_TYPE_REGISTER_ID;
      case SIZE_PREFIX:
        return IOperandTree.NODE_TYPE_SIZE_PREFIX_ID;
      case SYMBOL:
        return IOperandTree.NODE_TYPE_SYMBOL_ID;
      default:
        throw new IllegalStateException(
            String.format("IE00225: Unknown expression type '%s'", type));
    }
  }
}
