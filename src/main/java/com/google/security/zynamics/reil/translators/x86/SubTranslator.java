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
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;


/**
 * Translates SUB instructions to REIL code.
 */
public class SubTranslator implements IInstructionTranslator {
  /**
   * Translates a SUB instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The SUB instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SUB instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sub");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a sub instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree operand1 = operands.get(0);
    final IOperandTree operand2 = operands.get(1);

    // Load source operand.
    final TranslationResult operand2Result =
        Helpers.translateOperand(environment, offset, operand2, true);
    instructions.addAll(operand2Result.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load destination operand.
    final TranslationResult operand1Result =
        Helpers.translateOperand(environment, offset, operand1, true);
    instructions.addAll(operand1Result.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final OperandSize size = operand1Result.getSize();

    final String register1 = operand1Result.getRegister();
    final String register2 = operand2Result.getRegister();

    // Subtract the two values
    final String subResultValue =
        Helpers.generateSub(environment, offset, size, register1, register2, instructions);

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();
    
    Helpers.writeParityFlag(environment, offset, size, subResultValue, instructions);
    offset = baseOffset + instructions.size();
    
    // Write the result of the value back into the target operand
    Helpers.writeBack(environment, offset, operand1, subResultValue, size,
        operand1Result.getAddress(), operand1Result.getType(), instructions);
  }
}
