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

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;

public abstract class Processor {
  private static final int REGISTER_SIZE = 32;

  protected static final OperandSize dw = OperandSize.DWORD;

  private static String[] extract(final ITranslationEnvironment environment, final String register,
      final int size, final long offset, final List<ReilInstruction> instructions) {
    assert (size == 8) || (size == 16);

    long baseOffset = offset;

    final List<String> results = new ArrayList<String>();

    for (int i = 0; i < (REGISTER_SIZE / size); i++) {
      final String andResult = environment.getNextVariableString();
      final String shiftResult = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, register, dw,
          String.valueOf(getMask(i, size)), dw, andResult));

      if (i != 0) {
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, andResult, dw,
            "-" + String.valueOf(i * size), dw, shiftResult));

        results.add(shiftResult);
      } else {
        results.add(andResult);
      }
    }

    return results.toArray(new String[results.size()]);
  }

  private static int getMask(final int bytePosition, final int size) {
    return (size == 8 ? 0xFF : 0xFFFF) << (bytePosition * size);
  }

  private static void merge(final ITranslationEnvironment environment, final long offset,
      final int shiftDelta, final String[] results, final String targetRegister, final int size,
      final List<ReilInstruction> instructions) {
    assert (results.length == 2) || (results.length == 4);

    long baseOffset = offset;

    final String tempResult = environment.getNextVariableString();
    long mask = results.length == 2 ? 0xFFFFL : 0xFFL;
    instructions.add(ReilHelpers.createStr(baseOffset++, dw, String.valueOf(0x0L), dw, tempResult));
    if (shiftDelta != 0) {
      mask = mask << 1;
    }
    int i = 0;
    for (final String element : results) {
      final String shiftResult = environment.getNextVariableString();
      final String andResult = environment.getNextVariableString();

      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, element, dw, String.valueOf(mask),
          dw, andResult));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, andResult, dw,
          String.valueOf((i * size) + shiftDelta), dw, shiftResult));
      instructions.add(ReilHelpers.createOr(baseOffset++, dw, tempResult, dw, shiftResult, dw,
          tempResult));
      i++;
    }

    instructions.add(ReilHelpers.createStr(baseOffset, dw, tempResult, dw, targetRegister));
  }

  protected int getResultShiftDelta() {
    return 0;
  }

  protected abstract String[] process(long baseOffset, String[] firstFour, String[] secondFour);

  public void generate(final ITranslationEnvironment environment, final long offset,
      final int size, final String sourceRegister1, final String sourceRegister2,
      final String targetRegister, final List<ReilInstruction> instructions) {
    long baseOffset = offset;

    final long oldBaseOffset = baseOffset;

    final String[] firstExtracted =
        extract(environment, sourceRegister1, size, baseOffset, instructions);
    baseOffset = oldBaseOffset + instructions.size();

    final String[] secondExtracted =
        extract(environment, sourceRegister2, size, baseOffset, instructions);
    baseOffset = oldBaseOffset + instructions.size();

    final String[] results = process(baseOffset, firstExtracted, secondExtracted);
    baseOffset = oldBaseOffset + instructions.size();

    assert results.length == firstExtracted.length;

    // get the results together
    merge(environment, baseOffset, getResultShiftDelta(), results, targetRegister, size,
        instructions);
  }
}
