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
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * This action class can be used to connect to the debug client.
 */
public final class CAttachAction extends AbstractAction {
  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that is used to attach to the target.
   */
  private final IFrontEndDebuggerProvider m_frontEndDebuggerProvider;

  /**
   * Creates a new attach action.
   *
   * @param parent Parent window used for dialogs.
   * @param frontEndDebuggerProvider Debugger that is used to attach to the target.
   */
  public CAttachAction(
      final JFrame parent, final IFrontEndDebuggerProvider frontEndDebuggerProvider) {
    m_parent = Preconditions.checkNotNull(parent, "IE00269: Parent argument can not be null");
    m_frontEndDebuggerProvider = Preconditions.checkNotNull(
        frontEndDebuggerProvider, "IE01528: Debugger argument can not be null");
    putValue(Action.SHORT_DESCRIPTION, "Start Debugger");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_frontEndDebuggerProvider.getCurrentSelectedDebugger();
    if (debugger == null) {
      CMessageBox.showInformation(
          m_parent, "No debugger selected. Did you configure a debugger for this module?");
    } else {
      CDebuggerFunctions.attach(
          m_parent, debugger, m_frontEndDebuggerProvider.getNotifier(debugger));
    }
  }
}
