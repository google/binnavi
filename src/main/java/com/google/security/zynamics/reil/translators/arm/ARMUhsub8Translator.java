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


public class ARMUhsub8Translator extends ARMBaseTranslator {
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

    final OperandSize wd = OperandSize.WORD;

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected int getResultShiftDelta() {
        return -1;
      }

      @Override
      protected String[] process(final long offset, final String[] firstFour,
          final String[] secondFour) {
        final String diff1 = environment.getNextVariableString();
        final String diff2 = environment.getNextVariableString();
        final String diff3 = environment.getNextVariableString();
        final String diff4 = environment.getNextVariableString();

        long baseOffset = offset;

        instructions.add(ReilHelpers.createSub(baseOffset++, wd, firstFour[0], wd, secondFour[0],
            dw, diff1));
        instructions.add(ReilHelpers.createSub(baseOffset++, wd, firstFour[1], wd, secondFour[1],
            dw, diff2));
        instructions.add(ReilHelpers.createSub(baseOffset++, wd, firstFour[2], wd, secondFour[2],
            dw, diff3));
        instructions.add(ReilHelpers.createSub(baseOffset++, wd, firstFour[3], wd, secondFour[3],
            dw, diff4));

        return new String[] {diff1, diff2, diff3, diff4};
      }
    }.generate(environment, baseOffset, 8, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * UHSUB8{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then diff = Rn[7:0] - Rm[7:0] // Unsigned subtraction Rd[7:0] =
   * diff[8:1] diff = Rn[15:8] - Rm[15:8] // Unsigned subtraction Rd[15:8] = diff[8:1] diff =
   * Rn[23:16] - Rm[23:16] // Unsigned subtraction Rd[23:16] = diff[8:1] diff = Rn[31:24] -
   * Rm[31:24] // Unsigned subtraction Rd[31:24] = diff[8:1]
   * 
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UHSUB8");
    translateAll(environment, instruction, "UHSUB8", instructions);
  }
}
