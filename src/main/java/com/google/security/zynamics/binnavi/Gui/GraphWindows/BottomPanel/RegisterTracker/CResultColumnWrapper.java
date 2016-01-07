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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;

/**
 * This is a small wrapper class that is used as the column object of status columns in the register
 * tracking table. The information inside this object is used to calculate the background color of
 * the status columns.
 */
public final class CResultColumnWrapper implements Comparable<CResultColumnWrapper> {
  /**
   * The register that was tracked.
   */
  private final String m_trackedRegister;

  /**
   * The register tracking result.
   */
  private final CInstructionResult m_instructionResult;

  /**
   * Start instruction of the tracking operation.
   */
  private final INaviInstruction m_startInstruction;

  /**
   * Creates a new column wrapper object.
   *
   * @param startInstruction Start instruction of the tracking operation.
   * @param trackedRegister The register that was tracked.
   * @param instructionResult The register tracking result.
   */
  public CResultColumnWrapper(final INaviInstruction startInstruction, final String trackedRegister,
      final CInstructionResult instructionResult) {
    Preconditions.checkNotNull(
        startInstruction, "IE01677: Start instruction argument can not be null");
    Preconditions.checkNotNull(
        trackedRegister, "IE01678: Tracked register argument can not be null");
    Preconditions.checkNotNull(
        instructionResult, "IE01679: Instruction result argument can not be null");

    m_startInstruction = startInstruction;
    m_trackedRegister = trackedRegister;
    m_instructionResult = instructionResult;
  }

  /**
   * Converts an instruction result into an integer number that represents its status.
   *
   * @param startInstruction The start instruction of the register tracking operation.
   * @param trackedRegister The register tracked by the register tracking operation.
   * @param result The instruction result to convert.
   * @return A numerical value that represents the type of the instruction result.
   */
  private static int getStatusValue(final INaviInstruction startInstruction,
      final String trackedRegister, final CInstructionResult result) {
    if (startInstruction == result.getInstruction()) {
      return 0;
    } else if (result.undefinesAll()) {
      return 1;
    } else if (result.getUndefinedRegisters().contains(trackedRegister)) {
      return 2;
    } else if (result.undefinesSome()) {
      return 3;
    } else {
      return 4;
    }
  }

  @Override
  public int compareTo(final CResultColumnWrapper o) {
    return getStatusValue(getStartInstruction(), getTrackedRegister(), getResult())
        - getStatusValue(o.getStartInstruction(), o.getTrackedRegister(), o.getResult());
  }

  /**
   * Returns the register tracking result.
   *
   * @return The register tracking result.
   */
  public CInstructionResult getResult() {
    return m_instructionResult;
  }

  /**
   * Returns the start instruction of the tracking operation.
   *
   * @return The start instruction of the tracking operation.
   */
  public INaviInstruction getStartInstruction() {
    return m_startInstruction;
  }

  /**
   * Returns the tracked register.
   *
   * @return The tracked register.
   */
  public String getTrackedRegister() {
    return m_trackedRegister;
  }
}
