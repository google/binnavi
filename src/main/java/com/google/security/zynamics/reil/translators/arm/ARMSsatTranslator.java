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


public class ARMSsatTranslator extends ARMBaseTranslator {
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
    final String sourceImmediate = (registerOperand2.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String shifterOperand = shifterPair.first();
    // final String shifterCarryOut = shifterPair.second();

    Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, shifterOperand, dw,
        shifterOperand, dw, shifterOperand, "SSAT", targetRegister,
        Integer.decode(sourceImmediate), "Q");
  }

  /**
   * SSAT{<cond>} <Rd>, #<immed>, <Rm>{, <shift>}
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if shift == 1 then if shift_imm == 0 then operand = (Rm
   * Artihmetic_Shift_Right 32)[31:0] else operand = (Rm Artihmetic_Shift_Right shift_imm)[31:0]
   * else operand = (Rm Logical_Shift_Left shift_imm)[31:0] Rd = SignedSat(operand, sat_imm + 1) if
   * SignedDoesSat(operand, sat_imm + 1) then Q Flag = 1
   * 
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SSAT");
    translateAll(environment, instruction, "SSAT", instructions);
  }
}
