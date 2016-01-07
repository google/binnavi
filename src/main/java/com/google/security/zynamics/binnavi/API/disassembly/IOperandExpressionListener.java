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

// / Used to listen on operand expressions.
/**
 * Interface that can be implemented by objects that want to be notified about changes in operand
 * expression objects.
 */
public interface IOperandExpressionListener {
  // ! Signals the arrival of a new reference.
  /**
   * Invoked after a reference was added to the operand expression.
   *
   * @param operandExpression The operand expression where the reference was added.
   * @param reference The added reference.
   */
  void addedReference(OperandExpression operandExpression, Reference reference);

  // ! Signals a change in the expression value.
  /**
   * Invoked after the string value of an operand expression changed.
   *
   * @param operandExpression The operand expression whose string value changed.
   */
  void changed(OperandExpression operandExpression);

  // ! Signals the removal of an existing reference.
  /**
   * Invoked after a reference was removed from the operand expression.
   *
   * @param operandExpression The operand expression from where the reference was removed.
   * @param reference The removed reference.
   */
  void removedReference(OperandExpression operandExpression, Reference reference);
}
