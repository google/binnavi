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
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * This class can be used to start the trace mode in the debugger.
 */
public final class CStartTraceAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5973796509140248996L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The debugger where the trace happens.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Graph to trace.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new start trace action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger where the trace happens.
   * @param graph Graph to trace.
   */
  public CStartTraceAction(
      final JFrame parent, final IFrontEndDebuggerProvider debugger, final ZyGraph graph) {
    m_parent = Preconditions.checkNotNull(parent, "IE00303: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01538: Debugger argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01539: Graph argument can not be null");

    putValue(Action.NAME, "BT");
    putValue(Action.SHORT_DESCRIPTION, "Start Trace Mode");
    putValue(Action.SMALL_ICON, new ImageIcon("data/record_up.jpg"));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger != null) {
      CTraceFunctions.startTrace(m_parent, debugger, m_graph, m_debugger.getTraceLogger(debugger));
    }
  }
}
