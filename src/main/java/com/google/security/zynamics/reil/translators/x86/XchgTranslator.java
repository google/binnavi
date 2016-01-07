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
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;


/**
 * Translates XCHG instructions to REIL code.
 */
public class XchgTranslator implements IInstructionTranslator {

  // TODO(timkornau): Check this code again

  /**
   * Translates a XCHG instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The XCHG instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an XCHG instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "xchg");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a xchg instruction (invalid number of operands)");
    }

    final long reilOffsetBase = instruction.getAddress().toLong() * 0x100;
    long offset = reilOffsetBase;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree firstOperand = operands.get(0);
    final IOperandTree secondOperand = operands.get(1);

    final OperandSize archSize = environment.getArchitectureSize();

    // Load first operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, firstOperand, true);
    instructions.addAll(firstResult.getInstructions());
    offset = reilOffsetBase + instructions.size();

    // Load second operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, secondOperand, true);
    instructions.addAll(secondResult.getInstructions());
    offset = reilOffsetBase + instructions.size();

    if (firstResult.getSize() != secondResult.getSize()) {
      throw new InternalTranslationException(
          "Error: The operands of XCHG instructions must have equal size");
    }

    final OperandSize size = firstResult.getSize();

    final String firstRegister = firstResult.getRegister();
    final String secondRegister = secondResult.getRegister();

    if (firstResult.getType() == TranslationResultType.REGISTER) {
      if (secondResult.getType() == TranslationResultType.REGISTER) {
        if (size == archSize) {
          final String temp = environment.getNextVariableString();
          instructions.add(ReilHelpers.createStr(offset, size, firstRegister, size, temp));
          instructions.add(ReilHelpers.createStr(offset + 1, size, secondRegister, size,
              firstRegister));
          instructions.add(ReilHelpers.createStr(offset + 2, size, temp, size, secondRegister));
          Helpers.writeBack(environment, offset + instructions.size(), firstOperand,
                            firstRegister, archSize, firstResult.getAddress(),
                            firstResult.getType(), instructions);
          Helpers.writeBack(environment, offset + instructions.size(), secondOperand,
                            secondRegister, archSize, secondResult.getAddress(),
                            secondResult.getType(), instructions);
        } else {
          Helpers.writeBack(environment, offset, secondOperand, firstRegister, size,
              secondResult.getAddress(), secondResult.getType(), instructions);
          offset = reilOffsetBase + instructions.size();

          Helpers.writeBack(environment, offset, firstOperand, secondRegister, size,
              firstResult.getAddress(), firstResult.getType(), instructions);
        }
      } else if (secondResult.getType() == TranslationResultType.MEMORY_ACCESS) {
        Helpers.writeBack(environment, offset, secondOperand, firstRegister, size,
            secondResult.getAddress(), secondResult.getType(), instructions);
        offset = reilOffsetBase + instructions.size();

        Helpers.writeBack(environment, offset, firstOperand, secondRegister, size,
            firstResult.getAddress(), firstResult.getType(), instructions);
      } else {
        assert false;
      }
    } else {
      assert false;
    }
  }
}
