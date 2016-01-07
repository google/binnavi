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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;
import com.google.security.zynamics.reil.translators.InternalTranslationException;

/**
 * Action class is executed when the user wants to track a register.
 */
public final class CTrackOperandAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7794473006806206907L;

  /**
   * Container object the result of the operation is written to.
   */
  private final CTrackingResultContainer m_resultsContainer;

  /**
   * Instruction where the tracking operation begins.
   */
  private final INaviInstruction m_instruction;

  /**
   * Register to track.
   */
  private final String m_register;

  /**
   * Register tracking options.
   */
  private final RegisterTrackingOptions m_options;

  /**
   * Creates a new register tracking action object.
   *
   * @param resultsContainer Container object the result of the operation is written to.
   * @param instruction Instruction where the tracking operation begins.
   * @param register Register to track.
   * @param options Register tracking options.
   */
  public CTrackOperandAction(final CTrackingResultContainer resultsContainer,
      final INaviInstruction instruction, final String register,
      final RegisterTrackingOptions options) {
    super(generateName(options));

    m_resultsContainer =
        Preconditions.checkNotNull(resultsContainer, "IE01696: Container argument can not be null");
    m_instruction =
        Preconditions.checkNotNull(instruction, "IE01697: Instruction argument can not be null");
    m_register = Preconditions.checkNotNull(register, "IE01698: Register argument can not be null");
    m_options = Preconditions.checkNotNull(options, "IE02307: options argument can not be null");
  }

  private static String generateName(final RegisterTrackingOptions options) {
    return options.getAnalysisDirection() == AnalysisDirection.DOWN
        ? "Track Operand Forward" : "Track Operand Backward";
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    try {
      final CTrackingResult result = CTracking.track(
          m_resultsContainer.getGraph().getRawView(), m_instruction, m_register, m_options);

      m_resultsContainer.setResult(result);
    } catch (final InternalTranslationException e) {
      CUtilityFunctions.logException(e);
    }
  }
}
