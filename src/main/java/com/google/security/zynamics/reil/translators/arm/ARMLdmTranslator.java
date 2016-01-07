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


public class ARMLdmTranslator extends ARMBaseTranslator {
  /**
   * LDM{<cond>}<addressing_mode> <Rn>{!}, <registers>
   */

  /**
   * LDM{<cond>}<addressing_mode> <Rn>, <registers_without_pc>^
   */

  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    /**
     * LDM{<cond>}<addressing_mode> <Rn>{!}, <registers>
     */

    String typeValue = "";

    if (instruction.getMnemonic().endsWith(".W")) {
      typeValue =
          instruction.getMnemonic().length() == 9 ? instruction.getMnemonic().substring(5, 7)
              : instruction.getMnemonic().substring(3, 5);
    } else {
      typeValue =
          instruction.getMnemonic().length() == 7 ? instruction.getMnemonic().substring(5)
              : instruction.getMnemonic().substring(3);
    }

    IOperandTreeNode registerOperand1;

    String wBit = "1";

    if (instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().size() == 1) {
      wBit = "2";
      registerOperand1 =
          instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().get(0);
    } else {
      registerOperand1 = instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    }

    final IOperandTreeNode rootNodeOfRegisterList =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String registerNodeValue = (registerOperand1.getValue());
    final int registerListLength = rootNodeOfRegisterList.getChildren().size();

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    final String tmpValue = environment.getNextVariableString();

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String addressPair =
        AddressingModeFourGenerator.generate(baseOffset, environment, instruction, instructions,
            typeValue, registerNodeValue, wBit, rootNodeOfRegisterList);

    final String tmpAddress = addressPair;

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    for (int i = 0; i < registerListLength; i++) {
      if ((rootNodeOfRegisterList.getChildren().get(i).getValue()).equalsIgnoreCase("PC")) {
        /*
         * right now we do assume all code we receive is ARMv5 and above
         */
        instructions.add(ReilHelpers.createLdm(baseOffset++, dw, tmpAddress, dw, tmpValue));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpValue, dw,
            String.valueOf(0xFFFFFFFEL), dw, ("PC")));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpValue, bt, String.valueOf(1),
            bt, "T"));

        // if lower
        // instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpValue, dw,
        // String.valueOf(0xFFFFFFFCL), dw, ("PC")));
      } else {
        instructions.add(ReilHelpers.createLdm(baseOffset++, dw, tmpAddress, dw,
            (rootNodeOfRegisterList.getChildren().get(i).getValue())));
      }
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpAddress, bt, String.valueOf(4L),
          dw, tmpAddress));
    }
  }

  /**
   * LDM{<cond>}<addressing_mode> <Rn>{!}, <registers_and_pc>^
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "LDM");
    translateAll(environment, instruction, "LDM", instructions);
  }
}
