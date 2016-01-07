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


public class THUMB2MlsTranslator extends ARMBaseTranslator {

  /**
   * MLS<c><q><Rd>, <Rn>, <Rm>, <Ra> if ConditionPassed() then EncodingSpecificOperations();
   * operand1 = SInt(R[n]); // operand1 = UInt(R[n]) produces the same final results operand2 =
   * SInt(R[m]); // operand2 = UInt(R[m]) produces the same final results addend = SInt(R[a]); //
   * addend = UInt(R[a]) produces the same final results result = addend - operand1 * operand2; R[d]
   * = result<31:0>;
   */

  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final OperandSize qw = OperandSize.QWORD;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final IOperandTreeNode targetOperand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceOperand1 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceOperand2 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceOperand3 =
        instruction.getOperands().get(3).getRootNode().getChildren().get(0);

    final String targetRegister = targetOperand.getValue();
    final String sourceRegister1 = sourceOperand1.getValue();
    final String sourceRegister2 = sourceOperand2.getValue();
    final String sourceRegister3 = sourceOperand3.getValue();

    final String multiplicationResult = environment.getNextVariableString();
    final String subtractionResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createMul(baseOffset++, dw, sourceRegister1, dw, sourceRegister2,
        qw, multiplicationResult));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister3, qw,
        multiplicationResult, qw, subtractionResult));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, subtractionResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "MLS");
    translateAll(environment, instruction, "MLS", instructions);

  }
}
