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


public class ARMMlaTranslator extends ARMBaseTranslator {
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

    final String targetRegister = (registerOperand1.getValue());
    final String multRegister1 = (registerOperand2.getValue());
    final String multRegister2 = (registerOperand3.getValue());
    final String addRegister = (registerOperand4.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    // Rd = (Rm * Rs + Rn)[31:0]
    instructions.add(ReilHelpers.createMul(baseOffset++, dw, multRegister1, dw, multRegister2, qw,
        tmpVar1));
    instructions
        .add(ReilHelpers.createAdd(baseOffset++, qw, tmpVar1, dw, addRegister, qw, tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar2, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));

    if (instruction.getMnemonic().endsWith("S") && (instruction.getMnemonic().length() != 5)) {
      // match the case where we have to set flags this does not handle the S == 1 and Rd == R15
      // case !!!
      final String tmpVar3 = environment.getNextVariableString();

      // N Flag Rd[31]
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, dw,
          String.valueOf(-31L), bt, tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
          "N"));

      // Z Flag if Rd == 0 then 1 else 0
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt, "Z"));
    }
  }

  /**
   * MLA{<cond>}{S} <Rd>, <Rm>, <Rs>, <Rn>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd = (Rm * Rs + Rn)[31:0] if S == 1 then N Flag = Rd[31] Z Flag =
   * if Rd == 0 then 1 else 0 C Flag = unaffected in v5 and above, UNPREDICTABLE in v4 and earlier V
   * Flag = unaffected
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "MLA");
    translateAll(environment, instruction, "MLA", instructions);
  }
}
