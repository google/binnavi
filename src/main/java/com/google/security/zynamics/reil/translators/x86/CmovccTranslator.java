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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;


/**
 * Translates conditional move instructions to REIL code.
 */
public class CmovccTranslator implements IInstructionTranslator {

  /**
   * Generator that generates the code of the condition
   */
  private final IConditionGenerator conditionGenerator;

  /**
   * Creates a new conditional move instruction generator.
   * 
   * @param conditionGenerator Generator that generates the code of the condition
   */
  public CmovccTranslator(final IConditionGenerator conditionGenerator) {

    this.conditionGenerator =
        Preconditions.checkNotNull(conditionGenerator,
            "Error: Argument conditionGenerator can't be null");
  }

  /**
   * Translates a Cmovcc instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The Cmovcc instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException Thrown if any of the arguments are null or if the
   *         instruction is not a valid conditional move instruction.
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");
    Preconditions
        .checkArgument(instruction.getOperands().size() == 2,
            "Error: Argument instruction is not a conditional move instruction (invalid number of operands)");

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // Generate the condition code
    final Pair<OperandSize, String> conditionResult =
        conditionGenerator.generate(environment, offset, instructions);
    final OperandSize conditionRegisterSize = conditionResult.first();
    final String conditionRegister = conditionResult.second();

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    // Flip the condition
    final String flippedCondition = environment.getNextVariableString();
    instructions.add(ReilHelpers.createBisz(offset, conditionRegisterSize, conditionRegister,
        OperandSize.BYTE, flippedCondition));

    // Generate the mov code (offset + 2 is correct; the code is placed behind the next JCC)
    final ArrayList<ReilInstruction> movCode = new ArrayList<ReilInstruction>();
    Helpers.generateMov(environment, offset + 2, instruction, movCode);

    // ESCA-JAVA0264: We will not get an overflow here
    final long lastOffset = instructions.size() + movCode.size() + 1;

    // Jump to the end of the block if the condition is not met
    final String jmpGoal = String.format("%d.%d", instruction.getAddress().toLong(), lastOffset);
    instructions.add(ReilHelpers.createJcc(offset + 1, OperandSize.BYTE, flippedCondition,
        OperandSize.ADDRESS, jmpGoal));

    // Add the mov code that's executed if the condition is true
    instructions.addAll(movCode);

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    // Add a terminating NOP, this makes it easier to get a target for the conditional jump
    instructions.add(ReilHelpers.createNop(offset));

  }

}
