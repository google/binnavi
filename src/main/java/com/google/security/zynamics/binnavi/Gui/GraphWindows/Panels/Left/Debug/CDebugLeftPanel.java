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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left.Debug;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.DebuggerSelectionPanel.CDebuggerSelectionPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.CRegisterView;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Panel that is shown on the left side of graph windows when the debug perspective is active.
 */
public final class CDebugLeftPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4005030063655631532L;

  /**
   * Shows current register values while debugging.
   */
  private final CRegisterView m_registerPanel;

  /**
   * Used to select from different available debuggers.
   */
  private final CDebuggerSelectionPanel m_debuggerSelectionPanel;

  /**
   * Creates a new panel object.
   *
   * @param parent Parent window used for dialogs.
   * @param provider Provides the available debuggers.
   * @param debugPerspectiveModel Describes the debug perspective.
   */
  public CDebugLeftPanel(final JFrame parent, final BackEndDebuggerProvider provider,
      final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01807: Parent argument can not be null");
    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01808: Debug perspective model argument can not be null");

    m_debuggerSelectionPanel = new CDebuggerSelectionPanel(provider, debugPerspectiveModel);
    m_debuggerSelectionPanel.setBorder(new TitledBorder("Active Debugger"));

    m_registerPanel = new CRegisterView(parent, debugPerspectiveModel);

    add(m_debuggerSelectionPanel, BorderLayout.NORTH);
    add(m_registerPanel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debuggerSelectionPanel.dispose();
    m_registerPanel.dispose();
  }
}
