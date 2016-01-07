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
 * Action class that is used to issue detach requests to the debug client.
 */
public final class CDetachAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3941159801769183319L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that is used to detach from the target.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Creates a new detach action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that is used to detach from the target.
   */
  public CDetachAction(final JFrame parent, final IFrontEndDebuggerProvider debugger) {
    m_parent = Preconditions.checkNotNull(parent, "IE00293: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01532: Debugger argument can not be null");

    putValue(Action.SHORT_DESCRIPTION, "Detach");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger != null) {
      CDebuggerFunctions.detach(m_parent, debugger);
    }
  }
}
