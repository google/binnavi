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


public class ARMSwpTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0).getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister1 = (registerOperand2.getValue());
    final String memoryRegister2 = (registerOperand3.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String negRotVal2 = environment.getNextVariableString();
    final String rotVal1 = environment.getNextVariableString();
    final String rotVal2 = environment.getNextVariableString();
    final String tmpResult = environment.getNextVariableString();
    final String tmpRotate1 = environment.getNextVariableString();
    final String tmpRotate2 = environment.getNextVariableString();
    final String tmpVal1 = environment.getNextVariableString();

    // load
    instructions.add(ReilHelpers.createLdm(baseOffset++, dw, memoryRegister2, dw, tmpVal1));

    // rotate
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, memoryRegister2, dw,
        String.valueOf(0x3), dw, rotVal1));
    instructions.add(ReilHelpers.createMul(baseOffset++, dw, rotVal1, dw, String.valueOf(8), dw,
        rotVal2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, String.valueOf(0), dw, rotVal2, dw,
        negRotVal2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVal1, dw, negRotVal2, dw,
        tmpRotate1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVal1, dw, rotVal2, dw, tmpRotate2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpRotate1, dw, tmpRotate2, dw,
        tmpResult));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));

    // store
    instructions.add(ReilHelpers.createStm(baseOffset++, dw, sourceRegister1, dw, memoryRegister2));
  }

  /**
   * SWP{<cond>} <Rd>, <Rm>, [<Rn>]
   * 
   * Operation:
   * 
   * MemoryAccess(B-bit, E-bit) processor_id = ExecutingProcessor() if ConditionPassed(cond) then if
   * (CP15_reg1_Ubit == 0) then temp = Memory[address,4] Rotate_Right (8 * address[1:0])
   * Memory[address,4] = Rm Rd = temp else // CP15_reg1_Ubit ==1 temp = Memory[address,4]
   * Memory[address,4] = Rm Rd = temp if Shared(address) then // ARMv6 physical_address =
   * TLB(address) ClearExclusiveByAddress(physical_address,processor_id,4) // See Summary of
   * operation on page A2-49
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SWP");
    translateAll(environment, instruction, "SWP", instructions);
  }
}
