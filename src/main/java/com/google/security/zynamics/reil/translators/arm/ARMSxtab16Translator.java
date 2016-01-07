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


public class ARMSxtab16Translator extends ARMBaseTranslator {
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

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String tmpVar1 = environment.getNextVariableString();
    final String highResult = environment.getNextVariableString();
    final String lowResult = environment.getNextVariableString();
    final String tmpHighResult = environment.getNextVariableString();
    final String tmpLowResult = environment.getNextVariableString();
    final String tmpOperand2from23to16 = environment.getNextVariableString();
    final String tmpOperand2from23to16signExtended = environment.getNextVariableString();
    final String tmpOperand2from7to0 = environment.getNextVariableString();
    final String tmpOperand2from7to0signExtended = environment.getNextVariableString();
    final String tmpOperand2Shifted = environment.getNextVariableString();
    final String tmpRn15to0 = environment.getNextVariableString();
    final String tmpRn31to16 = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String shifterOperand = shifterPair.first();
    // final String shifterCarryOut = shifterPair.second();

    // Operand2
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shifterOperand, wd,
        String.valueOf(0xFFL), dw, tmpOperand2from7to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, shifterOperand, wd,
        String.valueOf(-16L), dw, tmpOperand2Shifted));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpOperand2Shifted, wd,
        String.valueOf(0xFFL), dw, tmpOperand2from23to16));

    Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, tmpOperand2from7to0,
        dw, tmpOperand2from7to0signExtended, 8);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.signExtend(baseOffset, environment, instruction, instructions, dw,
        tmpOperand2from23to16, dw, tmpOperand2from23to16signExtended, 8);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // Rn
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, wd,
        String.valueOf(0xFFFFL), dw, tmpRn15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, wd,
        String.valueOf(-16L), dw, tmpRn31to16));

    // ADD
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpRn15to0, dw,
        tmpOperand2from7to0signExtended, dw, tmpLowResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpLowResult, dw,
        String.valueOf(0xFFFFL), wd, lowResult));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpRn31to16, dw,
        tmpOperand2from23to16signExtended, dw, tmpHighResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpHighResult, dw,
        String.valueOf(0xFFFFL), wd, highResult));
    instructions.add(ReilHelpers.createBsh(baseOffset++, wd, highResult, wd, String.valueOf(16L),
        dw, tmpVar1));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, lowResult, dw,
        targetRegister));
  }

  /**
   * SXTAB16{<cond>} <Rd>, <Rn>, <Rm>{, <rotation>}
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then operand2 = Rm Rotate_Right(8 * rotate) Rd[15:0] = Rn[15:0] +
   * SignExtend(operand2[7:0]) Rd[31:16] = Rn[31:16] + SignExtend(operand2[23:16])
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SXTAB16");
    translateAll(environment, instruction, "SXTAB16", instructions);
  }
}
