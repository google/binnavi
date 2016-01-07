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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Encapsulates the thread selection combobox with its label.
 */
public final class CThreadPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7249548420182035120L;

  /**
   * The threads IDs of the active threads in the target process are listed in this combobox
   */
  private final CThreadComboBox m_tidBox = new CThreadComboBox();

  /**
   * Synchronizes the content of the thread box with the state of the Debug GUI perspective.
   */
  private final CThreadPanelSynchronizer m_synchronizer;

  /**
   * Creates a new thread panel.
   *
   * @param debugPerspectiveModel The active Debug GUI perspective model.
   */
  public CThreadPanel(final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01518: Debug perspective model argument can not be null");

    setBorder(new TitledBorder(""));

    final JLabel threadLabel = new JLabel("Thread ID");
    threadLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

    add(threadLabel, BorderLayout.WEST);
    add(m_tidBox, BorderLayout.CENTER);

    m_synchronizer = new CThreadPanelSynchronizer(m_tidBox, debugPerspectiveModel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
  }
}
