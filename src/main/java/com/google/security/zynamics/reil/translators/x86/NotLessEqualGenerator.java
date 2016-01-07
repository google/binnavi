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

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


/**
 * Generator that generates code for the NotLessEqual condition.
 */
public class NotLessEqualGenerator implements IConditionGenerator {

  /**
   * Generates code for the NotLessEqual condition.
   * 
   * @param environment A valid translation environment (cannot be null)
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
    if (environment == null) {
      throw new InternalTranslationException("Error: Argument environment cannot be null");
    }

    if (instructions == null) {
      throw new InternalTranslationException("Error: Argument instructions cannot be null");
    }

    if (offset < 0) {
      throw new InternalTranslationException("Error: Argument offset cannot be less than 0");
    }

    // not less or equal: (SF == OF AND ZF == 0)

    final String connected = environment.getNextVariableString();
    final String ored = environment.getNextVariableString();
    final String negated = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset, OperandSize.BYTE, Helpers.SIGN_FLAG,
        OperandSize.BYTE, Helpers.OVERFLOW_FLAG, OperandSize.BYTE, connected));
    instructions.add(ReilHelpers.createOr(offset + 1, OperandSize.BYTE, connected,
        OperandSize.BYTE, Helpers.ZERO_FLAG, OperandSize.BYTE, ored));
    instructions.add(ReilHelpers.createXor(offset + 2, OperandSize.BYTE, ored, OperandSize.BYTE,
        "1", OperandSize.BYTE, negated));

    return new Pair<OperandSize, String>(OperandSize.BYTE, negated);
  }
}
