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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.util.Collection;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterSetLatticeElement;

/**
 * Represents the register tracking result for a single BinNavi instruction.
 */
public final class CInstructionResult {
  /**
   * A BinNavi instruction.
   */
  private final INaviInstruction m_instruction;
  private final RegisterSetLatticeElement m_registerSet;

  /**
   * Creates a new instruction result object.
   *
   * @param instruction
   * @param registerSetLatticeElement
   */
  public CInstructionResult(final INaviInstruction instruction,
      final RegisterSetLatticeElement registerSetLatticeElement) {
    m_instruction =
        Preconditions.checkNotNull(instruction, "IE01666: Instruction argument can not be null");
    m_registerSet = Preconditions.checkNotNull(
        registerSetLatticeElement, "IE01667: RegisterSetLatticeElement can not be null");
  }

  public boolean clearsTrackedRegister(final String register) {
    return getUndefinedRegisters().contains(register);
  }

  public boolean defines() {
    return getNewlyTaintedRegisters().size() != 0;
  }

  /**
   * Returns the registers defined at the end of the instruction.
   *
   * @return The registers that are defined at the end of the instruction.
   */
  public Collection<String> getDefinedRegisters() {
    return m_registerSet.getTaintedRegisters();
  }

  /**
   * Returns the input registers of the instruction.
   *
   * @return The input registers of the instruction.
   */
  public Collection<String> getInputRegisters() {
    return m_registerSet.getReadRegisters();
  }

  /**
   * Returns the instruction.
   *
   * @return The instruction.
   */
  public INaviInstruction getInstruction() {
    return m_instruction;
  }

  /**
   * Returns the output registers of the instruction.
   *
   * @return The output registers of the instruction.
   */
  public Collection<String> getNewlyTaintedRegisters() {
    return m_registerSet.getNewlyTaintedRegisters();
  }

  /**
   * Returns the registers that are undefined by the instruction.
   *
   * @return The registers that are undefined by the instruction.
   */
  public Collection<String> getUndefinedRegisters() {
    return m_registerSet.getUntaintedRegisters();
  }

  public Collection<String> getUpdatedRegisters() {
    return m_registerSet.getUpdatedRegisters();
  }

  /**
   * Determines whether the instruction undefines all tracked registers.
   *
   * @return True, if the instruction undefines all tracked register. False, otherwise.
   */
  public boolean undefinesAll() {
    return getDefinedRegisters().size() == 0;
  }

  /**
   * Determines whether the instruction undefines some tracked registers.
   *
   * @return True, if the instruction undefines some tracked register. False, otherwise.
   */
  public boolean undefinesSome() {
    return (getUndefinedRegisters().size() != 0) && (getDefinedRegisters().size() != 0);
  }

  public boolean updates() {
    return getUpdatedRegisters().size() != 0;
  }

  /**
   * Determines whether the instruction uses some tracked registers.
   *
   * @return True, if the instruction uses some tracked registers. False, otherwise.
   */
  public boolean uses() {
    return getInputRegisters().size() != 0;
  }
}
