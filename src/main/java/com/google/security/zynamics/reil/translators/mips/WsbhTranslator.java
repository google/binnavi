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


public class WsbhTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "wsbh");

    final String rd =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String rt =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final OperandSize dw = OperandSize.DWORD;

    final String rtByte1 = environment.getNextVariableString();
    final String rtByte2 = environment.getNextVariableString();
    final String rtByte3 = environment.getNextVariableString();
    final String rtByte4 = environment.getNextVariableString();

    final String newRtByte1 = environment.getNextVariableString();
    final String newRtByte2 = environment.getNextVariableString();
    final String newRtByte3 = environment.getNextVariableString();
    final String newRtByte4 = environment.getNextVariableString();

    final String tempResultLower = environment.getNextVariableString();
    final String tempResultUpper = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(offset++, dw, rt, dw, String.valueOf(0xFF000000L), dw,
        rtByte4));
    instructions.add(ReilHelpers.createAnd(offset++, dw, rt, dw, String.valueOf(0x00FF0000L), dw,
        rtByte3));
    instructions.add(ReilHelpers.createAnd(offset++, dw, rt, dw, String.valueOf(0x0000FF00L), dw,
        rtByte2));
    instructions.add(ReilHelpers.createAnd(offset++, dw, rt, dw, String.valueOf(0x000000FFL), dw,
        rtByte1));

    instructions.add(ReilHelpers.createBsh(offset++, dw, rtByte4, dw, String.valueOf(-8L), dw,
        newRtByte3));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rtByte3, dw, String.valueOf(8L), dw,
        newRtByte4));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rtByte2, dw, String.valueOf(-8L), dw,
        newRtByte1));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rtByte1, dw, String.valueOf(8L), dw,
        newRtByte2));

    instructions.add(ReilHelpers.createOr(offset++, dw, newRtByte3, dw, newRtByte4, dw,
        tempResultUpper));
    instructions.add(ReilHelpers.createOr(offset++, dw, newRtByte1, dw, newRtByte2, dw,
        tempResultLower));

    instructions
        .add(ReilHelpers.createOr(offset, dw, tempResultUpper, dw, tempResultLower, dw, rd));
  }
}
