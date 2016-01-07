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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;


/**
 * Panel where the special instruction highlighting is shown.
 */
public final class CInstructionHighlighterPanel extends JPanel implements IGraphPanelExtension {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3020711878666713588L;

  /**
   * Synchronizes the special instruction results with the graph.
   */
  private CGraphSynchronizer m_synchronizer;

  /**
   * Panel where the highlighting results are shown.
   */
  private CResultsPanel m_resultsPanel = null;

  /**
   * Panel where the options can be configured.
   */
  private COptionsPanel m_optionsPanel = null;

  /**
   * Creates a new panel object.
   */
  public CInstructionHighlighterPanel() {
    super(new BorderLayout());
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    // TODO: This function is never called.

    m_synchronizer.dispose();
    m_resultsPanel.dispose();
    m_optionsPanel.dispose();
  }

  @Override
  public void visit(final CGraphModel model, final IGraphPanelExtender extender) {
    extender.addTab("Special Instructions", this);

    final JTabbedPane tabbedPane = new JTabbedPane();

    final CSpecialInstructionsModel instructionsModel = new CSpecialInstructionsModel();

    m_synchronizer = new CGraphSynchronizer(model.getGraph(), instructionsModel);

    m_resultsPanel = new CResultsPanel(model.getGraph(), instructionsModel);
    m_optionsPanel = new COptionsPanel(instructionsModel.getDescriptions());

    tabbedPane.addTab("Highlighted Instruction", m_resultsPanel);
    tabbedPane.addTab("Options", m_optionsPanel);

    add(tabbedPane);
  }
}
