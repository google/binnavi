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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;

/**
 * During range tracking, this transformer transforms the global state whenever a STR instruction is
 * processed.
 */
public final class StrTransformer extends BaseTransformer {
  public static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement incomingState) {
    // STR x, , y

    final ReilOperand inputOperand = instruction.getFirstOperand();
    final ReilOperand outputOperand = instruction.getThirdOperand();

    final Register outputRegister = new Register(outputOperand.getValue());

    final IValueElement previousState = getOperandValue(inputOperand, incomingState);

    if (previousState == null) {
      final IValueElement inputValue = getAtomicType(inputOperand);

      return incomingState.update(instruction, outputRegister, inputValue);
    } else {
      return incomingState.update(instruction, outputRegister, previousState.clone());
    }
  }
}
