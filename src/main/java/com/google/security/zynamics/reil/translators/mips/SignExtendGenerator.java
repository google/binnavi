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
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

public class SignExtendGenerator {
  public static String extend16BitTo32(long baseOffset, final ITranslationEnvironment environment,
      final String sourceValue, final List<ReilInstruction> instructions) {
    final String targetRegister = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final OperandSize dw = OperandSize.DWORD;

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceValue, dw,
        String.valueOf(0x00008000), dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw,
        String.valueOf(0x0000FFFFL), dw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0x00008000L), dw, targetRegister));

    return targetRegister;
  }

  public static String extend16BitTo64(long baseOffset, final ITranslationEnvironment environment,
      final String sourceValue, final List<ReilInstruction> instructions) {
    final String targetRegister = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final OperandSize qw = OperandSize.QWORD;

    instructions.add(ReilHelpers.createAdd(baseOffset++, qw, sourceValue, qw,
        String.valueOf(0x0000000000008000L), qw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, qw,
        String.valueOf(0x000000000000FFFFL), qw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++, qw, tmpVar2, qw,
        String.valueOf(0x0000000000008000L), qw, targetRegister));

    return targetRegister;
  }

  public static String extend8BitTo32(long baseOffset, final ITranslationEnvironment environment,
      final String sourceValue, final List<ReilInstruction> instructions) {
    final String targetRegister = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final OperandSize dw = OperandSize.DWORD;

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceValue, dw,
        String.valueOf(0x00000080), dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw,
        String.valueOf(0x000000FFL), dw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0x00000080L), dw, targetRegister));

    return targetRegister;
  }

  public static String extendAndAdd(long baseOffset, final ITranslationEnvironment environment,
      final IOperandTree operand, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String targetRegister = environment.getNextVariableString();
    final String addedValue = environment.getNextVariableString();
    final String truncatedValue = environment.getNextVariableString();

    final OperandSize dw = OperandSize.DWORD;

    final Pair<IOperandTreeNode, IOperandTreeNode> combinedOperand =
        OperandLoader.load(operand.getRootNode());

    if (combinedOperand.second() == null) {
      // Workaround for ksh.idb example
      // .MIPS.stubs:0043C3F0 lw $t9, dword_481E70

      return combinedOperand.first().getValue();
    }

    final String register = combinedOperand.first().getValue();
    final String integerValue = combinedOperand.second().getValue();

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, integerValue, dw,
        String.valueOf(0x00008000), dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw,
        String.valueOf(0x0000FFFFL), dw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0x00008000L), dw, targetRegister));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, register, dw, targetRegister,
        OperandSize.QWORD, addedValue));
    instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, addedValue, dw,
        String.valueOf(0xFFFFFFFFL), dw, truncatedValue));

    return truncatedValue;
  }

  public static String extendFromByte(long baseOffset, final ITranslationEnvironment environment,
      final String sourceValue, final List<ReilInstruction> instructions) {
    final String targetRegister = environment.getNextVariableString();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final OperandSize dw = OperandSize.DWORD;

    instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.BYTE, sourceValue,
        OperandSize.BYTE, String.valueOf(0x00000080), dw, tmpVar1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar1, dw,
        String.valueOf(0x000000FFL), dw, tmpVar2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0x00000080L), dw, targetRegister));

    return targetRegister;
  }
}
