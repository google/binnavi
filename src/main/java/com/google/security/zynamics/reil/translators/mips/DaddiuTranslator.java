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

import java.util.List;


public class DaddiuTranslator implements IInstructionTranslator {

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "daddiu");

    final String targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String sourceRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();
    final String sourceImmediate =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0).getValue();

    final OperandSize qw = OperandSize.QWORD;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String temporaryResult = environment.getNextVariableString();

    final String sourceImmediateSignExtended =
        SignExtendGenerator.extend16BitTo64(offset, environment, sourceImmediate, instructions);

    offset = ReilHelpers.nextReilAddress(instruction, instructions);
    instructions.add(ReilHelpers.createAdd(offset++, qw, sourceRegister, qw,
        sourceImmediateSignExtended, qw, temporaryResult));
    instructions.add(ReilHelpers.createAnd(offset, qw, temporaryResult, qw,
        String.valueOf(0xFFFFFFFFFFFFFFFFL), qw, targetRegister));
  }
}
