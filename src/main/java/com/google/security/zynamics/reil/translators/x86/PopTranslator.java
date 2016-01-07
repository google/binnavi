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
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;


/**
 * Translates POP instructions to REIL code.
 */
public class PopTranslator implements IInstructionTranslator {
  /**
   * Translates a POP instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The PUSH instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an POP instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a pop instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // POP instructions have exactly one operand
    final IOperandTree operand = instruction.getOperands().get(0);

    // Load the operand
    final TranslationResult result = Helpers.translateOperand(environment, offset, operand, false);
    final TranslationResultType resultType = result.getType();
    final OperandSize resultSize = result.getSize();

    instructions.addAll(result.getInstructions());

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    // Load the value from the stack
    final String popResult =
        Helpers.generatePop(environment, offset, resultSize, null, instructions);

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    // Write the loaded value into the target register
    Helpers.writeBack(environment, offset, operand, popResult, resultSize, result.getAddress(),
        resultType, instructions);
  }
}
