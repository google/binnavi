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
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;

/**
 * Action class that is used send a single step request to the debug client.
 */
public final class CStepIntoAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2945259974573766830L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The debugger that executes the step operation.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Creates a new single step action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that executes the step operation.
   */
  public CStepIntoAction(final JFrame parent, final IFrontEndDebuggerProvider debugger) {
    m_parent = Preconditions.checkNotNull(parent, "IE00310: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01544: Debugger argument can not be null");
    putValue(Action.SHORT_DESCRIPTION, "Step Into");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        debugger == null ? null : debugger.getProcessManager().getActiveThread();

    if ((debugger != null) && (activeThread != null)) {
      CDebuggerFunctions.stepInto(m_parent, debugger);
    }
  }
}
