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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CInstructionFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;


/**
 * Action class for showing the REIL code of an instruction.
 */
public class CActionShowReilCode extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6414093733346244397L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * The instruction whose REIL code is shown.
   */
  private final INaviInstruction m_instruction;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param instruction The instruction whose REIL code is shown.
   */
  public CActionShowReilCode(final Window parent, final INaviInstruction instruction) {
    super("Show REIL Code");

    m_parent = parent;
    m_instruction = instruction;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CInstructionFunctions.showReilCode(m_parent, m_instruction);
  }
}
