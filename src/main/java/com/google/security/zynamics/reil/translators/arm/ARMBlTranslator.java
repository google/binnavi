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


public class ARMBlTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final String[] meta =
        instruction.getMnemonic().startsWith("BL") ? new String[] {"isCall", "true"}
            : new String[0];

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String sourceRegister1 = (registerOperand1.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    if (instruction.getMnemonic().startsWith("BL")) {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw,
          String.valueOf(instruction.getAddress().toLong() + 0x4), dw, ("LR")));
    }
    instructions.add(ReilHelpers.createJcc(baseOffset++, dw, String.valueOf(1L), dw,
        sourceRegister1, meta));
  }

  /**
   * B{L}{<cond>} <target_address>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if L == 1 then LR = address of the instruction after the branch
   * instruction PC = PC + (SignExtend_30(signed_immed_24) << 2)
   * 
   * @throws InternalTranslationException
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "B");

    if (instruction.getMnemonic().startsWith("BL")) {
      translateAll(environment, instruction, "BL", instructions);
    } else {
      translateAll(environment, instruction, "B", instructions);
    }
  }
}
