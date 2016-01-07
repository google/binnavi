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
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicStyle;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where hierarchic layout settings can be configured.
 */
public final class CHierarchicPanel extends CAbstractSettingsPanel {
  /**
   * Used to select between horizontal and vertical orientation.
   */
  private final JComboBox<String> m_cbHierarchicOrientation = new JComboBox<String>();

  /**
   * Used to select the edge routing style.
   */
  private final JComboBox<String> m_cbHierarchicEdgeRoutingStyle = new JComboBox<String>();

  /**
   * Used to select the minimum distance between graph layers.
   */
  private final JFormattedTextField m_tfHierarchicMinLayerDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Used to select the minimum distance between nodes.
   */
  private final JFormattedTextField m_tfHierarchicMinNodeDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Used to select the minimum distance between edges.
   */
  private final JFormattedTextField m_tfHierarchicMinEdgeDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Used to select the minimum distance between nodes and non-adjacend edges.
   */
  private final JFormattedTextField m_tfHierarchicMinNodeEdgeDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Creates a new hierarchic settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public CHierarchicPanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(6, 1));

    Preconditions.checkNotNull(settings, "IE01589: Settings argument can not be null");

    setBorder(new TitledBorder("Hierarchic Layout"));

    CSettingsPanelBuilder.addComboBox(this,
        m_cbHierarchicOrientation,
        "Orientation:",
        "Specifies whether hierarchic graph orientation goes from left to right or "
            + "from top to bottom.",
        new String[] {"Vertical", "Horizontal"},
        settings.getLayoutSettings().getHierarchicalSettings().getOrientation().ordinal());

    CSettingsPanelBuilder.addComboBox(this,
        m_cbHierarchicEdgeRoutingStyle,
        "Edge Routing Style:",
        "Specifies how edges are routed between nodes.",
        new String[] {"Octlinear Optimal",
            "Orthogonal Optimal",
            "Polyline Optimal",
            "Octlinear Topmost",
            "Orthogonal Topmost",
            "Polyline Topmost",
            "Octlinear Tight Tree",
            "Orthogonal Tight Tree",
            "Polyline Tight Tree",
            "Octlinear BFS",
            "Orthogonal BFS",
            "Polyline BFS"},
        settings.getLayoutSettings().getHierarchicalSettings().getStyle().ordinal());

    CSettingsPanelBuilder.addTextField(this, m_tfHierarchicMinLayerDistance,
        "Minimum Layer Distance:", "Minimum distance between hierarchic graph layers in pixels.",
        String.valueOf(
            settings.getLayoutSettings().getHierarchicalSettings().getMinimumLayerDistance()));

    CSettingsPanelBuilder.addTextField(this, m_tfHierarchicMinNodeDistance,
        "Node to Node Distance:", "Minimum distance between nodes in pixels on the same layer.",
        String.valueOf(
            settings.getLayoutSettings().getHierarchicalSettings().getMinimumNodeDistance()));

    CSettingsPanelBuilder.addTextField(this, m_tfHierarchicMinEdgeDistance,
        "Edge to Edge Distance:", "Minimum distance between edges in pixels on the same layer.",
        String.valueOf(
            settings.getLayoutSettings().getHierarchicalSettings().getMinimumEdgeDistance()));

    CSettingsPanelBuilder.addTextField(this, m_tfHierarchicMinNodeEdgeDistance,
        "Node to Edge Distance:",
        "Minimum distance between a node and a non-adjacent edge in the same layer", String.valueOf(
            settings.getLayoutSettings().getHierarchicalSettings().getMinimumEdgeDistance()));
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    final boolean needsLayouting =
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumLayerDistance()
        != Integer.valueOf(m_tfHierarchicMinLayerDistance.getText()) ||
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumNodeDistance()
        != Integer.valueOf(m_tfHierarchicMinNodeDistance.getText()) ||
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumEdgeDistance()
        != Integer.valueOf(m_tfHierarchicMinEdgeDistance.getText()) ||
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumNodeEdgeDistance()
        != Integer.valueOf(m_tfHierarchicMinNodeEdgeDistance.getText()) ||
        settings.getLayoutSettings().getHierarchicalSettings().getOrientation()
        != HierarchicOrientation.parseInt(m_cbHierarchicOrientation.getSelectedIndex()) ||
        settings.getLayoutSettings().getHierarchicalSettings().getStyle()
        != HierarchicStyle.parseInt(m_cbHierarchicEdgeRoutingStyle.getSelectedIndex());

    settings.getLayoutSettings().getHierarchicalSettings()
        .setMinimumLayerDistance(Integer.valueOf(m_tfHierarchicMinLayerDistance.getText()));
    settings.getLayoutSettings().getHierarchicalSettings()
        .setMinimumNodeDistance(Integer.valueOf(m_tfHierarchicMinNodeDistance.getText()));
    settings.getLayoutSettings().getHierarchicalSettings()
        .setMinimumEdgeDistance(Integer.valueOf(m_tfHierarchicMinEdgeDistance.getText()));
    settings.getLayoutSettings().getHierarchicalSettings()
        .setMinimumNodeEdgeDistance(Integer.valueOf(m_tfHierarchicMinNodeEdgeDistance.getText()));
    settings.getLayoutSettings().getHierarchicalSettings().setOrientation(
        HierarchicOrientation.parseInt(m_cbHierarchicOrientation.getSelectedIndex()));
    settings.getLayoutSettings().getHierarchicalSettings()
        .setStyle(HierarchicStyle.parseInt(m_cbHierarchicEdgeRoutingStyle.getSelectedIndex()));

    return needsLayouting;
  }
}
