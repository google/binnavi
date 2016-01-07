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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.zylib.disassembly.CAddress;

// ! Provides oft-used REIL functions.
/**
 * This class provides a few helper functions which are very useful while working with REIL code.
 */
public final class ReilHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ReilHelpers() {
  }

  // ! Checks whether an instruction is a function call.
  /**
   * Determines whether a given REIL instruction is a function call.
   *
   * @param instruction The instruction to check.
   *
   * @return True, if the instruction is a function call. False, otherwise.
   */
  public static boolean isFunctionCall(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null");

    return com.google.security.zynamics.reil.ReilHelpers.isFunctionCall(instruction.getNative());
  }

  /**
   * Determines whether a given operand is a native register.
   *
   * @param operand The value to check.
   *
   * @return True, if the operand is a native register. False, otherwise.
   */
  public static boolean isNativeRegister(final ReilOperand operand) {
    Preconditions.checkNotNull(operand, "Operand argument can not be null");

    return com.google.security.zynamics.reil.ReilHelpers.isNativeRegister(operand.getNative());
  }

  /**
   * Determines whether a given REIL operand is a register.
   *
   * @param operand The REIL operand to check.
   *
   * @return True, if the operand is a register operand. False, otherwise.
   */
  public static boolean isRegister(final ReilOperand operand) {
    Preconditions.checkNotNull(operand, "Operand argument argument can not be null");

    return operand.getType() == OperandType.REGISTER;
  }

  /**
   * Determines whether a given operand is a REIL register.
   *
   * @param operand The operand to check.
   *
   * @return True, if the operand is a REIL register. False, otherwise.
   */
  public static boolean isReilRegister(final ReilOperand operand) {
    Preconditions.checkNotNull(operand, "Operand argument can not be null");

    return com.google.security.zynamics.reil.ReilHelpers.isTemporaryRegister(operand.getNative());
  }

  /**
   * Determines whether a given string value is a REIL register.
   *
   * @param value The string value to check.
   *
   * @return True, if the string value is a REIL register. False, otherwise.
   */
  public static boolean isReilRegister(final String value) {
    Preconditions.checkNotNull(value, "Value argument can not be null");

    return com.google.security.zynamics.reil.ReilHelpers.isTemporaryRegister(value);
  }

  /**
   * Determines whether a given REIL instruction is one of the ternary instructions that use all of
   * their operands.
   *
   * @param instruction The REIL instruction to check.
   *
   * @return True, if the instruction is a ternary instruction. False, otherwise.
   */
  public static boolean isTernaryInstruction(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Instruction argument can not be null");

    return instruction.getMnemonic().equalsIgnoreCase("ADD")
        || instruction.getMnemonic().equalsIgnoreCase("SUB")
        || instruction.getMnemonic().equalsIgnoreCase("MUL")
        || instruction.getMnemonic().equalsIgnoreCase("DIV")
        || instruction.getMnemonic().equalsIgnoreCase("MOD")
        || instruction.getMnemonic().equalsIgnoreCase("BSH")
        || instruction.getMnemonic().equalsIgnoreCase("AND")
        || instruction.getMnemonic().equalsIgnoreCase("OR")
        || instruction.getMnemonic().equalsIgnoreCase("XOR");
  }

  /**
   * Determines whether a given REIL instruction is an unconditional jump instruction.
   *
   * @param instruction The REIL instruction to check.
   *
   * @return True, if the REIL instruction is an unconditional jump instruction. False, otherwise.
   */
  public static boolean isUnconditionalJump(final ReilInstruction instruction) {
    Preconditions.checkNotNull(instruction, "Instruction argument can not be null");

    return com.google.security.zynamics.reil.ReilHelpers.isUnconditionalJump(instruction.getNative());
  }

  // ! Converts REIL addresses to native addresses.
  /**
   * Converts a REIL address to a native address by getting rid of the lowest 8 bits of the REIL
   * input address.
   *
   * @param address A REIL address.
   *
   * @return A native address.
   */
  public static Address toNativeAddress(final Address address) {
    return new Address(com.google.security.zynamics.reil.ReilHelpers.toNativeAddress(new CAddress(address.toLong()))
        .toBigInteger());
  }

  // ! Converts native addresses to REIL addresses.
  /**
   * Converts a native address to a REIL address by multiplying the native address by 0x100.
   *
   * @param address A native address.
   *
   * @return A REIL address.
   */
  public static Address toReilAddress(final Address address) {
    return new Address(com.google.security.zynamics.reil.ReilHelpers.toReilAddress(new CAddress(address.toLong()))
        .toBigInteger());
  }
}
