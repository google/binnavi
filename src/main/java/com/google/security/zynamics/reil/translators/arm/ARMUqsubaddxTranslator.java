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


public class ARMUqsubaddxTranslator extends ARMBaseTranslator {
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

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    new Processor() {
      @Override
      protected String[] process(long baseOffset, final String[] firstTwo, final String[] secondTwo) {
        final String diff1 = environment.getNextVariableString();
        final String sum1 = environment.getNextVariableString();
        final String diff1Sat = environment.getNextVariableString();
        final String sum1Sat = environment.getNextVariableString();

        // do the add
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[0], dw, secondTwo[1], dw,
            sum1));

        // do the sub
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstTwo[1], dw, secondTwo[0], dw,
            diff1));

        // do the sat
        Helpers.unsignedSat(baseOffset, environment, instruction, instructions, firstTwo[0],
            secondTwo[1], sum1, "ADD", sum1Sat, 16, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        Helpers.unsignedSat(baseOffset, environment, instruction, instructions, firstTwo[1],
            secondTwo[0], diff1, "SUB", diff1Sat, 16, "");
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        return new String[] {sum1Sat, diff1Sat};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);

  }

  /**
   * UQSUBADDX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[31:16] = UnsignedSat(Rn[31:16] - Rm[15:0], 16) Rd[15:0] =
   * UnsignedSat(Rn[15:0] + Rm[31:16], 16)
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("UQSAX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UQSAX");
      translateAll(environment, instruction, "UQSAX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "UQSUBADDX");
      translateAll(environment, instruction, "UQSUBADDX", instructions);
    }
  }
}
