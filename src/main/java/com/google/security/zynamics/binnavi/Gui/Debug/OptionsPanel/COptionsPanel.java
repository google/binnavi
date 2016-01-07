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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.OptionsPanel.Actions.CShowOptionsDialogAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Panel that is used to display debugger options on the right side of the debugger GUI.
 */
public final class COptionsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 212662840552542949L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides the debug target.
   */
  private final DebugTargetSettings m_debugTarget;

  /**
   * The graph to debug.
   */
  private final ZyGraph m_graph;

  /**
   * Keeps the options panel synchronized with the debugger GUI.
   */
  private final IDebugPerspectiveModelListener m_listener = new InternalDebuggerListener();

  /**
   * Creates a new debugger options panel.
   *
   * @param parent Parent window used for dialogs.
   * @param debugTarget Provides the debug target.
   * @param graph The graph that is debugged by the debugger.
   * @param debugModel The debug model the panel belongs to.
   */
  public COptionsPanel(final JFrame parent, final DebugTargetSettings debugTarget,
      final ZyGraph graph, final CDebugPerspectiveModel debugModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(graph, "IE01468: Graph argument can not be null");

    m_parent = parent;
    m_debugTarget = debugTarget;
    m_graph = graph;

    setBorder(new TitledBorder("Debugger Options"));

    setDebugger(debugModel.getCurrentSelectedDebugger());

    debugModel.addListener(m_listener);

    setPreferredSize(new Dimension(200, 300));
  }

  /**
   * Updates the options panel if a new debugger is activated.
   *
   * @param debugger The new debugger.
   */
  private void setDebugger(final IDebugger debugger) {
    removeAll();

    if (debugger != null) {
      final JPanel innerOptionsPanel = new JPanel(new GridLayout(1, 1));

      innerOptionsPanel.add(new CRelocationCheckBox(m_graph, debugger));

      add(innerOptionsPanel, BorderLayout.NORTH);

      final JButton showButton = new JButton(CActionProxy.proxy(
          new CShowOptionsDialogAction(m_parent, m_debugTarget, debugger)));

      add(showButton, BorderLayout.SOUTH);
    }
  }

  /**
   * Keeps the options panel synchronized with the debugger GUI.
   */
  private class InternalDebuggerListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      setDebugger(newDebugger);
    }
  }
}
