// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

/**
 * Translates SAR instructions to REIL code.
 */
public class SarTranslator implements IInstructionTranslator {
  /**
   * Translates a SAR instruction to REIL code by first performing an unsigned division and then
   * (possibly) correcting the result afterwards.
   *
   * @param environment A valid translation environment.
   * @param instruction The SAR instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SAR instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sar");
    Preconditions.checkArgument(instruction.getOperands().size() == 2,
        "Error: Argument instruction is not a sar instruction (invalid number of operands)");

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

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(sourceSize));

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(sourceSize));
    final String modValue = String.valueOf(targetSize.getBitSize());

    final String shiftMask = environment.getNextVariableString();
    final String shiftMaskZero = environment.getNextVariableString();
    final String shiftMaskLessOne = environment.getNextVariableString();
    final String shiftMaskOne = environment.getNextVariableString();
    final String shiftMaskNeg = environment.getNextVariableString();
    final String result = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String isPositive = environment.getNextVariableString();
    final String divisor = environment.getNextVariableString();
    final String divisionResult = environment.getNextVariableString();
    final String negateMask = environment.getNextVariableString();
    final String twoComplementResult = environment.getNextVariableString();
    final String shiftBit = environment.getNextVariableString();
    final String shiftedBitsMask = environment.getNextVariableString();
    final String shiftedBits = environment.getNextVariableString();
    final String shiftedBitsZero = environment.getNextVariableString();
    final String shiftedBitsNonZero = environment.getNextVariableString();
    final String shiftAmountMinOne = environment.getNextVariableString();
    final String isNegative = environment.getNextVariableString();
    final String roundTowNegInf = environment.getNextVariableString();
    final String cfBitMask = environment.getNextVariableString();
    final String cfBitResult = environment.getNextVariableString();
    final String tmpCf = environment.getNextVariableString();

    // Generate the unsigned value of the shift value
    // Note: this code must not be moved further down, otherwise all the offset become invalid
    final Pair<String, String> targetRegister1Abs =
        Helpers.generateAbs(environment, offset, targetRegister, targetSize, instructions);
    final String targetRegister1Absolute = targetRegister1Abs.second();
    offset = baseOffset + instructions.size();

    // Since the Reil infrastructure lacks ability to establish branches between instructions
    // objects in (as opposed to raw offsets) we need this hack to correct the jump target offsets
    // The reason is that the are already some instructions in the list so, e.g., createMod(offset +
    // 1) does not actually create an instruction at offset 1 but at offset delta + 1
    final int delta = instructions.size();

    // Make sure to shift less than the size of the target register
    instructions.add(ReilHelpers.createMod(offset, sourceSize, sourceRegister, targetSize,
        modValue, targetSize, shiftMask));

    // Find out if the shift mask is zero or non-zero
    instructions.add(ReilHelpers.createBisz(offset + 1, targetSize, shiftMask, OperandSize.BYTE,
        shiftMaskZero));

    // Bail out if shift count is zero
    final String jmpEnd = String.format("%s.%s", instruction.getAddress().toLong(), delta + 39);
    instructions.add(ReilHelpers.createJcc(offset + 2, OperandSize.BYTE, shiftMaskZero,
        OperandSize.ADDRESS, jmpEnd));

    // AF is undefined if shift count is non-zero
    instructions.add(ReilHelpers.createUndef(offset + 3, OperandSize.BYTE, Helpers.AUXILIARY_FLAG));

    // The carry flag corresponds to the last bit shifted out of the operand
    instructions.add(ReilHelpers.createSub(offset + 4, OperandSize.BYTE, shiftMask,
        OperandSize.BYTE, "1", OperandSize.BYTE, shiftAmountMinOne));
    instructions.add(ReilHelpers.createBsh(offset + 5, OperandSize.BYTE, "1", OperandSize.BYTE,
        shiftAmountMinOne, OperandSize.BYTE, cfBitMask));
    instructions.add(ReilHelpers.createAnd(offset + 6, targetSize, targetRegister,
        OperandSize.BYTE, cfBitMask, OperandSize.BYTE, cfBitResult));
    instructions.add(ReilHelpers.createBisz(offset + 7, OperandSize.BYTE, cfBitResult,
        OperandSize.BYTE, tmpCf));
    instructions.add(ReilHelpers.createXor(offset + 8, OperandSize.BYTE, "1", OperandSize.BYTE,
        tmpCf, OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Find out if the shift mask is 1
    instructions.add(ReilHelpers.createSub(offset + 9, targetSize, "1", targetSize, shiftMask,
        targetSize, shiftMaskLessOne));
    instructions.add(ReilHelpers.createBisz(offset + 10, targetSize, shiftMaskLessOne,
        OperandSize.BYTE, shiftMaskOne));

    // Negate the shift-mask so we can perform a right shift
    instructions.add(ReilHelpers.createSub(offset + 11, targetSize, "0", targetSize, shiftMask,
        targetSize, shiftMaskNeg));

    // Test if the highest bit of the input value was set; if so, we need to perform a sign
    // extension on the shift result
    instructions.add(ReilHelpers.createAnd(offset + 12, sourceSize, msbMask, targetSize,
        targetRegister, sourceSize, msbResult));
    instructions.add(ReilHelpers.createBisz(offset + 13, sourceSize, msbResult, OperandSize.BYTE,
        isPositive));
    instructions.add(ReilHelpers.createXor(offset + 14, OperandSize.BYTE, "1", OperandSize.BYTE,
        isPositive, OperandSize.BYTE, isNegative));

    // Create divisor based on shift amount and calculate unsigned division
    instructions.add(ReilHelpers.createBsh(offset + 15, OperandSize.DWORD, "1", OperandSize.BYTE,
        shiftMask, OperandSize.DWORD, divisor));
    instructions.add(ReilHelpers.createDiv(offset + 16, targetSize, targetRegister1Absolute,
        OperandSize.DWORD, divisor, OperandSize.DWORD, divisionResult));

    // If the MSB of the value to be shifted is set, we create a mask of 0xFFFFFFFF to convert the
    // result to two's complement
    instructions.add(ReilHelpers.createSub(offset + 17, OperandSize.BYTE, "0", OperandSize.BYTE,
        isNegative, OperandSize.DWORD, negateMask));
    instructions.add(ReilHelpers.createXor(offset + 18, OperandSize.DWORD, divisionResult,
        OperandSize.DWORD, negateMask, OperandSize.DWORD, result));

    // If the source value is positive we need to skip adding one to the result
    final String jmpSkipTwosComplement =
        String.format("%s.%s", instruction.getAddress().toLong(), delta + 28);
    instructions.add(ReilHelpers.createJcc(offset + 19, OperandSize.BYTE, isPositive,
        OperandSize.ADDRESS, jmpSkipTwosComplement));

    // Convert to two's complement by adding one to the result
    instructions.add(ReilHelpers.createAdd(offset + 20, OperandSize.DWORD, result,
        OperandSize.BYTE, "1", OperandSize.DWORD, twoComplementResult));

    // We need to subtract one to realize "rounding towards infinity" iff the bits shifted out are
    // not all zeros
    // First we need to mask out all the bits which are shifted out on the right side
    instructions.add(ReilHelpers.createBsh(offset + 21, OperandSize.BYTE, "1", targetSize,
        shiftMask, targetSize, shiftBit));
    instructions.add(ReilHelpers.createSub(offset + 22, targetSize, shiftBit, OperandSize.BYTE,
        "1", targetSize, shiftedBitsMask));
    instructions.add(ReilHelpers.createAnd(offset + 23, targetSize, targetRegister, targetSize,
        shiftedBitsMask, targetSize, shiftedBits));

    // Possibly subtract one from the result, i.e. round towards negative infinity
    instructions.add(ReilHelpers.createBisz(offset + 24, targetSize, shiftedBits, OperandSize.BYTE,
        shiftedBitsZero));
    instructions.add(ReilHelpers.createXor(offset + 25, OperandSize.BYTE, "1", OperandSize.BYTE,
        shiftedBitsZero, OperandSize.BYTE, shiftedBitsNonZero));
    instructions.add(ReilHelpers.createAnd(offset + 26, OperandSize.BYTE, isNegative,
        OperandSize.BYTE, shiftedBitsNonZero, OperandSize.BYTE, roundTowNegInf));
    instructions.add(ReilHelpers.createSub(offset + 27, OperandSize.DWORD, twoComplementResult,
        OperandSize.BYTE, roundTowNegInf, targetSize, result));

    // Truncate the result to the correct size (skip two complement conversion jump target)
    instructions.add(ReilHelpers.createAnd(offset + 28, resultSize, result, sourceSize,
        truncateMask, sourceSize, truncatedResult));

    // Don't change the flags if the shift value was zero
    final String jmpGoal = String.format("%s.%s", instruction.getAddress().toLong(), delta + 39);
    instructions.add(ReilHelpers.createJcc(offset + 29, OperandSize.BYTE, shiftMaskZero,
        OperandSize.ADDRESS, jmpGoal));

    // The SF is always 0
    instructions.add(ReilHelpers.createBisz(offset + 30, OperandSize.BYTE, isPositive,
        OperandSize.BYTE, Helpers.SIGN_FLAG));

    // Set the ZF
    instructions.add(ReilHelpers.createBisz(offset + 31, sourceSize, truncatedResult,
        OperandSize.BYTE, Helpers.ZERO_FLAG));

    // If shift count is one, we need to zero the OF
    final String jmpDontZeroOF =
        String.format("%s.%s", instruction.getAddress().toLong(), delta + 34);
    instructions.add(ReilHelpers.createJcc(offset + 32, OperandSize.BYTE, shiftMaskOne,
        OperandSize.ADDRESS, jmpDontZeroOF));
    instructions.add(ReilHelpers.createStr(offset + 33, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.OVERFLOW_FLAG));

    // Set the OF to undefined if the shift-mask was not zero and not one
    final String shiftCountZeroOrOne = environment.getNextVariableString();
    instructions.add(ReilHelpers.createOr(offset + 34, OperandSize.BYTE, shiftMaskOne,
        OperandSize.BYTE, shiftMaskZero, OperandSize.BYTE, shiftCountZeroOrOne));
    final String jmpSkipUndefOF =
        String.format("%s.%s", instruction.getAddress().toLong(), delta + 38);
    instructions.add(ReilHelpers.createJcc(offset + 35, OperandSize.BYTE, shiftCountZeroOrOne,
        OperandSize.ADDRESS, jmpSkipUndefOF));
    instructions.add(ReilHelpers.createUndef(offset + 36, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));
    // always jump to writeback instruction if OF was undefined
    final String jmpGoal3 = String.format("%s.%s", instruction.getAddress().toLong(), delta + 39);
    instructions.add(ReilHelpers.createJcc(offset + 37, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        jmpGoal3));

    // OF is always zero for the SAR instruction
    instructions.add(ReilHelpers.createStr(offset + 38, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.OVERFLOW_FLAG));

    final int sizeBefore = instructions.size();
    Helpers.writeBack(environment, offset + 39, targetOperand, result, targetSize,
        targetResult.getAddress(), targetResult.getType(), instructions);
    final int sizeAfter = instructions.size();

    instructions.add(ReilHelpers.createNop((sizeAfter - sizeBefore - 1) + offset + 40));
  }
}
