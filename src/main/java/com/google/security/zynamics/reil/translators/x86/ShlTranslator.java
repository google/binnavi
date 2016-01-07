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

import com.google.common.base.Preconditions;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Translates SHL instructions to REIL code.
 */
public class ShlTranslator implements IInstructionTranslator {

  // TODO(timkornau): Check this code again

  /**
   * Translates a SHL instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The SHL instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SHL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    if (!instruction.getMnemonic().equals("shl") && !instruction.getMnemonic().equals("sal")) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a shl instruction (wrong mnemonic)");
    }

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a shl instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load destination operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(secondResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final OperandSize size1 = firstResult.getSize();
    final OperandSize size2 = secondResult.getSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(size1);

    final String operand1 = firstResult.getRegister();
    final String operand2 = secondResult.getRegister();

    final String shiftMsbLsbValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size1));
    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size1));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(size1));
    final String modValue = String.valueOf(size1.getBitSize());
    final String carryMask = String.valueOf(Helpers.getCarryMask(size1));
    final String shiftCarryValue = String.valueOf(-size1.getBitSize());

    final String shiftMask = environment.getNextVariableString();
    final String shiftMaskZero = environment.getNextVariableString();
    final String shiftMaskLessOne = environment.getNextVariableString();
    final String shiftMaskOne = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String carryResult = environment.getNextVariableString();

    final int before = instructions.size();

    final List<ReilInstruction> writebackInstructions = new ArrayList<>();

    // Write the result of the SHR operation back into the target register
    Helpers.writeBack(environment, offset + 16, operands.get(0), truncatedResult, size1,
        firstResult.getAddress(), firstResult.getType(), writebackInstructions);

    // Make sure to shift less than the size1 of the register
    instructions.add(ReilHelpers.createMod(offset, size2, operand2, size2, modValue, size2,
        shiftMask));

    // Find out if the shift mask is 0 and negate the result
    instructions.add(ReilHelpers.createBisz(offset + 1, size2, shiftMask, OperandSize.BYTE,
        shiftMaskZero));

    // Find out if the shift mask is 1
    instructions.add(ReilHelpers.createSub(offset + 2, size2, "1", size2, shiftMask, size2,
        shiftMaskLessOne));
    instructions.add(ReilHelpers.createBisz(offset + 3, size2, shiftMaskLessOne, OperandSize.BYTE,
        shiftMaskOne));

    // Perform the shift
    instructions.add(ReilHelpers.createBsh(offset + 4, size1, operand1, size2, shiftMask,
        resultSize, result));

    // Truncate the result to the correct size1
    instructions.add(ReilHelpers.createAnd(offset + 5, resultSize, result, size1, truncateMask,
        size1, truncatedResult));

    // Don't change the flags if the shift value was zero (jump to writeBack).
    final String jmpGoalWriteBack =
        String.format("%d.%d", instruction.getAddress().toLong(), before + 16);
    instructions.add(ReilHelpers.createJcc(offset + 6, OperandSize.BYTE, shiftMaskZero,
        OperandSize.ADDRESS, jmpGoalWriteBack));

    // Extract the MSB of the result and shift it into the SF
    instructions.add(ReilHelpers.createAnd(offset + 7, resultSize, result, resultSize, msbMask,
        resultSize, msbResult));
    instructions.add(ReilHelpers.createBsh(offset + 8, resultSize, msbResult, resultSize,
        shiftMsbLsbValue, OperandSize.BYTE, Helpers.SIGN_FLAG));

    // Set the CF to the MSB of the result
    instructions.add(ReilHelpers.createAnd(offset + 9, resultSize, result, resultSize, carryMask,
        resultSize, carryResult));
    instructions.add(ReilHelpers.createBsh(offset + 10, resultSize, carryResult, resultSize,
        shiftCarryValue, OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Set the ZF
    instructions.add(ReilHelpers.createBisz(offset + 11, size1, truncatedResult, OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    // The OF needs to be set to a different value if the shift-mask was 1
    final String jmpGoal2 = String.format("%d.%d", instruction.getAddress().toLong(), before + 15);
    instructions.add(ReilHelpers.createJcc(offset + 12, OperandSize.BYTE, shiftMaskOne,
        OperandSize.ADDRESS, jmpGoal2));

    // Set the OF to undefined if the shift-mask was positive but not 1
    instructions.add(ReilHelpers.createUndef(offset + 13, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Jump to writeBack.
    instructions.add(ReilHelpers.createJcc(offset + 14, OperandSize.BYTE, "1",
        OperandSize.ADDRESS, jmpGoalWriteBack));

    // Set the OF if the shift-mask was 1.
    instructions.add(ReilHelpers.createXor(offset + 15, OperandSize.BYTE, Helpers.SIGN_FLAG,
        OperandSize.BYTE, Helpers.CARRY_FLAG, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Write back to the target register.
    instructions.addAll(writebackInstructions);
  }

}
