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
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class THUMBLdrTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    // Hack for LDR Rx, =0xSOMEADDR
    IOperandTreeNode registerOperand2 = null;
    if ((instruction.getOperands()
        .get(1).getRootNode().getChildren().get(0).getChildren().get(0).getType()
        == ExpressionType.IMMEDIATE_INTEGER) || (instruction.getOperands()
        .get(1).getRootNode().getChildren().get(0).getChildren().get(0).getType()
        == ExpressionType.REGISTER)) {
      registerOperand2 =
          instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0);
    } else {
      registerOperand2 = instruction.getOperands()
          .get(1).getRootNode().getChildren().get(0).getChildren().get(0).getChildren().get(0);
    }

    IOperandTreeNode variableOperand1 = null;
    if (instruction.getOperands()
        .get(1).getRootNode().getChildren().get(0).getChildren().get(0).getChildren().size() == 2) {
      variableOperand1 = instruction.getOperands()
          .get(1).getRootNode().getChildren().get(0).getChildren().get(0).getChildren().get(1);
    }
    final String targetRegister1 = (registerOperand1.getValue());
    final String sourceRegister1 = (registerOperand2.getValue());
    final String sourceVariable1 = variableOperand1 == null ? "0" : (variableOperand1.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String tmpAddress = environment.getNextVariableString();

    if (sourceRegister1.equals("PC") || (sourceRegister1).equalsIgnoreCase(("PC"))) {
      /**
       * LDR <Rd>, [PC, #<immed_8> * 4]
       *
       *  Operation:
       *
       *  MemoryAccess(B-bit, E-bit) address = (PC & 0xFFFFFFFC) + (immed_8 * 4) Rd =
       * Memory[address, 4]
       */
      final String tmpVar1 = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(
          baseOffset++, dw, ("PC"), dw, String.valueOf(0xFFFFFFFCL), dw, tmpVar1));
      instructions.add(
          ReilHelpers.createAdd(baseOffset++, dw, tmpVar1, dw, sourceVariable1, dw, tmpAddress));
      instructions.add(ReilHelpers.createLdm(baseOffset++, dw, tmpAddress, dw, targetRegister1));
    } else
    /* sourceRegister2 != "SP" */
    {
      /**
       * LDR <Rd>, [<Rn>, #<immed_5> * 4]
       *
       *  Operation:
       *
       *  MemoryAccess(B-bit, E-bit) address = Rn + (immed_5 * 4) if (CP15_reg1_Ubit == 0) if
       * address[1:0] == 0b00 then data = Memory[address,4] else data = UNPREDICTABLE else //
       * CP15_reg1_Ubit == 1 data = Memory[address,4] Rd = data
       */

      /**
       * LDR <Rd>, [SP, #<immed_8> * 4]
       *
       *  Operation:
       *
       *  MemoryAccess(B-bit, E-bit) address = SP + (immed_8 * 4) if (CP15_reg1_Ubit == 0) if
       * address[1:0] == 0b00 then data = Memory[address,4] else data = UNPREDICTABLE else //
       * CP15_reg1_Ubit == 1 data = Memory[address,4] Rd = data
       */

      /**
       * LDR <Rd>, [<Rn>, <Rm>]
       *
       *  Operation:
       *
       *  MemoryAccess(B-bit, E-bit) address = Rn + Rm if (CP15_reg1_Ubit == 0) if address[1:0] ==
       * 0b00 then data = Memory[address,4] else data = UNPREDICTABLE else // CP15_reg1_Ubit == 1
       * data = Memory[address,4] Rd = data
       */
      instructions.add(ReilHelpers.createAdd(
          baseOffset++, dw, sourceRegister1, dw, sourceVariable1, dw, tmpAddress));
      instructions.add(ReilHelpers.createLdm(baseOffset++, dw, tmpAddress, dw, targetRegister1));
    }
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "LDR");
    translateAll(environment, instruction, "LDR", instructions);
  }
}
