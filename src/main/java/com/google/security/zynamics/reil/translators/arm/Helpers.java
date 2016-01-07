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
package com.google.security.zynamics.reil.translators.arm;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

public final class Helpers {
  private Helpers() {

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
      long offset, final String value, final OperandSize size,
      final List<ReilInstruction> instructions) {

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));

    final String maskedMsb = environment.getNextVariableString();
    final String msbInLsb = environment.getNextVariableString();
    final String signMask = environment.getNextVariableString();

    // Extract the sign
    instructions.add(ReilHelpers.createAnd(offset++, size, value, size, msbMask, size, maskedMsb));

    // Shift the sign into the LSB
    instructions.add(
        ReilHelpers.createBsh(offset++, size, maskedMsb, size, shiftValue, size, msbInLsb));

    // Calculate 0 - sign
    instructions.add(ReilHelpers.createSub(offset++, size, "0", size, msbInLsb, size, signMask));

    return new Pair<String, String>(msbInLsb, signMask);
  }

  public static Pair<OperandSize, String> absValue(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String firstOperand,
      final OperandSize size) throws InternalTranslationException {
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    long baseOffset = offset;

    final String secondOperand =
        shiftRightSigned(baseOffset, environment, instructions, size, firstOperand, -31);

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        size,
        firstOperand,
        size,
        secondOperand,
        size,
        tmpVar1));
    instructions.add(
        ReilHelpers.createXor(baseOffset++, size, tmpVar1, size, secondOperand, size, tmpVar2));

    return new Pair<OperandSize, String>(size, tmpVar2);
  }

  /**
   * @param offset
   * @param environment A valid REIL translation environment
   * @param instructions A list of REIL instructions where the new REIL code is added
   * @param firstOperand A String containing the first Operand witch was added
   * @param secondOperand A String containing the second Operand witch was added
   * @param resultOperand A String holding the result of the addition
   * @param overflow A String which is set to 1 if Overflow has occurred
   * @param size A long holding the size for overflow calculation
   */
  public static void addOverflow(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final OperandSize firstOperandSize,
      final String firstOperand,
      final OperandSize secondOperandSize,
      final String secondOperand,
      final OperandSize resultOperandSize,
      final String resultOperand,
      final String overflow,
      final long size) throws IllegalArgumentException {

    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(firstOperand, "Error: Argument firstOperand can't be null");
    Preconditions.checkNotNull(secondOperand, "Error: Argument secondOperand can't be null");
    Preconditions.checkNotNull(resultOperand, "Error: Argument resultOperand can't be null");
    Preconditions.checkArgument(size != 0, "Error: Argument size can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    final String msbVara = environment.getNextVariableString();
    final String msbVarb = environment.getNextVariableString();
    final String msbVarr = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    final String shiftValue = "-" + String.valueOf(size - 1);

    long baseOffset = offset;

    // Isolate summands msb's
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        firstOperandSize,
        firstOperand,
        wd,
        shiftValue,
        bt,
        msbVara));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        secondOperandSize,
        secondOperand,
        wd,
        shiftValue,
        bt,
        msbVarb));

    // Isolate MSB(Result)
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        resultOperandSize,
        resultOperand,
        wd,
        shiftValue,
        bt,
        msbVarr));

    // clean leftovers
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVara, bt, String.valueOf(1), bt, msbVara));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVarb, bt, String.valueOf(1), bt, msbVarb));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVarr, bt, String.valueOf(1), bt, msbVarr));

    // perform overflow calculation ( msbA XOR msbR ) AND ( msbB XOR msbR ) == OF
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVara, bt, msbVarr, bt, tmpVar4));
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVarb, bt, msbVarr, bt, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, bt, tmpVar3, bt, overflow));
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

    long offset = (baseOffset + instructions.size()) - oldInstructionsSize;

    final String toggledSign = environment.getNextVariableString();
    final String absValue = environment.getNextVariableString();

    instructions.add(
        ReilHelpers.createXor(offset++, size, value, size, signMask.second(), size, toggledSign));
    instructions.add(ReilHelpers.createSub(offset++,
        size,
        toggledSign,
        size,
        signMask.second(),
        size,
        absValue));

    return new Pair<String, String>(signMask.first(), absValue);
  }

  /**
   * Generate CRM Mask
   */
  public static String getCRM(final int crm) {
    Long mask = 0L;
    for (int i = 0; i < 8; i++) {
      mask = (mask << 4);
      if (((crm >> (7 - i)) & 1) == 1) {
        mask |= 15;
      }
    }
    return mask.toString();
  }

  /**
   * Get the CR register index
   *
   * @param register as "cr5"
   * @return index as 5
   */
  public static int getCRRegisterIndex(final String register) {
    Integer retval = 0;
    try {
      retval = Integer.decode(register);
    } catch (final NumberFormatException e) {
      final String registerNumber = register.substring(2);
      retval = Integer.decode(registerNumber);
    }
    return retval;
  }

  /**
   * Get the register index
   *
   * @param register
   * @return returns the register index.
   */
  public static int getRegisterIndex(final String register) {
    if (register.equals("SP")) {
      return 13;
    } else if (register.equals("LR")) {
      return 14;
    } else if (register.equals("PC")) {
      return 15;
    } else {
      return Integer.decode(register.substring(1));
    }
  }

  /**
   * Generate Rotate Mask
   */
  public static String getRotateMask(final String maskMB, final String maskME) {
    Long mask = 0L;
    final int mb = Integer.decode(maskMB);
    final int me = Integer.decode(maskME);
    if ((mb == 0) && (me == 31)) {
      return String.valueOf(0xFFFFFFFFL);
    }
    for (int i = mb; i != ((me + 1) % 32); i = (i + 1) % 32) {
      mask |= ((mask >> (31 - i)) | 1) << (31 - i);
    }
    return mask.toString();
  }

  public static String highestNegativeValue(final long bits) {
    return String.valueOf(((0x1L << bits) ^ 0xFFFFFFFFL) + 1);
  }

  /**
   * Negative Bitmask function for creating 0xFFFF8000 style masks
   */
  public static String negBitMask(final long bits) {
    return String.valueOf((0xFFFFFFFFL << bits) & 0xFFFFFFFFL);
  }

  /**
   * overflow condition for unsigned operations
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperand
   */
  public static void overflowCondition(final long offset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String firstOperand) {

    final OperandSize qw = OperandSize.QWORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    final String isNotOverflowed = environment.getNextVariableString();
    final String tmpVar7 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        qw,
        firstOperand,
        dw,
        String.valueOf(-32),
        dw,
        tmpVar7));
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, tmpVar7, bt, isNotOverflowed));
    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, isNotOverflowed, bt, "Q"));
  }

  /**
   * Mask function for creating 0x7f masks
   */
  public static String posBitMask(final long bits) {
    return String.valueOf(((0xFFFFFFFFL << bits) & 0xFFFFFFFFL) ^ 0xFFFFFFFFL);
  }

  public static String reverseUnsignedInteger(final ITranslationEnvironment environment,
      final long offset, final String inputRegister, final List<ReilInstruction> instructions) {
    /**
     * x = (x & 0x55555555) << 1 | (x >> 1) & 0x55555555 x = (x & 0x33333333) << 2 | (x >> 2) &
     * 0x33333333 x = (x & 0x0F0F0F0F) << 4 | (x >> 4) & 0x0F0F0F0F x = (x << 24) | ((x & 0xFF00 )
     * << 8) | ((x >> 8) & 0xFF00 | (x >> 24)
     */

    long baseOffset = offset;

    final OperandSize dw = OperandSize.DWORD;

    final String tempVar10 = environment.getNextVariableString();
    final String tempVar11 = environment.getNextVariableString();
    final String tempVar12 = environment.getNextVariableString();
    final String tempVar13 = environment.getNextVariableString();
    final String tempVar1x = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        inputRegister,
        dw,
        String.valueOf(0x55555555L),
        dw,
        tempVar10));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar10, dw, String.valueOf(1L), dw, tempVar11));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        inputRegister,
        dw,
        String.valueOf(-1L),
        dw,
        tempVar12));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar12,
        dw,
        String.valueOf(0x55555555L),
        dw,
        tempVar13));
    instructions.add(
        ReilHelpers.createOr(baseOffset++, dw, tempVar11, dw, tempVar13, dw, tempVar1x));

    final String tempVar20 = environment.getNextVariableString();
    final String tempVar21 = environment.getNextVariableString();
    final String tempVar22 = environment.getNextVariableString();
    final String tempVar23 = environment.getNextVariableString();
    final String tempVar2x = environment.getNextVariableString();
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar1x,
        dw,
        String.valueOf(0x33333333L),
        dw,
        tempVar20));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar20, dw, String.valueOf(2L), dw, tempVar21));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar1x, dw, String.valueOf(-2L), dw, tempVar22));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar22,
        dw,
        String.valueOf(0x33333333L),
        dw,
        tempVar23));
    instructions.add(
        ReilHelpers.createOr(baseOffset++, dw, tempVar21, dw, tempVar23, dw, tempVar2x));

    final String tempVar30 = environment.getNextVariableString();
    final String tempVar31 = environment.getNextVariableString();
    final String tempVar32 = environment.getNextVariableString();
    final String tempVar33 = environment.getNextVariableString();
    final String tempVar3x = environment.getNextVariableString();
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar2x,
        dw,
        String.valueOf(0x0F0F0F0FL),
        dw,
        tempVar30));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar30, dw, String.valueOf(4L), dw, tempVar31));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar2x, dw, String.valueOf(-4L), dw, tempVar32));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar32,
        dw,
        String.valueOf(0x0F0F0F0FL),
        dw,
        tempVar33));
    instructions.add(
        ReilHelpers.createOr(baseOffset++, dw, tempVar31, dw, tempVar33, dw, tempVar3x));

    final String tempVar40 = environment.getNextVariableString();
    final String tempVar41 = environment.getNextVariableString();
    final String tempVar42 = environment.getNextVariableString();
    final String tempVar4x = environment.getNextVariableString();
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar3x, dw, String.valueOf(24), dw, tempVar40));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar3x,
        dw,
        String.valueOf(0xFF00L),
        dw,
        tempVar41));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar41, dw, String.valueOf(8L), dw, tempVar42));
    instructions.add(
        ReilHelpers.createOr(baseOffset++, dw, tempVar42, dw, tempVar40, dw, tempVar4x));

    final String tempVar50 = environment.getNextVariableString();
    final String tempVar51 = environment.getNextVariableString();
    final String tempVar52 = environment.getNextVariableString();
    final String tempVar5x = environment.getNextVariableString();
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, dw, tempVar3x, dw, String.valueOf(-8L), dw, tempVar50));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        tempVar50,
        dw,
        String.valueOf(0xFF00L),
        dw,
        tempVar51));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        tempVar3x,
        dw,
        String.valueOf(-24L),
        dw,
        tempVar52));
    instructions.add(
        ReilHelpers.createOr(baseOffset++, dw, tempVar51, dw, tempVar52, dw, tempVar5x));

    final String result = environment.getNextVariableString();
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tempVar4x, dw, tempVar5x, dw, result));

    return result;
  }

  public static String shiftRightSigned(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final OperandSize size,
      final String operandValue,
      final int shiftValue) throws InternalTranslationException {
    if (shiftValue > 0) {
      throw new InternalTranslationException("right shift needs negative shiftvalue");
    }

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        size,
        operandValue,
        size,
        String.valueOf(0x80000000L),
        size,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        size,
        tmpVar1,
        size,
        String.valueOf(shiftValue),
        size,
        tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        size,
        String.valueOf(0x80000000L),
        size,
        String.valueOf(shiftValue),
        size,
        tmpVar3));
    instructions.add(
        ReilHelpers.createSub(baseOffset++, size, tmpVar2, size, tmpVar3, size, tmpVar4));

    return tmpVar4;
  }

  /**
   * perform signed multiplication
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperandSize
   * @param firstOperand is limited from 1 to 32 Bits
   * @param secondOperand is limited from 1 to 32 Bits
   * @param resultOperandSize
   * @param resultOperand
   */
  public static void signedMul(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
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
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    final Pair<String, String> abs2 =
        generateAbs(environment, baseOffset, secondOperand, secondOperandSize, instructions);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

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
    instructions.add(ReilHelpers.createAdd(baseOffset++,
        resultOperandSize,
        xoredResult,
        firstOperandSize,
        xoredSigns,
        resultOperandSize,
        resultOperand));
  }

  /**
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperand
   * @param secondOperand
   * @param resultOperand
   * @param operation
   * @param tmpResultVar
   * @param size
   * @param signedDoesSat
   */
  public static void signedSat(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
      final List<ReilInstruction> instructions,
      final OperandSize firstOperandSize,
      final String firstOperand,
      final OperandSize secondOperandSize,
      final String secondOperand,
      final OperandSize resultOperandSize,
      final String resultOperand,
      final String operation,
      final String tmpResultVar,
      final long size,
      final String signedDoesSat) {

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    final String signedSatMaskLess = posBitMask(size - 1);
    final String lowSatResult = highestNegativeValue(size - 1);
    final String highSatResult = signedSatMaskLess;

    final String inRange = environment.getNextVariableString();
    final String inRangeMask = environment.getNextVariableString();
    final String isGreaterCondition = environment.getNextVariableString();
    final String isGreaterMask = environment.getNextVariableString();
    final String isLessCondition = environment.getNextVariableString();
    final String isLessMask = environment.getNextVariableString();
    final String tmpLowResult1 = environment.getNextVariableString();
    final String tmpLowResult2 = environment.getNextVariableString();
    final String tmpLowResult3 = environment.getNextVariableString();
    final String tmpLowResult4 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String overflow = environment.getNextVariableString();
    final String isNegative = environment.getNextVariableString();
    final String isPositive = environment.getNextVariableString();

    long baseOffset = offset;

    final String addOperation = "ADD";

    if ((operation.equalsIgnoreCase(addOperation)) || (operation.equalsIgnoreCase("SUB"))) {
      if (operation.equalsIgnoreCase(addOperation)) {
        addOverflow(baseOffset,
            environment,
            instructions,
            firstOperandSize,
            firstOperand,
            secondOperandSize,
            secondOperand,
            resultOperandSize,
            resultOperand,
            overflow,
            size);
      } else if (operation.equalsIgnoreCase("SUB")) {
        subOverflow(baseOffset,
            environment,
            instruction,
            instructions,
            firstOperandSize,
            firstOperand,
            secondOperandSize,
            secondOperand,
            resultOperandSize,
            resultOperand,
            overflow,
            size);
      }

      // extract the sign of the result to see which way to overflow
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          resultOperand,
          dw,
          "-" + String.valueOf(size - 1),
          bt,
          tmpVar1));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, bt, tmpVar1, bt, String.valueOf(1L), bt, isNegative));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, isNegative, bt, isPositive));

      // combine the results to get the true answer
      baseOffset++;
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, bt, isPositive, bt, overflow, bt, isLessCondition));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          bt,
          isNegative,
          bt,
          overflow,
          bt,
          isGreaterCondition));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, overflow, bt, inRange));

      // create low half masks
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0),
          bt,
          isLessCondition,
          dw,
          isLessMask));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0),
          bt,
          isGreaterCondition,
          dw,
          isGreaterMask));
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0), bt, inRange, dw, inRangeMask));

      // return the result
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          resultOperand,
          dw,
          inRangeMask,
          dw,
          tmpLowResult1));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, lowSatResult, dw, isLessMask, dw, tmpLowResult2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          highSatResult,
          dw,
          isGreaterMask,
          dw,
          tmpLowResult3));

      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult1,
          dw,
          tmpLowResult2,
          dw,
          tmpLowResult4));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult4,
          dw,
          tmpLowResult3,
          dw,
          tmpResultVar));
    } else
    /* sat without computation SSAT16 */
    {
      final String needsShiftCompare = environment.getNextVariableString();
      final String xMinusy = environment.getNextVariableString();
      final String xMinusyXorx = environment.getNextVariableString();
      final String xXory = environment.getNextVariableString();
      final String xXoryAndxMinusXorx = environment.getNextVariableString();
      final String lowOverflow = environment.getNextVariableString();
      final String highOverflow = environment.getNextVariableString();

      /*
       * -2(n-1) if X is < -2(n-1) X if -2(n-1) <= X <= 2(n-1) - 1 2(n-1) - 1 if X > 2(n-1) - 1
       */
      // ( x - y ) XOR [(x XOR y) AND ((x - y) XOR x)]
      // low overflow
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, firstOperand, dw, lowSatResult, dw, xMinusy));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, firstOperand, dw, lowSatResult, dw, xXory));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, xMinusy, dw, firstOperand, dw, xMinusyXorx));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, xXory, dw, xMinusyXorx, dw, xXoryAndxMinusXorx));
      instructions.add(ReilHelpers.createXor(baseOffset++,
          dw,
          xMinusy,
          dw,
          xXoryAndxMinusXorx,
          dw,
          needsShiftCompare));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          needsShiftCompare,
          dw,
          String.valueOf(-31L),
          bt,
          lowOverflow));

      // high overflow
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, highSatResult, dw, firstOperand, dw, xMinusy));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, highSatResult, dw, firstOperand, dw, xXory));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, xMinusy, dw, highSatResult, dw, xMinusyXorx));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, xXory, dw, xMinusyXorx, dw, xXoryAndxMinusXorx));
      instructions.add(ReilHelpers.createXor(baseOffset++,
          dw,
          xMinusy,
          dw,
          xXoryAndxMinusXorx,
          dw,
          needsShiftCompare));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          needsShiftCompare,
          dw,
          String.valueOf(-31L),
          bt,
          highOverflow));

      instructions.add(
          ReilHelpers.createOr(baseOffset++, bt, lowOverflow, bt, highOverflow, bt, overflow));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, overflow, bt, inRange));
      if (operation.equalsIgnoreCase("SSAT")) {
        // create low half masks
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            lowOverflow,
            dw,
            isLessMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            highOverflow,
            dw,
            isGreaterMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            inRange,
            dw,
            inRangeMask));
      } else {
        // create low half masks
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            highOverflow,
            dw,
            isLessMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            lowOverflow,
            dw,
            isGreaterMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            inRange,
            dw,
            inRangeMask));
      }

      // return the result
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          resultOperand,
          dw,
          inRangeMask,
          dw,
          tmpLowResult1));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, lowSatResult, dw, isLessMask, dw, tmpLowResult2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          highSatResult,
          dw,
          isGreaterMask,
          dw,
          tmpLowResult3));

      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult1,
          dw,
          tmpLowResult2,
          dw,
          tmpLowResult4));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult4,
          dw,
          tmpLowResult3,
          dw,
          tmpResultVar));
    }
  }

  /**
   * Signed subtraction
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperand
   * @param secondOperand
   * @param resultOperand
   * @param trueResult
   */
  public static void signedSub(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
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
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        qw,
        trueResult,
        dw,
        String.valueOf(0xFFFFFFFFL),
        dw,
        resultOperand));
  }

  /**
   * sign Extend 8,16,32 Bit Registers
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperand
   * @param resultOperand
   * @param size
   */
  public static void signExtend(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
      final List<ReilInstruction> instructions,
      final OperandSize firstOperandSize,
      final String firstOperand,
      final OperandSize resultOperandSize,
      final String resultOperand,
      final int size) {

    final OperandSize dw = OperandSize.DWORD;

    String fMask = "";
    String eightMask = "";

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    if (size == 8) {
      fMask = String.valueOf(0xFFL);
      eightMask = String.valueOf(0x80L);
    } else if (size == 16) {
      fMask = String.valueOf(0xFFFFL);
      eightMask = String.valueOf(0x8000L);
    } else
    /* size == 32 */
    {
      fMask = String.valueOf(0xFFFFFFFFL);
      eightMask = String.valueOf(0x80000000L);
    }

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        firstOperandSize,
        firstOperand,
        dw,
        eightMask,
        dw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, fMask, dw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dw,
        tmpVar2,
        dw,
        eightMask,
        resultOperandSize,
        resultOperand));
  }

  /**
   * overflow condition if operation was a subtraction
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperandSize TODO
   * @param firstOperand
   * @param secondOperandSize TODO
   * @param secondOperand
   * @param resultOperandSize TODO
   * @param resultOperand
   * @param overflow
   */
  public static void subOverflow(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
      final List<ReilInstruction> instructions,
      final OperandSize firstOperandSize,
      final String firstOperand,
      final OperandSize secondOperandSize,
      final String secondOperand,
      final OperandSize resultOperandSize,
      final String resultOperand,
      final String overflow,
      final long size) {

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    final String msbVara = environment.getNextVariableString();
    final String msbVarb = environment.getNextVariableString();
    final String msbVarr = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    final String shiftVal = "-" + String.valueOf(size - 1);

    long baseOffset = offset;

    // Isolate summands msb's
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        firstOperandSize,
        firstOperand,
        wd,
        shiftVal,
        bt,
        msbVara));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        secondOperandSize,
        secondOperand,
        wd,
        shiftVal,
        bt,
        msbVarb));

    // Isolate MSB(Result)
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        resultOperandSize,
        resultOperand,
        wd,
        shiftVal,
        bt,
        msbVarr));

    // perform clean up
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVara, bt, String.valueOf(1), bt, msbVara));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVarb, bt, String.valueOf(1), bt, msbVarb));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, bt, msbVarr, bt, String.valueOf(1), bt, msbVarr));

    // perform overflow calculation ( msbA XOR msbB ) AND ( msbA XOR msbR ) == OF
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVara, bt, msbVarb, bt, tmpVar4));
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVara, bt, msbVarr, bt, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, bt, tmpVar3, bt, overflow));
  }

  /**
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param xOperand
   * @param yOperand
   * @param isSmaller
   */
  public static void unsignedCompareXSmallerY(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
      final List<ReilInstruction> instructions,
      final OperandSize xOperandSize,
      final String xOperand,
      final OperandSize yOperandSize,
      final String yOperand,
      final OperandSize isSmallerSize,
      final String isSmaller) {
    final OperandSize dw = OperandSize.DWORD;

    final String notXandYOperand = environment.getNextVariableString();
    final String notXOperand = environment.getNextVariableString();
    final String notXorYOperand = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String xMinusYOperand = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createXor(baseOffset++,
        xOperandSize,
        xOperand,
        dw,
        String.valueOf(0xFFFFFFFFL),
        dw,
        notXOperand));

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dw,
        notXOperand,
        yOperandSize,
        yOperand,
        dw,
        notXandYOperand));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dw,
        notXOperand,
        yOperandSize,
        yOperand,
        dw,
        notXorYOperand));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        xOperandSize,
        xOperand,
        yOperandSize,
        yOperand,
        dw,
        xMinusYOperand));

    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, notXorYOperand, dw, xMinusYOperand, dw, tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++,
        dw,
        notXandYOperand,
        dw,
        tmpVar1,
        isSmallerSize,
        isSmaller));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        isSmallerSize,
        isSmaller,
        dw,
        String.valueOf(-31L),
        isSmallerSize,
        isSmaller));
  }

  /**
   * unsigned saturation
   *
   * @param offset
   * @param environment
   * @param instruction
   * @param instructions
   * @param firstOperand
   * @param secondOperand
   * @param resultOperand
   * @param operation
   * @param tmpResultVar
   * @param size
   * @param signedDoesSat
   */
  public static void unsignedSat(final long offset,
      final ITranslationEnvironment environment,
      final IInstruction instruction,
      final List<ReilInstruction> instructions,
      final String firstOperand,
      final String secondOperand,
      final String resultOperand,
      final String operation,
      final String tmpResultVar,
      final long size,
      final String signedDoesSat) {

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    final String lowSatResult = String.valueOf(0);
    final String highSatResult = String.valueOf((long) Math.pow(2, size) - 1);

    final String inRange = environment.getNextVariableString();
    final String inRangeMask = environment.getNextVariableString();
    final String isGreaterCondition = environment.getNextVariableString();
    final String isGreaterMask = environment.getNextVariableString();
    final String isLessCondition = environment.getNextVariableString();
    final String isLessMask = environment.getNextVariableString();
    final String tmpLowResult1 = environment.getNextVariableString();
    final String tmpLowResult2 = environment.getNextVariableString();
    final String tmpLowResult3 = environment.getNextVariableString();
    final String tmpLowResult4 = environment.getNextVariableString();
    final String overflow = environment.getNextVariableString();
    final String isNegative = environment.getNextVariableString();
    final String isPositive = environment.getNextVariableString();

    long baseOffset = offset;

    final String addOperation = "ADD";

    if ((operation.equalsIgnoreCase(addOperation)) || (operation.equalsIgnoreCase("SUB"))) {
      // overflow
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          resultOperand,
          dw,
          "-" + String.valueOf(size),
          bt,
          overflow));

      if (operation.equalsIgnoreCase(addOperation)) {
        instructions.add(
            ReilHelpers.createStr(baseOffset++, bt, String.valueOf(1L), bt, isPositive));
        instructions.add(
            ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, isNegative));
      } else {
        // trial
        instructions.add(
            ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, isPositive));
        instructions.add(
            ReilHelpers.createStr(baseOffset++, bt, String.valueOf(1L), bt, isNegative));
      }

      // combine the results to get the true answer
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, bt, isNegative, bt, overflow, bt, isLessCondition));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          bt,
          isPositive,
          bt,
          overflow,
          bt,
          isGreaterCondition));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, overflow, bt, inRange));

      // create low half masks
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0),
          bt,
          isLessCondition,
          dw,
          isLessMask));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0),
          bt,
          isGreaterCondition,
          dw,
          isGreaterMask));
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0), bt, inRange, dw, inRangeMask));

      // return the result
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          resultOperand,
          dw,
          inRangeMask,
          dw,
          tmpLowResult1));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, lowSatResult, dw, isLessMask, dw, tmpLowResult2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          highSatResult,
          dw,
          isGreaterMask,
          dw,
          tmpLowResult3));

      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult1,
          dw,
          tmpLowResult2,
          dw,
          tmpLowResult4));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult4,
          dw,
          tmpLowResult3,
          dw,
          tmpResultVar));
    } else
    /* sat without computation SSAT16 */
    {
      final String needsShiftCompare = environment.getNextVariableString();
      final String xMinusy = environment.getNextVariableString();
      final String xMinusyXorx = environment.getNextVariableString();
      final String xXory = environment.getNextVariableString();
      final String xXoryAndxMinusXorx = environment.getNextVariableString();
      final String lowOverflow = environment.getNextVariableString();
      final String highOverflow = environment.getNextVariableString();

      /*
       * -2(n-1) if X is < -2(n-1) X if -2(n-1) <= X <= 2(n-1) - 1 2(n-1) - 1 if X > 2(n-1) - 1
       */
      // ( x - y ) XOR [(x XOR y) AND ((x - y) XOR x)]
      // low overflow
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, firstOperand, dw, lowSatResult, dw, xMinusy));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, firstOperand, dw, lowSatResult, dw, xXory));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, xMinusy, dw, firstOperand, dw, xMinusyXorx));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, xXory, dw, xMinusyXorx, dw, xXoryAndxMinusXorx));
      instructions.add(ReilHelpers.createXor(baseOffset++,
          dw,
          xMinusy,
          dw,
          xXoryAndxMinusXorx,
          dw,
          needsShiftCompare));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          needsShiftCompare,
          dw,
          String.valueOf(-31L),
          bt,
          lowOverflow));

      // high overflow
      baseOffset++;
      instructions.add(
          ReilHelpers.createSub(baseOffset++, dw, highSatResult, dw, firstOperand, dw, xMinusy));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, highSatResult, dw, firstOperand, dw, xXory));
      instructions.add(
          ReilHelpers.createXor(baseOffset++, dw, xMinusy, dw, highSatResult, dw, xMinusyXorx));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, xXory, dw, xMinusyXorx, dw, xXoryAndxMinusXorx));
      instructions.add(ReilHelpers.createXor(baseOffset++,
          dw,
          xMinusy,
          dw,
          xXoryAndxMinusXorx,
          dw,
          needsShiftCompare));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          needsShiftCompare,
          dw,
          String.valueOf(-31L),
          bt,
          highOverflow));

      instructions.add(
          ReilHelpers.createOr(baseOffset++, bt, lowOverflow, bt, highOverflow, bt, overflow));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, overflow, bt, inRange));
      if (operation.equalsIgnoreCase("USAT")) {
        // create low half masks
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            lowOverflow,
            dw,
            isLessMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            highOverflow,
            dw,
            isGreaterMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            inRange,
            dw,
            inRangeMask));
      } else {
        // create low half masks
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            highOverflow,
            dw,
            isLessMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            lowOverflow,
            dw,
            isGreaterMask));
        instructions.add(ReilHelpers.createSub(baseOffset++,
            dw,
            String.valueOf(0),
            bt,
            inRange,
            dw,
            inRangeMask));
      }

      // return the result
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          resultOperand,
          dw,
          inRangeMask,
          dw,
          tmpLowResult1));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, dw, lowSatResult, dw, isLessMask, dw, tmpLowResult2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dw,
          highSatResult,
          dw,
          isGreaterMask,
          dw,
          tmpLowResult3));

      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult1,
          dw,
          tmpLowResult2,
          dw,
          tmpLowResult4));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dw,
          tmpLowResult4,
          dw,
          tmpLowResult3,
          dw,
          tmpResultVar));
    }
  }
}
