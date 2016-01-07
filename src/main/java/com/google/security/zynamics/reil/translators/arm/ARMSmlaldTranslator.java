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


public class ARMSmlaldTranslator extends ARMBaseTranslator {
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

    final String accValue = environment.getNextVariableString();
    final String operand2 = environment.getNextVariableString();
    final String operand2from15to0 = environment.getNextVariableString();
    final String operand2from31to16 = environment.getNextVariableString();
    final String registerRm15to0 = environment.getNextVariableString();
    final String registerRm31to16 = environment.getNextVariableString();
    final String tmpAccValue = environment.getNextVariableString();
    final String tmpResult1 = environment.getNextVariableString();
    final String tmpResult2 = environment.getNextVariableString();
    final String tmpResult3 = environment.getNextVariableString();
    final String tmpRotate1 = environment.getNextVariableString();
    final String tmpRotate2 = environment.getNextVariableString();
    final String product1 = environment.getNextVariableString();
    final String product2 = environment.getNextVariableString();

    if (instruction.getMnemonic().contains("X")) {
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister4, bt,
          String.valueOf(-16), dw, tmpRotate1));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister4, bt,
          String.valueOf(16), dw, tmpRotate2));
      instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpRotate1, dw, tmpRotate2, dw,
          operand2));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand2, dw,
          String.valueOf(0xFFFFFFFFL), dw, operand2));
    } else {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, sourceRegister4, dw, operand2));
    }

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, sourceRegister1, qw, accValue));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, wd,
        String.valueOf(32L), qw, tmpAccValue));
    instructions.add(ReilHelpers
        .createOr(baseOffset++, qw, tmpAccValue, dw, accValue, qw, accValue));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand2, dw, String.valueOf(0xFFFFL),
        dw, operand2from15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, operand2, dw, String.valueOf(-16L),
        dw, operand2from31to16));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister3, dw,
        String.valueOf(0xFFFFL), dw, registerRm15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister3, dw,
        String.valueOf(-16L), dw, registerRm31to16));

    Helpers.signedMul(baseOffset, environment, instruction, instructions, wd, registerRm15to0, wd,
        operand2from15to0, dw, product1);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.signedMul(baseOffset, environment, instruction, instructions, wd, registerRm31to16, wd,
        operand2from31to16, dw, product2);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers
        .createAdd(baseOffset++, dw, product1, dw, product2, qw, tmpResult1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpResult1, dw,
        String.valueOf(0xFFFFFFFFL), dw, tmpResult1));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpResult1, qw, accValue, qw,
        tmpResult2));

    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpResult2, dw,
        String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpResult2, qw,
        String.valueOf(0xFFFFFFFF00000000L), qw, tmpResult3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpResult3, dw, String.valueOf(-32L),
        dw, sourceRegister2));
  }

  /**
   * SMLALD{X}{<cond>} <RdLo>, <RdHi>, <Rm>, <Rs>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if X == 1 then operand2 = Rs Rotate_Right 16 else operand2 = Rs
   * accvalue[31:0] = RdLo accvalue[63:32] = RdHi product1 = Rm[15:0] * operand2[15:0] // Signed
   * multiplication product2 = Rm[31:16] * operand2[31:16] // Signed multiplication result =
   * accvalue + product1 + product2 // Signed addition RdLo = result[31:0] RdHi = result[63:32]
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SMLALD");
    translateAll(environment, instruction, "SMLALD", instructions);
  }
}
