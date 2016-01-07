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


public class ARMPkhbtTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode shifter = instruction.getOperands().get(2).getRootNode();

    final String targetRegister = registerOperand1.getValue();
    final String sourceRegister = registerOperand2.getValue();

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String shifterOperand = shifterPair.first();
    final String tmpResult1 = environment.getNextVariableString();
    final String tmpResult2 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shifterOperand, dw,
        String.valueOf(0xFFFF0000L), dw, tmpResult1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0xFFFFL), dw, tmpResult2));

    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpResult1, dw, tmpResult2, dw,
        targetRegister));
  }

  /**
   * PKHBT {<cond>} <Rd>, <Rn>, <Rm> {, LSL #<shift_imm>}
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15:0] = Rn[15:0] Rd[31:16] = (Rm Logical_Shift_Left
   * shift_imm)[31:16]
   * 
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "PKHBT");
    translateAll(environment, instruction, "PKHBT", instructions);
  }
}
