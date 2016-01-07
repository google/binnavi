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

import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.MemoryCell;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;

public final class StmTransformer extends BaseTransformer {
  private static IValueElement getValue(final ReilOperand firstOperand,
      final IValueElement previousState2) {
    if (previousState2 instanceof Undefined) {
      return new Undefined();
    } else {
      return getAtomicType(firstOperand);
    }
  }

  public static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement state) {
    // STM x, , y

    final ReilOperand inputOperand = instruction.getFirstOperand();
    final ReilOperand addressOperand = instruction.getThirdOperand();

    final OperandType inputOperandType = inputOperand.getType();

    if (inputOperandType == OperandType.INTEGER_LITERAL) {
      final IValueElement previousAddressState = state.getState(addressOperand.getValue());
      final IValueElement outputValue = getValue(inputOperand, previousAddressState);

      if ((previousAddressState == null) || (previousAddressState instanceof Undefined)) {
        final IValueElement newThirdState = getAtomicType(addressOperand);

        return state.update(instruction, new MemoryCell(newThirdState), outputValue);
      } else {
        final IValueElement previousState2b = state.getState(new MemoryCell(previousAddressState));

        if ((previousState2b == null) || (previousState2b instanceof Undefined)) {
          return state.update(instruction, new MemoryCell(previousAddressState), outputValue);
        } else {
          return state.update(instruction, new MemoryCell(previousState2b), outputValue);
        }
      }
    } else if (inputOperandType == OperandType.REGISTER) {
      final IValueElement newThirdState = getAtomicType(addressOperand);

      final IValueElement previousStateInput = state.getState(inputOperand.getValue());
      final IValueElement previousState2 = getOperandValue(addressOperand, state);

      if ((previousStateInput == null) && (previousState2 == null)) {
        return state
            .update(instruction, new MemoryCell(newThirdState), getAtomicType(inputOperand));
      } else if ((previousStateInput == null) && (previousState2 != null)) {
        final IValueElement previousState2b = state.getState(new MemoryCell(previousState2));

        if (previousState2b == null) {
          return state.update(instruction, new MemoryCell(previousState2),
              getAtomicType(inputOperand));
        } else {
          return state.update(instruction, new MemoryCell(previousState2b),
              getAtomicType(inputOperand));
        }
      } else if ((previousStateInput != null) && (previousState2 == null)) {
        return state.update(instruction, new MemoryCell(newThirdState), previousStateInput);
      } else if (previousState2 instanceof Undefined) {
        return state.update(instruction, new MemoryCell(newThirdState), new Undefined());
      } else {
        final IValueElement previousState2b = state.getState(new MemoryCell(previousState2));

        if ((previousState2b == null) || (previousState2b instanceof Undefined)) {
          return state.update(instruction, new MemoryCell(previousState2), previousStateInput);
        } else {
          return state.update(instruction, new MemoryCell(previousState2b), previousStateInput);
        }
      }
    }

    throw new IllegalStateException("Not yet implemented");
  }
}
