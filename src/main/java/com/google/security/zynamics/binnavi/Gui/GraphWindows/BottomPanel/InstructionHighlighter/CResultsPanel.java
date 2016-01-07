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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * Panel where the results are shown.
 */
public final class CResultsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 922845731594903437L;

  /**
   * Table where the results are shown.
   */
  private final CResultsTable m_table;

  /**
   * Creates a new results panel.
   *
   * @param graph The graph where highlighting happens.
   * @param model Provides the results to display.
   */
  public CResultsPanel(final ZyGraph graph, final CSpecialInstructionsModel model) {
    super(new BorderLayout());

    m_table = new CResultsTable(graph, model);

    add(new JScrollPane(m_table));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_table.dispose();
  }
}
