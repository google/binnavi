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


public class ARMUsub16Translator extends ARMBaseTranslator {
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

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpResult = environment.getNextVariableString();
    final String tmpRm15to0 = environment.getNextVariableString();
    final String tmpRm31to16 = environment.getNextVariableString();
    final String tmpRn15to0 = environment.getNextVariableString();
    final String tmpRn31to16 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar1Safe = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar3Safe = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();

    // Low
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFFFFL), dw, tmpRn15to0));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0xFFFFL), dw, tmpRm15to0));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRn15to0, dw, tmpRm15to0, dw,
        tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw, String.valueOf(0xFFFFL),
        dw, tmpVar1Safe));

    // High
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(-16), dw, tmpRn31to16));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(-16), dw, tmpRm31to16));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRn31to16, dw, tmpRm31to16, dw,
        tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar3, dw, String.valueOf(0xFFFFL),
        dw, tmpVar3Safe));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3Safe, dw, String.valueOf(16L),
        dw, tmpResult));

    // Result
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpResult, dw, tmpVar1Safe, dw,
        targetRegister));

    // CPSR GE
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, wd, String.valueOf(-15L), bt,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, dw, String.valueOf(1L), bt,
        "CPSR_GE_0"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3, wd, String.valueOf(-15L), bt,
        tmpVar5));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar5, dw, String.valueOf(1L), bt,
        "CPSR_GE_2"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));
  }

  /**
   * USUB16{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15:0] = Rn[15:0] - Rm[15:0] GE[1:0] = if BorrowFrom(Rn[15:0] -
   * Rm[15:0]) then 0 else 0b11 Rd[31:16] = Rn[31:16] - Rm[31:16] GE[3:2] = if BorrowFrom(Rn[31:16]
   * - Rm[31:16]) then 0 else 0b11
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "USUB16");
    translateAll(environment, instruction, "USUB16", instructions);
  }
}
