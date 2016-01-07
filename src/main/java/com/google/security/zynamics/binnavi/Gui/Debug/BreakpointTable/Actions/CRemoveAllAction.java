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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointRemoveFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;


/**
 * Action class that removes all breakpoints.
 */
public final class CRemoveAllAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2508528705873111612L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides the debuggers where breakpoints can be set.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public CRemoveAllAction(final JFrame parent, final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(parent, "IE01352: Parent argument can not be null");

    Preconditions.checkNotNull(debuggerProvider, "IE01353: Manager argument can not be null");

    m_parent = parent;
    m_debuggerProvider = debuggerProvider;

    putValue(Action.SHORT_DESCRIPTION, "Remove all breakpoints");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBreakpointRemoveFunctions.removeAll(m_parent, m_debuggerProvider);
  }
}
