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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointRemoveFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Action class that removes all breakpoints of a view.
 */
public final class CRemoveAllViewAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3658980287592043629L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides the debuggers where breakpoints can be set.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * The view to consider when disabling the breakpoints.
   */
  private final INaviView m_view;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view The view to consider when enabling the breakpoints.
   */
  public CRemoveAllViewAction(
      final JFrame parent, final BackEndDebuggerProvider debuggerProvider, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE01354: Parent argument can not be null");

    Preconditions.checkNotNull(debuggerProvider, "IE01355: Manager argument can not be null");

    Preconditions.checkNotNull(view, "IE01356: View argument can not be null");

    m_parent = parent;
    m_debuggerProvider = debuggerProvider;
    m_view = view;

    putValue(SHORT_DESCRIPTION, "Remove all view breakpoints");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBreakpointRemoveFunctions.removeAllView(m_parent, m_debuggerProvider, m_view);
  }
}
