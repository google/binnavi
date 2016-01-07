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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


public class ARMRscTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode shifter = instruction.getOperands().get(2).getRootNode();

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister = (registerOperand2.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String notCFlag = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String shifterOperand = shifterPair.first();
    // final String shifterCarryOut = shifterPair.second();

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, shifterOperand, dw, sourceRegister,
        dw, tmpVar1));
    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "C", bt, notCFlag));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar1, bt, notCFlag, dw, tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));

    if (instruction.getMnemonic().endsWith("S") && (instruction.getMnemonic().length() != 5)) {
      // match the case where we have to set flags this does not handle the S == 1 and Rd == R15
      // case !!!
      final String tmpVar3 = environment.getNextVariableString();
      final String tmpVar4 = environment.getNextVariableString();

      final String msbVar1 = environment.getNextVariableString();
      final String msbVar2 = environment.getNextVariableString();

      // N Flag Rd[31]
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, dw,
          String.valueOf(-31L), bt, tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
          "N"));

      // Z Flag if Rd == 0 then 1 else 0
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt, "Z"));

      // TODO: borrow
      // C Flag NOT BorrowFrom(Rn + shifter_operand)
      // instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, wd, String.valueOf(-32L),
      // bt, tmpVar4));
      // instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, bt, String.valueOf(1L),
      // bt, "C"));

      // V OverflowFrom(shifter_operand - Rn - NOT(C Flag))
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, wd, String.valueOf(-31L),
          bt, msbVar1));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, wd,
          String.valueOf(-31L), bt, msbVar2));

      // ( msbA XOR msbB ) AND ( msbA XOR msbR ) == OF
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, msbVar1, dw, msbVar2, dw, tmpVar4));
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, msbVar1, dw, "N", dw, tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar4, dw, tmpVar3, dw, "V"));
    }
  }

  /**
   * RSC{<cond>}{S} <Rd>, <Rn>, <shifter_operand>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd = shifter_operand - Rn - NOT(C Flag) if S == 1 and Rd == R15
   * then if CurrentModeHasSPSR() then CPSR = SPSR else UNPREDICTABLE else if S == 1 then N Flag =
   * Rd[31] Z Flag = if Rd == 0 then 1 else 0 C Flag = NOT BorrowFrom(shifter_operand - Rn - NOT(C
   * Flag)) V Flag = OverflowFrom(shifter_operand - Rn - NOT(C Flag))
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "RSC");
    translateAll(environment, instruction, "RSC", instructions);
  }
}
