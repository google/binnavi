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
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.general.Pair;


/**
 * Translates conditional set instructions to REIL code.
 */
public class SetccTranslator implements IInstructionTranslator {

  /**
   * Generator that generates the code of the condition
   */
  private final IConditionGenerator conditionGenerator;

  /**
   * Creates a new conditional set instruction generator.
   * 
   * @param conditionGenerator Generator that generates the code of the condition
   */
  public SetccTranslator(final IConditionGenerator conditionGenerator) {

    Preconditions.checkNotNull(conditionGenerator,
        "Error: Argument conditionGenerator can't be null");

    this.conditionGenerator = conditionGenerator;
  }

  /**
   * Translates a SETcc instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The SETcc instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a conditional set instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a conditional setcc instruction (invalid number of operands)");
    }

    final long reilOffsetBase = instruction.getAddress().toLong() * 0x100;
    long reilOffset = reilOffsetBase;

    // SETCC instructions have exactly one operand.
    final IOperandTree operand = instruction.getOperands().get(0);

    // Load the operand.
    final TranslationResult result =
        Helpers.translateOperand(environment, reilOffset, operand, false);
    final OperandSize size = result.getSize();
    final TranslationResultType type = result.getType();
    final String address = result.getAddress();
    instructions.addAll(result.getInstructions());
    
    // Adjust the offset of the next REIL instruction.
    reilOffset = reilOffsetBase + instructions.size();

    final Pair<OperandSize, String> condition =
        conditionGenerator.generate(environment, reilOffset, instructions);

    reilOffset = reilOffsetBase + instructions.size();

    final String conditionRegister = condition.second();

    Helpers.writeBack(environment, reilOffset, operand, conditionRegister, size, address, type,
        instructions);
  }
}
