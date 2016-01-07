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

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;

/**
 * Contains the result of a register tracking operation.
 */
public final class CTrackingResult {
  /**
   * Start instruction of the tracking operation.
   */
  private final INaviInstruction m_startInstruction;

  /**
   * The tracked register.
   */
  private final String m_trackedRegister;

  /**
   * Tracking results for individual instructions.
   */
  private final List<CInstructionResult> m_results;

  /**
   * Creates a new register tracking result.
   *
   * @param startInstruction Start instruction of the tracking operation.
   * @param trackedRegister The tracked register.
   * @param results Tracking results for individual instructions.
   */
  public CTrackingResult(final INaviInstruction startInstruction, final String trackedRegister,
      final List<CInstructionResult> results) {
    m_startInstruction = Preconditions.checkNotNull(
        startInstruction, "IE02301: startInstruction argument can not be null");
    m_trackedRegister = Preconditions.checkNotNull(
        trackedRegister, "IE02302: trackedRegister argument can not be null");
    m_results = new ArrayList<CInstructionResult>(Preconditions.checkNotNull(
        results, "IE02303: results argument can not be null"));
  }

  /**
   * Returns the individual tracking results.
   *
   * @return Tracking results for individual instructions.
   */
  public List<CInstructionResult> getResults() {
    return new ArrayList<CInstructionResult>(m_results);
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
