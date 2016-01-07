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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsPanel.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations.CDebuggerFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;

/**
 * Action class for showing the debugger options dialog.
 */
public final class CShowOptionsDialogAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8558850181285424824L;

  /**
   * Parent of the dialog.
   */
  private final JFrame m_parent;

  /**
   * Provides the debug target.
   */
  private final DebugTargetSettings m_debugTarget;

  /**
   * Debugger whose options are shown.
   */
  private final IDebugger m_debugger;

  /**
   * Creates a new action object.
   *
   * @param parent Parent of the dialog.
   * @param debugTarget Provides the debug target.
   * @param debugger Debugger whose options are shown.
   */
  public CShowOptionsDialogAction(
      final JFrame parent, final DebugTargetSettings debugTarget, final IDebugger debugger) {
    super("Show Options");

    Preconditions.checkNotNull(parent, "IE01471: Parent argument can not be null");

    Preconditions.checkNotNull(debugTarget, "IE01472: Debug target argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01473: Options argument can not be null");

    m_parent = parent;
    m_debugTarget = debugTarget;
    m_debugger = debugger;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDebuggerFunctions.showDebuggerOptionsDialogAlways(m_parent, m_debugTarget, m_debugger);
  }
}
