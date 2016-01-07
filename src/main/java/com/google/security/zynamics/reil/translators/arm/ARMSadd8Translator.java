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


public class ARMSadd8Translator extends ARMBaseTranslator {
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

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    new Processor() {
      @Override
      protected String[] process(
          final long offset, final String[] firstFour, final String[] secondFour) {
        final String sum1 = environment.getNextVariableString();
        final String sum2 = environment.getNextVariableString();
        final String sum3 = environment.getNextVariableString();
        final String sum4 = environment.getNextVariableString();

        long baseOffset = offset;

        instructions.add(
            ReilHelpers.createAdd(baseOffset++, dw, firstFour[0], dw, secondFour[0], dw, sum1));
        instructions.add(
            ReilHelpers.createAdd(baseOffset++, dw, firstFour[1], dw, secondFour[1], dw, sum2));
        instructions.add(
            ReilHelpers.createAdd(baseOffset++, dw, firstFour[2], dw, secondFour[2], dw, sum3));
        instructions.add(
            ReilHelpers.createAdd(baseOffset++, dw, firstFour[3], dw, secondFour[3], dw, sum4));

        // CPSR GE
        instructions.add(
            ReilHelpers.createBsh(baseOffset++, dw, sum1, wd, String.valueOf(-8), bt, "CPSR_GE_0"));
        instructions.add(
            ReilHelpers.createBsh(baseOffset++, dw, sum2, wd, String.valueOf(-8), bt, "CPSR_GE_1"));
        instructions.add(
            ReilHelpers.createBsh(baseOffset++, dw, sum3, wd, String.valueOf(-8), bt, "CPSR_GE_2"));
        instructions.add(
            ReilHelpers.createBsh(baseOffset++, dw, sum4, wd, String.valueOf(-8), bt, "CPSR_GE_3"));

        return new String[] {sum1, sum2, sum3, sum4};
      }
    }.generate(
        environment, baseOffset, 8, sourceRegister1, sourceRegister2, targetRegister, instructions);
  }

  /**
   * SADD8{<cond>} <Rd>, <Rn>, <Rm>
   *
   *  Operation:
   *
   *  if ConditionPassed(cond) then sum = Rn[7:0] + Rm[7:0] // Signed addition Rd[7:0] = sum[7:0]
   * GE[0] = if sum >= 0 then 1 else 0 sum = Rn[15:8] + Rm[15:8] // Signed addition Rd[15:8] =
   * sum[7:0] GE[1] = if sum >= 0 then 1 else 0 sum = Rn[23:16] + Rm[23:16] // Signed addition
   * Rd[23:16] = sum[7:0] GE[2] = if sum >= 0 then 1 else 0 sum = Rn[31:24] + Rm[31:24] // Signed
   * addition Rd[31:24] = sum[7:0] GE[3] = if sum >= 0 then 1 else 0
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SADD8");
    translateAll(environment, instruction, "SADD8", instructions);
  }
}
