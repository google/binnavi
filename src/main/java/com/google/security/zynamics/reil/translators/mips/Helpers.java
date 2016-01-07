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
package com.google.security.zynamics.reil.translators.mips;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

public class Helpers {
  private static int registerSize = 32;

  private static void generateDelayBranchInternal(final List<ReilInstruction> instructions,
      final long baseOffset,
      final OperandSize conditionSize,
      final String conditionOperand,
      final OperandSize targetSize,
      final IOperandTreeNode targetOperand,
      final String[] meta) {
    long offset = baseOffset;

    if (targetOperand.getType() != ExpressionType.REGISTER) {
      instructions.add(
          ReilHelpers.createStr(offset++, conditionSize, conditionOperand, conditionSize, "t254"));
      instructions.add(ReilHelpers.createJcc(offset,
          conditionSize,
          "t254",
          targetSize,
          targetOperand.getValue(),
          meta));
    } else {
      instructions.add(ReilHelpers.createStr(offset++, targetSize, targetOperand.getValue(),
          targetSize, "t255"));
      instructions.add(
          ReilHelpers.createStr(offset++, conditionSize, conditionOperand, conditionSize, "t254"));
      instructions.add(
          ReilHelpers.createJcc(offset, conditionSize, "t254", targetSize, "t255", meta));
    }
  }

