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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.zylib.gui.sliders.DoubleLabeledSlider;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;

import java.awt.GridLayout;
import java.text.ParseException;

import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where control settings can be configured.
 */
public final class CControlsPanel extends CAbstractSettingsPanel {
  /**
   * Used to select the scroll sensibility of the mouse.
   */
  private final DoubleLabeledSlider m_tfScrollSensitivity =
      new DoubleLabeledSlider("Low", "High", 1, 5);

  /**
   * Used to select the zoom sensibility of the mouse.
   */
  private final DoubleLabeledSlider m_tfZoomSensitivity =
      new DoubleLabeledSlider("Low", "High", 1, 5);

  /**
   * Used to select the behavior of the mouse wheel.
   */
  private final JComboBox<String> mouseWheelBehaviorBox = new JComboBox<>();

  /**
   * Creates a new control settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public CControlsPanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(3, 1));
    setBorder(new TitledBorder("Controls"));

    CSettingsPanelBuilder.addComboBox(this,
        mouseWheelBehaviorBox,
        "Mousewheel Action" + ":",
        "Specifies whether the mousewheel is used for zooming or scrolling in graph windows.",
        new String[] {"Zoom", "Scroll"},
        settings.getMouseSettings().getMouseWheelAction().ordinal());

    CSettingsPanelBuilder.addDoubleSlider(this, m_tfScrollSensitivity, "Scroll Sensitivity" + ":",
        "Mouse sensitivity during scroll operations.",
        settings.getMouseSettings().getScrollSensitivity() / 5);

    CSettingsPanelBuilder.addDoubleSlider(this, m_tfZoomSensitivity, "Zoom Sensitivity" + ":",
        "Mouse sensitivity during zoom operations.",
        settings.getMouseSettings().getZoomSensitivity() / 5);
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    settings.getMouseSettings().setScrollSensitivity(5 * m_tfScrollSensitivity.getValue());
    settings.getMouseSettings().setZoomSensitivity(5 * m_tfZoomSensitivity.getValue());

    try {
      settings.getMouseSettings().setMousewheelAction(
          MouseWheelAction.parseInt(mouseWheelBehaviorBox.getSelectedIndex()));
    } catch (final ParseException e) {
      CUtilityFunctions.logException(e);
    }

    return false;
  }
}
