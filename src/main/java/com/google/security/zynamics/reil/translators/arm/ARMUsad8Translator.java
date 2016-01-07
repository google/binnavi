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

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class ARMUsad8Translator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister1 = (registerOperand2.getValue());
    final String sourceRegister2 = (registerOperand3.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String diff1 = environment.getNextVariableString();
    final String diff11 = environment.getNextVariableString();
    final String diff12 = environment.getNextVariableString();
    final String diff1MaskRmRs = environment.getNextVariableString();
    final String diff1MaskRsRm = environment.getNextVariableString();
    final String diff1RmRs = environment.getNextVariableString();
    final String diff1RsRm = environment.getNextVariableString();
    final String diff2 = environment.getNextVariableString();
    final String diff21 = environment.getNextVariableString();
    final String diff22 = environment.getNextVariableString();
    final String diff2MaskRmRs = environment.getNextVariableString();
    final String diff2MaskRsRm = environment.getNextVariableString();
    final String diff2RmRs = environment.getNextVariableString();
    final String diff2RsRm = environment.getNextVariableString();
    final String diff3 = environment.getNextVariableString();
    final String diff31 = environment.getNextVariableString();
    final String diff32 = environment.getNextVariableString();
    final String diff3MaskRmRs = environment.getNextVariableString();
    final String diff3MaskRsRm = environment.getNextVariableString();
    final String diff3RmRs = environment.getNextVariableString();
    final String diff3RsRm = environment.getNextVariableString();
    final String diff4 = environment.getNextVariableString();
    final String diff41 = environment.getNextVariableString();
    final String diff42 = environment.getNextVariableString();
    final String diff4MaskRmRs = environment.getNextVariableString();
    final String diff4MaskRsRm = environment.getNextVariableString();
    final String diff4RmRs = environment.getNextVariableString();
    final String diff4RsRm = environment.getNextVariableString();
    final String isSmaller1 = environment.getNextVariableString();
    final String isSmaller2 = environment.getNextVariableString();
    final String isSmaller3 = environment.getNextVariableString();
    final String isSmaller4 = environment.getNextVariableString();
    final String sum1 = environment.getNextVariableString();
    final String sum2 = environment.getNextVariableString();
    final String tmpRm15to8 = environment.getNextVariableString();
    final String tmpRm23to16 = environment.getNextVariableString();
    final String tmpRm31to24 = environment.getNextVariableString();
    final String tmpRm7to0 = environment.getNextVariableString();
    final String tmpRs15to8 = environment.getNextVariableString();
    final String tmpRs23to16 = environment.getNextVariableString();
    final String tmpRs31to24 = environment.getNextVariableString();
    final String tmpRs7to0 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String zeroDiff1 = environment.getNextVariableString();
    final String zeroDiff2 = environment.getNextVariableString();
    final String zeroDiff3 = environment.getNextVariableString();
    final String zeroDiff4 = environment.getNextVariableString();

    // Rm
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x000000FFL), dw, tmpRm7to0));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x0000FF00L), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-8), dw,
        tmpRm15to8));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x00FF0000L), dw, tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, dw, String.valueOf(-16), dw,
        tmpRm23to16));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFF000000L), dw, tmpVar3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3, dw, String.valueOf(-24), dw,
        tmpRm31to24));

    // Rs
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x000000FFL), dw, tmpRs7to0));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x0000FF00L), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-8), dw,
        tmpRs15to8));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x00FF0000L), dw, tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, dw, String.valueOf(-16), dw,
        tmpRs23to16));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0xFF000000L), dw, tmpVar3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3, dw, String.valueOf(-24), dw,
        tmpRs31to24));

    // Do the compares
    Helpers.unsignedCompareXSmallerY(baseOffset, environment, instruction, instructions, dw,
        tmpRm7to0, dw, tmpRs7to0, dw, isSmaller1);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.unsignedCompareXSmallerY(baseOffset, environment, instruction, instructions, dw,
        tmpRm15to8, dw, tmpRs15to8, dw, isSmaller2);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.unsignedCompareXSmallerY(baseOffset, environment, instruction, instructions, dw,
        tmpRm23to16, dw, tmpRs23to16, dw, isSmaller3);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.unsignedCompareXSmallerY(baseOffset, environment, instruction, instructions, dw,
        tmpRm31to24, dw, tmpRs31to24, dw, isSmaller4);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // prepare the masks according to the comparison results
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0L), dw, isSmaller1,
        dw, diff1MaskRsRm));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, diff1MaskRsRm, dw,
        String.valueOf(0xFFFFFFFFL), dw, diff1MaskRmRs));

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0L), dw, isSmaller2,
        dw, diff2MaskRsRm));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, diff2MaskRsRm, dw,
        String.valueOf(0xFFFFFFFFL), dw, diff2MaskRmRs));

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0L), dw, isSmaller3,
        dw, diff3MaskRsRm));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, diff3MaskRsRm, dw,
        String.valueOf(0xFFFFFFFFL), dw, diff3MaskRmRs));

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0L), dw, isSmaller4,
        dw, diff4MaskRsRm));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, diff4MaskRsRm, dw,
        String.valueOf(0xFFFFFFFFL), dw, diff4MaskRmRs));

    // do the subs.
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRs7to0, dw, tmpRm7to0, dw,
        diff1RsRm));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRs15to8, dw, tmpRm15to8, dw,
        diff2RsRm));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRs23to16, dw, tmpRm23to16, dw,
        diff3RsRm));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRs31to24, dw, tmpRm31to24, dw,
        diff4RsRm));

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRm7to0, dw, tmpRs7to0, dw,
        diff1RmRs));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRm15to8, dw, tmpRs15to8, dw,
        diff2RmRs));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRm23to16, dw, tmpRs23to16, dw,
        diff3RmRs));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRm31to24, dw, tmpRs31to24, dw,
        diff4RmRs));

    // filter the ones really used
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff1RsRm, dw, diff1MaskRsRm, dw,
        diff11));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff2RsRm, dw, diff2MaskRsRm, dw,
        diff21));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff3RsRm, dw, diff3MaskRsRm, dw,
        diff31));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff4RsRm, dw, diff4MaskRsRm, dw,
        diff41));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff1RmRs, dw, diff1MaskRmRs, dw,
        diff12));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff2RmRs, dw, diff2MaskRmRs, dw,
        diff22));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff3RmRs, dw, diff3MaskRmRs, dw,
        diff32));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff4RmRs, dw, diff4MaskRmRs, dw,
        diff42));

    // get real diff results
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff11, dw, diff12, dw, diff1));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff21, dw, diff22, dw, diff2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff31, dw, diff32, dw, diff3));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff41, dw, diff42, dw, diff4));

    // zero extend results
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff1, dw, String.valueOf(0xFFL), dw,
        zeroDiff1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff2, dw, String.valueOf(0xFFL), dw,
        zeroDiff2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff3, dw, String.valueOf(0xFFL), dw,
        zeroDiff3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff4, dw, String.valueOf(0xFFL), dw,
        zeroDiff4));

    // perform the addition to receive the result
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, zeroDiff1, dw, zeroDiff2, dw, sum1));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, zeroDiff3, dw, zeroDiff4, dw, sum2));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sum1, dw, sum2, dw, targetRegister));
  }

  /**
   * USAD8{<cond>} <Rd>, <Rm>, <Rs>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if Rm[7:0] < Rs[7:0] then // Unsigned comparison diff1 = Rs[7:0]
   * - Rm[7:0] else diff1 = Rm[7:0] - Rs[7:0] if Rm[15:8] < Rs[15:8] then // Unsigned comparison
   * diff2 = Rs[15:8] - Rm[15:8] else diff2 = Rm[15:8] - Rs[15:8] if Rm[23:16] < Rs[23:16] then //
   * Unsigned comparison diff3 = Rs[23:16] - Rm[23:16] else diff3 = Rm[23:16] - Rs[23:16] if
   * Rm[31:24] < Rs[31:24] then // Unsigned comparison diff4 = Rs[31:24] - Rm[31:24] else diff4 =
   * Rm[31:24] - Rs[31:24] Rd = ZeroExtend(diff1) + ZeroExtend(diff2) + ZeroExtend(diff3) +
   * ZeroExtend(diff4]
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "USAD8");
    translateAll(environment, instruction, "USAD8", instructions);
  }
}
