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

// / Adapter class for operand expressions.
/**
 * Adapter class that can be used by objects that want to listen on operand expressions but only
 * need to process few events.
 */
public class OperandExpressionListenerAdapter implements IOperandExpressionListener {
  @Override
  public void addedReference(final OperandExpression operandExpression, final Reference reference) {
    // Empty default implementation
  }

  @Override
  public void changed(final OperandExpression operandExpression) {
    // Empty default implementation
  }

  @Override
  public void removedReference(
      final OperandExpression operandExpression, final Reference reference) {
    // Empty default implementation
  }
}
