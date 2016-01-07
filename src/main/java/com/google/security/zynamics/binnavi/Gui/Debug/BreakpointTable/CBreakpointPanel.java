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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * This is the panel where the user can manipulate the set breakpoints.
 */
public final class CBreakpointPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8330583208655765895L;

  /**
   * Table where the breakpoints are shown.
   */
  private final CBreakpointTable m_breakpointTable;

  /**
   * Creates a new breakpoint panel.
   *
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param graph Graph that is shown in the window the panel belongs to.
   * @param viewContainer View container of the graph.
   */
  public CBreakpointPanel(final JFrame parent, final BackEndDebuggerProvider debuggerProvider,
      final ZyGraph graph, final IViewContainer viewContainer) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01331: Parent argument can not be null");
    Preconditions.checkNotNull(
        debuggerProvider, "IE01332: Debugger provider argument can not be null");
    Preconditions.checkNotNull(graph, "IE01333: Graph argument can not be null");

    m_breakpointTable = new CBreakpointTable(debuggerProvider, graph, viewContainer);

    add(new CBreakpointToolbar(parent, debuggerProvider, graph.getRawView()), BorderLayout.NORTH);
    add(new JScrollPane(m_breakpointTable), BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    m_breakpointTable.dispose();
  }

  @Override
  public String getTitle() {
    return "Breakpoints";
  }
}
