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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySelectionPanel;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.CMemoryPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.CMemoryRefreshButtonPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel.CMemorySectionPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Encapsulates the memory viewer, the memory section combobox, and the memory refresh button.
 */
public final class CMemorySelectionPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2787600129006804398L;

  /**
   * Panel where the target process memory is shown.
   */
  private final CMemoryPanel m_memoryPanel;

  /**
   * Used to select the currently visible memory region.
   */
  private final CMemorySectionPanel m_memorySectionPanel;

  /**
   * Used to refresh the visible memory.
   */
  private final CMemoryRefreshButtonPanel m_refreshPanel;

  /**
   * Creates a new memory selection panel.
   *
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Describes a debug GUI perspective.
   * @param refreshPanel Used to refresh the visible memory.
   */
  public CMemorySelectionPanel(final JFrame parent,
      final CDebugPerspectiveModel debugPerspectiveModel,
      final CMemoryRefreshButtonPanel refreshPanel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01456: Parent argument can not be null");

    Preconditions.checkNotNull(debugPerspectiveModel, "IE01457: Debugger argument can not be null");

    m_memorySectionPanel = new CMemorySectionPanel(debugPerspectiveModel);
    m_refreshPanel = refreshPanel;
    m_memoryPanel = new CMemoryPanel(parent, debugPerspectiveModel);

    setBorder(new TitledBorder("Target Memory"));

    // There is the memory panel which contains the memory layout combobox
    // and the refresh button.
    final JPanel headerPanel = new JPanel(new BorderLayout());

    headerPanel.add(m_memorySectionPanel, BorderLayout.CENTER);
    headerPanel.add(m_refreshPanel, BorderLayout.EAST);

    add(headerPanel, BorderLayout.NORTH);
    add(m_memoryPanel, BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_memoryPanel.dispose();
    m_memorySectionPanel.dispose();
    m_refreshPanel.dispose();
  }

  /**
   * Returns the memory panel.
   *
   * @return The memory panel.
   */
  public CMemoryPanel getMemoryPanel() {
    return m_memoryPanel;
  }
}
