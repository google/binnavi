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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class that can be used to send Step Over requests to the debug client.
 */
public final class CStepOverAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6797972614447876945L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The debugger that executes the step operation.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * The graph where the step operation happens.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new step over action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger that executes the step operation.
   * @param graph The graph where the step operation happens.
   */
  public CStepOverAction(
      final JFrame parent, final IFrontEndDebuggerProvider debugger, final ZyGraph graph) {
    m_parent = Preconditions.checkNotNull(parent, "IE00311: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01546: Debugger argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01547: Graph argument can not be null");
    putValue(Action.SHORT_DESCRIPTION, "Step Over");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger != null) {
      CDebuggerFunctions.stepOver(m_parent, debugger, m_graph);
    }
  }
}
