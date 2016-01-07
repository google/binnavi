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


public class ARMUmaalTranslator extends ARMBaseTranslator {
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

    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createMul(baseOffset++, dw, sourceRegister3, dw, sourceRegister4,
        qw, tmpVar1));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegister1, dw, sourceRegister2,
        qw, tmpVar2));

    instructions.add(ReilHelpers.createAdd(baseOffset++, qw, tmpVar1, qw, tmpVar2, qw, tmpVar3));

    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar3, dw,
        String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar3, wd, String.valueOf(-32L), dw,
        sourceRegister2));
  }

  /**
   * UMAAL{<cond>} <RdLo>, <RdHi>, <Rm>, <Rs>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then result = Rm * Rs + RdLo + RdHi // Unsigned multiplication and
   * additions RdLo = result[31:0] RdHi = result[63:32]
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UMAAL");
    translateAll(environment, instruction, "UMAAL", instructions);
  }
}
