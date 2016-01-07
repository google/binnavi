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
import com.google.security.zynamics.zylib.gui.sliders.DoubleLabeledSlider;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where miscellaneous settings can be configured.
 */
public final class CMiscPanel extends CAbstractSettingsPanel {
  /**
   * Used to switch layout animation on or off.
   */
  private final JComboBox<String> layoutAnimationBox = new JComboBox<>();

  /**
   * Used to toggle function information shown in function nodes.
   */
  private final JComboBox<String> functionInformationBox = new JComboBox<>();

  /**
   * Used to configure the animation speed.
   */
  private final DoubleLabeledSlider m_tfAnimationSpeed =
      new DoubleLabeledSlider("Slow", "Fast", 1, 5);

  /**
   * Used to configure whether a gradient background is displayed or not.
   */
  private final JComboBox<String> gradientBackgroundBox = new JComboBox<>();

  /**
   * Creates a new miscellaneous settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   * @param isCallgraph True, to indicate that the settings dialog is used for call graphs. False,
   *        otherwise.
   */
  public CMiscPanel(final ZyGraphViewSettings settings, final boolean isCallgraph) {
    super(new GridLayout(3 + (isCallgraph ? 1 : 0), 1));

    Preconditions.checkNotNull(settings, "IE01590: Settings argument can not be null");

    setBorder(new TitledBorder("Miscellaneous"));

    CSettingsPanelBuilder.addComboBox(this, gradientBackgroundBox, "Gradient Background" + ":",
        "Toggles between gradient backgrounds and solid white backgrounds in graph windows.",
        settings.getDisplaySettings().getGradientBackground());

    CSettingsPanelBuilder.addComboBox(this, layoutAnimationBox, "Layout Animation" + ":",
        "If enabled, a small layout animation is shown when graphs are layouted.",
        settings.getLayoutSettings().getAnimateLayout());

    CSettingsPanelBuilder.addDoubleSlider(this, m_tfAnimationSpeed, "Animation Speed" + ":",
        "Specifies the animation speed if layout operations are animated.",
        (25 - settings.getDisplaySettings().getAnimationSpeed()) / 5);

    if (isCallgraph) {
      CSettingsPanelBuilder.addComboBox(this, functionInformationBox, "Function Information" + ":",
          "If enabled, statistical information about functions is shown in function nodes.",
          settings.getDisplaySettings().getFunctionNodeInformation());
    }
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    settings.getLayoutSettings().setAnimateLayout(layoutAnimationBox.getSelectedIndex() == 0);
    settings.getDisplaySettings().setAnimationSpeed(
        25 - 5 * Integer.valueOf(m_tfAnimationSpeed.getValue()));
    settings.getDisplaySettings().setGradientBackground(
        gradientBackgroundBox.getSelectedIndex() == 0);
    settings.getDisplaySettings().setFunctionNodeInformation(
        functionInformationBox.getSelectedIndex() == 0);

    return false;
  }
}
