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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;


/**
 * Action for changing the color of nodes.
 */
public final class CActionColorNodes extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8015267915010689781L;

  /**
   * Panel where the nodes are colored.
   */
  private final CGraphPanel m_graphPanel;

  /**
   * Creates a new action object.
   * 
   * @param graphPanel Panel where the nodes are colored.
   */
  public CActionColorNodes(final CGraphPanel graphPanel) {
    super("Set Node Color");

    m_graphPanel =
        Preconditions.checkNotNull(graphPanel, "IE02815: graphPanel argument can not be null");

    putValue(Action.SMALL_ICON, new ImageIcon(CMain.class.getResource("data/nodecolor_up.jpg")));
    putValue(Action.SHORT_DESCRIPTION, "Change the colors of the selected nodes");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_graphPanel.getDialogs().showColorizeNodesDialog();
  }
}
