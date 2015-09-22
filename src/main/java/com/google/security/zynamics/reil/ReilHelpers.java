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
package com.google.security.zynamics.reil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Convert;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides some helper functions for working with REIL instructions.
 */
public class ReilHelpers {
  /**
   * The mnemonic of the REIL instruction ADD.
   */
  public static final String OPCODE_ADD = "add";
  public static final int _OPCODE_ADD = 0;

  /**
   * The mnemonic of the REIL instruction AND.
   */
  public static final String OPCODE_AND = "and";
  public static final int _OPCODE_AND = 1;

  /**
   * The mnemonic of the REIL instruction BISZ.
   */
  public static final String OPCODE_BISZ = "bisz";
  public static final int _OPCODE_BISZ = 2;

  /**
   * The mnemonic of the REIL instruction BSH.
   */
  public static final String OPCODE_BSH = "bsh";
  public static final int _OPCODE_BSH = 3;

  /**
   * The mnemonic of the REIL instruction CONSUME.
   */
  public static final String OPCODE_CONSUME = "consume";
  public static final int _OPCODE_CONSUME = 4;

  /**
   * The mnemonic of the REIL instruction DEFINE.
   */
  public static final String OPCODE_DEFINE = "define";
  public static final int _OPCODE_DEFINE = 5;

  /**
   * The mnemonic of the REIL instruction DIV.
   */
  public static final String OPCODE_DIV = "div";
  public static final int _OPCODE_DIV = 6;

  /**
   * The mnemonic of the REIL instruction JCC.
   */
  public static final String OPCODE_JCC = "jcc";
  public static final int _OPCODE_JCC = 7;

  /**
   * The mnemonic of the REIL instruction LDM.
   */
  public static final String OPCODE_LDM = "ldm";
  public static final int _OPCODE_LDM = 8;

  /**
   * The mnemonic of the REIL instruction MOD.
   */
  public static final String OPCODE_MOD = "mod";
  public static final int _OPCODE_MOD = 9;

  /**
   * The mnemonic of the REIL instruction MUL.
   */
  public static final String OPCODE_MUL = "mul";
  public static final int _OPCODE_MUL = 10;

  /**
   * The mnemonic of the REIL instruction NOP.
   */
  public static final String OPCODE_NOP = "nop";
  public static final int _OPCODE_NOP = 11;

  /**
   * The mnemonic of the REIL instruction OR.
   */
  public static final String OPCODE_OR = "or";
  public static final int _OPCODE_OR = 12;

  /**
   * The mnemonic of the REIL instruction STM.
   */
  public static final String OPCODE_STM = "stm";
  public static final int _OPCODE_STM = 13;

  /**
   * The mnemonic of the REIL instruction STR.
   */
  public static final String OPCODE_STR = "str";
  public static final int _OPCODE_STR = 14;

  /**
   * The mnemonic of the REIL instruction SUB.
   */
  public static final String OPCODE_SUB = "sub";
  public static final int _OPCODE_SUB = 15;

  /**
   * The mnemonic of the REIL instruction UNDEF.
   */
  public static final String OPCODE_UNDEF = "undef";
  public static final int _OPCODE_UNDEF = 16;

  /**
   * The mnemonic of the REIL instruction UNKN.
   */
  public static final String OPCODE_UNKNOWN = "unkn";
  public static final int _OPCODE_UNKNOWN = 17;

  /**
   * The mnemonic of the REIL instruction XOR.
   */
  public static final String OPCODE_XOR = "xor";
  public static final int _OPCODE_XOR = 18;

  private static final ImmutableBiMap<String, Integer> MnemonicCodeMap =
      new ImmutableBiMap.Builder<String, Integer>()
          .put(OPCODE_ADD, _OPCODE_ADD)
          .put(OPCODE_AND, _OPCODE_AND)
          .put(OPCODE_BISZ, _OPCODE_BISZ)
          .put(OPCODE_BSH, _OPCODE_BSH)
          .put(OPCODE_CONSUME, _OPCODE_CONSUME)
          .put(OPCODE_DEFINE, _OPCODE_DEFINE)
          .put(OPCODE_DIV, _OPCODE_DIV)
          .put(OPCODE_JCC, _OPCODE_JCC)
          .put(OPCODE_LDM, _OPCODE_LDM)
          .put(OPCODE_MOD, _OPCODE_MOD)
          .put(OPCODE_MUL, _OPCODE_MUL)
          .put(OPCODE_NOP, _OPCODE_NOP)
          .put(OPCODE_OR, _OPCODE_OR)
          .put(OPCODE_STM, _OPCODE_STM)
          .put(OPCODE_STR, _OPCODE_STR)
          .put(OPCODE_SUB, _OPCODE_SUB)
          .put(OPCODE_UNDEF, _OPCODE_UNDEF)
          .put(OPCODE_UNKNOWN, _OPCODE_UNKNOWN)
          .put(OPCODE_XOR, _OPCODE_XOR)
          .build();

