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


public class THUMB2TbhTranslator extends ARMBaseTranslator {
  /**
   * if ConditionPassed() then EncodingSpecificOperations(); NullCheckIfThumbEE(n); if is_tbh then
   * halfwords = UInt(MemU[R[n]+LSL(R[m],1), 2]); else halfwords = UInt(MemU[R[n]+R[m], 1]);
   * BranchWritePC(PC + 2*halfwords);
   */
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize bt = OperandSize.BYTE;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().get(0)
            .getChildren().get(0);
    final IOperandTreeNode shifterRootOperand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().get(0)
            .getChildren().get(1);

    final String firstRegister = registerOperand1.getValue();
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Pair<String, String> shifterOut =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifterRootOperand);

    final String memoryAddress = environment.getNextVariableString();
    final String halfword = environment.getNextVariableString();
    final String twoTimesHalfword = environment.getNextVariableString();
    final String jumpTarget = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstRegister, dw, shifterOut.first(),
        dw, memoryAddress));

    instructions.add(ReilHelpers.createLdm(baseOffset++, dw, memoryAddress, wd, halfword));
    instructions.add(ReilHelpers.createMul(baseOffset++, wd, halfword, wd, String.valueOf(2), dw,
        twoTimesHalfword));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, "PC", dw, twoTimesHalfword, dw,
        jumpTarget));
    instructions.add(ReilHelpers.createJcc(baseOffset++, bt, String.valueOf(1), dw, jumpTarget));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "TBH");
    translateAll(environment, instruction, "TBH", instructions);
  }
}
