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
package com.google.security.zynamics.reil.translators.arm;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class ARMMrsTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());

    final String tmpVar1 = environment.getNextVariableString();

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    /*
     * CPSR is really 32 [ N | Z | C | V | Q | reserved[2] | J | reserved[4] | GE[4] | reserved[6] |
     * E | A | I | F | T | M[5] ]
     */

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, "N", dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "Z", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "C", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "V", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "Q", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(3L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "J", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(8L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "CPSR_GE", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(7L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "E", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "A", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "I", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "F", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(1L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "T", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(5L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, "CPSR_M", dw, tmpVar1, dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(5L), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, tmpVar1, dw, targetRegister));
  }

  /**
   * MRS{<cond>} <Rd>, CPSR MRS{<cond>} <Rd>, SPSR
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then if R == 1 then Rd = SPSR else Rd = CPSR
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "MRS");
    translateAll(environment, instruction, "MRS", instructions);
  }
}
