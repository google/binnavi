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

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;

/**
 * This extension object extends the graph panel by adding an additional menu to the context menu
 * and a results panel to the bottom of the graph panel.
 */
public final class CRegisterTrackingExtension implements IGraphPanelExtension {
  /**
   * The settings panel where the user can configure the register tracking settings.
   */
  private final CSettingsPanel m_settingsPanel = new CSettingsPanel();

  /**
   * Results container object where tracking results are stored.
   */
  private CTrackingResultContainer m_resultsContainer;

  /**
   * Used for interacting with the tabbed pane that displays the results.
   */
  private IGraphPanelExtender m_extender;

  /**
   * The outermost panel that is shown in the graph window.
   */
  private final JPanel m_outerPanel = new JPanel(new BorderLayout());

  /**
   * The tabbed pane where results and settings tabs are shown.
   */
  private final JTabbedPane m_tabbedPane = new JTabbedPane();

  /**
   * Updates the results display when new results come in.
   */
  private final ITrackingResultsListener m_internalListener = new InternalListener();

  private CTrackingResultsPanel m_resultsPanel;

  @Override
  public void dispose() {
    m_resultsPanel.dispose();
    m_resultsContainer.removeListener(m_internalListener);
  }

  @Override
  public void visit(final CGraphModel model, final IGraphPanelExtender extender) {
    m_resultsContainer = new CTrackingResultContainer(model.getViewContainer(), model.getGraph());
    m_extender = extender;
    m_outerPanel.add(m_tabbedPane);

    m_tabbedPane.addTab("Results", m_resultsPanel =
        new CTrackingResultsPanel(m_extender, m_resultsContainer));
    m_tabbedPane.addTab("Settings", m_settingsPanel);

    // Add a new panel to the graph window
    extender.addTab("Register Tracking", m_outerPanel);

    // Register a code node extension object that is extends
    // the context menu of code nodes.
    extender.registerCodeNodeExtension(new OperandMenuExtension());

    m_resultsContainer.addListener(m_internalListener);
  }

  /**
   * Updates the results display when new results come in.
   */
  private class InternalListener implements ITrackingResultsListener {
    @Override
    public void updatedResult(final CTrackingResultContainer container, final CTrackingResult result) {
      m_extender.selectTab(m_outerPanel);
      m_tabbedPane.setSelectedIndex(0);
    }
  }

  /**
   * Code node menu extension object.
   */
  private class OperandMenuExtension implements ICodeNodeExtension {
    @Override
    public void extendIncomingRegistersMenu(final JMenu menu, final INaviCodeNode node,
        final INaviInstruction instruction, final String register) {
      menu.add(CActionProxy.proxy(new CTrackOperandAction(m_resultsContainer, instruction,
          register, new RegisterTrackingOptions(m_settingsPanel.doClearAllRegisters(),
              m_settingsPanel.getClearedRegisters(), true, AnalysisDirection.DOWN))));
      menu.add(CActionProxy.proxy(new CTrackOperandAction(m_resultsContainer, instruction,
          register, new RegisterTrackingOptions(m_settingsPanel.doClearAllRegisters(),
              m_settingsPanel.getClearedRegisters(), true, AnalysisDirection.UP))));
    }

    @Override
    public void extendInstruction(final JMenu menu, final INaviCodeNode node,
        final INaviInstruction instruction) {
      // Instructions are not extended
    }

    @Override
    public void extendOutgoingRegistersMenu(final JMenu menu, final INaviCodeNode node,
        final INaviInstruction instruction, final String register) {
      menu.add(CActionProxy.proxy(new CTrackOperandAction(m_resultsContainer, instruction,
          register, new RegisterTrackingOptions(m_settingsPanel.doClearAllRegisters(),
              m_settingsPanel.getClearedRegisters(), false, AnalysisDirection.DOWN))));
      menu.add(CActionProxy.proxy(new CTrackOperandAction(m_resultsContainer, instruction,
          register, new RegisterTrackingOptions(m_settingsPanel.doClearAllRegisters(),
              m_settingsPanel.getClearedRegisters(), false, AnalysisDirection.UP))));
    }
  }
}
