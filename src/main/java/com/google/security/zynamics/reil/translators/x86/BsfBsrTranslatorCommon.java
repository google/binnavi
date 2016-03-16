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
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.ArrayList;
import java.util.List;

/**
 * BSF and BSR have so much in common that most of their translation is identical.
 * This is a helper class that contains the code for the translation of both.
 */
public class BsfBsrTranslatorCommon {

  public static void translateBsfOrBsr(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      boolean translateBsf) throws InternalTranslationException {
  
    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a bsr/bsf instruction (invalid number of operands)");
    }
  
    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;
  
    final IOperandTree targetOperand = instruction.getOperands().get(0);
    final IOperandTree sourceOperand = instruction.getOperands().get(1);
  
    // Load the source operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, sourceOperand, true);
    instructions.addAll(sourceResult.getInstructions());
  
    offset = baseOffset + instructions.size();
  
    final OperandSize sourceSize = sourceResult.getSize();    
    final List<ReilInstruction> tempInstructions = new ArrayList<>();
  
    final String targetRegister = Helpers.getLeafValue(targetOperand.getRootNode());
    
    final String labelNotZero =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 4);
    final String labelLoopStart =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 7);
    final String labelLoopEnd =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 12);
    // TODO(thomasdullien): Clean up the use of tempInstructions.size() here, it is
    // unclear what it is used for.
    final String labelEnd =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size()
            + tempInstructions.size() + 12);
  
    instructions.add(ReilHelpers.createJcc(offset++, sourceSize, sourceResult.getRegister(),
        OperandSize.ADDRESS, labelNotZero));
  
    // Input value is 0
    instructions.add(ReilHelpers.createStr(offset++, OperandSize.BYTE, "1", OperandSize.BYTE,
        Helpers.ZERO_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, environment.getArchitectureSize(),
        targetRegister));
    instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        labelEnd));
  
    // Input value is not 0
  
    final String counter = environment.getNextVariableString();
    final String shiftedValue = environment.getNextVariableString();
    final String isolatedMsb = environment.getNextVariableString();
  
    instructions.add(ReilHelpers.createStr(offset++, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.ZERO_FLAG));
    instructions.add(ReilHelpers.createStr(offset++, sourceSize, sourceResult.getRegister(),
        sourceSize, shiftedValue));
  
    if (translateBsf) {
      instructions.add(ReilHelpers.createStr(offset++, OperandSize.BYTE, "0", OperandSize.BYTE,
          counter));
      instructions.add(ReilHelpers.createAnd(offset++, sourceSize, shiftedValue, sourceSize,
          "1", OperandSize.BYTE, isolatedMsb));        
    } else {
      instructions.add(ReilHelpers.createStr(offset++, OperandSize.BYTE, "31", OperandSize.BYTE,
          counter));
      // Generate the instruction for a BSR, e.g. bitmask is 0x80000000.
      instructions.add(ReilHelpers.createAnd(offset++, sourceSize, shiftedValue, sourceSize,
          String.valueOf(TranslationHelpers.getMsbMask(sourceSize)), OperandSize.BYTE, 
          isolatedMsb));
    }
    instructions.add(ReilHelpers.createJcc(offset++, sourceSize, isolatedMsb, OperandSize.ADDRESS,
        labelLoopEnd));
  
    if (translateBsf) {
      instructions.add(ReilHelpers.createAdd(offset++, OperandSize.BYTE, counter, OperandSize.BYTE,
          "1", OperandSize.BYTE, counter));
      instructions.add(ReilHelpers.createBsh(offset++, sourceSize, shiftedValue, sourceSize, "-1",
          sourceSize, shiftedValue));
    } else {
      instructions.add(ReilHelpers.createSub(offset++, OperandSize.BYTE, counter, OperandSize.BYTE,
          "1", OperandSize.BYTE, counter));
      instructions.add(ReilHelpers.createBsh(offset++, sourceSize, shiftedValue, sourceSize, "1",
          sourceSize, shiftedValue));
    }
    instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        labelLoopStart));
  
    instructions.add(ReilHelpers.createStr(offset++, OperandSize.DWORD, counter, OperandSize.DWORD,
        targetRegister));
  
    instructions.add(ReilHelpers.createNop(offset++));    
  }
}
