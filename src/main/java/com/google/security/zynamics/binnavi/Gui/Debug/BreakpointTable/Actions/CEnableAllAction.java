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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointSetFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;


/**
 * Action class that enables all breakpoints.
 */
public final class CEnableAllAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2248603396543802677L;

  /**
   * Provides the debuggers where breakpoints can be set.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Creates a new action object.
   *
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public CEnableAllAction(final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01349: Debugger provider argument can not be null");

    m_debuggerProvider = debuggerProvider;

    putValue(Action.SHORT_DESCRIPTION, "Enable all breakpoints");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBreakpointSetFunctions.enableAll(m_debuggerProvider);
  }
}
