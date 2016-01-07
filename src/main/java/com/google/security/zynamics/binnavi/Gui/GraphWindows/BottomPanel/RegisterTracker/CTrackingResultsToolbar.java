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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Toolbar used in the register tracking panel.
 */
public final class CTrackingResultsToolbar extends JToolBar {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3912462323753347468L;

  /**
   * Extends the graph panel.
   */
  private final IGraphPanelExtender m_extender;

  /**
   * Contains the results of tracking operations.
   */
  private final CTrackingResultContainer m_container;

  /**
   * Creates a new toolbar object.
   *
   * @param extender Extends the graph panel.
   * @param container Contains the results of tracking operations.
   */
  public CTrackingResultsToolbar(
      final IGraphPanelExtender extender, final CTrackingResultContainer container) {
    m_container =
        Preconditions.checkNotNull(container, "IE02305: container argument can not be null");
    m_extender = Preconditions.checkNotNull(extender, "IE02306: extender argument can not be null");

    setFloatable(false);

    createAndAddIconToToolbar(this, new ClearAction(), "data/clearinstructionhighlighting_up.png",
        "data/clearinstructionhighlighting_hover.png",
        "data/clearinstructionhighlighting_down.png");
    createAndAddIconToToolbar(this, new CloneAction(), "data/createnewgraph_up.png",
        "data/createnewgraph_hover.png", "data/createnewgraph_down.png");
  }

  /**
   * Small helper function for adding buttons to the toolbar.
   *
   * @param toolBar
   * @param action Action associated with the new button.
   * @param defaultIconPath Path to the default icon for the button.
   * @param rolloverIconPath Path to the roll-over icon for the button.
   * @param pressedIconPath Path to the pressed icon for the button.
   *
   * @return The created button.
   */
  // ESCA-JAVA0138:
  private static JButton createAndAddIconToToolbar(final JToolBar toolBar,
      final AbstractAction action, final String defaultIconPath, final String rolloverIconPath,
      final String pressedIconPath) {
    final JButton button = toolBar.add(CActionProxy.proxy(action));
    button.setBorder(new EmptyBorder(0, 0, 0, 0));

    button.setIcon(new ImageIcon(CMain.class.getResource(defaultIconPath)));
    button.setRolloverIcon(new ImageIcon(CMain.class.getResource(rolloverIconPath)));
    button.setPressedIcon(new ImageIcon(CMain.class.getResource(pressedIconPath)));

    return button;
  }

  /**
   * Action for clearing the tracking results.
   */
  private class ClearAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2927736768691304058L;

    /**
     * Creates a new action object.
     */
    private ClearAction() {
      putValue(SHORT_DESCRIPTION, "Remove results highlighting from the graph");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final CTrackingResult result = m_container.getResult();

      if (result == null) {
        return;
      }

      CTrackingResultsHighlighter.updateHighlighting(m_container.getGraph(),
          result.getStartInstruction(), "", new ArrayList<CInstructionResult>());
    }
  }

  /**
   * Action for creating a view from register tracking results.
   */
  private class CloneAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2088458708261529407L;

    /**
     * Creates a new action object.
     */
    private CloneAction() {
      putValue(SHORT_DESCRIPTION, "Create a new graph with only register tracking results");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final CTrackingResult result = m_container.getResult();

      if (result == null) {
        return;
      }

      final List<INaviInstruction> keptInstructions = new ArrayList<INaviInstruction>();

      for (final CInstructionResult instructionResult : result.getResults()) {
        if ((instructionResult.getInstruction() == result.getStartInstruction())
            || instructionResult.uses() || instructionResult.undefinesAll()) {
          keptInstructions.add(instructionResult.getInstruction());
        }
      }

      final INaviView view = CViewPruner.prune(
          m_container.getViewContainer(), m_container.getGraph().getRawView(), keptInstructions);

      m_extender.openView(m_container.getViewContainer(), view);
    }
  }
}
