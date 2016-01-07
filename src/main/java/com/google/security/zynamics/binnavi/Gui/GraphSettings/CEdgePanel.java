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
package com.google.security.zynamics.binnavi.Gui.GraphSettings;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.zylib.gui.CDecFormatter;
import com.google.security.zynamics.zylib.gui.zygraph.EdgeHidingMode;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where edge settings can be configured.
 */
public final class CEdgePanel extends CAbstractSettingsPanel {

  /**
   * Used to configure whether multiple edges are displayed as one.
   */
  private final JComboBox<String> m_cbDisplayMultiEdgesAsOne = new JComboBox<>();

  /**
   * Used to configure whether selected bends should be drawn.
   */
  private final JComboBox<String> m_cbDrawSelectedBends = new JComboBox<>();

  /**
   * Used to configure how to hide edges.
   */
  private final JComboBox<String> m_cbEdgeHidingMode = new JComboBox<>();

  /**
   * Used to configure the edge hiding threshold that is used in combination with the edge hiding
   * mode.
   */
  private final JFormattedTextField m_tfEdgeHidingThreshold =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Creates a new edge settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public CEdgePanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(4, 1));

    Preconditions.checkNotNull(settings, "IE01585: Settings argument can not be null");

    setBorder(new TitledBorder("Edge Settings"));

    final String[] modes = new String[] {
        com.google.security.zynamics.zylib.resources.Constants.HIDE_NEVER,
        com.google.security.zynamics.zylib.resources.Constants.HIDE_ALWAYS,
        com.google.security.zynamics.zylib.resources.Constants.THRESHOLD};

    final int selectionIndex = settings.getEdgeSettings().getEdgeHidingMode().ordinal();

    CSettingsPanelBuilder.addComboBox(this, m_cbDisplayMultiEdgesAsOne,
        "Display multiple edges as one" + ":",
            "If enabled, only one edge between two nodes is shown even if more than one edge "
            + "connects the two nodes.\nThis is especially useful for cutting down on the "
            + "number of edges in callgraphs.",
        settings.getEdgeSettings().getDisplayMultipleEdgesAsOne());

    CSettingsPanelBuilder.addComboBox(this, m_cbDrawSelectedBends, "Draw Selected Bends" + ":",
        "If enabled, corners of selected edges are highlighted.",
        settings.getEdgeSettings().getDrawSelectedBends());

    CSettingsPanelBuilder.addComboBox(this,
        m_cbEdgeHidingMode,
        "Hide Edges when zoomed out" + ":",
        "Specifies under what circumstances edges are not drawn when the graph is zoomed out.",
        modes,
        selectionIndex);

    CSettingsPanelBuilder.addTextField(this, m_tfEdgeHidingThreshold,
        "Edge Hiding Threshold when zoomed out" + ":",
            "If threshold mode is enabled, edges are automatically hidden when more than the "
            + "specified number of edges are visible.",
        String.format("%d", settings.getEdgeSettings().getEdgeHidingThreshold()));
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    final boolean needsLayouting = settings.getEdgeSettings().getDisplayMultipleEdgesAsOne()
        != (m_cbDisplayMultiEdgesAsOne.getSelectedIndex() == 0) ||
        settings.getEdgeSettings().getDrawSelectedBends()
        != (m_cbDrawSelectedBends.getSelectedIndex() == 0) ||
        settings.getEdgeSettings().getEdgeHidingMode()
        != EdgeHidingMode.parseInt(m_cbEdgeHidingMode.getSelectedIndex()) ||
        settings.getEdgeSettings().getEdgeHidingThreshold()
        != Integer.parseInt(m_tfEdgeHidingThreshold.getText());

    settings.getEdgeSettings().setDisplayMultipleEdgesAsOne(
        m_cbDisplayMultiEdgesAsOne.getSelectedIndex() == 0);
    settings.getEdgeSettings().setDrawSelectedBends(m_cbDrawSelectedBends.getSelectedIndex() == 0);
    settings.getEdgeSettings().setEdgeHidingMode(
        EdgeHidingMode.parseInt(m_cbEdgeHidingMode.getSelectedIndex()));
    settings.getEdgeSettings().setEdgeHidingThreshold(
        Integer.parseInt(m_tfEdgeHidingThreshold.getText()));

    return needsLayouting;
  }
}
