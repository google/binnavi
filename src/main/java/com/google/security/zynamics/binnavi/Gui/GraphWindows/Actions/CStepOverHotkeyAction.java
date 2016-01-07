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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class for handling the debugger Step Over hotkey.
 */
public final class CStepOverHotkeyAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7075492324276372634L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph where stepping happens.
   */
  private final ZyGraph m_graph;

  /**
   * Provides the active debugger.
   */
  private final IFrontEndDebuggerProvider m_debugPerspectiveModel;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph where the step operation happens.
   * @param panel Provides the active debugger.
   */
  public CStepOverHotkeyAction(
      final JFrame parent, final ZyGraph graph, final IFrontEndDebuggerProvider panel) {
    Preconditions.checkNotNull(panel, "IE01656: Panel argument can not be null");

    m_parent = parent;
    m_graph = graph;
    m_debugPerspectiveModel = panel;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger != null) {
      CDebuggerFunctions.stepOver(m_parent, debugger, m_graph);
    }
  }
}
