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

import java.util.List;


public class ARMSBFXTranslator extends ARMBaseTranslator {

  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final String destination =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String operand =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();
    final Integer lsb =
        Integer.parseInt(instruction.getOperands().get(2).getRootNode().getChildren().get(0)
            .getValue());
    final Integer width =
        Integer.parseInt(instruction.getOperands().get(3).getRootNode().getChildren().get(0)
            .getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Integer msb = (lsb + width) - 1;
    if (msb <= 31) {
      final long mask = TranslationHelpers.generateOneMask(lsb, width, OperandSize.DWORD);
      final String tempVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, operand, dw, String.valueOf(mask),
          dw, tempVar1));

      Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, tempVar1, dw,
          destination, 32);
    } else {
      instructions.add(ReilHelpers.createUnknown(baseOffset));
    }
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SBFX");
    translateAll(environment, instruction, "SBFX", instructions);
  }
}
