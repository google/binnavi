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


public class ARMSmlalTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand4 =
        instruction.getOperands().get(3).getRootNode().getChildren().get(0);

    final String sourceRegister1 = (registerOperand1.getValue());
    final String sourceRegister2 = (registerOperand2.getValue());
    final String sourceRegister3 = (registerOperand3.getValue());
    final String sourceRegister4 = (registerOperand4.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String firstZero = environment.getNextVariableString();
    final String secondZero = environment.getNextVariableString();
    final String tmpCarry = environment.getNextVariableString();
    final String tmpResult1 = environment.getNextVariableString();
    final String tmpResult2 = environment.getNextVariableString();
    final String tmpResult3 = environment.getNextVariableString();
    final String tmpResult4 = environment.getNextVariableString();
    final String trueCarry = environment.getNextVariableString();
    final String tmpResult = environment.getNextVariableString();

    Helpers.signedMul(baseOffset, environment, instruction, instructions, dw, sourceRegister3, dw,
        sourceRegister4, qw, tmpResult);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // RdLo
    instructions.add(ReilHelpers.createAdd(baseOffset++, qw, tmpResult, dw, sourceRegister1, qw,
        tmpResult1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpResult1, dw,
        String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));

    // carry
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpResult1, wd, String.valueOf(-32),
        bt, tmpCarry));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpCarry, bt, String.valueOf(1L), bt,
        trueCarry));

    // RdHi
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpResult, dw, String.valueOf(-32L),
        dw, tmpResult2));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpResult2, bt, trueCarry, qw,
        tmpResult3));
    instructions.add(ReilHelpers.createAdd(baseOffset++, qw, tmpResult3, dw, sourceRegister2, qw,
        tmpResult4));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpResult4, dw,
        String.valueOf(0xFFFFFFFFL), dw, sourceRegister2));

    if (instruction.getMnemonic().endsWith("S") && (instruction.getMnemonic().length() != 7)) {
      /**
       * if S == 1 then N Flag = RdHi[31] Z Flag = if (RdHi == 0) and (RdLo == 0) then 1 else 0
       */

      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, wd,
          String.valueOf(-31), bt, "N"));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister2, bt, firstZero));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister1, bt, secondZero));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, firstZero, bt, secondZero, bt, "Z"));
    }
  }

  /**
   * SMLAL{<cond>}{S} <RdLo>, <RdHi>, <Rm>, <Rs>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then RdLo = (Rm * Rs)[31:0] + RdLo // Signed multiplication RdHi = (Rm
   * * Rs)[63:32] + RdHi + CarryFrom((Rm * Rs)[31:0] + RdLo) if S == 1 then N Flag = RdHi[31] Z Flag
   * = if (RdHi == 0) and (RdLo == 0) then 1 else 0 C Flag = unaffected V Flag = unaffected
   * 
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SMLAL");
    translateAll(environment, instruction, "SMLAL", instructions);
  }
}
