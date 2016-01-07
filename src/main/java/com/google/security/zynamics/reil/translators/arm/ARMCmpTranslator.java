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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


public class ARMCmpTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode shifter = instruction.getOperands().get(1).getRootNode();

    final String sourceRegister = (registerOperand1.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String aluOut = environment.getNextVariableString();
    final String tmpBorrow = environment.getNextVariableString();

    // compute <shifter_operand>
    final Pair<String, String> shifterPair =
        AddressingModeOneGenerator.generate(baseOffset, environment, instruction, instructions,
            shifter);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String shifterOperand = shifterPair.first();

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister, dw, shifterOperand,
        qw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
        String.valueOf(0xFFFFFFFFL), dw, aluOut));

    // match the case where we have to set flags this does not handle the S == 1 and Rd == R15 case
    // !!!
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    final String msbVar1 = environment.getNextVariableString();
    final String msbVar2 = environment.getNextVariableString();

    // N Flag Rd[31]
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, dw, String.valueOf(-31L), bt,
        tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
        "N"));

    // Z Flag if Rd == 0 then 1 else 0
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, aluOut, bt, "Z"));

    // C Flag NOT BorrowFrom(Rn - shifter_operand)
    // TODO; check if this is really the borrow or something else
    instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, wd, String.valueOf(-32L), bt,
        tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, bt, String.valueOf(1L), bt,
        tmpBorrow));
    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpBorrow, bt, "C"));

    // V Flag OverflowFrom(Rn - shifter_operand)
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, shifterOperand, wd,
        String.valueOf(-31L), bt, msbVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, wd,
        String.valueOf(-31L), bt, msbVar2));

    // ( msbA XOR msbB ) AND ( msbA XOR msbR ) == OF
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVar1, bt, msbVar2, bt, tmpVar4));
    instructions.add(ReilHelpers.createXor(baseOffset++, bt, msbVar1, bt, "N", bt, tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar4, bt, tmpVar3, bt, "V"));
  }

  /**
   * CMP{<cond>} <Rn>, <shifter_operand>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then alu_out = Rn - shifter_operand N Flag = alu_out[31] Z Flag = if
   * alu_out == 0 then 1 else 0 C Flag = NOT BorrowFrom(Rn - shifter_operand) V Flag =
   * OverflowFrom(Rn - shifter_operand)
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "CMP");
    translateAll(environment, instruction, "CMP", instructions);
  }
}
