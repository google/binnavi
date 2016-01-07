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

import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class ARMQadd16Translator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();
    final String sourceRegister1 = registerOperand2.getValue();
    final String sourceRegister2 = registerOperand3.getValue();

    final String addOperation = "ADD";

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected int getResultShiftDelta() {
        return 0;
      }

      @Override
      protected String[] process(final long offset, final String[] firstTwo,
          final String[] secondTwo) {
        final String sum1 = environment.getNextVariableString();
        final String sum2 = environment.getNextVariableString();
        final String sum1Sat = environment.getNextVariableString();
        final String sum2Sat = environment.getNextVariableString();

        long baseOffset = offset;

        // do the adds
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[0], dw, secondTwo[0], dw,
            sum1));
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[1], dw, secondTwo[1], dw,
            sum2));

        // Do the Sat
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstTwo[0], dw,
            secondTwo[0], dw, sum1, addOperation, sum1Sat, 16L, "");
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstTwo[1], dw,
            secondTwo[1], dw, sum2, addOperation, sum2Sat, 16L, "");

        return new String[] {sum1Sat, sum2Sat};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * QADD16{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15:0] = SignedSat(Rn[15:0] + Rm[15:0], 16) Rd[31:16] =
   * SignedSat(Rn[31:16] + Rm[31:16], 16)
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "QADD16");
    translateAll(environment, instruction, "QADD16", instructions);
  }
}
