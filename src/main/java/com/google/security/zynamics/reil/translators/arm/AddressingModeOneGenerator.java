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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;


public final class AddressingModeOneGenerator {

  final private static OperandSize byteSize = OperandSize.BYTE;

  final private static OperandSize wordSize = OperandSize.WORD;
  final private static OperandSize dWordSize = OperandSize.DWORD;
  final private static OperandSize qWordSize = OperandSize.QWORD;
  private static String bitMaskAllBitsSet = String.valueOf(0xFFFFFFFFL);

  private static String bitMaskHighestBitSet = String.valueOf(0x80000000L);
  private static String oneSet = String.valueOf(1L);
  private static String zeroSet = String.valueOf(0L);
  private static String thirtyOneSet = String.valueOf(31L);
  private static String minusThirtyOneSet = String.valueOf(-31L);
  private static String notThirtyOneSet = String.valueOf(~31L);

  private AddressingModeOneGenerator() {

  }

  /**
   * #<immediate> Case where rotate_imm != 0
   *
   *  Operation:
   *
   *  shifter_operand = immed_8 Rotate_Right (rotate_imm * 2) if rotate_imm == 0 then
   * shifter_carry_out = C flag else // rotate_imm != 0 shifter_carry_out = shifter_operand[31]
   */
  private static Pair<String, String> immediateROR(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String immediateNodeValue1, final String immediateNodeValue2) {

    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        immediateNodeValue1,
        wordSize,
        "-" + immediateNodeValue2,
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        immediateNodeValue1,
        wordSize,
        String.valueOf((32 - Integer.decode(immediateNodeValue2))),
        qWordSize,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dWordSize,
        tmpVar1,
        qWordSize,
        tmpVar2,
        qWordSize,
        tmpVar3));

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        qWordSize,
        tmpVar3,
        dWordSize,
        bitMaskAllBitsSet,
        dWordSize,
        shifterOperand));

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        qWordSize,
        tmpVar3,
        dWordSize,
        minusThirtyOneSet,
        dWordSize,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar4,
        dWordSize,
        oneSet,
        byteSize,
        shifterCarryOut));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);
  }

  /**
   * #<immediate> Case where rotate_imm == 0
   *
   *  Operation:
   *
   *  shifter_operand = immed_8 Rotate_Right (rotate_imm * 2) if rotate_imm == 0 then
   * shifter_carry_out = C flag else // rotate_imm != 0 shifter_carry_out = shifter_operand[31]
   */
  private static Pair<String, String> immediateRotateZero(final String immediateNodeValue) {
    return new Pair<String, String>(immediateNodeValue, "C");
  }

  /**
   * <Rm>
   *
   *  Operation:
   *
   * shifter_operand = Rm shifter_carry_out = C Flag
   */
  private static Pair<String, String> register(final String registerNodeValue) {
    return new Pair<String, String>(registerNodeValue, "C");
  }

  /**
   * <Rm>, ROR #<shift_imm>
   *
   *  Operation:
   *
   *  if shift_imm == 0 then See Data-processing operands - Rotate right with extend on page A5-17
   * else / shift_imm > 0 / shifter_operand = Rm Rotate_Right shift_imm shifter_carry_out =
   * Rm[shift_imm - 1]
   */
  private static Pair<String, String> rorImmediate(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {
    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    long baseOffset = offset;

    if (immediateNodeValue.equals("0")) {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          byteSize,
          "C",
          wordSize,
          thirtyOneSet,
          dWordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          byteSize,
          "-" + String.valueOf(1),
          dWordSize,
          tmpVar2));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dWordSize,
          tmpVar1,
          dWordSize,
          tmpVar2,
          dWordSize,
          shifterOperand));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dWordSize,
          registerNodeValue,
          byteSize,
          String.valueOf(1),
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          "-" + Integer.decode(immediateNodeValue),
          dWordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          String.valueOf((32 - Integer.decode(immediateNodeValue))),
          qWordSize,
          tmpVar2));
      instructions.add(ReilHelpers.createOr(baseOffset++,
          dWordSize,
          tmpVar1,
          qWordSize,
          tmpVar2,
          qWordSize,
          tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          qWordSize,
          tmpVar3,
          dWordSize,
          bitMaskAllBitsSet,
          dWordSize,
          shifterOperand));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          dWordSize,
          String.valueOf(-(Integer.decode(immediateNodeValue) - 1)),
          dWordSize,
          tmpVar4));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dWordSize,
          tmpVar4,
          byteSize,
          String.valueOf(1),
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    }
  }

  /**
   * <Rm>, ASR #<shift_imm>
   *
   *  Operation:
   *
   *  if shift_imm == 0 then if Rm[31] == 0 then shifter_operand = 0 shifter_carry_out = Rm[31] else
   * // Rm[31] == 1 / shifter_operand = 0xFFFFFFFF shifter_carry_out = Rm[31] else / shift_imm > 0 /
   * shifter_operand = Rm Arithmetic_Shift_Right <shift_imm> shifter_carry_out = Rm[shift_imm - 1]
   */
  protected static Pair<String, String> asrImmediate(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {

    long baseOffset = offset;
    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();
    if (immediateNodeValue.equals("0")) {
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          thirtyOneSet,
          byteSize,
          tmpVar1));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          byteSize,
          tmpVar1,
          byteSize,
          oneSet,
          byteSize,
          shifterCarryOut));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          byteSize,
          shifterCarryOut,
          byteSize,
          String.valueOf(1),
          dWordSize,
          shifterOperand));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();
      final String tmpVar5 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAdd(baseOffset++,
          dWordSize,
          registerNodeValue,
          dWordSize,
          bitMaskHighestBitSet,
          qWordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          qWordSize,
          tmpVar1,
          wordSize,
          "-" + immediateNodeValue,
          dWordSize,
          tmpVar2));

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          bitMaskHighestBitSet,
          wordSize,
          "-" + immediateNodeValue,
          dWordSize,
          tmpVar3));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dWordSize,
          tmpVar2,
          dWordSize,
          tmpVar3,
          qWordSize,
          tmpVar4));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          qWordSize,
          tmpVar4,
          dWordSize,
          bitMaskAllBitsSet,
          dWordSize,
          shifterOperand));

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          dWordSize,
          String.valueOf(-(Integer.decode(immediateNodeValue) - 1)),
          wordSize,
          tmpVar5));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          wordSize,
          tmpVar5,
          byteSize,
          oneSet,
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    }
  }

  /**
   * <Rm>, ASR <Rs>
   *
   *  Operation:
   *
   *  if Rs[7:0] == 0 then shifter_operand = Rm shifter_carry_out = C Flag else if Rs[7:0] < 32 then
   * shifter_operand = Rm Arithmetic_Shift_Right Rs[7:0] shifter_carry_out = Rm[Rs[7:0] - 1] else /
   * Rs[7:0] >= 32 / if Rm[31] == 0 then shifter_operand = 0 shifter_carry_out = Rm[31] else /
   * Rm[31] == 1 / shifter_operand = 0xFFFFFFFF shifter_carry_out = Rm[31]
   */
  protected static Pair<String, String> asrRegister(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {

    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    final String isZeroCondition = environment.getNextVariableString();
    final String isLessCondition = environment.getNextVariableString();
    final String isGtEqCondition = environment.getNextVariableString();

    final String shifterCarryOutTmp1 = environment.getNextVariableString();
    final String shifterCarryOutTmp2 = environment.getNextVariableString();
    final String shifterCarryOutTmp3 = environment.getNextVariableString();
    final String shifterCarryOutTmp4 = environment.getNextVariableString();

    final String shifterOperandTmp1 = environment.getNextVariableString();
    final String shifterOperandTmp2 = environment.getNextVariableString();
    final String shifterOperandTmp3 = environment.getNextVariableString();
    final String shifterOperandTmp4 = environment.getNextVariableString();

    final String tmpRsRegister = environment.getNextVariableString();
    final String negativeTmpRsRegister = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();
    final String tmpVar6 = environment.getNextVariableString();
    final String tmpVar7 = environment.getNextVariableString();
    final String tmpVar8 = environment.getNextVariableString();
    final String tmpVar9 = environment.getNextVariableString();
    final String tmpVar10 = environment.getNextVariableString();
    final String tmpVar11 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue2,
        dWordSize,
        String.valueOf(0xFFL),
        dWordSize,
        tmpRsRegister));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        dWordSize,
        tmpRsRegister,
        dWordSize,
        negativeTmpRsRegister));

    // Rs[7:0] == 0
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpRsRegister, byteSize, isZeroCondition));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        "C",
        byteSize,
        isZeroCondition,
        byteSize,
        shifterCarryOutTmp1));

    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        String.valueOf(0),
        byteSize,
        isZeroCondition,
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar1,
        dWordSize,
        registerNodeValue2,
        dWordSize,
        shifterOperandTmp1));

    // Rs[7:0] < 32
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpRsRegister,
        dWordSize,
        String.valueOf(0xFFFFFFE0L),
        dWordSize,
        tmpVar2));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar2, byteSize, isLessCondition));

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        bitMaskHighestBitSet,
        dWordSize,
        tmpVar3));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        tmpVar3,
        dWordSize,
        negativeTmpRsRegister,
        dWordSize,
        tmpVar4));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        bitMaskHighestBitSet,
        dWordSize,
        negativeTmpRsRegister,
        dWordSize,
        tmpVar5));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        tmpVar4,
        dWordSize,
        tmpVar5,
        dWordSize,
        tmpVar6));

    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        byteSize,
        isLessCondition,
        dWordSize,
        tmpVar7));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar6,
        dWordSize,
        tmpVar7,
        dWordSize,
        shifterOperandTmp2));

    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        tmpRsRegister,
        byteSize,
        oneSet,
        dWordSize,
        tmpVar8));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        dWordSize,
        tmpVar8,
        dWordSize,
        tmpVar8));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar8,
        dWordSize,
        tmpVar9));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar9,
        byteSize,
        isLessCondition,
        byteSize,
        shifterCarryOutTmp2));

    // RS[7:0] >= 32
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        isLessCondition,
        byteSize,
        tmpVar10));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, byteSize, tmpVar10, byteSize, isGtEqCondition));

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        minusThirtyOneSet,
        byteSize,
        tmpVar11));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        byteSize,
        zeroSet,
        byteSize,
        isGtEqCondition,
        dWordSize,
        shifterOperandTmp3));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        tmpVar11,
        byteSize,
        isGtEqCondition,
        byteSize,
        shifterCarryOutTmp3));

    // or the results of the carry's and the operands to get the real output
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp1,
        byteSize,
        shifterCarryOutTmp2,
        byteSize,
        shifterCarryOutTmp4));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp3,
        byteSize,
        shifterCarryOutTmp4,
        byteSize,
        shifterCarryOut));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dWordSize,
        shifterOperandTmp1,
        dWordSize,
        shifterOperandTmp2,
        dWordSize,
        shifterOperandTmp4));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dWordSize,
        shifterOperandTmp3,
        dWordSize,
        shifterOperandTmp4,
        dWordSize,
        shifterOperand));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);
  }

  /**
   * <Rm>, LSL #<shift_imm>
   *
   *  Operation:
   *
   *  if shift_imm == 0 then // Register Operand shifter_operand = Rm shifter_carry_out = C Flag
   * else // shift_imm > 0 shifter_operand = Rm Logical_Shift_Left shift_imm shifter_carry_out =
   * Rm[32 - shift_imm]
   */
  protected static Pair<String, String> lslImmediate(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {
    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    long baseOffset = offset;

    if (immediateNodeValue.equals("0")) {
      return new Pair<String, String>(registerNodeValue, "C");
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          immediateNodeValue,
          qWordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          qWordSize,
          tmpVar1,
          dWordSize,
          bitMaskAllBitsSet,
          dWordSize,
          shifterOperand));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          String.valueOf(-(32 - Integer.decode(immediateNodeValue))),
          dWordSize,
          tmpVar2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dWordSize,
          tmpVar2,
          byteSize,
          oneSet,
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    }
  }

  /**
   * <Rm>, LSL <Rs>
   *
   *  Operation:
   *
   *  if Rs[7:0] == 0 then shifter_operand = Rm shifter_carry_out = C Flag else if Rs[7:0] < 32 then
   * shifter_operand = Rm Logical_Shift_Left Rs[7:0] shifter_carry_out = Rm[32 - Rs[7:0]] else if
   * Rs[7:0] == 32 then shifter_operand = 0 shifter_carry_out = Rm[0] else // Rs[7:0] > 32
   * shifter_operand = 0 shifter_carry_out = 0
   */
  protected static Pair<String, String> lslRegister(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {

    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    long baseOffset = offset;

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();
    final String tmpVar6 = environment.getNextVariableString();
    final String tmpVar7 = environment.getNextVariableString();

    final String isZeroCondition = environment.getNextVariableString();

    final String shifterCarryOutTmp1 = environment.getNextVariableString();
    final String shifterCarryOutTmp2 = environment.getNextVariableString();
    final String shifterCarryOutTmp3 = environment.getNextVariableString();
    final String shifterCarryOutTmp4 = environment.getNextVariableString();

    // do shift first because it does not "really" depend on the value in <Rs>
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue2,
        dWordSize,
        String.valueOf(0x000000FFL),
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar1,
        qWordSize,
        tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        qWordSize,
        tmpVar2,
        dWordSize,
        bitMaskAllBitsSet,
        dWordSize,
        shifterOperand));

    // find out what the shifter carry is "try to get it done without jump"
    // Rs[7:0] == 0
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar1, byteSize, isZeroCondition));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        "C",
        byteSize,
        shifterCarryOutTmp1));

    // Rs[7:0] == 32
    instructions.add(ReilHelpers.createXor(baseOffset++,
        dWordSize,
        tmpVar1,
        wordSize,
        String.valueOf(0x20L),
        dWordSize,
        tmpVar3));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar3, byteSize, isZeroCondition));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        oneSet,
        byteSize,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        tmpVar4,
        byteSize,
        shifterCarryOutTmp2));

    // Rs[7:0] < 32
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar1,
        dWordSize,
        notThirtyOneSet,
        dWordSize,
        tmpVar5));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar5, byteSize, isZeroCondition));

    // Rm[32 - Rs[7:0]]
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        String.valueOf(32L),
        dWordSize,
        tmpVar1,
        dWordSize,
        tmpVar6));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        "-" + tmpVar6,
        byteSize,
        tmpVar7));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        tmpVar7,
        byteSize,
        shifterCarryOutTmp3));

    // or the result of the carry's to get the right result
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp1,
        byteSize,
        shifterCarryOutTmp2,
        byteSize,
        shifterCarryOutTmp4));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp3,
        byteSize,
        shifterCarryOutTmp4,
        byteSize,
        shifterCarryOut));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);

  }

  /**
   * <Rm>, LSR #<shift_imm>
   *
   *  Operation:
   *
   *  if shift_imm == 0 then shifter_operand = 0 shifter_carry_out = Rm[31] else // shift_imm > 0
   * shifter_operand = Rm Logical_Shift_Right shift_imm shifter_carry_out = Rm[shift_imm - 1]
   */
  protected static Pair<String, String> lsrImmediate(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {

    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    long baseOffset = offset;

    if (immediateNodeValue.equals("0")) {
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          minusThirtyOneSet,
          wordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          wordSize,
          tmpVar1,
          byteSize,
          oneSet,
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(String.valueOf(0), shifterCarryOut);
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          "-" + immediateNodeValue,
          qWordSize,
          tmpVar1));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          qWordSize,
          tmpVar1,
          dWordSize,
          bitMaskAllBitsSet,
          dWordSize,
          shifterOperand));

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dWordSize,
          registerNodeValue,
          wordSize,
          String.valueOf(-(Integer.decode(immediateNodeValue) - 1)),
          dWordSize,
          tmpVar2));
      instructions.add(ReilHelpers.createAnd(baseOffset++,
          dWordSize,
          tmpVar2,
          byteSize,
          oneSet,
          byteSize,
          shifterCarryOut));

      return new Pair<String, String>(shifterOperand, shifterCarryOut);
    }
  }

  /**
   * <Rm>, LSR <Rs>
   *
   *  Operation:
   *
   *  if Rs[7:0] == 0 then shifter_operand = Rm shifter_carry_out = C Flag else if Rs[7:0] < 32 then
   * shifter_operand = Rm Logical_Shift_Right Rs[7:0] shifter_carry_out = Rm[Rs[7:0] - 1] else if
   * Rs[7:0] == 32 then shifter_operand = 0 shifter_carry_out = Rm[31] else // Rs[7:0] > 32
   * shifter_operand = 0 shifter_carry_out = 0
   */
  protected static Pair<String, String> lsrRegister(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar11 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();
    final String tmpVar6 = environment.getNextVariableString();
    final String tmpVar7 = environment.getNextVariableString();

    final String isZeroCondition = environment.getNextVariableString();

    final String shifterCarryOutTmp1 = environment.getNextVariableString();
    final String shifterCarryOutTmp2 = environment.getNextVariableString();
    final String shifterCarryOutTmp3 = environment.getNextVariableString();
    final String shifterCarryOutTmp4 = environment.getNextVariableString();

    long baseOffset = offset;

    // do shift first because it does not "really" depend on the value in <Rs>
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue2,
        dWordSize,
        String.valueOf(0x000000FFL),
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        String.valueOf(0),
        dWordSize,
        tmpVar1,
        dWordSize,
        tmpVar11));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar11,
        dWordSize,
        tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar2,
        dWordSize,
        bitMaskAllBitsSet,
        dWordSize,
        shifterOperand));

    // find out what the shifter carry is "try to get it done without jump"
    // Rs[7:0] == 0
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar11, byteSize, isZeroCondition));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        "C",
        byteSize,
        shifterCarryOutTmp1));

    // Rs[7:0] == 32
    instructions.add(ReilHelpers.createXor(baseOffset++,
        dWordSize,
        tmpVar11,
        wordSize,
        String.valueOf(0x20L),
        dWordSize,
        tmpVar3));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar3, byteSize, isZeroCondition));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        thirtyOneSet,
        byteSize,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        tmpVar4,
        byteSize,
        shifterCarryOutTmp2));

    // Rs[7:0] < 32
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar11,
        dWordSize,
        notThirtyOneSet,
        dWordSize,
        tmpVar5));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpVar5, byteSize, isZeroCondition));
    // Rm[32 - Rs[7:0]]
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        tmpVar11,
        dWordSize,
        oneSet,
        dWordSize,
        tmpVar6));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        dWordSize,
        tmpVar6,
        dWordSize,
        tmpVar6));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar6,
        byteSize,
        tmpVar7));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        isZeroCondition,
        byteSize,
        tmpVar7,
        byteSize,
        shifterCarryOutTmp3));

    // or the result of the carrys to get the right result
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp1,
        byteSize,
        shifterCarryOutTmp2,
        byteSize,
        shifterCarryOutTmp4));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp3,
        byteSize,
        shifterCarryOutTmp4,
        byteSize,
        shifterCarryOut));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);
  }

  /**
   * <Rm>, ROR <Rs>
   *
   *  Operation:
   *
   *  if Rs[7:0] == 0 then shifter_operand = Rm shifter_carry_out = C Flag else if Rs[4:0] == 0 then
   * shifter_operand = Rm shifter_carry_out = Rm[31] else / Rs[4:0] > 0 / shifter_operand = Rm
   * Rotate_Right Rs[4:0] shifter_carry_out = Rm[Rs[4:0] - 1]
   */
  protected static Pair<String, String> rorRegister(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {

    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    final String tmpRsFour = environment.getNextVariableString();
    final String tmpRsFourNegative = environment.getNextVariableString();
    final String tmpRsSeven = environment.getNextVariableString();

    final String isZeroConditionFour = environment.getNextVariableString();
    final String isNotZeroConditionFour = environment.getNextVariableString();
    final String isZeroConditionSeven = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();
    final String tmpVar6 = environment.getNextVariableString();
    final String tmpVar7 = environment.getNextVariableString();
    final String tmpVar8 = environment.getNextVariableString();

    final String shifterCarryOutTmp1 = environment.getNextVariableString();
    final String shifterCarryOutTmp2 = environment.getNextVariableString();
    final String shifterCarryOutTmp3 = environment.getNextVariableString();
    final String shifterCarryOutTmp4 = environment.getNextVariableString();

    long baseOffset = offset;

    // Rs[7:0] == 0
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue2,
        dWordSize,
        String.valueOf(0xFFL),
        dWordSize,
        tmpRsSeven));
    instructions.add(ReilHelpers.createBisz(
        baseOffset++, dWordSize, tmpRsSeven, byteSize, isZeroConditionSeven));

    // Rs[4:0] == 0
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpRsSeven,
        dWordSize,
        String.valueOf(0x1FL),
        dWordSize,
        tmpRsFour));
    instructions.add(
        ReilHelpers.createBisz(baseOffset++, dWordSize, tmpRsFour, byteSize, isZeroConditionFour));
    instructions.add(ReilHelpers.createBisz(
        baseOffset++, byteSize, isZeroConditionFour, byteSize, isNotZeroConditionFour));

    // do the rotate anyways because if Rs[4:0] == 0 it does not matter
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        dWordSize,
        tmpRsFour,
        dWordSize,
        tmpRsFourNegative));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpRsFourNegative,
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        String.valueOf(32L),
        dWordSize,
        tmpRsFour,
        dWordSize,
        tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar2,
        dWordSize,
        tmpVar3));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dWordSize,
        tmpVar1,
        dWordSize,
        tmpVar3,
        dWordSize,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar4,
        dWordSize,
        bitMaskAllBitsSet,
        dWordSize,
        shifterOperand));

    // do the carry out magic
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        "C",
        byteSize,
        isZeroConditionSeven,
        byteSize,
        shifterCarryOutTmp1));

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        minusThirtyOneSet,
        byteSize,
        tmpVar5));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        byteSize,
        tmpVar5,
        byteSize,
        isZeroConditionFour,
        byteSize,
        shifterCarryOutTmp2));

    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        tmpRsFour,
        byteSize,
        oneSet,
        dWordSize,
        tmpVar6));
    instructions.add(ReilHelpers.createSub(baseOffset++,
        dWordSize,
        zeroSet,
        dWordSize,
        tmpVar6,
        dWordSize,
        tmpVar7));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue1,
        dWordSize,
        tmpVar7,
        dWordSize,
        tmpVar8));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar8,
        byteSize,
        isNotZeroConditionFour,
        byteSize,
        shifterCarryOutTmp3));

    // or the three carry cases to get the real carry
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp1,
        byteSize,
        shifterCarryOutTmp2,
        byteSize,
        shifterCarryOutTmp4));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        byteSize,
        shifterCarryOutTmp3,
        byteSize,
        shifterCarryOutTmp4,
        byteSize,
        shifterCarryOut));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);
  }

  /**
   * <Rm>, RRX
   *
   *  Operation:
   *
   *  shifter_operand = (C Flag Logical_Shift_Left 31) OR (Rm Logical_Shift_Right 1)
   * shifter_carry_out = Rm[0]
   */
  protected static Pair<String, String> rrxRegister(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue) {
    final String shifterOperand = environment.getNextVariableString();
    final String shifterCarryOut = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        byteSize,
        "C",
        wordSize,
        thirtyOneSet,
        dWordSize,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dWordSize,
        registerNodeValue,
        byteSize,
        "-" + oneSet,
        dWordSize,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++,
        dWordSize,
        tmpVar1,
        dWordSize,
        tmpVar2,
        dWordSize,
        tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        tmpVar3,
        dWordSize,
        bitMaskAllBitsSet,
        dWordSize,
        shifterOperand));

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        dWordSize,
        registerNodeValue,
        byteSize,
        String.valueOf(1),
        byteSize,
        shifterCarryOut));

    return new Pair<String, String>(shifterOperand, shifterCarryOut);
  }

  /**
   * The FlexirandGenerator Class takes the Flexible Operand <operand2> of an ARM instruction and
   * provides the result of the computation within <operand2> to the caller.
   *
   * @return a pair with optional overflow and optional carry-out of shifter
   */
  public static Pair<String, String> generate(final long baseOffset,
      final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions, final IOperandTreeNode rootNode)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    /**
     * parse the received operand tree to see what we need to be calling
     */
    if (rootNode.getChildren().get(0).getType() == ExpressionType.IMMEDIATE_INTEGER) {
      // matched #<immediate> with rotate_imm == 0
      return immediateRotateZero(rootNode.getChildren().get(0).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("ROR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.IMMEDIATE_INTEGER)) {
      // matched #<immediate> with rotate_imm != 0
      return immediateROR(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if (rootNode.getChildren().get(0).getType() == ExpressionType.REGISTER) {
      // matched <Rm>
      return register(rootNode.getChildren().get(0).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("LSL") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.IMMEDIATE_INTEGER)) {
      // matched Rn LSL #imm
      return lslImmediate(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("LSL") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.REGISTER)) {
      // matched Rn LSL Rn
      return lslRegister(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("LSR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.IMMEDIATE_INTEGER)) {
      // matched Rn LSR #imm
      return lsrImmediate(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("LSR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.REGISTER)) {
      // matched Rn LSR Rn
      return lsrRegister(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("ASR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.IMMEDIATE_INTEGER)) {
      // matched Rn ASR #imm
      return asrImmediate(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("ASR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.REGISTER)) {
      // matched Rn ASR Rn
      return asrRegister(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("ROR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.IMMEDIATE_INTEGER)) {
      // matched Rn ROR #imm
      return rorImmediate(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("ROR") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER) && (
            rootNode.getChildren().get(0).getChildren().get(1).getType()
            == ExpressionType.REGISTER)) {
      // matched Rn ROR Rn
      return rorRegister(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue(),
          rootNode.getChildren().get(0).getChildren().get(1).getValue());
    } else if ((rootNode.getChildren().get(0).getType() == ExpressionType.OPERATOR)
        && rootNode.getChildren().get(0).getValue().equals("RRX") && (
            rootNode.getChildren().get(0).getChildren().get(0).getType()
            == ExpressionType.REGISTER)) {
      // matched Rn RRX
      return rrxRegister(baseOffset, environment, instructions,
          rootNode.getChildren().get(0).getChildren().get(0).getValue());
    } else {
      throw new InternalTranslationException("Error: AddressOperandTypeOne OperandTree is not valid"
          + instruction.getMnemonic() + " " + instruction.getAddress().toString());
    }
  }
}
