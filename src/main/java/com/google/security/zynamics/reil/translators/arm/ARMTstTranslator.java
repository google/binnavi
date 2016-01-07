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


public class ARMTstTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode shifter = instruction.getOperands().get(1).getRootNode();

    final String sourceRegister = (registerOperand1.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String shifterOperand = shifterPair.first();
    final String shifterCarryOut = shifterPair.second();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw, shifterOperand,
        dw, tmpVar1));

    // N Flag
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-31L), bt,
        "N"));

    // Z Flag
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, tmpVar1, bt, "Z"));

    // C Flag
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, shifterCarryOut, bt, "C"));
  }

  /**
   * TST{<cond>} <Rn>, <shifter_operand>
   * 
   * Operation:
   * 
   * 
   * if ConditionPassed(cond) then alu_out = Rn AND shifter_operand N Flag = alu_out[31] Z Flag = if
   * alu_out == 0 then 1 else 0 C Flag = shifter_carry_out V Flag = unaffected
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "TST");
    translateAll(environment, instruction, "TST", instructions);
  }
}
