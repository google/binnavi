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


public class THUMB2BFITranslator extends ARMBaseTranslator {
  /**
   * BFI{<c>}{<q><Rd>, <Rn>, #<lsb>, #<width>
   * 
   * <lsb>+<width>-1 = msbit
   * 
   * if ConditionPassed() then EncodingSpecificOperations(); if msbit >= lsbit then
   * R[d]<msbit:lsbit> = R[n]<(msbit-lsbit):0>; // Other bits of R[d] are unchanged else
   * UNPREDICTABLE;
   */
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode immediateOperand1 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);
    final IOperandTreeNode immediateOperand2 =
        instruction.getOperands().get(3).getRootNode().getChildren().get(0);

    final String sourceRegister = registerOperand2.getValue();
    final String destinationRegister = registerOperand1.getValue();

    final String tempVar1 = environment.getNextVariableString();
    final String tempVar2 = environment.getNextVariableString();
    final String tempVar3 = environment.getNextVariableString();

    final long oneMask =
        TranslationHelpers.generateOneMask(0, Integer.parseInt(immediateOperand2.getValue()),
            OperandSize.DWORD);

    final long zeroMask =
        TranslationHelpers.generateZeroMask(Integer.parseInt(immediateOperand1.getValue()),
            Integer.parseInt(immediateOperand2.getValue()), OperandSize.DWORD);

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(oneMask), dw, tempVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tempVar1, dw,
        immediateOperand1.getValue(), dw, tempVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, destinationRegister, dw,
        String.valueOf(zeroMask), dw, tempVar3));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tempVar2, dw, tempVar3, dw,
        destinationRegister));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BFI");
    translateAll(environment, instruction, "BFI", instructions);
  }
}
