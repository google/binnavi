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


public class ARMBicTranslator extends ARMBaseTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BIC");
    translateAll(environment, instruction, "BIC", instructions);
  }

  /**
   * BIC{<cond>}{S} <Rd>, <Rn>, <shifter_operand>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd = Rn AND NOT shifter_operand if S == 1 and Rd == R15 then if
   * CurrentModeHasSPSR() then CPSR = SPSR else UNPREDICTABLE else if S == 1 then N Flag = Rd[31] Z
   * Flag = if Rd == 0 then 1 else 0 C Flag = shifter_carry_out V Flag = unaffected
   */
  @Override
  public void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode shifter = instruction.getOperands().get(2).getRootNode();

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister = (registerOperand2.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String shifterOperand = shifterPair.first();

    // !x == -x -1
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0L), dw,
        shifterOperand, dw, tmpVar1));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar2));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw, tmpVar2, dw,
        targetRegister));

    if (instruction.getMnemonic().endsWith("S") && (instruction.getMnemonic().length() != 5)) {
      // match the case where we have to set flags this does not handle the S == 1 and Rd == R15
      // case !!!
      final String tmpVar3 = environment.getNextVariableString();

      final String shifterCarryOut = shifterPair.second();

      // N Flag Rd[31]
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, dw,
          String.valueOf(-31L), bt, tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
          "N"));

      // Z Flag if Rd == 0 then 1 else 0
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt, "Z"));

      // C Flag shifter_carry_out
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, shifterCarryOut, bt, "C"));
    }
  }
}
