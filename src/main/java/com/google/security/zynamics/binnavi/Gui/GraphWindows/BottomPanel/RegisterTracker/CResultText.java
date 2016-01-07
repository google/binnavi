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
 * Helper class to find the description text of a tracking result.
 */
public final class CResultText {
  /**
   * You are not supposed to instantiate this class.
   */
  private CResultText() {
  }

  /**
   * Determines the description text used to display a given instruction.
   *
   * @param startInstruction Start instruction of the tracking operation.
   * @param trackedRegister The tracked register.
   * @param result Result of the tracking operation.
   *
   * @return Description text used to display the result instruction.
   */
  public static String determineDescription(final INaviInstruction startInstruction,
      final String trackedRegister, final CInstructionResult result) {
    Preconditions.checkNotNull(
        startInstruction, "IE01680: Start instruction argument can not be null");
    Preconditions.checkNotNull(
        trackedRegister, "IE01681: Tracked register argument can not be null");
    Preconditions.checkNotNull(result, "IE01682: Result argument can not be null");

    if (result.getInstruction() == startInstruction) {
      return "Start";
    } else if (result.undefinesAll()) {
      // This is the text for instructions that undefine all tracked
      // registers and therefore end the register tracking for one
      // code execution path.

      return "Clears all effects";
    } else if (result.clearsTrackedRegister(trackedRegister)) {
      // This is the text used for instructions that undefined the
      // tracked register but do not undefine all tracked registers.

      return "Clears tracked register";
    } else if (result.undefinesSome()) {
      // This is the text used for instructions that undefine some
      // tracked registers but not the originally tracked register.

      return "Clears some effects";
    } else if (result.defines()) {
      // This is the text used for instructions that do not undefine
      // any registers but use any of the tracked registers.

      return "Depends on tracked register";
    } else if (result.updates()) {
      // This is the test used for instructions that update any of
      // the tainted registers with new information.
      return "Updates tainted register";
    } else if (result.uses()) {
      // This is the text used for instructions that read any of
      // the tainted registers.
      return "Reads tainted register";
    } else {
      return "";
    }
  }
}
