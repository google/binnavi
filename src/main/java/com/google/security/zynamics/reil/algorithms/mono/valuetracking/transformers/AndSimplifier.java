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

import java.math.BigInteger;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.BitwiseAnd;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IElementGenerator;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Register;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;
import com.google.security.zynamics.reil.translators.TranslationHelpers;

public class AndSimplifier {
  private static boolean isTruncateMask(final IValueElement value, final OperandSize size) {
    return (value instanceof Literal)
        && ((Literal) value).getValue().equals(
            BigInteger.valueOf(TranslationHelpers.getAllBitsMask(size)));
  }

  public static ValueTrackerElement simplifyAnd(final ReilInstruction instruction,
      final ReilOperand firstOperand, final ReilOperand secondOperand,
      final BitwiseAnd previousAnd, final ValueTrackerElement state,
      final IElementGenerator generator) {
    final ReilOperand thirdOperand = instruction.getThirdOperand();

    final Register targetRegister = new Register(thirdOperand.getValue());

    if (isTruncateMask(previousAnd.getLhs(), firstOperand.getSize())) {
      // (0xFFFFFFFF & X) + Y => (X + Y) & 0xFFFFFFFF

      // final Addition newAddition = new Addition(previousAnd.getRhs(), new Literal(new
      // BigInteger(secondOperand.getValue())));
      final IValueElement newAddition =
          generator.generate(previousAnd.getRhs(),
              new Literal(new BigInteger(secondOperand.getValue())));

      final BitwiseAnd newBitwiseAnd =
          new BitwiseAnd(new Literal(newAddition.evaluate()), previousAnd.getLhs());

      return state.update(instruction, targetRegister, newBitwiseAnd);
    } else if (isTruncateMask(previousAnd.getRhs(), firstOperand.getSize())) {
      // (X & 0xFFFFFFFF) + Y => (X + Y) & 0xFFFFFFFF

      final IValueElement previousLhs = previousAnd.getLhs();

      if (previousLhs instanceof Undefined) {
        return state.update(instruction, targetRegister, new Undefined());
      } else {
        // final Addition newAddition = new Addition(previousLhs, new Literal(new
        // BigInteger(secondOperand.getValue())));
        final IValueElement newAddition =
            generator.generate(previousLhs, new Literal(new BigInteger(secondOperand.getValue())));

        final BitwiseAnd newBitwiseAnd =
            new BitwiseAnd(newAddition.getSimplified(), previousAnd.getRhs());

        return state.update(instruction, targetRegister, newBitwiseAnd);
      }
    } else {
      final IValueElement previousState = state.getState(firstOperand.getValue());

      // final IValueElement addition = new Addition(previousState, new Literal(new
      // BigInteger(secondOperand.getValue())));
      final IValueElement addition =
          generator.generate(previousState, new Literal(new BigInteger(secondOperand.getValue())));

      return state.update(instruction, targetRegister, addition);
    }
  }
}
