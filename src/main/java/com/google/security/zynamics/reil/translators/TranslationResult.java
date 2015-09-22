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
package com.google.security.zynamics.reil.translators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.util.List;

/**
 * Used to store the result of partial translations of native code to REIL code.
 */
public class TranslationResult {

  /**
   * The REIL code that was created during the partial translation
   */
  private List<ReilInstruction> instructions;

  /**
   * The name of the register that contains the last value of the partial translation
   */
  private String register;

  /**
   * The size of the result register
   */
  private OperandSize size;

  /**
   * The address (if applicable) that was last used to load a value from/ store a value to
   */
  private String address;

  /**
   * The type of the translation result
   */
  private TranslationResultType type;

  /**
   * The base address of the first instruction of this partial translation.
   */
  private long baseAddress;

  /**
   * Creates a new translation result object.
   *
   * @param register The name of the register
   * @param size The size of the register
   * @param type The type of the result
   * @param address The address of the result
   * @param instructions The instructions of the partial translation
   */
  public TranslationResult(final String register,
      final OperandSize size,
      final TranslationResultType type,
      final String address,
      final List<ReilInstruction> instructions,
      final long baseAddress) {

    this.baseAddress = baseAddress;
    this.address = address;
    this.register = register;
    this.size = size;
    this.type = type;
    this.instructions = instructions;
  }

  public String getAddress() {
    return address;
  }

  public List<ReilInstruction> getInstructions() {
    return instructions;
  }

  public String getRegister() {
    return register;
  }

  public OperandSize getSize() {
    return size;
  }

  public TranslationResultType getType() {
    return type;
  }

  /**
   * Updates the data about the result; this is usually needed when instructions are added to a
   * result.
   *
   * @param result The string that describes the register in which the final result of the existing
   *        translation is placed.
   * @param resultSize The size of the result.
   * @param address The address from which this result was loaded, if we are dealing with a partial
   *        translation of a complex operand. May be null.
   * @param type Indicates the type of the result - was this obtained from a memory dereference or
   *        from a register etc.
   */
  public void updateResult(final String result, final OperandSize resultSize, final String address,
      TranslationResultType type) {
    register = Preconditions.checkNotNull(result);
    size = Preconditions.checkNotNull(resultSize);
    this.type = Preconditions.checkNotNull(type);
    this.address = address;
  }

  public void updateBaseAndReil(long baseAddress, final List<ReilInstruction> instructions) {
    this.instructions = instructions;
    this.baseAddress = baseAddress;
  }

  /**
   * Adds an instruction to this internal result, without any delta for the
   * address.
   */
  public void addInstruction(final ReilInstruction instruction) {
    addInstructionWithDelta(instruction, 0 /* no delta */);
  }

  /**
   * Adds an instruction with a given delta to this internal result. The delta is only needed
   * because we don't have a proper system for using labels as a normal assembler would have.
   *
   * @param instruction
   */
  public void addInstructionWithDelta(final ReilInstruction instruction, final long delta) {
    instruction.setAddress(new CAddress(baseAddress + instructions.size() + delta));
    instructions.add(instruction);
  }
}
