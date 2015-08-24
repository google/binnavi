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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;

import java.awt.Window;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.ReilInstructionDialog.CReilInstructionDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.InternalTranslationException;

/**
 * Contains helper functions for dealing with instructions.
 */
public final class CInstructionFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CInstructionFunctions() {
  }

  /**
   * Shows the REIL code for a code node.
   * 
   * @param parent Parent window used for dialogs.
   * @param codeNode The code node whose REIL code is shown.
   */
  public static void showReilCode(final Window parent, final INaviCodeNode codeNode) {
    try {
      CReilInstructionDialog.show(parent, codeNode);
    } catch (final InternalTranslationException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00XXX: " + "Could not show REIL code";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not show the REIL code of node at '%X'.",
                  codeNode.getAddress()),
              new String[] {"The node could not be converted to REIL code."},
              new String[] {"You can not fix this problem yourself. Please contact the "
                  + "BinNavi support."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Shows the REIL code of a single instruction.
   * 
   * @param parent Parent window used for dialogs.
   * @param instruction The instruction whose REIL code is shown.
   */
  public static void showReilCode(final Window parent, final INaviInstruction instruction) {
    try {
      CReilInstructionDialog.show(parent, instruction);
    } catch (final InternalTranslationException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00035: " + "Could not show REIL code";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not show the REIL code of instruction '%s'.", instruction.toString()),
              new String[] {"The instruction could not be converted to REIL code."},
              new String[] {"You can not fix this problem yourself. Please contact the "
                  + "BinNavi support."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }
}
