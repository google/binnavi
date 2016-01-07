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


public class THUMB2BFCTranslator extends ARMBaseTranslator {

  /**
   * if ConditionPassed() then EncodingSpecificOperations(); if msbit >= lsbit then
   * R[d]<msbit:lsbit> = Replicate('0', msbit-lsbit+1); // Other bits of R[d] are unchanged else
   * UNPREDICTABLE;
   */
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode immediateOperand1 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode immediateOperand2 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final long clearMask =
        TranslationHelpers.generateZeroMask(Integer.parseInt(immediateOperand1.getValue()),
            Integer.parseInt(immediateOperand2.getValue()), OperandSize.DWORD);

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, registerOperand.getValue(), dw,
        String.valueOf(clearMask), dw, registerOperand.getValue()));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BFC");
    translateAll(environment, instruction, "BFC", instructions);
  }
}
