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
 * Translates ROR instructions to REIL code.
 */
public class RorTranslator implements IInstructionTranslator {

  // TODO(timkornau): Check this code again

  /**
   * Translates a ROR instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The ROR instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the
   *           passed instruction is not an ROR instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a ror instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree targetOperand = operands.get(0);
    final IOperandTree sourceOperand = operands.get(1);

    // Load source operand.
    final TranslationResult sourceResult = Helpers.translateOperand(environment, offset,
        sourceOperand, true);
    instructions.addAll(sourceResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load destination operand.
    final TranslationResult targetResult = Helpers.translateOperand(environment, offset,
        targetOperand, true);
    instructions.addAll(targetResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();
    final int linesBefore = instructions.size();

    final OperandSize sourceSize = sourceResult.getSize();
    final OperandSize targetSize = targetResult.getSize();

    final String sourceRegister = sourceResult.getRegister();
    final String targetRegister = targetResult.getRegister();

    final String rotateMask = environment.getNextVariableString();
    final String rotateMaskZero = environment.getNextVariableString();
    final String rotateMaskLessOne = environment.getNextVariableString();
    final String rotateMaskOne = environment.getNextVariableString();
    final String shrValue = environment.getNextVariableString();
    final String shredResult = environment.getNextVariableString();
    final String shlValue = environment.getNextVariableString();
    final String shledResult = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String tempCf = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String tempOfLsb = environment.getNextVariableString();

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(targetSize));
    final String modVal = String.valueOf(targetSize.getBitSize());
    final String msbMask2nd = String.valueOf(TranslationHelpers.getMsbMask(targetSize) / 2);
    final String shiftMsbLsb = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(targetSize));
    final String shift2ndMsbLsb = String
        .valueOf(TranslationHelpers.getShiftMsbLsbMask(targetSize) + 1);

    // Make sure to rotate less than the size of the register
    instructions.add(ReilHelpers.createMod(offset, sourceSize, sourceRegister, targetSize, modVal,
        targetSize, rotateMask));

    // Find out if the rotate mask is 0 and negate the result
    instructions.add(ReilHelpers.createBisz(offset + 1, targetSize, rotateMask, OperandSize.BYTE,
        rotateMaskZero));

    // Find out if the rotate mask is 1
    instructions.add(ReilHelpers.createSub(offset + 2, targetSize, rotateMask, targetSize, "1",
        targetSize, rotateMaskLessOne));
    instructions.add(ReilHelpers.createBisz(offset + 3, targetSize, rotateMaskLessOne,
        OperandSize.BYTE, rotateMaskOne));

    // Negate the rotate-mask => ROT to the right
    instructions.add(ReilHelpers.createSub(offset + 4, OperandSize.BYTE, "0", OperandSize.BYTE,
        rotateMask, OperandSize.BYTE, shrValue));

    // Perform the rotate
    instructions.add(ReilHelpers.createBsh(offset + 5, targetSize, targetRegister, OperandSize.BYTE,
        shrValue, targetSize, shredResult));
    instructions.add(ReilHelpers.createSub(offset + 6, OperandSize.BYTE, modVal, OperandSize.BYTE,
        rotateMask, OperandSize.BYTE, shlValue));
    instructions.add(ReilHelpers.createBsh(offset + 7, targetSize, targetRegister, OperandSize.BYTE,
        shlValue, targetSize, shledResult));
    instructions.add(ReilHelpers.createOr(offset + 8, targetSize, shredResult, targetSize,
        shledResult, targetSize, result));

    // Don't change the flags if the rotate value was zero
    final String jmpGoal = String.format("%d.%d", instruction.getAddress().toLong(),
        linesBefore + 18);
    instructions.add(ReilHelpers.createJcc(offset + 9, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal));

    // Set the CF to the new MSB
    instructions.add(ReilHelpers.createAnd(offset + 10, targetSize, result, targetSize, msbMask,
        targetSize, tempCf));
    instructions.add(ReilHelpers.createBsh(offset + 11, targetSize, tempCf, targetSize, shiftMsbLsb,
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    // The OF needs to be set to a different value if the rotate-mask was 1
    final String jmpGoal2 = String.format("%d.%d", instruction.getAddress().toLong(),
        linesBefore + 15);
    instructions.add(ReilHelpers.createJcc(offset + 12, OperandSize.BYTE, rotateMaskOne,
        OperandSize.ADDRESS, jmpGoal2));

    // Set the OF to undefined if the rotate-mask was positive but not 1
    instructions.add(ReilHelpers.createUndef(offset + 13, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Jump to the end
    final String jmpGoal3 = String.format("%d.%d", instruction.getAddress().toLong(),
        linesBefore + 18);
    instructions.add(
        ReilHelpers.createJcc(offset + 14, OperandSize.BYTE, "1", OperandSize.ADDRESS, jmpGoal3));

    // Set the OF to the old MSB
    instructions.add(ReilHelpers.createAnd(offset + 15, targetSize, result, targetSize, msbMask2nd,
        targetSize, tempOf));
    instructions.add(ReilHelpers.createBsh(offset + 16, targetSize, tempOf, targetSize,
        shift2ndMsbLsb, OperandSize.BYTE, tempOfLsb));
    instructions.add(ReilHelpers.createXor(offset + 17, OperandSize.BYTE, tempOfLsb,
        OperandSize.BYTE, Helpers.CARRY_FLAG, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    Helpers.writeBack(environment, offset + 18, targetOperand, result, targetResult.getSize(),
        targetResult.getAddress(), targetResult.getType(), instructions);
  }
}
