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


public class SwlTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "swl");

    final OperandSize dw = OperandSize.DWORD;

    final String lastTwoAddressBits = environment.getNextVariableString();
    final String shiftValue = environment.getNextVariableString();
    final String memoryMaskShiftValue = environment.getNextVariableString();
    final String registerShiftAmount = environment.getNextVariableString();
    final String memoryContent = environment.getNextVariableString();
    final String memoryMask = environment.getNextVariableString();
    final String memoryMaskedContent = environment.getNextVariableString();
    final String combinedContent = environment.getNextVariableString();
    final String registerShiftedContent = environment.getNextVariableString();
    final String address = environment.getNextVariableString();

    final IOperandTreeNode sourceRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String extendedValue =
        SignExtendGenerator.extendAndAdd(offset, environment, instruction.getOperands().get(1),
            instructions);

    offset = baseOffset + instructions.size();

    instructions.add(ReilHelpers.createAnd(offset++, dw, extendedValue, dw,
        String.valueOf(0xFFFFFFFCL), dw, address));
    instructions.add(ReilHelpers.createLdm(offset++, dw, address, dw, memoryContent));

    instructions.add(ReilHelpers.createAnd(offset++, dw, extendedValue, dw, String.valueOf(3L), dw,
        lastTwoAddressBits));
    instructions.add(ReilHelpers.createBsh(offset++, dw, lastTwoAddressBits, dw,
        String.valueOf(3L), dw, shiftValue));

    final String endianess = "little";

    if (endianess.equalsIgnoreCase("little")) {
      // little endianess case

      // address bits 1:0 00
      // register contents 0x11223344 shifted right by 24 -> 0x00000011
      // memory contents 0xAABBCCDD masked with 0xFFFFFF00 -> 0xAABBCC00
      // both ored -> 0xAABBCCDD11
      // anded with register mask 0xFFFFFFFF -> 0xAABBCC11

      instructions.add(ReilHelpers.createAdd(offset++, dw, String.valueOf(-24L), dw, shiftValue,
          dw, registerShiftAmount));
      instructions.add(ReilHelpers.createAdd(offset++, dw, String.valueOf(8L), dw, shiftValue, dw,
          memoryMaskShiftValue));
    } else if (endianess.equalsIgnoreCase("big")) {
      // big endian case

      // address bits 1:0
      // register contents 0x11223344 shifted right by 0 -> 0x11223344
      // memory contents 0xAABBCCDD masked with 0x00000000 -> 0x00000000
      // both ored -> 0x11223344
      // stored to memory
      instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(0L), dw, shiftValue, dw,
          registerShiftAmount));
      instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(32L), dw, shiftValue, dw,
          memoryMaskShiftValue));
    }

    // prepare memory to be correct for storage
    instructions.add(ReilHelpers.createBsh(offset++, dw, String.valueOf(0xFFFFFFFFL), dw,
        memoryMaskShiftValue, dw, memoryMask));
    instructions.add(ReilHelpers.createAnd(offset++, dw, memoryContent, dw, memoryMask, dw,
        memoryMaskedContent));

    // prepare register for storage
    instructions.add(ReilHelpers.createBsh(offset++, dw, sourceRegister.getValue(), dw,
        registerShiftAmount, dw, registerShiftedContent));

    // combine memory and register
    instructions.add(ReilHelpers.createOr(offset++, dw, registerShiftedContent, dw,
        memoryMaskedContent, dw, combinedContent));

    // store to memory
    instructions.add(ReilHelpers.createStm(offset++, dw, combinedContent, dw, address));
  }
}
