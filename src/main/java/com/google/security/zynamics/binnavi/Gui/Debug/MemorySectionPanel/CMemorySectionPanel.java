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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Panel that encapsulates a memory section box with a synchronizer that keeps the content of the
 * memory section box synchronized with the general state of a debug GUI perspective and the
 * available debuggers.
 */
public final class CMemorySectionPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8636503927797326338L;

  /**
   * The memory layout box is the combobox that is used to display the available regions of the
   * memory of the debugged process.
   */
  private final CMemorySectionBox m_memoryLayoutBox = new CMemorySectionBox();

  /**
   * The synchronizer that keeps the content of the memory section box up to date.
   */
  private final CMemorySectionPanelSynchronizer m_synchronizer;

  /**
   * Creates a new memory section panel.
   *
   * @param debugPerspectiveModel Describes a debug GUI perspective.
   */
  public CMemorySectionPanel(final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01453: Debug perspective model argument can not be null");

    setBorder(new TitledBorder(""));

    add(m_memoryLayoutBox);

    m_synchronizer = new CMemorySectionPanelSynchronizer(m_memoryLayoutBox, debugPerspectiveModel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
  }
}
