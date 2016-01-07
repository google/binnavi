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


public final class AddressingModeTwoGenerator {
  final static OperandSize bt = OperandSize.BYTE;

  final static OperandSize wd = OperandSize.WORD;
  final static OperandSize dw = OperandSize.DWORD;
  final static OperandSize qw = OperandSize.QWORD;
  private static String dWordBitMask = String.valueOf(0xFFFFFFFFL);

  /**
   * The AddressingModeGenerator Class takes the addressing_mode Operand of an ARM instruction and
   * provides the result of the computation within the operand to the caller. This Class is for
   * Addressing Mode 2.
   */
  private AddressingModeTwoGenerator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Operation: [<Rn>, +/-<Rm>, ASR #<shift_imm>]
   *
   *  0b10 / ASR / if shift_imm == 0 then / ASR #32 / if Rm[31] == 1 then index = 0xFFFFFFFF else
   * index = 0 else index = Rm Arithmetic_Shift_Right shift_imm endcase if U == 1 then address = Rn
   * + index else / U == 0 / address = Rn - index
   */
  private static Pair<String, String> offsetASR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {

    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    long baseOffset = offset;

    if (immediateNodeValue.equals("0")) {
      final String isZeroCondition = environment.getNextVariableString();
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          registerNodeValue2,
          wd,
          String.valueOf(-31),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, tmpVar1, bt, isZeroCondition));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0x0L),
          bt,
          isZeroCondition,
          dw,
          index));
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAdd(baseOffset++,
          dw,
          registerNodeValue2,
          dw,
          String.valueOf(0x80000000L),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          tmpVar1,
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar2));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          String.valueOf(0x80000000L),
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar3));
      instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw, tmpVar3, qw, tmpVar4));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, qw, tmpVar4, dw, dWordBitMask, dw, index));
    }

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, #+/-<offset_12>]
   *
   *  Operation:
   *
   * if U == 1 then address = Rn + offset_12 else / U == 0 / address = Rn - offset_12
   *
   */
  private static Pair<String, String> offsetImm(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue,
        dw,
        immediateNodeValue,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue);
  }

  /**
   * Operation: [<Rn>, +/-<Rm>, LSL #<shift_imm>]
   *
   *  case shift of 0b00 / LSL / index = Rm Logical_Shift_Left shift_imm if U == 1 then address = Rn
   * + index else / U == 0 / address = Rn - index
   */
  private static Pair<String, String> offsetLSL(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>, +/-<Rm>, LSR #<shift_imm>]
   *
   *  0b01 / LSR / if shift_imm == 0 then / LSR #32 / index = 0 else index = Rm Logical_Shift_Right
   * shift_imm if U == 1 then address = Rn + index else / U == 0 / address = Rn - index
   *
   */
  private static Pair<String, String> offsetLSR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>]
   *
   *  Operation:
   *
   * if U == 1 then address = Rn + Rm else / U == 0 / address = Rn - Rm
   */
  private static Pair<String, String> offsetReg(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue1,
        dw,
        registerNodeValue2,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>, +/-<Rm>, ROR #<shift_imm>]
   *
   *  0b11 / ROR or RRX / if shift_imm == 0 then / RRX / index = (C Flag Logical_Shift_Left 31) OR
   * (Rm Logical_Shift_Right 1) else / ROR / index = Rm Rotate_Right shift_imm endcase if U == 1
   * then address = Rn + index else / U == 0 / address = Rn - index
   */
  private static Pair<String, String> offsetROR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + Integer.decode(immediateNodeValue),
        dw,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        String.valueOf(32 - Integer.decode(immediateNodeValue)),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>, +/-<Rm>, RRX]
   *
   *  0b11 / ROR or RRX / if shift_imm == 0 then / RRX / index = (C Flag Logical_Shift_Left 31) OR
   * (Rm Logical_Shift_Right 1) else / ROR / index = Rm Rotate_Right shift_imm endcase if U == 1
   * then address = Rn + index else / U == 0 / address = Rn - index
   */
  private static Pair<String, String> offsetRRX(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(
        ReilHelpers.createBsh(baseOffset++, bt, "C", wd, String.valueOf(31), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        bt,
        String.valueOf(-1),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>], +/-<Rm>, ASR #<shift_imm> address = Rn 0b10 / ASR / if shift_imm == 0 then /
   * ASR #32 / if Rm[31] == 1 then index = 0xFFFFFFFF else index = 0 else index = Rm
   * Arithmetic_Shift_Right shift_imm if ConditionPassed(cond) then if U == 1 then Rn = Rn + index
   * else / U == 0 / Rn = Rn - index
   */
  private static Pair<String, String> postIndexedASR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {

    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));

    if (immediateNodeValue.equals("0")) {
      final String isZeroCondition = environment.getNextVariableString();
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          registerNodeValue2,
          wd,
          String.valueOf(-31),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, tmpVar1, bt, isZeroCondition));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0x0L),
          bt,
          isZeroCondition,
          dw,
          index));
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAdd(baseOffset++,
          dw,
          registerNodeValue2,
          dw,
          String.valueOf(0x80000000L),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          tmpVar1,
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar2));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          String.valueOf(0x80000000L),
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar3));
      instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw, tmpVar3, qw, tmpVar4));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, qw, tmpVar4, dw, dWordBitMask, dw, index));
    }

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>], #+/-<offset_12>
   *
   *  Operation:
   *
   *  address = Rn if ConditionPassed(cond) then if U == 1 then Rn = Rn + offset_12 else / U == 0 /
   * Rn = Rn - offset_12
   */
  private static Pair<String, String> postIndexedImm(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue, dw, address));
    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue,
        dw,
        immediateNodeValue,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, registerNodeValue));

    return new Pair<String, String>(address, registerNodeValue);
  }

  /**
   * Operation: [<Rn>], +/-<Rm>, LSL #<shift_imm>
   *
   *  address = Rn case shift of 0b00 / LSL / index = Rm Logical_Shift_Left shift_imm
   *
   * if ConditionPassed(cond) then if U == 1 then Rn = Rn + index else / U == 0 / Rn = Rn - index
   */
  private static Pair<String, String> postIndexedLSL(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>], +/-<Rm>, LSR #<shift_imm> address = Rn 0b01 / LSR / if shift_imm == 0 then /
   * LSR #32 / index = 0 else index = Rm Logical_Shift_Right shift_imm if ConditionPassed(cond) then
   * if U == 1 then Rn = Rn + index else / U == 0 / Rn = Rn - index
   */
  private static Pair<String, String> postIndexedLSR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>], +/-<Rm>
   *
   *  Operation:
   *
   *  address = Rn if ConditionPassed(cond) then if U == 1 then Rn = Rn + Rm else / U == 0 / Rn = Rn
   * - Rm
   */
  private static Pair<String, String> postIndexedReg(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));
    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue1,
        dw,
        registerNodeValue2,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>], +/-<Rm>, ROR #<shift_imm> address = Rn 0b11 / ROR or RRX / if shift_imm == 0
   * then / RRX / index = (C Flag Logical_Shift_Left 31) OR (Rm Logical_Shift_Right 1) else / ROR /
   * index = Rm Rotate_Right shift_imm if ConditionPassed(cond) then if U == 1 then Rn = Rn + index
   * else / U == 0 / Rn = Rn - index
   */
  private static Pair<String, String> postIndexedROR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + Integer.decode(immediateNodeValue),
        dw,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        String.valueOf(32 - Integer.decode(immediateNodeValue)),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * Operation: [<Rn>], +/-<Rm>, RRX address = Rn 0b11 / ROR or RRX / if shift_imm == 0 then / RRX /
   * index = (C Flag Logical_Shift_Left 31) OR (Rm Logical_Shift_Right 1) else / ROR / index = Rm
   * Rotate_Right shift_imm if ConditionPassed(cond) then if U == 1 then Rn = Rn + index else / U ==
   * 0 / Rn = Rn - index
   */
  private static Pair<String, String> postIndexedRRX(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue1, dw, address));
    instructions.add(
        ReilHelpers.createBsh(baseOffset++, bt, "C", wd, String.valueOf(31), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        bt,
        String.valueOf(-1),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>, ASR #<shift_imm>]! 0b10 / ASR / if shift_imm == 0 then / ASR #32 / if Rm[31] ==
   * 1 then index = 0xFFFFFFFF else index = 0 else index = Rm Arithmetic_Shift_Right shift_imm
   *
   *  if U == 1 then address = Rn + index else / U == 0 / address = Rn - index if
   * ConditionPassed(cond) then Rn = address
   */
  private static Pair<String, String> preIndexedASR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {

    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    long baseOffset = offset;

    if (immediateNodeValue.equals("0")) {
      final String isZeroCondition = environment.getNextVariableString();
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          registerNodeValue2,
          wd,
          String.valueOf(-31),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, tmpVar1, bt, isZeroCondition));
      instructions.add(ReilHelpers.createSub(baseOffset++,
          dw,
          String.valueOf(0x0L),
          bt,
          isZeroCondition,
          dw,
          index));
    } else {
      final String tmpVar1 = environment.getNextVariableString();
      final String tmpVar2 = environment.getNextVariableString();
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAdd(baseOffset++,
          dw,
          registerNodeValue2,
          dw,
          String.valueOf(0x80000000L),
          dw,
          tmpVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          tmpVar1,
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar2));
      instructions.add(ReilHelpers.createBsh(baseOffset++,
          dw,
          String.valueOf(0x80000000L),
          dw,
          "-" + immediateNodeValue,
          dw,
          tmpVar3));
      instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw, tmpVar3, qw, tmpVar4));
      instructions.add(
          ReilHelpers.createAnd(baseOffset++, qw, tmpVar4, dw, dWordBitMask, dw, index));
    }

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, #+/-<offset_12>]!
   *
   *  Operation:
   *
   *  if U == 1 then address = Rn + offset_12 else / if U == 0 / address = Rn - offset_12 if
   * ConditionPassed(cond) then Rn = address
   */
  private static Pair<String, String> preIndexedImm(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue, final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue,
        dw,
        immediateNodeValue,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue));

    return new Pair<String, String>(address, registerNodeValue);
  }

  /**
   * [<Rn>, +/-<Rm>, LSL #<shift_imm>]! 0b00 / LSL / index = Rm Logical_Shift_Left shift_imm
   *
   *  if U == 1 then address = Rn + index else / U == 0 / address = Rn - index if
   * ConditionPassed(cond) then Rn = address
   */
  private static Pair<String, String> preIndexedLSL(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>, LSR #<shift_imm>]! 0b01 / LSR / if shift_imm == 0 then / LSR #32 / index = 0
   * else index = Rm Logical_Shift_Right shift_imm
   *
   *  if U == 1 then address = Rn + index else / U == 0 / address = Rn - index if
   * ConditionPassed(cond) then Rn = address
   */
  private static Pair<String, String> preIndexedLSR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String index = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + immediateNodeValue,
        qw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar2));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>]!
   *
   *  Operation:
   *
   *  if U == 1 then address = Rn + Rm else / U == 0 / address = Rn - Rm if ConditionPassed(cond)
   * then Rn = address
   */
  private static Pair<String, String> preIndexedReg(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++,
        dw,
        registerNodeValue1,
        dw,
        registerNodeValue2,
        dw,
        tmpVar1));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>, ROR #<shift_imm>]! 0b11 / ROR or RRX / if shift_imm == 0 then / RRX / index =
   * (C Flag Logical_Shift_Left 31) OR (Rm Logical_Shift_Right 1) else / ROR / index = Rm
   * Rotate_Right shift_imm if U == 1 then address = Rn + index else / U == 0 / address = Rn - index
   * if ConditionPassed(cond) then Rn = address
   *
   */
  private static Pair<String, String> preIndexedROR(final long offset,
      final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final String registerNodeValue1,
      final String registerNodeValue2,
      final String immediateNodeValue) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        "-" + Integer.decode(immediateNodeValue),
        dw,
        tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        dw,
        String.valueOf(32 - Integer.decode(immediateNodeValue)),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  /**
   * [<Rn>, +/-<Rm>, RRX]! 0b11 / ROR or RRX / if shift_imm == 0 then / RRX / index = (C Flag
   * Logical_Shift_Left 31) OR (Rm Logical_Shift_Right 1) else / ROR / index = Rm Rotate_Right
   * shift_imm if U == 1 then address = Rn + index else / U == 0 / address = Rn - index if
   * ConditionPassed(cond) then Rn = address
   *
   */
  private static Pair<String, String> preIndexedRRX(final long offset,
      final ITranslationEnvironment environment, final List<ReilInstruction> instructions,
      final String registerNodeValue1, final String registerNodeValue2) {
    final String address = environment.getNextVariableString();
    final String index = environment.getNextVariableString();
    final String tmpVar = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    long baseOffset = offset;

    instructions.add(
        ReilHelpers.createBsh(baseOffset++, bt, "C", wd, String.valueOf(31), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++,
        dw,
        registerNodeValue2,
        bt,
        String.valueOf(-1),
        dw,
        tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, dWordBitMask, dw, index));

    instructions.add(
        ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue1, dw, index, dw, tmpVar));
    instructions.add(
        ReilHelpers.createAnd(baseOffset++, dw, tmpVar, dw, dWordBitMask, dw, address));
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, address, dw, registerNodeValue1));

    return new Pair<String, String>(address, registerNodeValue1);
  }

  public static Pair<String, String> generate(final long baseOffset,
      final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions, final IOperandTreeNode rootNode)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    /**
     * parse the OperandTree to see what type of operand we received
     *
     *  This code is very verbose right now and due to this fact if a Bug is found in one type of
     * the functions be sure to check both other functions if they are OK
     *
     */
    if (rootNode.getChildren().get(0).getValue().equals("!")) {
      // matched pre- indexed
      if ((rootNode.getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getType() == ExpressionType.REGISTER) && (rootNode.getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(1)
          .getType() == ExpressionType.IMMEDIATE_INTEGER)) {
        // matched [ Rn , #imm ]!
        return preIndexedImm(baseOffset, environment, instructions, rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getValue(), rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue());
      } else if ((rootNode.getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getType() == ExpressionType.REGISTER) && (rootNode.getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(0)
          .getChildren()
          .get(1)
          .getType() == ExpressionType.REGISTER)) {
        // matched [ Rn, Rm ]!
        return preIndexedReg(baseOffset, environment, instructions, (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0).getValue()), (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1).getValue()));
      } else {
        // matched [ Rn, Rm, <shift>, #imm ]!
        if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("LSL")) {
          return preIndexedLSL(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("LSR")) {
          return preIndexedLSR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("ASR")) {
          return preIndexedASR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("ROR")) {
          return preIndexedROR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("RRX")) {
          return preIndexedRRX(baseOffset, environment, instructions, (rootNode.getChildren()
              .get(0)
              .getChildren()
              .get(0)
              .getChildren()
              .get(0)
              .getChildren()
              .get(0).getValue()), (rootNode.getChildren()
              .get(0)
              .getChildren()
              .get(0)
              .getChildren()
              .get(0)
              .getChildren()
              .get(1)
              .getChildren()
              .get(0).getValue()));
        } else {
          throw new InternalTranslationException(
              "Error: AddressOperandTypeTwo preIndexed shifter is not valid");
        }
      }
    } else if (rootNode.getChildren().get(0).getValue().equals(",")) {
      // matched post- indexed
      if ((rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.REGISTER) && (
          rootNode.getChildren().get(0).getChildren().get(1).getType()
          == ExpressionType.IMMEDIATE_INTEGER)) {
        // matched [ Rn ], #imm
        return postIndexedImm(baseOffset, environment, instructions,
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
            (rootNode.getChildren().get(0).getChildren().get(1).getValue()));
      } else if ((rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.REGISTER) && (
          rootNode.getChildren().get(0).getChildren().get(1).getType()
          == ExpressionType.REGISTER)) {
        // matched [ Rn ], Rm
        return postIndexedReg(baseOffset, environment, instructions,
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
            (rootNode.getChildren().get(0).getChildren().get(1).getValue()));
      } else {
        // matched [ Rn ], Rm, <shift>, #imm
        if (rootNode.getChildren().get(0).getChildren().get(1).getValue().equals("LSL")) {
          return postIndexedLSL(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(1).getValue()));
        } else if (rootNode.getChildren().get(0).getChildren().get(1).getValue().equals("LSR")) {
          return postIndexedLSR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(1).getValue()));
        } else if (rootNode.getChildren().get(0).getChildren().get(1).getValue().equals("ASR")) {
          return postIndexedASR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(1).getValue()));
        } else if (rootNode.getChildren().get(0).getChildren().get(1).getValue().equals("ROR")) {
          return postIndexedROR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(1).getValue()));
        } else if (rootNode.getChildren().get(0).getChildren().get(1).getValue().equals("RRX")) {
          return postIndexedRRX(baseOffset, environment, instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren().get(0).getChildren().get(1).getChildren().get(0).getValue()));
        } else {
          throw new InternalTranslationException(
              "Error: AddressOperandTypeTwo postIndexed shifter is not valid");
        }
      }
    } else if (rootNode.getChildren().get(0).getValue().equals("[")) {
      // matched offset
      if (rootNode.getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.IMMEDIATE_INTEGER) {
        return new Pair<String, String>(
            rootNode.getChildren().get(0).getChildren().get(0).getValue(), "");
      } else if (rootNode.getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.REGISTER) {
        return new Pair<String, String>(
            (rootNode.getChildren().get(0).getChildren().get(0).getValue()), "");
      }
 else if ((rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.REGISTER) && (
          rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(1).getType()
          == ExpressionType.IMMEDIATE_INTEGER)) {
        // matched [ Rn , #imm ]
        return offsetImm(baseOffset, environment, instructions,
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(1).getValue()));
      } else if ((rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getType()
          == ExpressionType.REGISTER) && (
          rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(1).getType()
          == ExpressionType.REGISTER)) {
        // matched [ Rn , Rm ]
        return offsetReg(baseOffset, environment, instructions,
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
            (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(1).getValue()));
      } else {
        // matched [ Rn , Rm, <shift>, #imm ]
        if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("LSL")) {
          return offsetLSL(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("LSR")) {
          return offsetLSR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("ASR")) {
          return offsetASR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("ROR")) {
          return offsetROR(baseOffset,
              environment,
              instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(1).getValue()));
        } else if (rootNode.getChildren()
            .get(0)
            .getChildren()
            .get(0)
            .getChildren()
            .get(1)
            .getValue()
            .equals("RRX")) {
          return offsetRRX(baseOffset, environment, instructions,
              (rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue()),
              (rootNode.getChildren()
                  .get(0)
                  .getChildren()
                  .get(0)
                  .getChildren()
                  .get(1)
                  .getChildren()
                  .get(0).getValue()));
        } else {
          throw new InternalTranslationException(
              "Error: AddressOperandTypeTwo offset shifter is not valid");
        }
      }
    } else {
      throw new InternalTranslationException(
          "Error: AddressOperandTypeTwo rootNodeValue is not valid "
          + rootNode.getChildren().get(0).getValue() + " " + instruction.getMnemonic());
    }
  }
}
