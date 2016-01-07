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
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Dereference;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.MemoryCell;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;

/**
 * During range tracking, this transformer transforms the global state whenever a LDM instruction is
 * processed.
 */
public final class LdmTransformer extends BaseTransformer {
  public static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement incomingState) {
    final ReilOperand memoryAddressOperand = instruction.getFirstOperand();
    final ReilOperand outputOperand = instruction.getThirdOperand();

    final Register outputRegister = new Register(outputOperand.getValue());

    final IValueElement memoryAddress = getOperandValue(memoryAddressOperand, incomingState);

    if ((memoryAddress == null) || (memoryAddress instanceof Undefined)) {
      final IValueElement memoryAddressValue = getAtomicType(memoryAddressOperand);

      final Dereference dereference = new Dereference(memoryAddressValue);

      return incomingState.update(instruction, outputRegister, dereference);
    } else {
      final IValueElement previousState2 = incomingState.getState(new MemoryCell(memoryAddress));

      if (previousState2 == null) {
        return incomingState.update(instruction, outputRegister, new Dereference(memoryAddress));
      } else {
        return incomingState.update(instruction, outputRegister, previousState2);
      }
    }
  }
}
