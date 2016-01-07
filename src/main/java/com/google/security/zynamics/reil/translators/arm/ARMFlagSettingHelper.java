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

import java.util.List;

public final class ARMFlagSettingHelper {
  public static void setThumbAddFlags(final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions,
      final long baseOffset,
      final OperandSize trueResultSize,
      final String trueResult,
      final OperandSize resultSize,
      final String resultOperand,
      final OperandSize addOperand1Size,
      final String addOperand1,
      final OperandSize addOperand2Size,
      final String addOperand2) {
    long offset = baseOffset;
    // N
    instructions.add(ReilHelpers.createBsh(offset++,
        resultSize,
        resultOperand,
        OperandSize.WORD,
        String.valueOf(-31L),
        OperandSize.BYTE,
        "N"));

    // Z
    instructions.add(
        ReilHelpers.createBisz(offset++, resultSize, resultOperand, OperandSize.BYTE, "Z"));

    // C
    instructions.add(ReilHelpers.createBsh(offset++,
        trueResultSize,
        trueResult,
        OperandSize.WORD,
        String.valueOf(-32L),
        OperandSize.BYTE,
        "C"));

    // V
    Helpers.addOverflow(offset,
        environment,
        instructions,
        addOperand1Size,
        addOperand1,
        addOperand2Size,
        addOperand2,
        trueResultSize,
        trueResult,
        "V",
        32);
  }

  public static void setThumbRotateFlags(final List<ReilInstruction> instructions,
      final long baseOffset,
      final OperandSize carrySize,
      final String carryOperand,
      final OperandSize resultSize,
      final String resultOperand) {
    long offset = baseOffset;
    instructions.add(ReilHelpers.createAnd(offset++,
        carrySize,
        carryOperand,
        OperandSize.BYTE,
        String.valueOf(1L),
        OperandSize.BYTE,
        "C"));
    instructions.add(ReilHelpers.createBsh(offset++,
        resultSize,
        resultOperand,
        OperandSize.WORD,
        String.valueOf(-31L),
        OperandSize.BYTE,
        "N"));
    instructions.add(
        ReilHelpers.createBisz(offset++, resultSize, resultOperand, OperandSize.BYTE, "Z"));
  }
}
