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
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;


/**
 * Translates XOR instructions to REIL code.
 */
public class XorTranslator implements IInstructionTranslator {
  /**
   * Translates a XOR instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The XOR instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an XOR instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "xor");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a xor instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree targetOperand = operands.get(0);
    final IOperandTree sourceOperand = operands.get(1);

    // Load source operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, sourceOperand, true);
    instructions.addAll(sourceResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load destination operand.
    final TranslationResult targetResult =
        Helpers.translateOperand(environment, offset, targetOperand, true);

    // Adjust the offset of the next REIL instruction.
    instructions.addAll(targetResult.getInstructions());
    offset = baseOffset + instructions.size();

    final OperandSize size = targetResult.getSize();

    final String sourceRegister = sourceResult.getRegister();
    final String targetRegister = targetResult.getRegister();

    final String xorResult = environment.getNextVariableString();

    // Do the XOR operation
    instructions.add(ReilHelpers.createXor(offset, size, sourceRegister, size, targetRegister,
        size, xorResult));

    // Set the flags according to the result of the XOR operation
    Helpers.generateBinaryOperationFlags(environment, offset + 1, xorResult, size, instructions);
    offset = baseOffset + instructions.size();

    // Write the result of the XOR operation into the target register
    Helpers.writeBack(environment, offset, targetOperand, xorResult, size,
        targetResult.getAddress(), targetResult.getType(), instructions);
  }
}
