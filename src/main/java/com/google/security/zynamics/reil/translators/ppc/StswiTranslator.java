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
package com.google.security.zynamics.reil.translators.ppc;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class StswiTranslator implements IInstructionTranslator {

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "stswi");

    final IOperandTreeNode countRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode addressRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode numBytes =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    // extract number of Bytes
    int n = numBytes.getValue() == "0" ? 32 : Integer.decode(numBytes.getValue());

    final OperandSize dw = OperandSize.DWORD;

    final String effectiveAddress = environment.getNextVariableString();
    final String tmpData = environment.getNextVariableString();

    Long baseOffset = instruction.getAddress().toLong() * 0x100;

    // always compute effective address
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, addressRegister.getValue(), dw,
        String.valueOf(0xFFFFFFFFL), dw, effectiveAddress));

    int r = Helpers.getRegisterIndex(countRegister.getValue()) - 1;

    while (n > 0) {
      // increment target register
      r = (r + 1) % 32;

      if (n == 3) {
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw,
            Helpers.getRealRegisterName("r" + r), dw, String.valueOf(0xFFFFFF00L), dw, tmpData));
      } else if (n == 2) {
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw,
            Helpers.getRealRegisterName("r" + r), dw, String.valueOf(0xFFFF0000L), dw, tmpData));
      } else if (n == 1) {
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw,
            Helpers.getRealRegisterName("r" + r), dw, String.valueOf(0xFF000000L), dw, tmpData));
      } else {
        instructions.add(ReilHelpers.createStr(baseOffset++, dw,
            Helpers.getRealRegisterName("r" + r), dw, tmpData));
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, effectiveAddress, dw,
            String.valueOf(4L), dw, effectiveAddress));
      }
      // do store
      instructions.add(ReilHelpers.createStm(baseOffset++, dw, tmpData, dw, effectiveAddress));

      n -= 4;
    }

  }
}
