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
package com.google.security.zynamics.reil.translators.x86;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.general.Pair;


/**
 * Generator that generates code for the NotBelowEqual condition.
 */
public class NotBelowEqualGenerator implements IConditionGenerator {

  /**
   * Generates code for the NotBelowEqual condition.
   * 
   * @param environment A valid translation environment (can't be null)
   * @param offset Next usable REIL offset (must be >= 0)
   * @param instructions The condition code is added to this list of instructions
   * 
   * @throws InternalTranslationException if the argument environment is null or the argument offset
   *         is less than 0.
   * 
   * @return The name and size of the register that holds the result of the condition.
   */
  @Override
  public Pair<OperandSize, String> generate(final ITranslationEnvironment environment,
      final long offset, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");
    Preconditions.checkArgument(offset >= 0, "Error: Argument offset can't be less than 0");

    // not below: (CF == 0 AND ZF == 0)

    final String negatedCarry = environment.getNextVariableString();
    final String negatedZero = environment.getNextVariableString();
    final String result = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset, OperandSize.BYTE, Helpers.CARRY_FLAG,
        OperandSize.BYTE, "1", OperandSize.BYTE, negatedCarry));
    instructions.add(ReilHelpers.createXor(offset + 1, OperandSize.BYTE, Helpers.OVERFLOW_FLAG,
        OperandSize.BYTE, "1", OperandSize.BYTE, negatedZero));
    instructions.add(ReilHelpers.createAnd(offset + 2, OperandSize.BYTE, negatedCarry,
        OperandSize.BYTE, negatedZero, OperandSize.BYTE, result));

    return new Pair<OperandSize, String>(OperandSize.BYTE, result);
  }

}
