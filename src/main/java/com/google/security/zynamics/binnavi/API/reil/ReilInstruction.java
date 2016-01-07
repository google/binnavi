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
package com.google.security.zynamics.binnavi.API.reil;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.zylib.disassembly.CAddress;

// / Single REIL instruction
/**
 * Represents a single REIL instruction.
 */
public final class ReilInstruction implements
    ApiObject<com.google.security.zynamics.reil.ReilInstruction> {
  /**
   * The wrapped internal instruction.
   */
  private final com.google.security.zynamics.reil.ReilInstruction m_instruction;

  /**
   * First operand of the instruction.
   */
  private final ReilOperand m_firstOperand;

  /**
   * Second operand of the instruction.
   */
  private final ReilOperand m_secondOperand;

  /**
   * Third operand of the instruction.
   */
  private final ReilOperand m_thirdOperand;

  // ! Creates a new REIL instruction.
  /**
   * Creates a new REIL instruction.
   *
   * @param address Address of the new REIL instruction.
   * @param mnemonic Mnemonic of the new REIL instruction. In most cases this is one of the
   *        constants found in ReilMnemonics.
   * @param firstOperand First operand of the new REIL instruction.
   * @param secondOperand Second operand of the new REIL instruction.
   * @param thirdOperand Third operand of the new REIL instruction.
   */
  public ReilInstruction(final Address address, final String mnemonic,
      final ReilOperand firstOperand, final ReilOperand secondOperand,
      final ReilOperand thirdOperand) {
    this(new com.google.security.zynamics.reil.ReilInstruction(
        new CAddress(check(address).toLong()), checkMnemonic(mnemonic),
        check1(firstOperand).getNative(), check2(secondOperand).getNative(),
        check3(thirdOperand).getNative()));
  }

  // / @cond INTERNAL
  /**
   * Creates a new API REIL instruction object.
   *
   * @param instruction The wrapped internal REIL instruction.
   */
  public ReilInstruction(final com.google.security.zynamics.reil.ReilInstruction instruction) {
    m_instruction = instruction;

    m_firstOperand = new ReilOperand(instruction.getFirstOperand());
    m_secondOperand = new ReilOperand(instruction.getSecondOperand());
    m_thirdOperand = new ReilOperand(instruction.getThirdOperand());
  }

  /**
   * Checks whether the address argument is null. If it is, an exception is thrown.
   *
   * @param address The address argument to check.
   *
   * @return The given address argument.
   */
  private static Address check(final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    return address;
  }

  /**
   * Checks whether the first operand argument is null. If it is, an exception is thrown.
   *
   * @param firstOperand The operand argument to check.
   *
   * @return The given operand argument.
   */
  private static ReilOperand check1(final ReilOperand firstOperand) {
    Preconditions.checkNotNull(firstOperand, "Error: First operand can not be null");

    return firstOperand;
  }

  /**
   * Checks whether the second operand argument is null. If it is, an exception is thrown.
   *
   * @param secondOperand The operand argument to check.
   *
   * @return The given operand argument.
   */
  private static ReilOperand check2(final ReilOperand secondOperand) {
    Preconditions.checkNotNull(secondOperand, "Error: Second operand can not be null");

    return secondOperand;
  }

  /**
   * Checks whether the third operand argument is null. If it is, an exception is thrown.
   *
   * @param thirdOperand The operand argument to check.
   *
   * @return The given operand argument.
   */
  // / @endcond
  private static ReilOperand check3(final ReilOperand thirdOperand) {
    Preconditions.checkNotNull(thirdOperand, "Error: Third operand can not be null");

    return thirdOperand;
  }

  private static String checkMnemonic(final String mnemonic) {
    Preconditions.checkNotNull(mnemonic, "Error: mnemonic argument can not be null");
    Preconditions.checkArgument(
        com.google.security.zynamics.reil.ReilHelpers.isValidReilMnemonic(mnemonic),
        "Error: mnemonic argument is not a valid REIL mnemonic");

    return mnemonic;
  }

  // ! Creates a new ADD instruction.
  /**
   * Creates a new ADD instruction.
   *
   * @param address Address of the ADD instruction.
   * @param firstOperand First operand of the ADD instruction.
   * @param secondOperand Second operand of the ADD instruction.
   * @param thirdOperand Third operand of the ADD instruction.
   *
   * @return The created ADD instruction.
   */
  public static ReilInstruction createAdd(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.ADD, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new AND instruction.
  /**
   * Creates a new AND instruction.
   *
   * @param address Address of the AND instruction.
   * @param firstOperand First operand of the AND instruction.
   * @param secondOperand Second operand of the AND instruction.
   * @param thirdOperand Third operand of the AND instruction.
   *
   * @return The created AND instruction.
   */
  public static ReilInstruction createAnd(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.AND, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new BISZ instruction.
  /**
   * Creates a new BISZ instruction.
   *
   * @param address Address of the BISZ instruction.
   * @param inputValue The input value of the BISZ instruction.
   * @param outputValue The output value of the BISZ instruction.
   *
   * @return The created BISZ instruction.
   */
  public static ReilInstruction createBisz(final Address address, final ReilOperand inputValue,
      final ReilOperand outputValue) {
    return new ReilInstruction(address, ReilMnemonics.BISZ, inputValue, ReilOperand.EMPTY_OPERAND,
        outputValue);
  }

  // ! Creates a new BSH instruction.
  /**
   * Creates a new BSH instruction.
   *
   * @param address Address of the BSH instruction.
   * @param firstOperand First operand of the BSH instruction.
   * @param secondOperand Second operand of the BSH instruction.
   * @param thirdOperand Third operand of the BSH instruction.
   *
   * @return The created BSH instruction.
   */
  public static ReilInstruction createBsh(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.BSH, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new DIV instruction.
  /**
   * Creates a new DIV instruction.
   *
   * @param address Address of the DIV instruction.
   * @param firstOperand First operand of the DIV instruction.
   * @param secondOperand Second operand of the DIV instruction.
   * @param thirdOperand Third operand of the DIV instruction.
   *
   * @return The created DIV instruction.
   */
  public static ReilInstruction createDiv(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.DIV, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new JCC instruction.
  /**
   * Creates a new JCC instruction.
   *
   * @param address Address of the JCC instruction.
   * @param jumpCondition Describes the jump condition of the JCC instruction.
   * @param jumpTarget Describes the jump target of the JCC instruction.
   *
   * @return The created JCC instruction.
   */
  public static ReilInstruction createJcc(final Address address, final ReilOperand jumpCondition,
      final ReilOperand jumpTarget) {
    return new ReilInstruction(address, ReilMnemonics.JCC, jumpCondition, ReilOperand.EMPTY_OPERAND,
        jumpTarget);
  }

  // ! Creates a new LDM instruction.
  /**
   * Creates a new LDM instruction.
   *
   * @param address Address of the LDM instruction.
   * @param loadSource Describes the memory address from which the LDM instruction reads.
   * @param loadTarget Describes the target register where the loaded value is stored.
   *
   * @return The created LDM instruction.
   */
  public static ReilInstruction createLdm(final Address address, final ReilOperand loadSource,
      final ReilOperand loadTarget) {
    return new ReilInstruction(address, ReilMnemonics.LDM, loadSource, ReilOperand.EMPTY_OPERAND,
        loadTarget);
  }

  // ! Creates a new MOD instruction.
  /**
   * Creates a new MOD instruction.
   *
   * @param address Address of the MOD instruction.
   * @param firstOperand First operand of the MOD instruction.
   * @param secondOperand Second operand of the MOD instruction.
   * @param thirdOperand Third operand of the MOD instruction.
   *
   * @return The created MOD instruction.
   */
  public static ReilInstruction createMod(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.MOD, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new MUL instruction.
  /**
   * Creates a new MUL instruction.
   *
   * @param address Address of the MUL instruction.
   * @param firstOperand First operand of the MUL instruction.
   * @param secondOperand Second operand of the MUL instruction.
   * @param thirdOperand Third operand of the MUL instruction.
   *
   * @return The created MUL instruction.
   */
  public static ReilInstruction createMul(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.MUL, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new NOP instruction.
  /**
   * Creates a new NOP instruction.
   *
   * @param address Address of the NOP instruction.
   *
   * @return The created NOP instruction.
   */
  public static ReilInstruction createNop(final Address address) {
    return new ReilInstruction(address, ReilMnemonics.NOP, ReilOperand.EMPTY_OPERAND,
        ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND);
  }

  // ! Creates a new OR instruction.
  /**
   * Creates a new OR instruction.
   *
   * @param address Address of the OR instruction.
   * @param firstOperand First operand of the OR instruction.
   * @param secondOperand Second operand of the OR instruction.
   * @param thirdOperand Third operand of the OR instruction.
   *
   * @return The created OR instruction.
   */
  public static ReilInstruction createOr(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.OR, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new STM instruction.
  /**
   * Creates a new STM instruction.
   *
   * @param address Address of the STM instruction.
   * @param storeValue Describes the value that is stored by the STM instruction.
   * @param storeTarget Describes the memory address where the value is stored.
   *
   * @return The created STM instruction.
   */
  public static ReilInstruction createStm(final Address address, final ReilOperand storeValue,
      final ReilOperand storeTarget) {
    return new ReilInstruction(address, ReilMnemonics.STM, storeValue, ReilOperand.EMPTY_OPERAND,
        storeTarget);
  }

  // ! Creates a new STR instruction.
  /**
   * Creates a new STR instruction.
   *
   * @param address Address of the STR instruction.
   * @param storeValue Describes the value that is stored by the STR instruction.
   * @param storeTarget Describes the register where the value is stored.
   *
   * @return The created STR instruction.
   */
  public static ReilInstruction createStr(final Address address, final ReilOperand storeValue,
      final ReilOperand storeTarget) {
    return new ReilInstruction(address, ReilMnemonics.STR, storeValue, ReilOperand.EMPTY_OPERAND,
        storeTarget);
  }

  // ! Creates a new SUB instruction.
  /**
   * Creates a new SUB instruction.
   *
   * @param address Address of the SUB instruction.
   * @param firstOperand First operand of the SUB instruction.
   * @param secondOperand Second operand of the SUB instruction.
   * @param thirdOperand Third operand of the SUB instruction.
   *
   * @return The created SUB instruction.
   */
  public static ReilInstruction createSub(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.SUB, firstOperand, secondOperand,
        thirdOperand);
  }

  // ! Creates a new UNDEF instruction.
  /**
   * Creates a new UNDEF instruction.
   *
   * @param address Address of the UNDEF instruction.
   *
   * @return The created UNDEF instruction.
   */
  public static ReilInstruction createUndef(final Address address) {
    return new ReilInstruction(address, ReilMnemonics.UNDEF, ReilOperand.EMPTY_OPERAND,
        ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND);
  }

  // ! Creates a new UNKNOWN instruction.
  /**
   * Creates a new UNKNOWN instruction.
   *
   * @param address Address of the UNKNOWN instruction.
   *
   * @return The created UNKNOWN instruction.
   */
  public static ReilInstruction createUnknown(final Address address) {
    return new ReilInstruction(address, ReilMnemonics.UNKNOWN, ReilOperand.EMPTY_OPERAND,
        ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND);
  }

  // ! Creates a new XOR instruction.
  /**
   * Creates a new XOR instruction.
   *
   * @param address Address of the XOR instruction.
   * @param firstOperand First operand of the XOR instruction.
   * @param secondOperand Second operand of the XOR instruction.
   * @param thirdOperand Third operand of the XOR instruction.
   *
   * @return The created XOR instruction.
   */
  public static ReilInstruction createXor(final Address address, final ReilOperand firstOperand,
      final ReilOperand secondOperand, final ReilOperand thirdOperand) {
    return new ReilInstruction(address, ReilMnemonics.XOR, firstOperand, secondOperand,
        thirdOperand);
  }

  @Override
  public com.google.security.zynamics.reil.ReilInstruction getNative() {
    return m_instruction;
  }

  // ! Address of the instruction.
  /**
   * Returns the address of the REIL instruction.
   *
   * @return The address of the REIL instruction.
   */
  public Address getAddress() {
    return new Address(m_instruction.getAddress().toBigInteger());
  }

  // ! First operand of the instruction.
  /**
   * Returns the first operand of the REIL instruction.
   *
   * @return The first operand of the REIL instruction.
   */
  public ReilOperand getFirstOperand() {
    return m_firstOperand;
  }

  // ! Metadata of the instruction.
  /**
   * Returns a map that contains all meta-information associated with the instruction.
   *
   * @return The meta-data map.
   */
  public Map<String, String> getMetaData() {
    return new HashMap<String, String>(m_instruction.getMetaData());
  }

  // ! Mnemonic of the instruction.
  /**
   * Returns the mnemonic of the REIL instruction.
   *
   * @return The mnemonic of the REIL instruction.
   */
  public String getMnemonic() {
    return m_instruction.getMnemonic();
  }

  // ! Second operand of the instruction.
  /**
   * Returns the second operand of the REIL instruction.
   *
   * @return The second operand of the REIL instruction.
   */
  public ReilOperand getSecondOperand() {
    return m_secondOperand;
  }

  // ! Third operand of the instruction.
  /**
   * Returns the third operand of the REIL instruction.
   *
   * @return The third operand of the REIL instruction.
   */
  public ReilOperand getThirdOperand() {
    return m_thirdOperand;
  }

  // ! Sets meta-data information of the REIL instruction.
  /**
   * Sets meta-data information of the REIL instruction.
   *
   * @param key They key of the piece of meta-data to set.
   * @param value The value of the piece of meta-data. If this value is null, the key is removed
   *        from the meta-data set.
   */
  public void setMetaData(final String key, final String value) {
    Preconditions.checkNotNull(key, "Error: Key argument can not be null");

    m_instruction.setMetaData(key, value);
  }

  // ! Printable representation of the instruction.
  /**
   * Returns the string representation of the REIL instruction.
   *
   * @return The string representation of the REIL instruction.
   */
  @Override
  public String toString() {
    return m_instruction.toString();
  }
}
