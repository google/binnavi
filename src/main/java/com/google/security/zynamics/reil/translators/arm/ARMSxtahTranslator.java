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


public class ARMSxtahTranslator extends ARMBaseTranslator {
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

    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String tmpOperand2from15to0 = environment.getNextVariableString();
    final String tmpOperand2from15to0signExtended = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String shifterOperand = shifterPair.first();

    // Operand2
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shifterOperand, wd,
        String.valueOf(0xFFFFL), dw, tmpOperand2from15to0));
    Helpers.signExtend(baseOffset, environment, instruction, instructions, dw,
        tmpOperand2from15to0, dw, tmpOperand2from15to0signExtended, 16);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegister, dw,
        tmpOperand2from15to0signExtended, qw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));
  }

  /**
   * SXTAH{<cond>} <Rd>, <Rn>, <Rm>{, <rotation>}
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then operand2 = Rm Rotate_Right(8 * rotate) Rd = Rn +
   * SignExtend(operand2[15:0])
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SXTAH");
    translateAll(environment, instruction, "SXTAH", instructions);
  }
}
