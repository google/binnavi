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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations.CDebuggerFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;

/**
 * Action class for handling the debugger resume hotkey.
 */
public final class CResumeHotkeyAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6717084853618998394L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides the active debugger.
   */
  private final IFrontEndDebuggerProvider m_debugPerspectiveModel;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Provides the active debugger.
   */
  public CResumeHotkeyAction(
      final JFrame parent, final IFrontEndDebuggerProvider debugPerspectiveModel) {
    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01653: Debug perspective model argument can not be null");

    m_parent = parent;
    m_debugPerspectiveModel = debugPerspectiveModel;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread currentThread =
        debugger == null ? null : debugger.getProcessManager().getActiveThread();

    if (currentThread != null) {
      CDebuggerFunctions.resume(m_parent, debugger);
    }
  }
}
