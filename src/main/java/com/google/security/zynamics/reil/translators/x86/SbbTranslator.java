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
 * Translates SBB instructions to REIL code.
 */
public class SbbTranslator implements IInstructionTranslator {
  /**
   * Translates a SBB instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The SBB instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SBB instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sbb");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a sbb instruction (invalid number of operand)");
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

    // Load target operand.
    final TranslationResult targetResult =
        Helpers.translateOperand(environment, offset, targetOperand, true);
    instructions.addAll(targetResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    if (sourceResult.getSize() != targetResult.getSize()) {
      throw new InternalTranslationException(
          "Error: The operands of SBB instructions must have equal size");
    }

    final OperandSize size = sourceResult.getSize();

    final String sourceRegister = sourceResult.getRegister();
    final String targetRegister = targetResult.getRegister();

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String carryMask = String.valueOf(Helpers.getCarryMask(size));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(size));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));
    final String shiftCarry = String.valueOf(-size.getBitSize());

    final OperandSize resultSize = TranslationHelpers.getNextSize(size);

    final String msb1 = environment.getNextVariableString();
    final String msb2 = environment.getNextVariableString();
    final String subResult = environment.getNextVariableString();
    final String subResultTemp = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String msbSameBefore = environment.getNextVariableString();
    final String msbChanged = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String tempCf = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();

    // Isolate the MSBs of the two operands
    instructions
        .add(ReilHelpers.createAnd(offset, size, sourceRegister, size, msbMask, size, msb1));
    instructions.add(ReilHelpers.createAnd(offset + 1, size, targetRegister, size, msbMask, size,
        msb2));

    // Perform the subtraction
    instructions.add(ReilHelpers.createSub(offset + 2, size, sourceRegister, size, targetRegister,
        resultSize, subResultTemp));
    instructions.add(ReilHelpers.createSub(offset + 3, resultSize, subResultTemp, OperandSize.BYTE,
        Helpers.CARRY_FLAG, resultSize, subResult));

    // Isolate the MSB of the result and put it into the Sign Flag
    instructions.add(ReilHelpers.createAnd(offset + 4, resultSize, subResult, resultSize, msbMask,
        size, msbResult));
    instructions.add(ReilHelpers.createBsh(offset + 5, size, msbResult, size, shiftValue,
        OperandSize.BYTE, Helpers.SIGN_FLAG));

    // Find out if the MSB of the two operands were different and whether the MSB of the first
    // operand changed
    instructions
        .add(ReilHelpers.createXor(offset + 6, size, msb1, size, msb2, size, msbSameBefore));
    instructions.add(ReilHelpers.createXor(offset + 7, size, msb1, size, msbResult, size,
        msbChanged));
    instructions.add(ReilHelpers.createAnd(offset + 8, size, msbSameBefore, size, msbChanged, size,
        tempOf));

    // Write the result into the Overflow Flag
    instructions.add(ReilHelpers.createBsh(offset + 9, size, tempOf, size, shiftValue,
        OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Update the Carry Flag
    instructions.add(ReilHelpers.createAnd(offset + 10, resultSize, subResult, resultSize,
        carryMask, resultSize, tempCf));
    instructions.add(ReilHelpers.createBsh(offset + 11, resultSize, tempCf, resultSize, shiftCarry,
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Truncate the result to fit into the target
    instructions.add(ReilHelpers.createAnd(offset + 12, resultSize, subResult, resultSize,
        truncateMask, size, truncatedResult));

    // Update the Zero Flag
    instructions.add(ReilHelpers.createBisz(offset + 13, size, truncatedResult, OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    // Write the result of the subtraction back to the target register
    Helpers.writeBack(environment, offset + 14, targetOperand, truncatedResult, size,
        targetResult.getAddress(), targetResult.getType(), instructions);
  }
}
