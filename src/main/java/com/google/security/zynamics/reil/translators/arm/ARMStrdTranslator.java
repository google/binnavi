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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


public class ARMStrdTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final Boolean writeBack =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().size()
        == 1 ? true : false;
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().size()
        == 1 ? instruction.getOperands()
        .get(0).getRootNode().getChildren().get(0).getChildren().get(0)
        : instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode rootNode = instruction.getOperands().get(1).getRootNode();

    final String registerNodeValue = (registerOperand1.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Pair<String, String> resultPair = AddressingModeTwoGenerator.generate(
        baseOffset, environment, instruction, instructions, rootNode);

    final String tmpAddress = resultPair.first();
    final String tmpAddress2 = environment.getNextVariableString();

    final int registerNum = Helpers.getRegisterIndex(registerNodeValue);

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    if (((registerNum % 2) == 0) && (registerNum != 14)) {
      instructions.add(ReilHelpers.createStm(baseOffset++, dw, registerNodeValue, dw, tmpAddress));
      instructions.add(ReilHelpers.createAdd(
          baseOffset++, dw, tmpAddress, bt, String.valueOf(4), dw, tmpAddress2));
      instructions.add(ReilHelpers.createStm(
          baseOffset++, dw, "R" + String.valueOf(registerNum + 1), dw, tmpAddress2));
      if (writeBack) {
        instructions.add(
            ReilHelpers.createStr(baseOffset++, dw, tmpAddress2, dw, registerNodeValue));
      }
    } else {
      instructions.add(ReilHelpers.createUnknown(baseOffset++));
    }
  }

  /**
   * STR{<cond>}D <Rd>, <addressing_mode>
   *
   *  Operation:
   *
   *  MemoryAccess(B-bit, E-bit) processor_id = ExecutingProcessor() if ConditionPassed(cond) then
   * if (Rd is even-numbered) and (Rd is not R14) and (address[1:0] == 0b00) and ((CP15_reg1_Ubit ==
   * 1) or (address[2] == 0)) then Memory[address,4] = Rd Memory[address+4,4] = R(d+1) else
   * UNPREDICTABLE if Shared(address) then // from ARMv6 physical_address = TLB(address)
   * ClearExclusiveByAddress(physical_address,processor_id,4) if Shared(address+4) physical_address
   * = TLB(address+4) ClearExclusiveByAddress(physical_address,processor_id,4)
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "STR");
    translateAll(environment, instruction, "STR", instructions);
  }
}
