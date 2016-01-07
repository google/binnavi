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


public class ARMUaddsubxTranslator extends ARMBaseTranslator {
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
    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String andedRm = environment.getNextVariableString();
    final String shiftedRn = environment.getNextVariableString();
    final String tmpHighResult = environment.getNextVariableString();
    final String andedRn = environment.getNextVariableString();
    final String shiftedRm = environment.getNextVariableString();
    final String tmpLowResult = environment.getNextVariableString();
    final String highResult = environment.getNextVariableString();
    final String lowResult = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    // Rd[31:16] = Rn[31:16] + Rm[15:0]
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(-16), dw, shiftedRn));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0xFFFFL), dw, andedRm));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, shiftedRn, dw, andedRm, dw,
        tmpHighResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpHighResult, dw,
        String.valueOf(0xFFFFL), dw, highResult));

    // Rd[15:0] = Rn[15:0] - Rm[31:16]
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFFFFL), dw, andedRn));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(-16), dw, shiftedRm));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, andedRn, dw, shiftedRm, dw,
        tmpLowResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpLowResult, dw,
        String.valueOf(0xFFFFL), dw, lowResult));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, highResult, dw, String.valueOf(16),
        dw, tmpVar1));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, lowResult, dw,
        targetRegister));

    // CPSR GE
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpHighResult, wd,
        String.valueOf(-16L), bt, tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L), bt,
        "CPSR_GE_2"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));

    // TODO this night not reflect borrow
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpHighResult, wd,
        String.valueOf(-15L), bt, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
        "CPSR_GE_0"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));
  }

  /**
   * UADDSUBX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then sum = Rn[31:16] + Rm[15:0] // unsigned addition Rd[31:16] =
   * sum[15:0] GE[3:2] = if CarryFrom16(Rn[31:16] + Rm[15:0]) then 0b11 else 0 diff = Rn[15:0] -
   * Rm[31:16] // unsigned subtraction Rd[15:0] = diff[15:0] GE[1:0] = if BorrowFrom(Rn[15:0] -
   * Rm[31:16]) then 0b11 else 0
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("UASX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UASX");
      translateAll(environment, instruction, "UASX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "UADDSUBX");
      translateAll(environment, instruction, "UADDSUBX", instructions);
    }
  }
}
