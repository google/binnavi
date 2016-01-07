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
 * Action for showing the Select by Criteria dialog of a graph.
 */
public final class CActionSelectByCriteria extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8412530731075701761L;

  /**
   * Panel whose Select by Criteria dialog is shown.
   */
  private final CGraphPanel m_panel;

  /**
   * Creates a new action object.
   * 
   * @param navi Panel whose Select by Criteria dialog is shown.
   * @param showIcon Flag to toggle whether the action has an icon.
   */
  public CActionSelectByCriteria(final CGraphPanel navi, final boolean showIcon) {
    super("Select by Criteria");

    m_panel = Preconditions.checkNotNull(navi, "IE02829: navi argument can not be null");

    if (showIcon) {
      putValue(SMALL_ICON, new ImageIcon(CMain.class.getResource("data/selcriteria_up.jpg")));
    }

    putValue(Action.SHORT_DESCRIPTION, "Select by Criteria");
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    m_panel.getDialogs().selectByCriteria();
  }
}
