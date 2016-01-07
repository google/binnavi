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
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointRemoveFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Action class that disables all breakpoints of a view.
 */
public final class CDisableAllViewAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3908237702347515104L;

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
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view The view to consider when disabling the breakpoints.
   */
  public CDisableAllViewAction(
      final BackEndDebuggerProvider debuggerProvider, final INaviView view) {
    m_debuggerProvider =
        Preconditions.checkNotNull(debuggerProvider, "IE01347: Manager argument can not be null");
    m_view = Preconditions.checkNotNull(view, "IE01348: View argument can not be null");

    putValue(Action.SHORT_DESCRIPTION, "Disable all view breakpoints");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBreakpointRemoveFunctions.disableAllView(m_debuggerProvider, m_view);
  }
}
