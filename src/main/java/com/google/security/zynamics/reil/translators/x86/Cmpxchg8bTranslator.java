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
package com.google.security.zynamics.reil.translators.x86;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;

/**
 * Translates CMPXCHG8B instructions to REIL code.
 */
public class Cmpxchg8bTranslator implements IInstructionTranslator {
  /**
   * Translates a CMPXCHG8B instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The CMPXCHG8B instruction to translate.
   * @param instructions The generated REIL code will be added to this list.
   *
   * @throws InternalTranslationException If any of the arguments are null the passed instruction is
   *         not a CMPXCHG instruction.
   *
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    // CMPXCHG8B:
    // Compares the 64-bit value in EDX:EAX with the operand (destination
    // operand). If the values are equal, the 64-bit value in ECX:EBX is stored
    // in the destination operand. Otherwise, the value in the destination
    // operand is loaded into EDX:EAX. The destination operand is an 8-byte
    // memory location. For the EDX:EAX and ECX:EBX register pairs, EDX and ECX
    // contain the high-order 32 bits and EAX and EBX contain the low-order 32
    // bits of a 64-bit value.
    //
    // In order to translate this, we perform the following steps:
    //   1) Translate the (single) operand properly. The operand can be of
    //   complex form, e.g. [ebx+0x20] etc., so we will obtain a group of
    //   instructions for it.
    //   2) Load 64 bit into memory using a 64-bit LDM REIL instruction
    //   3) Combine the value in EDX:EAX into one 64-bit REIL register
    //   4) Compare the two values (by subtracting them)
    //   5) If the ZF is not set, load the value obtained in step (2) into
    //   EDX:EAX, then jump to 7)
    //   6) If the ZF is set (e.g. the values were equal), combine ECX and
    //   EBX into one value, and emit a 64-bit STM. Then jump to 7)
    //   7) End.
    //
    // We see that the translated code will contain two JCCs -- one conditional
    // at the beginning of step 5, leading to 6, and one unconditional at the
    // end of step 5, leading to 7.

    TranslationHelpers.checkTranslationArguments(
        environment, instruction, instructions, "cmpxchg8b");
    Preconditions.checkArgument(instruction.getOperands().size() == 1,
        "Error: Argument instruction is not a cmpxchg8b instruction (invalid number of operands)");

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    final long offset = baseOffset;

    // Step (1) Translate the (single) operand properly.
    final List<? extends IOperandTree> operands = instruction.getOperands();
    final IOperandTree targetOperand = operands.get(0);
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, targetOperand, false);
    instructions.addAll(firstResult.getInstructions());

    // Step (2) Load 64 bit into memory using a 64-bit LDM REIL instruction.
    final String loadResult = environment.getNextVariableString();
    instructions.add(ReilHelpers.createLdm(
        baseOffset + instructions.size(), OperandSize.DWORD, firstResult.getAddress(),
        OperandSize.QWORD, loadResult));

    // Step (3) Combine the value in EDX:EAX into one 64-bit REIL register
    final String combinedEdxEax = environment.getNextVariableString();
    instructions.add(ReilHelpers.createBsh(baseOffset + instructions.size(),
        OperandSize.DWORD,
        "edx",
        OperandSize.DWORD,
        "32",
        OperandSize.QWORD,
        combinedEdxEax));
    instructions.add(ReilHelpers.createOr(baseOffset + instructions.size(),
        OperandSize.QWORD,
        "eax",
        OperandSize.QWORD,
        combinedEdxEax,
        OperandSize.QWORD,
        combinedEdxEax));
    final String comparisonResult = environment.getNextVariableString();

    // Step (4) Compare the two values (by subtracting them).
    instructions.add(ReilHelpers.createSub(baseOffset + instructions.size(),
        OperandSize.QWORD,
        combinedEdxEax,
        OperandSize.QWORD,
        loadResult,
        OperandSize.QWORD,
        comparisonResult));
    // Set the ZF if the two values were equal.
    instructions.add(ReilHelpers.createBisz(
        baseOffset + instructions.size(), OperandSize.QWORD, comparisonResult, OperandSize.BYTE,
        Helpers.ZERO_FLAG));

    // Beginning of step (5) - if the ZF is set, jump to step (6). The reason
    // for the constant "+4" in the jump offset is that the size of the body of
    // step (5) is exactly 4 instructions:
    //      JCC ZF, --, step 6
    //      AND val, 0xFFFFFFFF, eax
    //      BSH val, -32, edx
    //      JCC 1, --, step 7
    // "val" is the value obtained in step (2).
    final String jmpGoal =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 4);
    instructions.add(ReilHelpers.createJcc(
        baseOffset + instructions.size(), OperandSize.BYTE, Helpers.ZERO_FLAG, OperandSize.ADDRESS,
        jmpGoal));
    // The body of step (5) - load the value obtained in step (2) into EDX:EAX
    instructions.add(ReilHelpers.createAnd(baseOffset + instructions.size(),
        OperandSize.QWORD,
        loadResult,
        OperandSize.QWORD,
        String.valueOf(TranslationHelpers.getAllBitsMask(OperandSize.DWORD)),
        OperandSize.DWORD,
        "eax"));
    instructions.add(ReilHelpers.createBsh(baseOffset + instructions.size(),
        OperandSize.QWORD,
        loadResult,
        OperandSize.DWORD,
        "-32",
        OperandSize.DWORD,
        "edx"));
    // Create a non-conditional JMP that skips step 6. Step 6, again, consists of
    // 4 instructions.
    final String jmpGoal2 =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 4);
    instructions.add(ReilHelpers.createJcc(
        baseOffset + instructions.size(), OperandSize.BYTE, "1", OperandSize.ADDRESS, jmpGoal2));

    // Step (6) If the ZF is set (e.g. the values were equal), combine ECX and
    // EBX into one value, and emit a 64-bit STM. Then fall through to step (7)
    instructions.add(ReilHelpers.createBsh(baseOffset + instructions.size(),
        OperandSize.DWORD,
        "ecx",
        OperandSize.DWORD,
        "32",
        OperandSize.QWORD,
        combinedEdxEax));
    instructions.add(ReilHelpers.createOr(baseOffset + instructions.size(),
        OperandSize.QWORD,
        "ebx",
        OperandSize.QWORD,
        combinedEdxEax,
        OperandSize.QWORD,
        combinedEdxEax));
    instructions.add(ReilHelpers.createStm(
        baseOffset + instructions.size(), OperandSize.QWORD, combinedEdxEax, OperandSize.DWORD,
        firstResult.getAddress()));
    // Step (7): Just a final NOP, needed as jump destination.
    instructions.add(ReilHelpers.createNop(baseOffset + instructions.size()));
  }
}
