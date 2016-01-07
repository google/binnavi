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
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalStyle;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where orthogonal layout settings can be configured.
 */
public final class COrthogonalPanel extends CAbstractSettingsPanel {
  /**
   * Used to select between horizontal and vertical orientation.
   */
  private final JComboBox<String> m_cbOrthoOrientation = new JComboBox<String>();

  /**
   * Used to select the concrete orthogonal layouting style.
   */
  private final JComboBox<String> m_cbOrthoLayoutStyle = new JComboBox<String>();

  /**
   * Used to configure the minimum distance between nodes.
   */
  private final JFormattedTextField m_tfOrthogonalMinNodeDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Creates a new orthogonal settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public COrthogonalPanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(3, 1));

    Preconditions.checkNotNull(settings, "IE01591: Settings argument can not be null");

    setBorder(new TitledBorder("Orthogonal Layout"));

    CSettingsPanelBuilder
        .addComboBox(this,
            m_cbOrthoOrientation,
            "Orientation" + ":",
            "Specifies whether orthogonal graph orientation goes from left to right or from top to bottom.",
            new String[] {"Vertical", "Horizontal"},
            settings.getLayoutSettings().getOrthogonalSettings().getOrientation().ordinal());

    CSettingsPanelBuilder.addComboBox(this,
        m_cbOrthoLayoutStyle,
        "Layout Style" + ":",
        "Exact layouting style used when doing orthogonal layouts.",
        new String[] {"Normal", "Tree"},
        settings.getLayoutSettings().getOrthogonalSettings().getStyle().ordinal());

    CSettingsPanelBuilder.addTextField(this, m_tfOrthogonalMinNodeDistance,
        "Minimum Node Distance" + ":", "Minimum distance between nodes in pixels.", String.valueOf(
            settings.getLayoutSettings().getOrthogonalSettings().getMinimumNodeDistance()));
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    final boolean needsLayouting =
        settings.getLayoutSettings().getOrthogonalSettings().getMinimumNodeDistance()
        != Integer.valueOf(m_tfOrthogonalMinNodeDistance.getText()) ||
        settings.getLayoutSettings().getOrthogonalSettings().getStyle()
        != OrthogonalStyle.parseInt(m_cbOrthoLayoutStyle.getSelectedIndex()) ||
        settings.getLayoutSettings().getOrthogonalSettings().getOrientation()
        != OrthogonalOrientation.parseInt(m_cbOrthoOrientation.getSelectedIndex());

    settings.getLayoutSettings().getOrthogonalSettings()
        .setMinimumNodeDistance(Integer.valueOf(m_tfOrthogonalMinNodeDistance.getText()));
    settings.getLayoutSettings().getOrthogonalSettings()
        .setStyle(OrthogonalStyle.parseInt(m_cbOrthoLayoutStyle.getSelectedIndex()));
    settings.getLayoutSettings().getOrthogonalSettings()
        .setOrientation(OrthogonalOrientation.parseInt(m_cbOrthoOrientation.getSelectedIndex()));

    return needsLayouting;
  }
}
