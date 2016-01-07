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

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.BitwiseAnd;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IElementGenerator;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;

/**
 * During range tracking, this transformer transforms the global state whenever an AND instruction
 * is processed.
 */
public final class AndTransformer extends BaseTransformer {
  private static IElementGenerator AND_GENERATOR = new AndGenerator();

  public static ValueTrackerElement transform(final ReilInstruction instruction,
      final ValueTrackerElement incomingState) {
    return transform(instruction, incomingState, AND_GENERATOR);
  }

  private static class AndGenerator implements IElementGenerator {
    private static boolean eitherZero(final IValueElement lhs, final IValueElement rhs) {
      return ((lhs instanceof Literal) && ((Literal) lhs).getValue().equals(BigInteger.ZERO))
          || ((rhs instanceof Literal) && ((Literal) rhs).getValue().equals(BigInteger.ZERO));
    }

    @Override
    public IValueElement generate(final IValueElement lhs, final IValueElement rhs) {
      if (eitherZero(lhs, rhs)) {
        // X & 0 = 0
        // 0 & X = 0

        return new Literal(BigInteger.ZERO);
      } else if (lhs.equals(rhs)) {
        // X & X = X

        return lhs.clone();
      } else {
        return new BitwiseAnd(lhs, rhs).getSimplified();
      }
    }
  }
}
