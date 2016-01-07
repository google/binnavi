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
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.zylib.gui.CDecFormatter;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Graph settings panel where automatism settings can be configured.
 */
public final class CAutomatismPanel extends CAbstractSettingsPanel {
  /**
   * Number of elements added to the panel if it displays initial graph settings.
   */
  private static final int NUMBER_OF_ROWS_INITIAL = 7;

  /**
   * Number of elements added to the panel if it displays the settings of a concrete graph.
   */
  private static final int NUMBER_OF_ROWS = NUMBER_OF_ROWS_INITIAL - 1;

  /**
   * Used to switch proximity browsing on and off.
   */
  private final JComboBox<String> m_cbProximityBrowsing = new JComboBox<>();

  /**
   * Used to switch auto-layout on and off.
   */
  private final JComboBox<String> m_cbAutolayout = new JComboBox<>();

  /**
   * Used to select a default layout style for graphs.
   */
  private final JComboBox<String> m_cbDefaultLayoutStyle = new JComboBox<>();

  /**
   * Used to select the proximity browsing activation threshold.
   */
  private final JFormattedTextField m_tfProximityBrowsingActivationThres =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(6)));

  /**
   * Used to select the auto-layout deactivation threshold.
   */
  private final JFormattedTextField m_tfAutolayoutDeactivationThres =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(6)));

  /**
   * Used to select the auto-layout warning threshold.
   */
  private final JFormattedTextField m_tfLayoutProcessingDurationWarningThres =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(6)));

  /**
   * Used to select the visibility warning threshold.
   */
  private final JFormattedTextField m_tfVisibilityWarningThres =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(6)));

  /**
   * Flag that indicates whether the settings panel displays graphs settings for a concrete graph or
   * for graphs in general.
   */
  private final boolean m_isDefaultSettingsDialog;

  /**
   * Creates a new automatism panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   * @param isDefaultSettingsDialog Flag that indicates whether the settings panel displays graphs
   *        settings for a concrete graph or for graphs in general.
   */
  public CAutomatismPanel(final ZyGraphViewSettings settings,
      final boolean isDefaultSettingsDialog) {
    super(new GridLayout(isDefaultSettingsDialog ? NUMBER_OF_ROWS_INITIAL : NUMBER_OF_ROWS, 1));

    Preconditions.checkNotNull(settings, "IE01583: Settings argument can not be null");

    m_isDefaultSettingsDialog = isDefaultSettingsDialog;

    setBorder(new TitledBorder("Automatism Settings"));

    if (isDefaultSettingsDialog) {
      CSettingsPanelBuilder.addComboBox(this,
          m_cbDefaultLayoutStyle,
          "Default Layout Style" + ":",
          "Initial layout style for newly opened graphs",
          new String[] {"Circular", "Hierarchical", "Orthogonal"},
          settings.getLayoutSettings().getDefaultGraphLayout().ordinal());
    }

    CSettingsPanelBuilder.addComboBox(this, m_cbAutolayout, "Automatic Layouting" + ":",
        "If activated, graphs are automatically layouted on relevant events.\n"
        + "This is especially useful when proximity browsing is activated.",
        settings.getLayoutSettings().getAutomaticLayouting());

    CSettingsPanelBuilder.addComboBox(this, m_cbProximityBrowsing, "Proximity Browsing" + ":",
        "If activated, proximity browsing is enabled by default.",
        settings.getProximitySettings().getProximityBrowsing());

    CSettingsPanelBuilder.addTextField(this, m_tfProximityBrowsingActivationThres,
        "Proximity Browsing Activation Threshold" + ":",
            "Enables proximity browsing on newly loaded graphs if "
            + "the graph has more than the specified number of nodes.",
        String.valueOf(settings.getProximitySettings().getProximityBrowsingActivationThreshold()));

    CSettingsPanelBuilder.addTextField(this, m_tfAutolayoutDeactivationThres,
        "Automatic Layouting Deactivation Threshold" + ":",
        "Disables automatic layouting if more than the specified number of nodes is visible.",
        String.valueOf(settings.getLayoutSettings().getAutolayoutDeactivationThreshold()));

    CSettingsPanelBuilder.addTextField(this, m_tfLayoutProcessingDurationWarningThres,
        "Layout Calculation Time Warning Threshold" + ":",
            "Shows a warning before layouting graphs when more than "
            + "the specified number of nodes is visible.",
        String.valueOf(settings.getLayoutSettings().getLayoutCalculationTimeWarningThreshold()));

    CSettingsPanelBuilder.addTextField(this, m_tfVisibilityWarningThres,
        "Visibility Warning Threshold" + ":",
            "Shows a warning if more than the specified number of graph nodes is "
            + "made visible in one step.",
        String.valueOf(settings.getLayoutSettings().getVisibilityWarningTreshold()));
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    settings.getLayoutSettings().setAutomaticLayouting(m_cbAutolayout.getSelectedIndex() == 0);
    settings.getProximitySettings().setProximityBrowsing(
        m_cbProximityBrowsing.getSelectedIndex() == 0);

    if (m_isDefaultSettingsDialog) {
      settings.getLayoutSettings().setDefaultGraphLayout(
          LayoutStyle.parseInt(m_cbDefaultLayoutStyle.getSelectedIndex()));
    }

    settings.getProximitySettings().setProximityBrowsingActivationThreshold(
        Integer.valueOf(m_tfProximityBrowsingActivationThres.getText()));
    settings.getLayoutSettings().setAutolayoutActivisionThreshold(
        Integer.valueOf(m_tfAutolayoutDeactivationThres.getText()));
    settings.getLayoutSettings().setLayoutCalculationTimeWarningThreshold(
        Integer.valueOf(m_tfLayoutProcessingDurationWarningThres.getText()));
    settings.getLayoutSettings().setVisibilityWarningThreshold(
        Integer.valueOf(m_tfVisibilityWarningThres.getText()));

    return false;
  }
}