  /**
   * Generates code that generates sign masks for values
   *
   * @param environment A valid translation environment
   * @param offset The next unused REIL offset
   * @param value The value in question
   * @param size The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return A pair that contains the MSB of the value shifted into the LSB and the register that
   *         contains the generated sign mask
   */
  private static Pair<String, String> generateSignMask(final ITranslationEnvironment environment,
      final long offset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) {

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));

    final String maskedMsb = environment.getNextVariableString();
    final String msbInLsb = environment.getNextVariableString();
    final String signMask = environment.getNextVariableString();

    // Extract the sign
    instructions.add(ReilHelpers.createAnd(offset, size, value, size, msbMask, size, maskedMsb));

    // Shift the sign into the LSB
    instructions.add(
        ReilHelpers.createBsh(offset + 1, size, maskedMsb, size, shiftValue, size, msbInLsb));

    // Calculate 0 - sign
    instructions.add(ReilHelpers.createSub(offset + 2, size, "0", size, msbInLsb, size, signMask));

    return new Pair<String, String>(msbInLsb, signMask);
  }

  /**
   *
   * @param environment A valid translation environment
   * @param baseOffset The next unused REIL offset
   * @param value The value in question
   * @param size The size of the value
   * @param instructions A list of REIL instructions where the new REIL code is added
   *
   * @return A pair that contains the MSB of the value shifted into the LSB and the register that
   *         contains the generated absolute value
   *
   * @throws IllegalArgumentException Thrown if any of the arguments are invalid
   */
  public static Pair<String, String> generateAbs(final ITranslationEnvironment environment,
      final long baseOffset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) throws IllegalArgumentException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(value, "Error: Argument value can't be null");
    Preconditions.checkNotNull(size, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final int oldInstructionsSize = instructions.size();

    final Pair<String, String> signMask =
        generateSignMask(environment, baseOffset, value, size, instructions);

    final long offset = (baseOffset + instructions.size()) - oldInstructionsSize;

    final String toggledSign = environment.getNextVariableString();
    final String absValue = environment.getNextVariableString();

    instructions.add(
        ReilHelpers.createXor(offset, size, value, size, signMask.second(), size, toggledSign));
    instructions.add(ReilHelpers.createSub(offset + 1,
        size,
        toggledSign,
        size,
        signMask.second(),
        size,
        absValue));

    return new Pair<String, String>(signMask.first(), absValue);
  }

  /**
   * generates the REIL instructions for a branch with a delay slot as found in the MIPS32
   * architecture.
   *
   * @param instructions
   * @param baseOffset
   * @param conditionSize
   * @param conditionOperand
   * @param targetSize
   * @param targetOperand
   */
  public static void generateDelayBranch(final List<ReilInstruction> instructions,
      final long baseOffset,
      final OperandSize conditionSize,
      final String conditionOperand,
      final OperandSize targetSize,
      final IOperandTreeNode targetOperand) {
    generateDelayBranchInternal(instructions,
        baseOffset,
        conditionSize,
        conditionOperand,
        targetSize,
        targetOperand,
        new String[] {"branch_delay", "true"});
  }

  public static void generateDelayBranchLikely(final List<ReilInstruction> instructions,
      final long baseOffset,
      final OperandSize conditionSize,
      final String conditionOperand,
      final OperandSize targetSize,
      final IOperandTreeNode targetOperand) {
    generateDelayBranchInternal(instructions,
        baseOffset,
        conditionSize,
        conditionOperand,
        targetSize,
        targetOperand,
        new String[] {"branch_delay_true", "true"});
  }

  public static int getRegisterSize() {
    return registerSize;
  }

  public static String getTRegister() {
    return "$t8";
  }

  public static void setRegisterSize(final int size) {
    registerSize = size;
  }

  /**
   * perform signed multiplication
   *
   * @param environment
   * @param instructions
   * @param firstOperand is limited from 1 to 32 Bits
   * @param secondOperand is limited from 1 to 32 Bits
   */
  public static void signedMul(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final OperandSize firstOperandSize,
      final String firstOperand,
      final OperandSize secondOperandSize,
      final String secondOperand,
      final OperandSize resultOperandSize,
      final String resultOperand) {

    final String xoredResult = environment.getNextVariableString();
    final String multResult = environment.getNextVariableString();
    final String toggleMask = environment.getNextVariableString();
    final String xoredSigns = environment.getNextVariableString();

    long baseOffset = offset;

    // get the absolute Value of the operands
    final Pair<String, String> abs1 =
        generateAbs(environment, baseOffset, firstOperand, firstOperandSize, instructions);
    final Pair<String, String> abs2 =
        generateAbs(environment, baseOffset, secondOperand, secondOperandSize, instructions);

    final String firstAbs = abs1.second();
    final String secondAbs = abs2.second();

    // compute the multiplication
    instructions.add(ReilHelpers.createMul(baseOffset++,
        firstOperandSize,
        firstAbs,
        secondOperandSize,
        secondAbs,
        resultOperandSize,
        multResult));

    // Find out if the two operands had different signs and adjust the result accordingly
    instructions.add(ReilHelpers.createXor(baseOffset++,
        firstOperandSize,
        abs1.first(),
        secondOperandSize,
        abs2.first(),
        firstOperandSize,
        xoredSigns));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        firstOperandSize,
        "0",
        firstOperandSize,
        xoredSigns,
        resultOperandSize,
        toggleMask));
    instructions.add(ReilHelpers.createXor(baseOffset++,
        resultOperandSize,
        toggleMask,
        resultOperandSize,
        multResult,
        resultOperandSize,
        xoredResult));
    instructions.add(ReilHelpers.createAdd(baseOffset,
        resultOperandSize,
        xoredResult,
        firstOperandSize,
        xoredSigns,
        resultOperandSize,
        resultOperand));
  }

  /**
   * Signed subtraction
   *
   * @param environment
   * @param instructions
   * @param firstOperand
   * @param secondOperand
   * @param resultOperand
   * @param trueResult
   */
  public static void signedSub(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String firstOperand,
      final String secondOperand,
      final String resultOperand,
      final String trueResult) {

    final String tmpResult = environment.getNextVariableString();
    final String twoComplementfirstOperand = environment.getNextVariableString();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;
    final OperandSize bt = OperandSize.BYTE;

    long baseOffset = offset;

    // perform actual subtraction in the 2's complement !rA + rB + 1
    instructions.add(ReilHelpers.createXor(baseOffset++,
        dw,
        firstOperand,
        dw,
        String.valueOf(0xFFFFFFFFL),
        dw,
        twoComplementfirstOperand));
    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        twoComplementfirstOperand,
        dw,
        secondOperand,
        qw,
        tmpResult));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, qw, tmpResult, bt, String.valueOf(1L), qw, trueResult));
    instructions.add(ReilHelpers.createAnd(baseOffset,
        qw,
        trueResult,
        dw,
        String.valueOf(0xFFFFFFFFL),
        dw,
        resultOperand));
  }
}
