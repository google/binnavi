/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.translators.x64;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;


/**
 * Translates LAHF instructions to REIL code.
 */
public class BswapTranslator implements IInstructionTranslator {

  /**
   * Translates a BSWAP instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The BSWAP instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a BSWAP instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "bswap");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a lahf instruction (invalid number of operands)");
    }
    String operand_size = instruction.getOperands().get(0).getRootNode().getValue();
    
    //There are 2 variants in 64 mode: qword and dword
    if(operand_size.toLowerCase().equals("qword")) {
      translate_64(environment, instruction, instructions);
    } else {
      translate_32(environment, instruction, instructions);
      
    }
  

    
  }
  private void translate_32(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException
  {
    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = OperandSize.DWORD;

    final String operand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();

    
    final String masked1st = environment.getNextVariableString();
    final String masked2nd = environment.getNextVariableString();
    final String masked3rd = environment.getNextVariableString();
    final String masked4th = environment.getNextVariableString();
    
    final String shifted1st = environment.getNextVariableString();
    final String shifted2nd = environment.getNextVariableString();
    final String shifted3rd = environment.getNextVariableString();
    final String shifted4th = environment.getNextVariableString();
    
    final String combined1 = environment.getNextVariableString();
    final String combined2 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset + 0, archSize, operand, archSize, "255",
        archSize, masked1st));
    instructions.add(ReilHelpers.createAnd(baseOffset + 1, archSize, operand, archSize, "65280",
        archSize, masked2nd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 2, archSize, operand, archSize, "16711680",
        archSize, masked3rd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, archSize, operand, archSize,
        "4278190080", archSize, masked4th));
    
    instructions.add(ReilHelpers.createBsh(baseOffset + 4, archSize, masked1st, archSize, "24",
        archSize, shifted1st));
    instructions.add(ReilHelpers.createBsh(baseOffset + 5, archSize, masked2nd, archSize, "8",
        archSize, shifted2nd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 6, archSize, masked3rd, archSize, "-8",
        archSize, shifted3rd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 7, archSize, masked4th, archSize, "-24",
        archSize, shifted4th));
    
    instructions.add(ReilHelpers.createOr(baseOffset + 8, archSize, shifted1st, archSize,
        shifted2nd, archSize, combined1));
    instructions.add(ReilHelpers.createOr(baseOffset + 9, archSize, shifted3rd, archSize,
        shifted4th, archSize, combined2));

    instructions.add(ReilHelpers.createOr(baseOffset + 10, archSize, combined1, archSize,
        combined2, archSize, operand));
  }
  private void translate_64(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException
  {
    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = OperandSize.QWORD;

    final String operand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();

    
    final String masked1st = environment.getNextVariableString();
    final String masked2nd = environment.getNextVariableString();
    final String masked3rd = environment.getNextVariableString();
    final String masked4th = environment.getNextVariableString();
    final String masked5th = environment.getNextVariableString();
    final String masked6th = environment.getNextVariableString();
    final String masked7th = environment.getNextVariableString();
    final String masked8th = environment.getNextVariableString();
    
    final String shifted1st = environment.getNextVariableString();
    final String shifted2nd = environment.getNextVariableString();
    final String shifted3rd = environment.getNextVariableString();
    final String shifted4th = environment.getNextVariableString();
    final String shifted5th = environment.getNextVariableString();
    final String shifted6th = environment.getNextVariableString();
    final String shifted7th = environment.getNextVariableString();
    final String shifted8th = environment.getNextVariableString();
    
    final String combined1 = environment.getNextVariableString();
    final String combined2 = environment.getNextVariableString();
    final String combined3 = environment.getNextVariableString();
    final String combined4 = environment.getNextVariableString();
    final String combined5 = environment.getNextVariableString();
    final String combined6 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset + 0, archSize, operand, archSize, "255",
        archSize, masked1st));
    instructions.add(ReilHelpers.createAnd(baseOffset + 1, archSize, operand, archSize, "65280",
        archSize, masked2nd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 2, archSize, operand, archSize, "16711680",
        archSize, masked3rd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, archSize, operand, archSize,
        "4278190080", archSize, masked4th));
    instructions.add(ReilHelpers.createAnd(baseOffset + 4, archSize, operand, archSize,
        "1095216660480", archSize, masked5th));
    instructions.add(ReilHelpers.createAnd(baseOffset + 5, archSize, operand, archSize,
        "280375465082880", archSize, masked6th));
    instructions.add(ReilHelpers.createAnd(baseOffset + 6, archSize, operand, archSize,
        "71776119061217280", archSize, masked7th));
    instructions.add(ReilHelpers.createAnd(baseOffset + 7, archSize, operand, archSize,
        "18374686479671623680", archSize, masked8th));
    
    instructions.add(ReilHelpers.createBsh(baseOffset + 8, archSize, masked1st, archSize, "56",
        archSize, shifted1st));
    instructions.add(ReilHelpers.createBsh(baseOffset + 9, archSize, masked2nd, archSize, "40",
        archSize, shifted2nd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 10, archSize, masked3rd, archSize, "24",
        archSize, shifted3rd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 11, archSize, masked4th, archSize, "8",
        archSize, shifted4th));
    instructions.add(ReilHelpers.createBsh(baseOffset + 12, archSize, masked5th, archSize, "-8",
        archSize, shifted5th));
    instructions.add(ReilHelpers.createBsh(baseOffset + 13, archSize, masked6th, archSize, "-24",
        archSize, shifted6th));
    instructions.add(ReilHelpers.createBsh(baseOffset + 14, archSize, masked7th, archSize, "-40",
        archSize, shifted7th));
    instructions.add(ReilHelpers.createBsh(baseOffset + 15, archSize, masked8th, archSize, "-56",
        archSize, shifted8th));
    
    instructions.add(ReilHelpers.createOr(baseOffset + 16, archSize, shifted1st, archSize,
        shifted2nd, archSize, combined1));
    instructions.add(ReilHelpers.createOr(baseOffset + 17, archSize, shifted3rd, archSize,
        shifted4th, archSize, combined2));
    instructions.add(ReilHelpers.createOr(baseOffset + 18, archSize, shifted5th, archSize,
        shifted6th, archSize, combined3));
    instructions.add(ReilHelpers.createOr(baseOffset + 19, archSize, shifted7th, archSize,
        shifted8th, archSize, combined4));
    
    instructions.add(ReilHelpers.createOr(baseOffset + 20, archSize, combined1, archSize,
        combined2, archSize, combined5));
    instructions.add(ReilHelpers.createOr(baseOffset + 21, archSize, combined3, archSize,
        combined4, archSize, combined6));
    
    instructions.add(ReilHelpers.createOr(baseOffset + 22, archSize, combined5, archSize,
        combined6, archSize, operand));
  }
}
