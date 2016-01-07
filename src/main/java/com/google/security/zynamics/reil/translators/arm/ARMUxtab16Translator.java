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


public class ARMUxtab16Translator extends ARMBaseTranslator {
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

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String highResult = environment.getNextVariableString();
    final String lowResult = environment.getNextVariableString();
    final String operand2 = environment.getNextVariableString();
    final String operand2from15to0 = environment.getNextVariableString();
    final String operand2from31to16 = environment.getNextVariableString();
    final String tmpHighResult = environment.getNextVariableString();
    final String tmpLowResult = environment.getNextVariableString();
    final String tmpRn15to0 = environment.getNextVariableString();
    final String tmpRn31to16 = environment.getNextVariableString();

    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String shifterOperand = shifterPair.first();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shifterOperand, dw,
        String.valueOf(0x00FF00FFL), dw, operand2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand2, dw, String.valueOf(0xFFFFL),
        dw, operand2from15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, operand2, dw, String.valueOf(-16L),
        dw, operand2from31to16));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0xFFFFL), dw, tmpRn15to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-16L), dw, tmpRn31to16));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, operand2from15to0, dw, tmpRn15to0, dw,
        tmpLowResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpLowResult, dw,
        String.valueOf(0xFFFFL), dw, lowResult));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, operand2from31to16, dw, tmpRn31to16,
        dw, tmpHighResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpHighResult, dw,
        String.valueOf(0xFFFFL), dw, highResult));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, highResult, wd, String.valueOf(16L),
        dw, targetRegister));

    instructions.add(ReilHelpers.createOr(baseOffset++, dw, targetRegister, dw, lowResult, dw,
        targetRegister));
  }

  /**
   * UXTAB16{<cond>} <Rd>, <Rn>, <Rm>{, <rotation>}
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then operand2 = (Rm Rotate_Right(8 * rotate)) AND 0x00ff00ff Rd[15:0]
   * = Rn[15:0] + operand2[15:0] Rd[31:16] = Rn[31:16] + operand2[23:16]
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UXTAB16");
    translateAll(environment, instruction, "UXTAB16", instructions);
  }
}
