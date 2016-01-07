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
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolverResult;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterSetLatticeElement;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTracker;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;


/**
 * Used to track registers forward.
 */
public final class CTracking {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTracking() {
  }

  /**
   * Performs a register forward tracking operation.
   * 
   * @param view The view where the operation happens.
   * @param startInstruction The start instruction.
   * @param trackedRegister The register to track.
   * @param options Register tracking options.
   * 
   * @return The result of the register tracking operation.
   * 
   * @throws InternalTranslationException Thrown if the graph from the code could not be translated
   *         to REIL.
   */
  public static CTrackingResult track(final INaviView view,
      final INaviInstruction startInstruction, final String trackedRegister,
      final RegisterTrackingOptions options) throws InternalTranslationException {
    Preconditions.checkNotNull(view, "IE01660: View argument can not be null");
    Preconditions.checkNotNull(startInstruction,
        "IE01661: Start instruction argument can not be null");
    Preconditions.checkNotNull(trackedRegister, "IE01662: Register argument can not be null");

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(view.getContent().getReilCode(), startInstruction, trackedRegister,
            options);

    final Map<IAddress, INaviInstruction> instructionMap =
        CRegisterTrackingHelper.getInstructionMap(view);
    final List<CInstructionResult> instructionResultList = new ArrayList<CInstructionResult>();

    final Map<IAddress, RegisterSetLatticeElement> perInstructionElement =
        result.generateAddressToStateMapping(startInstruction, options.trackIncoming());

    for (final Map.Entry<IAddress, RegisterSetLatticeElement> addressToStateMapEntry : perInstructionElement
        .entrySet()) {
      final RegisterSetLatticeElement element = addressToStateMapEntry.getValue();

      if (!element.getReadRegisters().isEmpty() || !element.getNewlyTaintedRegisters().isEmpty()
          || !element.getUntaintedRegisters().isEmpty() || !element.getUpdatedRegisters().isEmpty()) {
        final CAddress concreteAddress =
            new CAddress(addressToStateMapEntry.getKey().toLong() >> 8);
        instructionResultList.add(new CInstructionResult(instructionMap.get(concreteAddress),
            addressToStateMapEntry.getValue()));
      }
    }
    return new CTrackingResult(startInstruction, trackedRegister, instructionResultList);
  }
}
