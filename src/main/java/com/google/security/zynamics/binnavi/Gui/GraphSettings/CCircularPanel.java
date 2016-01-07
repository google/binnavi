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
import com.google.security.zynamics.zylib.gui.zygraph.layouters.CircularStyle;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where circular layout settings can be configured.
 */
public final class CCircularPanel extends CAbstractSettingsPanel {
  /**
   * Used to select the concrete circular layout style.
   */
  private final JComboBox<String> m_cbCircularLayoutStyle = new JComboBox<String>();

  /**
   * Used to select the minimum distance between nodes.
   */
  private final JFormattedTextField m_tfCircularMinNodeDistance =
      new JFormattedTextField(new CDecFormatter(6));

  /**
   * Creates a new circular settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public CCircularPanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(2, 1));
    setBorder(new TitledBorder("Circular Layout"));

    Preconditions.checkNotNull(settings, "IE01584: Settings argument can not be null");

    CSettingsPanelBuilder.addComboBox(this,
        m_cbCircularLayoutStyle,
        "Layout Style" + ":",
        "Exact layouting style used when doing circular layouts.",
        new String[] {"Compact", "Isolated", "Single Cycle"},
        settings.getLayoutSettings().getCircularSettings().getStyle().ordinal());

    CSettingsPanelBuilder.addTextField(this, m_tfCircularMinNodeDistance,
        "Minimum Node Distance" + ":", "Minimum distance between nodes in pixels.", String.valueOf(
            settings.getLayoutSettings().getCircularSettings().getMinimumNodeDistance()));
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    final boolean needsLayouting = (Integer.valueOf(m_tfCircularMinNodeDistance.getText())
        != settings.getLayoutSettings().getCircularSettings().getMinimumNodeDistance()) || (
        CircularStyle.parseInt(m_cbCircularLayoutStyle.getSelectedIndex())
        != settings.getLayoutSettings().getCircularSettings().getStyle());

    settings.getLayoutSettings().getCircularSettings()
        .setMinimumNodeDistance(Integer.valueOf(m_tfCircularMinNodeDistance.getText()));
    settings.getLayoutSettings().getCircularSettings()
        .setStyle(CircularStyle.parseInt(m_cbCircularLayoutStyle.getSelectedIndex()));

    return needsLayouting;
  }
}
