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
package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerSelectionPanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Panel that contains a debugger selection combobox and a synchronizer that keeps the information
 * shown in the panel synchronized to the current settings of a debug GUI perspective.
 */
public final class CDebuggerSelectionPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5899260875435019353L;

  /**
   * Available debuggers can be selected here.
   */
  private final CDebuggerComboBox m_debuggerBox;

  /**
   * Synchronizes the panel with the state of a debug GUI perspective.
   */
  private final CDebuggerSelectionPanelSynchronizer m_synchronizer;

  /**
   * Creates a new debugger selection panel.
   *
   * @param provider Provides the available debuggers.
   * @param debugPerspectiveModel Describes the debug GUI perspective that is synchronized with the
   *        panel.
   */
  public CDebuggerSelectionPanel(
      final BackEndDebuggerProvider provider, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(provider, "IE01364: Provider can not be null");
    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01365: Debug perspective model can not be null");

    m_debuggerBox = new CDebuggerComboBox(provider);

    add(m_debuggerBox);

    m_synchronizer = new CDebuggerSelectionPanelSynchronizer(m_debuggerBox, debugPerspectiveModel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debuggerBox.dispose();
    m_synchronizer.dispose();
  }
}
