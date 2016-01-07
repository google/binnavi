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


public class ARMSmuadTranslator extends ARMBaseTranslator {
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
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;
    final OperandSize wd = OperandSize.WORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String operand2 = environment.getNextVariableString();
    final String operand2from15to0 = environment.getNextVariableString();
    final String operand2from31to16 = environment.getNextVariableString();
    final String registerRm15to0 = environment.getNextVariableString();
    final String registerRm31to16 = environment.getNextVariableString();
    final String tmpRotate1 = environment.getNextVariableString();
    final String tmpRotate2 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String product1 = environment.getNextVariableString();
    final String product2 = environment.getNextVariableString();

    if (instruction.getMnemonic().contains("X")) {
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, bt,
          String.valueOf(-16), dw, tmpRotate1));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, bt,
          String.valueOf(16), dw, tmpRotate2));
      instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpRotate1, dw, tmpRotate2, dw,
          operand2));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand2, dw,
          String.valueOf(0xFFFFFFFFL), dw, operand2));
    } else {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, sourceRegister2, dw, operand2));
    }

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand2, dw, String.valueOf(0xFFFFL),
        dw, operand2from15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, operand2, dw, String.valueOf(-16L),
        dw, operand2from31to16));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFFFFL), dw, registerRm15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(-16L), dw, registerRm31to16));

    Helpers.signedMul(baseOffset, environment, instruction, instructions, wd, registerRm15to0, wd,
        operand2from15to0, dw, product1);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    instructions.add(ReilHelpers.createNop(baseOffset++));
    Helpers.signedMul(baseOffset, environment, instruction, instructions, wd, registerRm31to16, wd,
        operand2from31to16, dw, product2);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createAdd(baseOffset++, qw, product1, qw, product2, qw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));
    Helpers.overflowCondition(baseOffset, environment, instruction, instructions, tmpVar1);
  }

  /**
   * SMUAD{X}{<cond>} <Rd>, <Rm>, <Rs>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if X == 1 then operand2 = Rs Rotate_Right 16 else operand2 = Rs
   * product1 = Rm[15:0] * operand2[15:0] // Signed multiplication product2 = Rm[31:16] *
   * operand2[31:16] // Signed multiplication Rd = product1 + product2 if OverflowFrom(product1 +
   * product2) then Q flag = 1
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SMUAD");
    translateAll(environment, instruction, "SMUAD", instructions);
  }
}
