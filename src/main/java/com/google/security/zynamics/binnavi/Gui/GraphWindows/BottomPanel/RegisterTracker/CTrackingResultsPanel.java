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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;

/**
 * Used to display the results of a register tracking operation.
 */
public final class CTrackingResultsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7770879916540629490L;

  private CTrackingResultsTable m_table;

  /**
   * Creates a new register tracking panel.
   *
   * @param extender Extends the graph panel.
   * @param container Graph in which register tracking is performed.
   */
  public CTrackingResultsPanel(
      final IGraphPanelExtender extender, final CTrackingResultContainer container) {
    super(new BorderLayout());

    Preconditions.checkNotNull(container, "IE01690: Container argument can not be null");
    Preconditions.checkNotNull(extender, "IE02304: extender argument can not be null");

    add(new CTrackingResultsToolbar(extender, container), BorderLayout.NORTH);
    add(new JScrollPane(m_table = new CTrackingResultsTable(container)), BorderLayout.CENTER);
  }

  public void dispose() {
    m_table.dispose();
  }
}
