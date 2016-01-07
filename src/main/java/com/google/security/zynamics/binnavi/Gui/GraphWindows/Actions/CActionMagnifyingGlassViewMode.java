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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphMagnifier;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used to switch the magnifying glass mode on and off.
 */
public final class CActionMagnifyingGlassViewMode extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6270887425285667410L;

  /**
   * Graph for whom the magnifying mode is toggled.
   */
  private final ZyGraph m_graph;

  /**
   * The button to keep synchronized with the state of the magnifying mode.
   */
  private JButton m_button = null;

  /**
   * Creates a new action object.
   * 
   * @param graph Graph for whom the magnifying mode is toggled.
   */
  public CActionMagnifyingGlassViewMode(final ZyGraph graph) {
    super("Toggle magnifying glass");

    m_graph = Preconditions.checkNotNull(graph, "IE01252: Graph argument can not be null");

    putValue(SMALL_ICON,
        new ImageIcon(CMain.class.getResource("data/nomagnifieingglass_hover.jpg")));
    putValue(Action.SHORT_DESCRIPTION, "Magnifying Glass");
  }

  /**
   * Toggles the button state in case magnifying mode is on.
   */
  private void selectButton() {
    m_button.setSelected(true);

    putValue(Action.NAME, "Magnifying Glass");
    putValue(Action.SHORT_DESCRIPTION, "Disables Magnifying Glass.");

    m_button
        .setSelectedIcon(new ImageIcon(CMain.class.getResource("data/magnifieingglass_up.jpg")));
    m_button.setRolloverSelectedIcon(new ImageIcon(CMain.class
        .getResource("data/magnifieingglass_hover.jpg")));
    m_button
        .setPressedIcon(new ImageIcon(CMain.class.getResource("data/magnifieingglass_down.jpg")));
  }

  /**
   * Toggles the button state in case magnifying mode is off.
   */
  private void unselectButton() {
    m_button.setSelected(false);

    putValue(Action.NAME, "Magnifying Glass");
    putValue(Action.SHORT_DESCRIPTION, "Enables Magnifying Glass.");

    m_button.setIcon(new ImageIcon(CMain.class.getResource("data/nomagnifieingglass_up.jpg")));
    m_button.setRolloverIcon(new ImageIcon(CMain.class
        .getResource("data/nomagnifieingglass_hover.jpg")));
    m_button.setPressedIcon(new ImageIcon(CMain.class
        .getResource("data/nomagnifieingglass_down.jpg")));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphMagnifier.toogleMagnifyingGlass(m_graph);
    updateButton();
  }

  /**
   * Sets the button to be synchronized with the toggle state of the magnifying mode.
   * 
   * @param button The button to synchronize.
   */
  public void setButton(final JButton button) {
    m_button = Preconditions.checkNotNull(button, "IE01253: Button argument can not be null");
    updateButton();
  }

  /**
   * Updates the toggle state of the button to remain synchronized with the magnifying mode state.
   */
  public void updateButton() {
    if (m_graph.getSettings().getDisplaySettings().getMagnifyingGlassMode()) {
      selectButton();
    } else {
      unselectButton();
    }
  }
}
