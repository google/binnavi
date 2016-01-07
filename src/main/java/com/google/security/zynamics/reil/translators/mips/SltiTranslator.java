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
package com.google.security.zynamics.reil.translators.mips;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.general.Triple;

import java.util.List;


public class SltiTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "slti");

    final Triple<IOperandTree, IOperandTree, IOperandTree> operands =
        OperandLoader.loadDuplicateFirst(instruction);
    final String targetRegister = operands.first().getRootNode().getChildren().get(0).getValue();
    final String sourceRegister1 = operands.second().getRootNode().getChildren().get(0).getValue();
    final String sourceRegister2 = operands.third().getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String subtractedValue = environment.getNextVariableString();
    final String maskedCarry = environment.getNextVariableString();

    // Subtract the input values
    instructions.add(ReilHelpers.createSub(offset++, dw, sourceRegister1, dw, sourceRegister2, qw,
        subtractedValue));

    // Isolate the carry flag
    instructions.add(ReilHelpers.createAnd(offset++, qw, subtractedValue, qw,
        String.valueOf(0x100000000L), qw, maskedCarry));

    // If the carry flag is set the first value was smaller => 0
    instructions.add(ReilHelpers.createBisz(offset++, qw, maskedCarry, dw, targetRegister));
    instructions.add(ReilHelpers.createBisz(offset++, dw, targetRegister, dw, targetRegister));
  }
}
