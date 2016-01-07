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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Center;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.CToolbarPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;


/**
 * Panel where the debugging toolbar is shown.
 */
public final class CDebuggerControlPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4711455633038417094L;

  /**
   * Debugging toolbar.
   */
  private final CToolbarPanel m_toolbarPanel;

  /**
   * Creates a new panel object.
   *
   * @param parent Parent window of the panel.
   * @param debugPerspectiveModel Debugger that is debugged by the toolbar.
   */
  public CDebuggerControlPanel(
      final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    m_toolbarPanel = new CToolbarPanel(parent, debugPerspectiveModel);

    add(m_toolbarPanel, BorderLayout.SOUTH);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_toolbarPanel.dispose();
  }
}
