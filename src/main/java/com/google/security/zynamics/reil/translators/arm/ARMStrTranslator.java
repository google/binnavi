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


public class ARMStrTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode rootNode = instruction.getOperands().get(1).getRootNode();

    final String registerNodeValue = (registerOperand1.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Pair<String, String> resultPair =
        AddressingModeTwoGenerator.generate(baseOffset, environment, instruction, instructions,
            rootNode);

    final String tmpAddress = resultPair.first();

    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    instructions.add(ReilHelpers.createStm(baseOffset, dw, registerNodeValue,
        dw, tmpAddress));
  }

  /**
   * STR{<cond>} <Rd>, <addressing_mode>
   * 
   * Operation:
   * 
   * MemoryAccess(B-bit, E-bit) processor_id = ExecutingProcessor() if ConditionPassed(cond) then
   * Memory[address,4] = Rd if Shared(address) then // from ARMv6 physical_address = TLB(address)
   * ClearExclusiveByAddress(physical_address,processor_id,4) // See Summary of operation on page
   * A2-49
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "STR");
    translateAll(environment, instruction, "STR", instructions);
  }
}
