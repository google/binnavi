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
 * Translates INC instructions to REIL code.
 */
public class IncTranslator implements IInstructionTranslator {

  /**
   * Translates an INC instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The INC instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an INC instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "inc");
    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not an inc instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // INC instructions have exactly one operand.
    final IOperandTree operand = instruction.getOperands().get(0);

    // Load the operand.
    final TranslationResult result = Helpers.translateOperand(environment, offset, operand, true);
    instructions.addAll(result.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String loadedRegister = result.getRegister();

    final OperandSize registerSize = result.getSize();
    final OperandSize nextSize = TranslationHelpers.getNextSize(registerSize);

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(registerSize));
    final String shiftMsbLsbMask =
        String.valueOf(TranslationHelpers.getShiftMsbLsbMask(registerSize));
    final String truncMask = String.valueOf(TranslationHelpers.getAllBitsMask(registerSize));

    final String maskedMsb = environment.getNextVariableString();
    final String addResult = environment.getNextVariableString();
    final String maskedMsbResult = environment.getNextVariableString();
    final String maskedMsbNeg = environment.getNextVariableString();
    final String tempOF = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();

    // Isolate the MSB of the operand
    instructions.add(ReilHelpers.createAnd(offset, registerSize, loadedRegister, registerSize,
        msbMask, registerSize, maskedMsb));

    // Increment the value
    instructions.add(ReilHelpers.createAdd(offset + 1, registerSize, loadedRegister, registerSize,
        "1", nextSize, addResult));

    // Isolate the MSB of the result and put it into the Sign Flag
    instructions.add(ReilHelpers.createAnd(offset + 2, nextSize, addResult, registerSize, msbMask,
        registerSize, maskedMsbResult));
    instructions.add(ReilHelpers.createBsh(offset + 3, registerSize, maskedMsbResult, registerSize,
        shiftMsbLsbMask, OperandSize.BYTE, Helpers.SIGN_FLAG));

    // The OF is only set if the result of the inc operation is 0x80
    // OF = ( MSB(old) == 0 ) AND ( MSB(new) == 1 )
    // OF = NOT(MSB(old)) AND MSB(new)
    instructions.add(ReilHelpers.createXor(offset + 4, registerSize, maskedMsb, registerSize,
        msbMask, registerSize, maskedMsbNeg));
    instructions.add(ReilHelpers.createAnd(offset + 5, registerSize, maskedMsbResult, registerSize,
        maskedMsbNeg, registerSize, tempOF));

    // Write the result into the Overflow Flag
    instructions.add(ReilHelpers.createBsh(offset + 6, registerSize, tempOF, registerSize,
        shiftMsbLsbMask, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // Truncate the result to fit into the target
    instructions.add(ReilHelpers.createAnd(offset + 7, nextSize, addResult, registerSize,
        truncMask, registerSize, truncatedResult));

    // Update the Zero Flag
    instructions.add(ReilHelpers.createBisz(offset + 8, registerSize, truncatedResult,
        OperandSize.BYTE, Helpers.ZERO_FLAG));

    // Write the truncated result back into the operand
    Helpers.writeBack(environment, offset + 9, operand, truncatedResult, registerSize,
        result.getAddress(), result.getType(), instructions);
  }

}
