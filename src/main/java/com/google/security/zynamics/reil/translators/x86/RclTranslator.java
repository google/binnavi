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
 * Translates RCL instructions to REIL code.
 */
public class RclTranslator implements IInstructionTranslator {
  // TODO:(timkornau@google.com) Check this code again

  /**
   * Translates a RCL instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The RCL instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an RCL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "rcl");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a rcl instruction (invalid number of operands)");
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
    final OperandSize resultSize = TranslationHelpers.getNextSize(targetSize);

    final String sourceRegister = sourceResult.getRegister();
    final String targetRegister = targetResult.getRegister();

    final String os = String.valueOf(targetSize.getBitSize());

    final String rotateMask = environment.getNextVariableString();
    final String rotateMaskZero = environment.getNextVariableString();
    final String rotateMaskLessOne = environment.getNextVariableString();
    final String rotateMaskOne = environment.getNextVariableString();
    final String shiftedCf = environment.getNextVariableString();
    final String realOp1 = environment.getNextVariableString();
    final String shrValue = environment.getNextVariableString();
    final String shredResult = environment.getNextVariableString();
    final String shledResult = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String tempOfLsb = environment.getNextVariableString();

    final String carryMask = String.valueOf(Helpers.getCarryMask(targetSize));
    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(targetSize));
    final String maskSize = String.valueOf(TranslationHelpers.getAllBitsMask(targetSize));
    final String modVal = String.valueOf(targetSize.getBitSize() + 1); // TODO: + 1 ?
    final String shiftMsbLsb = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(targetSize));

    final int linesBefore = instructions.size();

    // Make sure to rotate less than the size of the register
    instructions.add(ReilHelpers.createMod(offset, sourceSize, sourceRegister, sourceSize, os,
        OperandSize.BYTE, rotateMask));

    // Find out if the rotate mask is 0
    instructions.add(ReilHelpers.createBisz(offset + 1, OperandSize.BYTE, rotateMask,
        OperandSize.BYTE, rotateMaskZero));

    // Find out if the rotate mask is 1
    instructions.add(ReilHelpers.createSub(offset + 2, OperandSize.BYTE, rotateMask,
        OperandSize.BYTE, "1", OperandSize.BYTE, rotateMaskLessOne));
    instructions.add(ReilHelpers.createBisz(offset + 3, OperandSize.BYTE, rotateMaskLessOne,
        OperandSize.BYTE, rotateMaskOne));

    // Rotating through the carry flag is like rotating through a 33 bit register
    // For rotating leftwards, the CF must be added at the MSB of the 32 bit register
    instructions.add(ReilHelpers.createBsh(offset + 4, OperandSize.BYTE, Helpers.CARRY_FLAG,
        sourceSize, os, resultSize, shiftedCf));
    instructions.add(ReilHelpers.createOr(offset + 5, targetSize, targetRegister, resultSize,
        shiftedCf, resultSize, realOp1));

    // Perform the rotate
    // y = ( x << n ) | ( x >> ( ( regsize + 1 ) - n ) )
    instructions.add(ReilHelpers.createBsh(offset + 6, resultSize, realOp1, OperandSize.BYTE,
        rotateMask, resultSize, shledResult));
    instructions.add(ReilHelpers.createAdd(offset + 7, OperandSize.BYTE, "-" + modVal,
        OperandSize.BYTE, rotateMask, OperandSize.BYTE, shrValue));
    instructions.add(ReilHelpers.createBsh(offset + 8, resultSize, realOp1, OperandSize.BYTE,
        shrValue, resultSize, shredResult));
    instructions.add(ReilHelpers.createOr(offset + 9, resultSize, shledResult, resultSize,
        shredResult, resultSize, result));

    // Truncate the result
    instructions.add(ReilHelpers.createAnd(offset + 10, resultSize, result, targetSize, maskSize,
        targetSize, truncatedResult));

    // Don't change the flags if the rotate value was zero
    final String jmpGoal =
        String.format("%d.%d", instruction.getAddress().toLong(), linesBefore + 20);
    instructions.add(ReilHelpers.createJcc(offset + 11, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal));

    // Set the CF to the MSB of the untruncated result
    instructions.add(ReilHelpers.createAnd(offset + 12, resultSize, result, resultSize, carryMask,
        resultSize, msbResult));
    instructions.add(ReilHelpers.createBsh(offset + 13, resultSize, msbResult, resultSize,
        "-" + os, OperandSize.BYTE, Helpers.CARRY_FLAG));

    // The OF needs to be set to a different value if the rotate-mask was 1
    final String jmpGoal2 =
        String.format("%d.%d", instruction.getAddress().toLong(), linesBefore + 17);
    instructions.add(ReilHelpers.createJcc(offset + 14, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal2));

    // Set the OF to undefined if the rotate-mask was positive but not 1
    instructions.add(ReilHelpers.createUndef(offset + 15, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Jump to the end
    final String jmpGoal3 =
        String.format("%d.%d", instruction.getAddress().toLong(), linesBefore + 20);
    instructions.add(ReilHelpers.createJcc(offset + 16, OperandSize.BYTE, rotateMaskZero,
        OperandSize.ADDRESS, jmpGoal3));

    // OF = MSB(DEST) XOR CF
    instructions.add(ReilHelpers.createAnd(offset + 17, sourceSize, truncatedResult, sourceSize,
        msbMask, sourceSize, tempOf));
    instructions.add(ReilHelpers.createBsh(offset + 18, sourceSize, tempOf, sourceSize,
        shiftMsbLsb, OperandSize.BYTE, tempOfLsb));
    instructions.add(ReilHelpers.createBsh(offset + 19, OperandSize.BYTE, tempOfLsb,
        OperandSize.BYTE, Helpers.CARRY_FLAG, OperandSize.BYTE, Helpers.CARRY_FLAG));

    Helpers.writeBack(environment, offset + 20, targetOperand, truncatedResult,
        targetResult.getSize(), targetResult.getAddress(), targetResult.getType(), instructions);
  }
}
