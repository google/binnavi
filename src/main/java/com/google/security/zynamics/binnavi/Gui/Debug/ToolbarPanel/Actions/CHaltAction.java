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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations.CDebuggerFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;

/**
 * This action class can be used to halt the target process.
 */
public final class CHaltAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2514000165001424956L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that halts the target process.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Creates a new halt action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that halts the target process.
   */
  public CHaltAction(final JFrame parent, final IFrontEndDebuggerProvider debugger) {
    m_parent = Preconditions.checkNotNull(parent, "IE00296: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01534: Debugger argument can not be null");
    putValue(Action.SHORT_DESCRIPTION, "Halt");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger != null) {
      CDebuggerFunctions.halt(m_parent, debugger);
    }
  }
}
