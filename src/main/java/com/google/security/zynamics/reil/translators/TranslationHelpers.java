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
package com.google.security.zynamics.reil.translators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ErrorStrings;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;

/**
 * Various system-independent translation helper functions.
 */
public class TranslationHelpers {
  /**
   * Generic check function for all translators after which a safe translation environment can be
   * assumed.
   *
   * @param environment The translation environment.
   * @param instruction The instruction to be translated.
   * @param instructions The list of REIL instructions in which the result will be written.
   * @param mnemonic The mnemonic which should be translated.
   *
   * @throws InternalTranslationException
   */
  public static void checkTranslationArguments(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic) throws InternalTranslationException {
    Preconditions.checkNotNull(environment, ErrorStrings.ENVIRONMENT_ARGUMENT_ERROR);
    Preconditions.checkNotNull(instruction, ErrorStrings.INSTRUCTION_ARGUMENT_ERROR);
    Preconditions.checkNotNull(instructions, ErrorStrings.INSTRUCTIONS_ARGUMENT_ERROR);
  }

  public static long generateOneMask(final int lsb, final int width, final OperandSize size) {
    return generateZeroMask(lsb, width, size) ^ getAllBitsMask(size);
  }

  /**
   * Generates a mask which starts at the lsb parameter and is width wide
   *
   * @param lsb the least significant bit valid 0-size.getBits().
   * @param width the width valid 1-(lsb+width)-1 <=size.getBits().
   * @param size The size of the mask valid 4-64. In OperandSize.
   * @return The mask.
   */
  public static long generateZeroMask(final int lsb, final int width, final OperandSize size) {
    Preconditions.checkNotNull(size, "Size argument can not be null");
    Preconditions.checkPositionIndex(lsb, size.getBitSize() - 1);
    Preconditions.checkArgument(width >= 1);
    Preconditions.checkPositionIndex((lsb + width) - 1, size.getBitSize());

    long mask = getAllBitsMask(size);
    final long msb = (lsb + width) - 1;

    final long xorBit = 1;

    for (long i = lsb; i <= msb; i++) {
      mask = (mask ^ (xorBit << i));
    }
    return mask & getAllBitsMask(size);
  }

  /**
   * Returns a mask that selects all bits for a given operand size.
   *
   * @param size The given operand size
   * @return A mask where all bits are set
   */
  public static long getAllBitsMask(final OperandSize size) {

    switch (size) {
      case BYTE:
        return 255L;
      case WORD:
        return 65535L;
      case DWORD:
        return 4294967295L;
      case QWORD:
        return 0xFFFFFFFFFFFFFFFFL;
      default:
        break;
    }

    throw new IllegalArgumentException("Error: Invalid argument size");
  }

  /**
   * Returns a mask that masks all bit set for values of a given operand size except for the bits
   * that are shared with the smaller operand size.
   *
   * @param largerSize The larger operand size
   * @param smallerSize The smaller operand size
   *
   * @return A mask of the form F..F0..0
   */
  public static long getAllButMask(final OperandSize largerSize, final OperandSize smallerSize) {
    return getAllBitsMask(largerSize) ^ getAllBitsMask(smallerSize);
  }

  /**
   * Returns a mask that isolates the most significant bit for values of the given size.
   *
   * @param size The size of the value
   *
   * @return The mask that masks the MSB of values of that size
   */
  public static long getMsbMask(final OperandSize size) {

    switch (size) {
      case BYTE:
        return 128L;
      case WORD:
        return 32768L;
      case DWORD:
        return 2147483648L;
      case QWORD:
        return 0x8000000000000000L;
      default:
        break;
    }

    throw new IllegalArgumentException("Error: Invalid argument size");
  }

  /**
   * Finds the next biggest operand size for a given operand size.
   *
   * @param size The smaller operand size
   *
   * @return The next bigger operand size
   */
  public static OperandSize getNextSize(final OperandSize size) {
    switch (size) {
      case BYTE:
        return OperandSize.WORD;
      case WORD:
        return OperandSize.DWORD;
      case DWORD:
        return OperandSize.QWORD;
      case QWORD:
        return OperandSize.OWORD;
      default:
        break;
    }

    throw new IllegalArgumentException("Error: Invalid argument size");
  }

  /**
   * Returns a shift mask that can be used to shift isolated MSBs into LSBs for values of a given
   * operand size.
   *
   * @param size The operand size of the value
   *
   * @return The shift mask.
   */
  public static long getShiftMsbLsbMask(final OperandSize size) {
    return -(size.getBitSize() - 1);
  }

  /**
   * Finds out whether an expression is a size information expression.
   *
   * @param expression The expression in question
   *
   * @return True, if the expression is a size information expression. False, otherwise.
   */
  public static boolean isSizeExpression(final IOperandTreeNode expression) {
    return OperandSize.isSizeString(expression.getValue());
  }
}

