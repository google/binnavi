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


public class THUMB2MovtTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode immediateOperand1 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();
    final String immediateValue = immediateOperand1.getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize wd = OperandSize.WORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createBsh(baseOffset++, wd, immediateValue, wd, String.valueOf(16),
        dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, targetRegister, dw,
        String.valueOf(0xFFFFL), dw, tmpVar2));
    instructions
        .add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, targetRegister));
  }

  /**
   * MOVT{<c>}{<q><Rd>, #<imm16>
   * 
   * if ConditionPassed() then EncodingSpecificOperations(); R[d]<31:16> = imm16; R[d]<15:0>
   * unchanged
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "MOVT");
    translateAll(environment, instruction, "MOVT", instructions);
  }
}
