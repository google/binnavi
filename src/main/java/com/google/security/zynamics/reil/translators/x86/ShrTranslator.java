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

import java.util.ArrayList;
import java.util.List;

/**
 * Translates SHR instructions to REIL code.
 */
public class ShrTranslator implements IInstructionTranslator {

  // TODO(timkornau): Check this code again

  /**
   * Translates a SHR instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The SHR instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SHR instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "shr");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a shr instruction (invalid number of operands)");
    }

    final long reilOffsetBase = instruction.getAddress().toLong() * 0x100;
    long offset = reilOffsetBase;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = reilOffsetBase + instructions.size();

    // Load destination operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(secondResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = reilOffsetBase + instructions.size();

    final OperandSize size1 = firstResult.getSize();
    final OperandSize size2 = secondResult.getSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(size1);

    final String operand1 = firstResult.getRegister();
    final String operand2 = secondResult.getRegister();

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(size1));
    final String modValue = String.valueOf(size1.getBitSize());

    final String shiftMask = environment.getNextVariableString();
    final String shiftMaskZero = environment.getNextVariableString();
    final String shiftMaskLessOne = environment.getNextVariableString();
    final String shiftMaskOne = environment.getNextVariableString();
    final String shiftMaskNeg = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();
    final String incShiftMaskNeg = environment.getNextVariableString();
    final String decResult = environment.getNextVariableString();

    final int before = instructions.size();

    final List<ReilInstruction> writebackInstructions = new ArrayList<ReilInstruction>();

    // Write the result of the SHR operation back into the target register
    Helpers.writeBack(environment, offset + 17, operands.get(0), truncatedResult, size1,
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

    // Negate the shift-mask => BSH to the right
    instructions.add(ReilHelpers.createSub(offset + 4, size2, "0", size2, shiftMask, size2,
        shiftMaskNeg));

    // Perform the shift
    instructions.add(ReilHelpers.createBsh(offset + 5, size1, operand1, size2, shiftMaskNeg,
        resultSize, result));

    // Truncate the result to the correct size1
    instructions.add(ReilHelpers.createAnd(offset + 6, resultSize, result, size1, truncateMask,
        size1, truncatedResult));


    // Don't change the flags if the shift value was zero (jump to writeBack).
    final String jmpGoalWriteBack =
        String.format("%d.%d", instruction.getAddress().toLong(), before + 17);
    instructions.add(ReilHelpers.createJcc(offset + 7, OperandSize.BYTE, shiftMaskZero,
        OperandSize.ADDRESS, jmpGoalWriteBack));

    // The SF is always 0
    instructions.add(ReilHelpers.createStr(offset + 8, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.SIGN_FLAG));

    // Set the CF to the last MSB shifted out of the register
    // Perform another shift, this time by one position less and take the LSB
    // This is only safe if the mask is not 0
    instructions.add(ReilHelpers.createAdd(offset + 9, size2, shiftMaskNeg, size2, "1", size2,
        incShiftMaskNeg));
    instructions.add(ReilHelpers.createBsh(offset + 10, size1, operand1, size2, incShiftMaskNeg,
        size1, decResult));
    instructions.add(ReilHelpers.createAnd(offset + 11, size1, decResult, OperandSize.BYTE, "1",
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Set the ZF
    instructions.add(ReilHelpers.createBisz(offset + 12, size1, truncatedResult, OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    // The OF needs to be set to a different value if the shift-mask was 1
    final String jmpGoal2 = String.format("%d.%d", instruction.getAddress().toLong(), before + 16);
    instructions.add(ReilHelpers.createJcc(offset + 13, OperandSize.BYTE, shiftMaskOne,
        OperandSize.ADDRESS, jmpGoal2));

    // Set the OF to undefined if the shift-mask was positive but not 1
    instructions.add(ReilHelpers.createUndef(offset + 14, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Jump to writeBack.
    instructions.add(ReilHelpers.createJcc(offset + 15, OperandSize.BYTE, "1",
        OperandSize.ADDRESS, jmpGoalWriteBack));

    // Set the OF if the shift-mask was 1
    instructions.add(ReilHelpers.createXor(offset + 16, OperandSize.BYTE, Helpers.SIGN_FLAG,
        OperandSize.BYTE, Helpers.CARRY_FLAG, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Write back to the target register.
    instructions.addAll(writebackInstructions);
  }
}
