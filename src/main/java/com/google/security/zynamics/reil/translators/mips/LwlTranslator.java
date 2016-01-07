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
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class LwlTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    // TODO final String todo64 = "";
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "lwl");

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final String lastTwoAddressBits = environment.getNextVariableString();
    final String shiftValue = environment.getNextVariableString();
    final String memoryShiftAmount = environment.getNextVariableString();
    final String registerMaskShiftAmount = environment.getNextVariableString();
    final String shiftedMemoryContent = environment.getNextVariableString();
    final String shiftedMemoryMask = environment.getNextVariableString();
    final String maskedRegisterContent = environment.getNextVariableString();
    final String temporaryResult = environment.getNextVariableString();
    final String memoryContent = environment.getNextVariableString();
    final String address = environment.getNextVariableString();

    final IOperandTreeNode targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String extendedValue =
        SignExtendGenerator.extendAndAdd(offset, environment, instruction.getOperands().get(1),
            instructions);

    offset = baseOffset + instructions.size();

    instructions.add(ReilHelpers.createAnd(offset++, dw, extendedValue, dw,
        String.valueOf(0xFFFFFFFCL), dw, address));
    instructions.add(ReilHelpers.createLdm(offset, dw, address, dw, memoryContent));

    instructions.add(ReilHelpers.createAnd(offset++, dw, extendedValue, dw, String.valueOf(3L), dw,
        lastTwoAddressBits));
    instructions.add(ReilHelpers.createBsh(offset++, dw, lastTwoAddressBits, dw,
        String.valueOf(3L), dw, shiftValue));

    // TODO this must match the imported file somehow
    final String endianess = "little";

    if (endianess.equalsIgnoreCase("little")) {
      // little endian case

      // address bits 1:0 00
      // memory content 0x11223344 shifted left by 24 -> 0x11223344000000
      // register content 0xAABBCCDD masked with 0x00FFFFFF -> 0x00BBCCDD
      // both ored -> 0x11223344BBCCDD
      // anded with register mask 0xFFFFFFFF -> 0x44BBCCDD

      instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(24L), dw, shiftValue, dw,
          memoryShiftAmount));
      instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(-8L), dw, shiftValue, dw,
          registerMaskShiftAmount));
    } else if (endianess.equalsIgnoreCase("big")) {
      // big endian case

      // address bits 1:0 00
      // memory content 0x11223344 shifted by 0 -> 0x11223344
      // register content 0xAABBCCDD masked with 0x00000000 -> 0x00000000
      // both ored -> 0x11223344
      // anded with register mask 0xFFFFFFFF -> 0x11223344

      instructions.add(ReilHelpers.createStr(offset++, dw, shiftValue, dw, memoryShiftAmount));
      instructions.add(ReilHelpers.createAdd(offset++, dw, String.valueOf(-32L), dw, shiftValue,
          dw, registerMaskShiftAmount));
    }

    // bring the loaded memory into the correct position for register transfer
    instructions.add(ReilHelpers.createBsh(offset++, dw, memoryContent, dw, memoryShiftAmount, qw,
        shiftedMemoryContent));

    // bring the register content into the correct form
    instructions.add(ReilHelpers.createBsh(offset++, dw, String.valueOf(0xFFFFFFFFL), dw,
        registerMaskShiftAmount, dw, shiftedMemoryMask));
    instructions.add(ReilHelpers.createAnd(offset++, dw, targetRegister.getValue(), dw,
        shiftedMemoryMask, dw, maskedRegisterContent));

    // combine the extracted information and adjust the size to machine register size
    instructions.add(ReilHelpers.createOr(offset++, dw, maskedRegisterContent, qw,
        shiftedMemoryContent, qw, temporaryResult));
    instructions.add(ReilHelpers.createAnd(offset++, qw, temporaryResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister.getValue()));
  }
}