  /**
   * Creates a REIL instruction with two operands.
   *
   * @param opcode The opcode of the instruction
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand
   * @param meta Meta data to be associated with the instruction.
   *
   * @return The created instruction
   */
  private static ReilInstruction createBinaryInstruction(final String opcode,
      final IAddress offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize thirdSize,
      final String thirdValue,
      final String... meta) {
    Preconditions.checkArgument((meta.length % 2) == 0,
        "Invalid number of arguments in metadata array.");

    final ReilOperand firstOperand = createOperand(firstSize, firstValue);
    final ReilOperand secondOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand thirdOperand = createOperand(thirdSize, thirdValue);

    final ReilInstruction instruction =
        new ReilInstruction(offset, opcode, firstOperand, secondOperand, thirdOperand);

    for (int i = 0; i < meta.length; i += 2) {
      instruction.setMetaData(meta[i], meta[i + 1]);
    }

    return instruction;
  }

  /**
   * Creates a REIL instruction with three operands.
   *
   * @param opcode The opcode of the instruction
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand
   *
   * @return The created instruction
   */
  private static ReilInstruction createTrinaryInstruction(final String opcode,
      final IAddress offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    final ReilOperand firstOperand = createOperand(firstSize, firstValue);
    final ReilOperand secondOperand = createOperand(secondSize, secondValue);
    final ReilOperand thirdOperand = createOperand(thirdSize, thirdValue);
    checkTrinaryOperandSizeTypes(firstSize, secondSize, thirdSize);
    return new ReilInstruction(offset, opcode, firstOperand, secondOperand, thirdOperand);
  }

  private static void checkTrinaryOperandSizeTypes(final OperandSize first,
      final OperandSize second, final OperandSize third) {
    checkBinaryOperandSizeTypes(first, second);
    Preconditions.checkNotNull(third, "First size argument can not be null.");
    Preconditions.checkArgument(third != OperandSize.ADDRESS,
        "The size for the third argument can not be of type ADDRESS.");
  }

  private static void checkBinaryOperandSizeTypes(final OperandSize first,
      final OperandSize second) {
    Preconditions.checkNotNull(first, "First size argument can not be null.");
    Preconditions.checkNotNull(second, "Second size argument can not be null.");
    Preconditions.checkArgument(first != OperandSize.ADDRESS,
        "The size for the first argument can not be of type ADDRESS.");
    Preconditions.checkArgument(second != OperandSize.ADDRESS,
        "The size for the second argument can not be of type ADDRESS.");
  }

  /**
   * Creates an ADD instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createAdd(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_ADD,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates an AND instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createAnd(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_AND,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a BISZ instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createBisz(final long offset, final OperandSize firstSize,
      final String firstValue, final OperandSize thirdSize, final String thirdValue) {
    return createBinaryInstruction(ReilHelpers.OPCODE_BISZ,
        new CAddress(offset),
        firstSize,
        firstValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a BSH instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (shift)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createBsh(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_BSH,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  public static ReilInstruction createDefine(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_DEFINE,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a DIV instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (dividend)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (divisor)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createDiv(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_DIV,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a JCC instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (jump condition)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (jump target)
   * @param meta Meta information to be associated with the instruction.
   *
   * @return The created instruction
   */
  public static ReilInstruction createJcc(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize thirdSize,
      final String thirdValue,
      final String... meta) {
    return createBinaryInstruction(ReilHelpers.OPCODE_JCC,
        new CAddress(offset),
        firstSize,
        firstValue,
        thirdSize,
        thirdValue,
        meta);
  }

