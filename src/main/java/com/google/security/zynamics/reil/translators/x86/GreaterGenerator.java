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
 * Generator that generates code for the Greater condition.
 */
public class GreaterGenerator implements IConditionGenerator {

  /**
   * Generates code for the Greater condition.
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

    // greater: ZF = 0 AND SF = OF

    final String xoredFlags = environment.getNextVariableString();
    final String combinedFlags = environment.getNextVariableString();
    final String negatedResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset, OperandSize.BYTE, Helpers.OVERFLOW_FLAG,
        OperandSize.BYTE, Helpers.SIGN_FLAG, OperandSize.BYTE, xoredFlags));
    instructions.add(ReilHelpers.createOr(offset + 1, OperandSize.BYTE, xoredFlags,
        OperandSize.BYTE, Helpers.ZERO_FLAG, OperandSize.BYTE, combinedFlags));
    instructions.add(ReilHelpers.createBisz(offset + 2, OperandSize.BYTE, combinedFlags,
        OperandSize.BYTE, negatedResult));

    return new Pair<OperandSize, String>(OperandSize.BYTE, negatedResult);
  }

}
