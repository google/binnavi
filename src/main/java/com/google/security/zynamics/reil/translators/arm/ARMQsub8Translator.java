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


public class ARMQsub8Translator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister1 = (registerOperand2.getValue());
    final String sourceRegister2 = (registerOperand3.getValue());

    final String subOperation = "SUB";

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstFour,
          final String[] secondFour) {
        final String diff1 = environment.getNextVariableString();
        final String diff2 = environment.getNextVariableString();
        final String diff3 = environment.getNextVariableString();
        final String diff4 = environment.getNextVariableString();

        final String trueDiff1 = environment.getNextVariableString();
        final String trueDiff2 = environment.getNextVariableString();
        final String trueDiff3 = environment.getNextVariableString();
        final String trueDiff4 = environment.getNextVariableString();

        long baseOffset = offset;

        // do the subs
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstFour[0], dw, secondFour[0],
            dw, trueDiff1));
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstFour[1], dw, secondFour[1],
            dw, trueDiff2));
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstFour[2], dw, secondFour[2],
            dw, trueDiff3));
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstFour[3], dw, secondFour[3],
            dw, trueDiff4));

        // do the sats
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstFour[0], dw,
            secondFour[0], dw, trueDiff1, subOperation, diff1, 8, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstFour[1], dw,
            secondFour[1], dw, trueDiff2, subOperation, diff2, 8, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstFour[2], dw,
            secondFour[2], dw, trueDiff3, subOperation, diff3, 8, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, firstFour[3], dw,
            secondFour[3], dw, trueDiff4, subOperation, diff4, 8, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        return new String[] {diff1, diff2, diff3, diff4};
      }
    }.generate(environment, baseOffset, 8, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * QSUB8{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[7:0] = SignedSat(Rn[7:0] - Rm[7:0], 8) Rd[15:8] =
   * SignedSat(Rn[15:8] - Rm[15:8], 8) Rd[23:16] = SignedSat(Rn[23:16] - Rm[23:16], 8) Rd[31:24] =
   * SignedSat(Rn[31:24] - Rm[31:24], 8)
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "QSUB8");
    translateAll(environment, instruction, "QSUB8", instructions);
  }
}