  /**
   * Creates a LDM instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (start address)
   * @param thirdSize The size of the third operand (number of bytes to be read from memory)
   * @param thirdValue The value of the third operand (target register)
   *
   * @return The created instruction
   */
  public static ReilInstruction createLdm(final long offset, final OperandSize firstSize,
      final String firstValue, final OperandSize thirdSize, final String thirdValue) {
    checkBinaryOperandSizeTypes(firstSize, thirdSize);
    return createBinaryInstruction(ReilHelpers.OPCODE_LDM,
        new CAddress(offset),
        firstSize,
        firstValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a MOD instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createMod(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_MOD,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a MUL instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createMul(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_MUL,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a NOP instruction.
   *
   * @param offset The offset of the instruction
   *
   * @return The created instruction
   */
  public static ReilInstruction createNop(final long offset) {
    final ReilOperand firstOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand secondOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand thirdOperand = createOperand(OperandSize.EMPTY, "");
    return new ReilInstruction(new CAddress(offset), ReilHelpers.OPCODE_NOP, firstOperand,
        secondOperand, thirdOperand);
  }

  public static ReilOperand createOperand(final OperandSize size, final String value) {
    final ReilOperandNode root =
        new ReilOperandNode(size.toSizeString(), ExpressionType.SIZE_PREFIX);
    final ReilOperandNode child = new ReilOperandNode(value, getOperandType(value));
    ReilOperandNode.link(root, child);
    return new ReilOperand(root);
  }

  /**
   * Creates a OR instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createOr(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_OR,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a STM instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (value to store)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (start address)
   *
   * @return The created instruction
   */
  public static ReilInstruction createStm(final long offset, final OperandSize firstSize,
      final String firstValue, final OperandSize thirdSize, final String thirdValue) {
    checkBinaryOperandSizeTypes(firstSize, thirdSize);
    return createBinaryInstruction(ReilHelpers.OPCODE_STM,
        new CAddress(offset),
        firstSize,
        firstValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a STR instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createStr(final long offset, final OperandSize firstSize,
      final String firstValue, final OperandSize thirdSize, final String thirdValue) {
    checkBinaryOperandSizeTypes(firstSize, thirdSize);
    return createBinaryInstruction(ReilHelpers.OPCODE_STR,
        new CAddress(offset),
        firstSize,
        firstValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates a SUB instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createSub(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_SUB,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  /**
   * Creates an UNDEF instruction.
   *
   * @param offset The offset of the instruction
   * @param size The size of the value
   * @param value The value to be undefined
   *
   * @return The created instruction
   */
  public static ReilInstruction createUndef(final long offset, final OperandSize size,
      final String value) {
    final ReilOperand firstOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand secondOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand thirdOperand = createOperand(size, value);
    return new ReilInstruction(new CAddress(offset), ReilHelpers.OPCODE_UNDEF, firstOperand,
        secondOperand, thirdOperand);
  }

  /**
   * Creates an UNKNOWN instruction.
   *
   * @param offset The offset of the instruction
   *
   * @return The created instruction
   */
  public static ReilInstruction createUnknown(final long offset) {
    final ReilOperand firstOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand secondOperand = createOperand(OperandSize.EMPTY, "");
    final ReilOperand thirdOperand = createOperand(OperandSize.EMPTY, "");
    return new ReilInstruction(new CAddress(offset), ReilHelpers.OPCODE_UNKNOWN, firstOperand,
        secondOperand, thirdOperand);
  }

  /**
   * Creates a XOR instruction.
   *
   * @param offset The offset of the instruction
   * @param firstSize The size of the first operand
   * @param firstValue The value of the first operand (input)
   * @param secondSize The size of the second operand
   * @param secondValue The value of the second operand (input)
   * @param thirdSize The size of the third operand
   * @param thirdValue The value of the third operand (result)
   *
   * @return The created instruction
   */
  public static ReilInstruction createXor(final long offset,
      final OperandSize firstSize,
      final String firstValue,
      final OperandSize secondSize,
      final String secondValue,
      final OperandSize thirdSize,
      final String thirdValue) {
    return createTrinaryInstruction(ReilHelpers.OPCODE_XOR,
        new CAddress(offset),
        firstSize,
        firstValue,
        secondSize,
        secondValue,
        thirdSize,
        thirdValue);
  }

  public static ExpressionType getOperandType(final String value) {
    if (Convert.isDecString(value) || value.startsWith("-")) {
      return ExpressionType.IMMEDIATE_INTEGER;
    } else if (value.startsWith("t")) {
      return ExpressionType.SYMBOL;
    } else if (OperandSize.isSizeString(value)) {
      return ExpressionType.SIZE_PREFIX;
    } else {
      return ExpressionType.REGISTER;
    }
  }

  /**
   * Checks whether instructions with the given mnemonic use all three of their operands.
   *
   * @param mnemonic The mnemonic of the instruction.
   *
   * @return True, for ADD, SUB, MUL, DIV, BSH, AND, OR and XOR. False, otherwise.
   */
  public static boolean isBinaryInstruction(final String mnemonic) {
    return mnemonic.equals(ReilHelpers.OPCODE_ADD) || mnemonic.equals(ReilHelpers.OPCODE_SUB)
        || mnemonic.equals(ReilHelpers.OPCODE_MUL) || mnemonic.equals(ReilHelpers.OPCODE_DIV)
        || mnemonic.equals(ReilHelpers.OPCODE_BSH) || mnemonic.equals(ReilHelpers.OPCODE_AND)
        || mnemonic.equals(ReilHelpers.OPCODE_OR) || mnemonic.equals(ReilHelpers.OPCODE_XOR);
  }

  /**
   * Checks whether a given instruction is a conditional jump.
   *
   * @param instruction The instruction in question.
   *
   * @return True, if the given instruction is a conditional jump. False, otherwise.
   */
  public static boolean isConditionalJump(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Argument instruction can't be null.");

    return instruction.getMnemonic().equals(OPCODE_JCC)
        && (instruction.getFirstOperand().getType() == OperandType.REGISTER);
  }

  public static boolean isDelayedBranch(final ReilInstruction instruction) {
    return instruction.getMetaData().containsKey("branch_delay")
        && instruction.getMetaData().get("branch_delay").equalsIgnoreCase("true");
  }

  public static boolean isDelayedTrueBranch(final ReilInstruction instruction) {
    return instruction.getMetaData().containsKey("branch_delay_true")
        && instruction.getMetaData().get("branch_delay_true").equalsIgnoreCase("true");
  }

  public static boolean isFunctionCall(final ReilInstruction instruction) {
    return instruction.getMetaData().containsKey("isCall")
        && instruction.getMetaData().get("isCall").equalsIgnoreCase("true");
  }

  public static boolean isJump(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Argument instruction can't be null.");

    return instruction.getMnemonic().equals(OPCODE_JCC);
  }

  public static boolean isNativeRegister(final ReilOperand operand) {
    return (operand.getType() == OperandType.REGISTER) && !isTemporaryRegister(operand);
  }

  public static boolean isTemporaryRegister(final ReilOperand operand) {
    return (operand.getType() == OperandType.REGISTER) && operand.getValue().startsWith("t");
  }

  public static boolean isTemporaryRegister(final String value) {
    return value.startsWith("t");
  }

  /**
   * Checks whether the instructions with the given mnemonic use only their first and third
   * operands.
   *
   * @param mnemonic The mnemonic of the instruction.
   *
   * @return True, for BISZ and STR. False, otherwise.
   */
  public static boolean isUnaryInstruction(final String mnemonic) {
    return mnemonic.equals(ReilHelpers.OPCODE_BISZ) || mnemonic.equals(ReilHelpers.OPCODE_STR);
  }

  /**
   * Checks whether a given instruction is an unconditional jump.
   *
   * @param instruction The instruction in question.
   *
   * @return True, if the given instruction is an unconditional jump. False, otherwise.
   */
  public static boolean isUnconditionalJump(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Argument instruction can't be null.");

    return instruction.getMnemonic().equals(OPCODE_JCC) && !isConditionalJump(instruction);
  }

  public static boolean isValidReilMnemonic(final String mnemonic) {
    return MnemonicCodeMap.containsKey(mnemonic);
  }

  public static String MnemonicCodeToMnemonic(final int code) {
    return MnemonicCodeMap.inverse().get(code);
  }

  public static int MnemonicToMnemonicCode(final String mnemonic) {
    Preconditions.checkNotNull(mnemonic, "Mnemonic argument can not be null.");

    return MnemonicCodeMap.get(mnemonic);
  }

  public static boolean setsValue(final ReilInstruction instruction, final String register) {
    return instruction.getThirdOperand().getValue().equals(register);
  }

  /**
   * Converts a string that contains a subaddress into a long value with the same offset as the
   * string. Attention: A valid subaddress of the form "number.number" must be passed to the
   * function or an exception is thrown.
   */
  public static long subAddressToLong(final String subaddress) {
    Preconditions.checkNotNull(subaddress, "Argument subaddress can't be null.");

    if (subaddress.contains(".")) {

      final String[] parts = subaddress.split("\\.");

      Preconditions.checkArgument(parts.length == 2,
          "Argument subaddress is not a valid subaddress.");

      final long firstPart = Long.parseLong(parts[0]);
      final long secondPart = Long.parseLong(parts[1]);

      return (firstPart * 0x100) + secondPart;

    } else {
      throw new IllegalArgumentException("Argument subaddress is not a valid subaddress.");
    }
  }

  public static IAddress toNativeAddress(final IAddress address) {
    return new CAddress(address.toLong() / 0x100);
  }

  public static IAddress toReilAddress(final IAddress address) {
    return new CAddress(address.toLong() * 0x100);
  }

  public static boolean uses(final ReilInstruction instruction, final String register) {
    return instruction.getFirstOperand().getValue().equals(register)
        || instruction.getSecondOperand().getValue().equals(register) || (
            instruction.getMnemonic().equals(ReilHelpers.OPCODE_STM)
            && instruction.getThirdOperand().getValue().equals(register)) || (
            instruction.getMnemonic().equals(ReilHelpers.OPCODE_JCC)
            && instruction.getThirdOperand().getValue().equals(register));
  }

  /**
   * Checks whether an instruction with the given mnemonic uses its first operand.
   *
   * @param mnemonic The mnemonic of the instruction.
   *
   * @return True, if the instruction uses its first operand. False, otherwise.
   */
  public static boolean usesFirstOperand(final Integer mnemonic) {
    return !mnemonic.equals(_OPCODE_NOP);
  }

  /**
   * Checks whether an instruction with the given mnemonic uses its second operand.
   *
   * @param mnemonic The mnemonic of the instruction.
   *
   * @return True, if the instruction uses its first operand. False, otherwise.
   */
  public static boolean usesSecondOperand(final Integer mnemonic) {
    return !mnemonic.equals(ReilHelpers._OPCODE_NOP) && !mnemonic.equals(ReilHelpers._OPCODE_BISZ)
        && !mnemonic.equals(ReilHelpers._OPCODE_STR) && !mnemonic.equals(ReilHelpers._OPCODE_STM)
        && !mnemonic.equals(ReilHelpers._OPCODE_LDM) && !mnemonic.equals(ReilHelpers._OPCODE_JCC);
  }

  /**
   * Checks whether an instruction with the given mnemonic uses its third operand.
   *
   * @param mnemonic The mnemonic of the instruction.
   *
   * @return True, if the instruction uses its first operand. False, otherwise.
   */
  public static boolean usesThirdOperand(final String mnemonic) {
    return !mnemonic.equals(ReilHelpers.OPCODE_NOP);
  }

  public static List<String> useValues(final ReilInstruction instruction) {
    final List<String> values = new ArrayList<>();

    if (instruction.getFirstOperand().getType() == OperandType.REGISTER) {
      values.add(instruction.getFirstOperand().getValue());
    }

    if (instruction.getSecondOperand().getType() == OperandType.REGISTER) {
      values.add(instruction.getSecondOperand().getValue());
    }

    return values;
  }

  public static boolean writesThirdOperand(final Integer mnemonic) {
    if (mnemonic.equals(_OPCODE_STM) || mnemonic.equals(_OPCODE_JCC)
      || mnemonic.equals(_OPCODE_NOP)) {
      return false;
    }
    return true;
  }

  public static long nextReilAddress(final IInstruction instruction,
      final List<ReilInstruction> instructions) {
    return instruction.getAddress().toLong() * 0x100 + instructions.size();
  }
}
