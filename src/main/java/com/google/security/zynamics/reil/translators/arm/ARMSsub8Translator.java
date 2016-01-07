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


public class ARMSsub8Translator extends ARMBaseTranslator {
  @Override
  protected final void translateCore(final ITranslationEnvironment environment,
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

    final OperandSize bt = OperandSize.BYTE;

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstFour,
          final String[] secondFour) {
        final String tmpResult1 = environment.getNextVariableString();
        final String tmpResult1Not = environment.getNextVariableString();
        final String tmpResult2 = environment.getNextVariableString();
        final String tmpResult2Not = environment.getNextVariableString();
        final String tmpResult3 = environment.getNextVariableString();
        final String tmpResult3Not = environment.getNextVariableString();
        final String tmpResult4 = environment.getNextVariableString();
        final String tmpResult4Not = environment.getNextVariableString();
        final String trueResult1 = environment.getNextVariableString();
        final String trueResult2 = environment.getNextVariableString();
        final String trueResult3 = environment.getNextVariableString();
        final String trueResult4 = environment.getNextVariableString();

        long baseOffset = offset;

        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondFour[0],
            firstFour[0], tmpResult1, trueResult1);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondFour[1],
            firstFour[1], tmpResult2, trueResult2);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondFour[2],
            firstFour[2], tmpResult3, trueResult3);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondFour[3],
            firstFour[3], tmpResult4, trueResult4);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

        // CPSR_GE
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueResult1, dw,
            String.valueOf(0xFFL), dw, tmpResult1Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult1Not, dw,
            String.valueOf(-7L), bt, "CPSR_GE_0"));

        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueResult2, dw,
            String.valueOf(0xFFL), dw, tmpResult2Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult2Not, dw,
            String.valueOf(-7L), bt, "CPSR_GE_1"));

        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueResult3, dw,
            String.valueOf(0xFFL), dw, tmpResult3Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult3Not, dw,
            String.valueOf(-7L), bt, "CPSR_GE_2"));

        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueResult4, dw,
            String.valueOf(0xFFL), dw, tmpResult4Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult4Not, dw,
            String.valueOf(-7L), bt, "CPSR_GE_3"));

        return new String[] {tmpResult1, tmpResult2, tmpResult3, tmpResult4};
      }
    }.generate(environment, baseOffset, 8, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * SSUB8{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then diff = Rn[7:0] - Rm[7:0] // Signed subtraction Rd[7:0] =
   * diff[7:0] GE[0] = if diff >= 0 then 1 else 0 diff = Rn[15:8] - Rm[15:8] // Signed subtraction
   * Rd[15:8] = diff[7:0] GE[1] = if diff >= 0 then 1 else 0 diff = Rn[23:16] - Rm[23:16] // Signed
   * subtraction Rd[23:16] = diff[7:0] GE[2] = if diff >= 0 then 1 else 0 diff = Rn[31:24] -
   * Rm[31:24] // Signed subtraction Rd[31:24] = diff[7:0] GE[3] = if diff >= 0 then 1 else 0
   * 
   */

  @Override
  public final void translate(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SSUB8");
    translateAll(environment, instruction, "SSUB8", instructions);
  }
}
