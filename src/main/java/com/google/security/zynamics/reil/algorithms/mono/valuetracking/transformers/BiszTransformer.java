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
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.NullCheck;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;

import java.math.BigInteger;

/**
 * During range tracking, this transformer transforms the global state whenever a BISZ instruction
 * is processed.
 */
public final class BiszTransformer extends BaseTransformer {
  private static IValueElement getOutputValue(final ReilOperand inputOperand,
      final IValueElement previousState) {
    if (previousState == null) {
      // BISZ R1, , R2 -> (R2 => R1 != 0)

      return new NullCheck(getAtomicType(inputOperand));
    } else if (previousState instanceof Undefined) {
      // BISZ R1, , R2 | R1 := Undefined -> (R2 => Undefined)

      return new Undefined();
    } else if (previousState instanceof Literal) {
      // BISZ L1, , R1 -> (R1 => L1 != 0)

      final BigInteger inputValue = previousState.evaluate();
      return new NullCheck(new Literal(inputValue)).getSimplified();
    } else {
      // BISZ R1, , R2 -> (R2 => state(R1) != 0)

      return new NullCheck(previousState).getSimplified();
    }
  }

  public static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement incomingState) {
    final ReilOperand inputOperand = instruction.getFirstOperand();
    final ReilOperand outputOperand = instruction.getThirdOperand();

    final Register outputRegister = new Register(outputOperand.getValue());

    final IValueElement inputValue = getOperandValue(inputOperand, incomingState);

    final IValueElement outputValue = getOutputValue(inputOperand, inputValue);

    return incomingState.update(instruction, outputRegister, outputValue);
  }
}
