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
 * Translates RCR instructions to REIL code.
 */
public class RcrTranslator implements IInstructionTranslator {

  // TODO(timkornau): Check this code again

  /**
   * Translates a RCR instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The RCR instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an RCR instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "rcr");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a rcr instruction (invalid number of operands)");
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

    final OperandSize sourceSize = sourceResult.getSize();
    final OperandSize targetSize = targetResult.getSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(sourceSize);

    final String sourceRegister = sourceResult.getRegister();
    final String targetRegister = targetResult.getRegister();

    final String rotateMask = environment.getNextVariableString();
    final String rotateMaskZero = environment.getNextVariableString();
    final String rotateMaskLessOne = environment.getNextVariableString();
    final String rotateMaskOne = environment.getNextVariableString();
    final String shiftedOp1 = environment.getNextVariableString();
    final String realOp1 = environment.getNextVariableString();
    final String shrValue = environment.getNextVariableString();
    final String shredResult = environment.getNextVariableString();
    final String shlValue = environment.getNextVariableString();
    final String shledResult = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String shiftedResult = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String tempOfLsb = environment.getNextVariableString();

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(sourceSize));
    final String maskSize = String.valueOf(TranslationHelpers.getAllBitsMask(sourceSize));
    final String modVal = String.valueOf(sourceSize.getBitSize());
    final String shiftMsbLsb = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(sourceSize));

    // Make sure to rotate less than the size of the register
    instructions.add(ReilHelpers.createMod(offset, targetSize, targetRegister, targetSize, modVal,
        targetSize, rotateMask));

    // Find out if the rotate mask is 0 and negate the result
    instructions.add(ReilHelpers.createBisz(offset + 1, targetSize, rotateMask, OperandSize.BYTE,
        rotateMaskZero));

    // Find out if the rotate mask is 1
    instructions.add(ReilHelpers.createSub(offset + 2, targetSize, rotateMask, targetSize, "1",
        targetSize, rotateMaskLessOne));
    instructions.add(ReilHelpers.createBisz(offset + 3, targetSize, rotateMaskLessOne,
        OperandSize.BYTE, rotateMaskOne));

    // Rotating through the carry flag is like rotating through a 33 bit register
    // For rotating rightwards, the CF must be added at the LSB of the 32 register
    instructions.add(ReilHelpers.createBsh(offset + 4, sourceSize, sourceRegister,
        OperandSize.BYTE, "1", resultSize, shiftedOp1));
    instructions.add(ReilHelpers.createOr(offset + 5, resultSize, shiftedOp1, OperandSize.BYTE,
        Helpers.CARRY_FLAG, resultSize, realOp1));

    // Negate the rotate-mask => ROT to the right
    instructions.add(ReilHelpers.createSub(offset + 6, OperandSize.BYTE, "0", OperandSize.BYTE,
        rotateMask, OperandSize.BYTE, shrValue));

    // Perform the rotate
    instructions.add(ReilHelpers.createBsh(offset + 7, sourceSize, realOp1, OperandSize.BYTE,
        shrValue, sourceSize, shredResult));
    instructions.add(ReilHelpers.createSub(offset + 8, OperandSize.BYTE, modVal, OperandSize.BYTE,
        rotateMask, OperandSize.BYTE, shlValue));
    instructions.add(ReilHelpers.createBsh(offset + 9, sourceSize, realOp1, OperandSize.BYTE,
        shlValue, sourceSize, shledResult));
    instructions.add(ReilHelpers.createOr(offset + 10, sourceSize, shredResult, sourceSize,
        shledResult, sourceSize, result));

    // Truncate the result (get rid of the CF in the LSB)
    instructions.add(ReilHelpers.createBsh(offset + 11, resultSize, result, OperandSize.BYTE, "-1",
        resultSize, shiftedResult));
    instructions.add(ReilHelpers.createAnd(offset + 12, resultSize, shiftedResult, sourceSize,
        maskSize, sourceSize, truncatedResult));

    // Don't change the flags if the rotate value was zero
    final String jmpGoal = "666";
    instructions.add(ReilHelpers.createJcc(offset + 13, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal));

    // Properly update OF if the rotate value == 1
    final String jmpGoal2 = "666";
    instructions.add(ReilHelpers.createJcc(offset + 14, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal2));

    // Set the OF to undefined if the rotate-mask was positive but not 1
    instructions.add(ReilHelpers.createUndef(offset + 15, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Update the CF now
    final String jmpGoal3 = "666";
    instructions.add(ReilHelpers.createJcc(offset + 15, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal3));

    instructions.add(ReilHelpers.createAnd(offset + 16, sourceSize, sourceRegister, sourceSize,
        msbMask, sourceSize, tempOf));
    instructions.add(ReilHelpers.createBsh(offset + 17, sourceSize, tempOf, sourceSize,
        shiftMsbLsb, OperandSize.BYTE, tempOfLsb));
    instructions.add(ReilHelpers.createXor(offset + 18, OperandSize.BYTE, tempOfLsb,
        OperandSize.BYTE, Helpers.CARRY_FLAG, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Set the CF to the LSB of the untruncated result
    instructions.add(ReilHelpers.createAnd(offset + 19, resultSize, result, OperandSize.BYTE, "1",
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    Helpers.writeBack(environment, offset + 20, targetOperand, result, targetResult.getSize(),
        targetResult.getAddress(), targetResult.getType(), instructions);
  }

}
