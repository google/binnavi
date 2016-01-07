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

import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.awt.event.ActionEvent;
import java.math.BigInteger;

import javax.swing.AbstractAction;



/**
 * Action class to follow an operand expression value in the memory window.
 */
public class CGotoOperandExpressionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4381722893659485259L;

  /**
   * Debug perspective model that shows the memory.
   */
  private final CDebugPerspectiveModel m_model;

  /**
   * Evaluated address of the expression.
   */
  private final BigInteger m_address;

  /**
   * Creates a new action object.
   *
   * @param model Debug perspective model that shows the memory.
   * @param value Printable string that represents the expression.
   * @param address Evaluated address of the expression.
   */
  public CGotoOperandExpressionAction(
      final CDebugPerspectiveModel model, final String value, final BigInteger address) {
    super(String.format("Follow '%s' (%s) in memory", value, address.toString(16).toUpperCase()));

    m_model = model;
    m_address = address;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.gotoOffset(m_model, new CAddress(m_address), true);
  }
}
