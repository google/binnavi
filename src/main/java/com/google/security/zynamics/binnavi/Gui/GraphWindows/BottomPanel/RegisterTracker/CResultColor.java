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

import java.awt.Color;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;

/**
 * Class that is used to calculate the background color of tracking table rows and instructions in
 * the graph.
 */
public final class CResultColor {
  /**
   * You are not supposed to instantiate this class.
   */
  private CResultColor() {
  }

  /**
   * Determines the background color to be used in the table and in the graph to highlight a given
   * instruction result.
   *
   * @param startInstruction The start instruction of the operand tracking operation.
   * @param trackedRegister The tracked register of the operand tracking operation.
   * @param result The result to highlight.
   *
   * @return The color used for highlighting.
   */
  public static Color determineBackgroundColor(final INaviInstruction startInstruction,
      final String trackedRegister, final CInstructionResult result) {
    Preconditions.checkNotNull(
        startInstruction, "IE01671: Start instruction argument can not be null");
    Preconditions.checkNotNull(
        trackedRegister, "IE01672: Tracked register argument can not be null");
    Preconditions.checkNotNull(result, "IE01673: Result argument can not be null");

    if (result.getInstruction() == startInstruction) {
      // This is the color used for the start instruction.

      return Color.decode("0x00BF00");
    } else if (result.undefinesAll()) {
      // This is the color for instructions that undefine all tracked
      // registers and therefore end the register tracking for one
      // code execution path.

      return Color.decode("0xB30000");
    } else if (result.clearsTrackedRegister(trackedRegister)) {
      // This is the color used for instructions that undefined the
      // tracked register but do not undefine all tracked registers.

      return Color.decode("0xA12967");
    } else if (result.undefinesSome()) {
      // This is the color used for instructions that undefine some
      // tracked registers but not the originally tracked register.

      return Color.decode("0xED693F");
    } else if (result.defines()) {
      // This is the color used for instructions that do not undefine
      // any registers but use any of the tracked registers.

      return Color.decode("0xFFCD55");
    } else if (result.updates()) {
      // This is the color used for instructions that update any of
      // the tainted registers with new information.
      return Color.decode("0x5AAB47");
    } else if (result.uses()) {
      // This is the text used for instructions that read any of
      // the tainted registers.
      return Color.decode("0x414142");
    } else {
      return Color.WHITE;
    }
  }

  /**
   * Determines the color used to display a given instruction.
   *
   * @param startInstruction Start instruction of the tracking operation.
   * @param trackedRegister The tracked register.
   * @param result Result of the tracking operation.
   *
   * @return Color used to display the result instruction.
   */
  public static Color determineForegroundColor(final INaviInstruction startInstruction,
      final String trackedRegister, final CInstructionResult result) {
    Preconditions.checkNotNull(
        startInstruction, "IE01674: Start instruction argument can not be null");
    Preconditions.checkNotNull(
        trackedRegister, "IE01675: Tracked register argument can not be null");
    Preconditions.checkNotNull(result, "IE01676: Result argument can not be null");

    if (result.getInstruction() == startInstruction) {
      return Color.BLACK;
    } else if (result.undefinesAll()) {
      return Color.WHITE;
    } else if (result.clearsTrackedRegister(trackedRegister)) {
      return Color.WHITE;
    } else if (result.undefinesSome()) {
      return Color.BLACK;
    } else if (result.defines()) {
      return Color.BLACK;
    } else if (result.updates()) {
      return Color.BLACK;
    } else if (result.uses()) {
      return Color.WHITE;
    } else {
      return Color.BLACK;
    }
  }
}
