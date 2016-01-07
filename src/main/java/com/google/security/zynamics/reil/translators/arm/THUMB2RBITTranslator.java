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


public class THUMB2RBITTranslator extends ARMBaseTranslator {

  /**
   * RBIT{<c>}{<q><Rd>, <Rm>
   * 
   * if ConditionPassed() then EncodingSpecificOperations(); bits(32) result; for i = 0 to 31
   * result<31-i> = R[m]<i>; R[d] = result;
   */
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String sourceRegister = registerOperand2.getValue();
    final String destinationRegister = registerOperand1.getValue();

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    final OperandSize dw = OperandSize.DWORD;

    final String temporaryRegister =
        Helpers.reverseUnsignedInteger(environment, baseOffset, sourceRegister, instructions);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, temporaryRegister, dw,
        String.valueOf(0xFFFFFFFFL), dw, destinationRegister));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "RBIT");
    translateAll(environment, instruction, "RBIT", instructions);
  }
}
