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
import javax.swing.JButton;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphFreezer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class for freezing and unfreezing a graph view.
 */
public final class CActionFreezeView extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4614240922663437427L;

  /**
   * Graph to be frozen and unfrozen.
   */
  private final ZyGraph m_graph;

  /**
   * The button to be toggled (this can be null).
   */
  private JButton m_ParentButton = null;

  /**
   * Creates a new action object.
   * 
   * @param graph Graph to be frozen and unfrozen.
   */
  public CActionFreezeView(final ZyGraph graph) {
    m_graph = Preconditions.checkNotNull(graph, "IE02818: graph argument can not be null");

    putValue(Action.SHORT_DESCRIPTION, "Freeze view");

    putValue(SMALL_ICON, new ImageIcon(CMain.class.getResource("data/viewlock_up.jpg")));
  }

  /**
   * Changes the button appearance to reflect a frozen graph.
   */
  private void selectButton() {
    m_ParentButton.setSelected(true);

    putValue(Action.NAME,
        "Frozen Proximitry Browsing. Selection changes do not change graph visibility"); // current
                                                                                         // state
    putValue(Action.SHORT_DESCRIPTION,
        "Unfreeze Proximitry Browsing. Selection changes will change graph visibility"); // state
                                                                                         // after
                                                                                         // pressing
                                                                                         // the
                                                                                         // button

    m_ParentButton.setSelectedIcon(new ImageIcon(CMain.class.getResource("data/viewlock_up.jpg")));
    m_ParentButton.setRolloverSelectedIcon(new ImageIcon(CMain.class
        .getResource("data/viewlock_hover.jpg")));
    m_ParentButton.setPressedIcon(new ImageIcon(CMain.class.getResource("data/viewnavi_down.jpg")));
  }

  /**
   * Changes the button appearance to reflect an unfrozen graph.
   */
  private void unselectButton() {
    m_ParentButton.setSelected(false);

    putValue(Action.NAME,
        "Unfreeze Proximitry Browsing. Selection changes will change graph visibility"); // current
                                                                                         // state
    putValue(Action.SHORT_DESCRIPTION,
        "Frozen Proximitry Browsing. Selection changes do not change graph visibility"); // state
                                                                                         // after
                                                                                         // pressing
                                                                                         // the
                                                                                         // button

    m_ParentButton.setIcon(new ImageIcon(CMain.class.getResource("data/viewnavi_up.jpg")));
    m_ParentButton
        .setRolloverIcon(new ImageIcon(CMain.class.getResource("data/viewnavi_hover.jpg")));
    m_ParentButton.setPressedIcon(new ImageIcon(CMain.class.getResource("data/viewlock_down.jpg")));
  }

  /**
   * Synchronizes the button appearance with the freeze node setting.
   */
  private void updateButton() {
    if (m_ParentButton != null) {
      if (m_graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
        selectButton();
      } else {
        unselectButton();
      }
    }
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphFreezer.toogleProximityFrozen(m_graph);

    updateButton();
  }

  /**
   * Sets the button to be toggled depending on the setting state.
   * 
   * @param button The button to be toggled.
   */
  public void setButton(final JButton button) {
    m_ParentButton = button;

    updateButton();
  }
}
