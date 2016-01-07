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
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.ReilOperandNode;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates CMPXCHG instructions to REIL code.
 */
public class CmpxchgTranslator implements IInstructionTranslator {

  /**
   * Translates a CMPXCHG instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The CMPXCHG instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CMPXCHG instruction
   *
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cmpxchg");
    Preconditions.checkArgument(instruction.getOperands().size() == 2,
        "Error: Argument instruction is not a cmp instruction (invalid number of operands)");

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree targetOperand = operands.get(0);
    final IOperandTree sourceOperand = operands.get(1);

    // Load first operand.
    final TranslationResult targetResult =
        Helpers.translateOperand(environment, offset, targetOperand, true);
    instructions.addAll(targetResult.getInstructions());
    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load second operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, sourceOperand, true);
    instructions.addAll(sourceResult.getInstructions());
    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Compare the first operand to AL/AX/EAX
    String xaxRegister;
    switch (targetResult.getSize()) {
      case BYTE:
        xaxRegister = "al";
        break;
      case WORD:
        xaxRegister = "ax";
        break;
      case DWORD:
        xaxRegister = "eax";
        break;
      default:
        throw new InternalTranslationException(
            "Error: The first operand has to be BYTE/WORD/DWORD !");
    }

    String comparisonResult = environment.getNextVariableString();
    OperandSize currentSize = targetResult.getSize();
    // Subtract the first operand from AL/AX/EAX
    instructions.add(
        ReilHelpers.createSub(
            baseOffset + instructions.size(),
            currentSize,
            xaxRegister,
            currentSize,
            targetResult.getRegister(),
            currentSize,
            comparisonResult));
    // Set the ZF if the two values were equal
    instructions.add(
        ReilHelpers.createBisz(
            baseOffset + instructions.size(),
            currentSize,
            comparisonResult,
            OperandSize.BYTE,
            Helpers.ZERO_FLAG));

    // The control flow is as follows:
    // Jump to secondWriteBack if not equal
    // firstWriteBack
    // Jump to terminatingNop (avoid falling through from the first case)
    // secondWriteBack
    // terminatingNop

    // firstWriteBack: if the content of AL/AX/EAX is equal to the source operand,
    // move sourceOperand to targetOperand.
    final List<ReilInstruction> firstWriteBack = new ArrayList<ReilInstruction>();
    Helpers.writeBack(
        environment,
        baseOffset + instructions.size() + 1,   // reserve space for the first JCC
        targetOperand,
        sourceResult.getRegister(),
        sourceResult.getSize(),
        targetResult.getAddress(),
        targetResult.getType(),
        firstWriteBack);

    // Jump to secondWriteBack if not equal.
    // Reserve space for the two JCC and firstWriteBack when calculating target address.
    final long secondWriteBackOffset = instructions.size() + firstWriteBack.size() + 3;
    final String secondWriteBackGoal = String.format("%d.%d",
                                                     instruction.getAddress().toLong(),
                                                     secondWriteBackOffset);
    instructions.add(
        ReilHelpers.createJcc(
            baseOffset + instructions.size(),
            currentSize,
            comparisonResult,
            OperandSize.ADDRESS,
            secondWriteBackGoal));
    // Add the mov code that's executed if the condition is true.
    instructions.addAll(firstWriteBack);

    // Create an operand representing the AL/AX/EAX register so that we can write back to it.
    ReilOperandNode xAXOperandRoot = new ReilOperandNode(currentSize.toSizeString(),
                                                         ExpressionType.SIZE_PREFIX);
    ReilOperandNode xAXOperandLeaf = new ReilOperandNode(xaxRegister, ExpressionType.REGISTER);
    ReilOperandNode.link(xAXOperandRoot, xAXOperandLeaf);
    ReilOperand xAXOperand = new ReilOperand(xAXOperandRoot);

    // secondWriteBack: if the content of AL/AX/EAX is not equal to the source operand,
    // move targetOperand to AL/AX/EAX.
    final List<ReilInstruction> secondWriteBack = new ArrayList<ReilInstruction>();
    Helpers.writeBack(
        environment,
        baseOffset + instructions.size() + 1, // reserve space for the second JCC
        xAXOperand,
        targetResult.getRegister(),
        currentSize,
        null /* Memory address of the writeBack target. Empty since target is a register. */,
        TranslationResultType.REGISTER,
        secondWriteBack);

    // Jump to terminatingNop (avoid falling through from firstWriteBack).
    // Reserve addresses for JCC and for secondWriteBack when calculating target address.
    final long terminatingNopOffset = instructions.size() + secondWriteBack.size() + 2;
    final String terminatingNopGoal = String.format("%d.%d",
                                                    instruction.getAddress().toLong(),
                                                    terminatingNopOffset);
    instructions.add(
        ReilHelpers.createJcc(
            baseOffset + instructions.size(),
            OperandSize.BYTE,
            "1",
            OperandSize.ADDRESS,
            terminatingNopGoal));
    // Add the mov code that's executed if the condition is true.
    instructions.addAll(secondWriteBack);

    // Add a terminating NOP, this makes it easier to get a target for the conditional jump.
    instructions.add(ReilHelpers.createNop(baseOffset + instructions.size()));
  }
}
