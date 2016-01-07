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
import com.google.security.zynamics.zylib.gui.JHint.JHintIcon;
import com.google.security.zynamics.zylib.gui.sliders.DoubleLabeledSlider;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Class that provides helper functions for the creation of the settings dialog GUI.
 */
public final class CSettingsPanelBuilder {
  /**
   * Standard border that is put around all components added by this class.
   */
  private static final Border STANDARD_EMPTY_BORDER = new EmptyBorder(2, 2, 2, 2);

  /**
   * Preferred width of all components added by this class.
   */
  private static final int PREFERRED_WIDTH = 175;

  /**
   * Preferred height of all components added by this class.
   */
  private static final int PREFERRED_HEIGHT = 25;

  /**
   * You are not supposed to instantiate this class.
   */
  private CSettingsPanelBuilder() {
  }

  /**
   * Adds a component that is used to configure a setting.
   *
   * @param panel The panel the component is added to.
   * @param component The component to add to the panel.
   * @param description The text of the label to be put next to the combobox.
   * @param hint Tooltip shown when the user mouse-overs the created hint icon.
   */
  private static void addComponent(
      final JPanel panel, final Component component, final String description, final String hint) {
    final JPanel settingPanel = new JPanel(new BorderLayout());
    settingPanel.setBorder(STANDARD_EMPTY_BORDER);

    settingPanel.add(new JLabel(description), BorderLayout.CENTER);

    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.add(component, BorderLayout.CENTER);

    final JHintIcon hintPopup = new JHintIcon(hint);
    hintPopup.setBorder(new EmptyBorder(0, 3, 0, 0));
    innerPanel.add(hintPopup, BorderLayout.EAST);

    settingPanel.add(innerPanel, BorderLayout.EAST);

    panel.add(settingPanel);
  }

  /**
   * Adds a combobox for a setting that can be switched on or off.
   *
   * @param panel The panel the combobox is added to.
   * @param comboBox The combobox to add.
   * @param description The text of the label to be put next to the combobox.
   * @param hint Tooltip shown when the user mouse-overs the created hint icon.
   * @param value True to set the combobox value to ON, false to set it to OFF.
   */
  public static void addComboBox(final JPanel panel, final JComboBox<String> comboBox,
      final String description, final String hint, final boolean value) {
    Preconditions.checkNotNull(panel, "IE01592: Panel argument can not be null");
    Preconditions.checkNotNull(comboBox, "IE01593: Combo box argument can not be null");
    Preconditions.checkNotNull(description, "IE01594: Description argument can not be null");

    comboBox.addItem("On");
    comboBox.addItem("Off");
    comboBox.setSelectedItem(value ? "On" : "Off");
    comboBox.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

    addComponent(panel, comboBox, description, hint);
  }

  /**
   * Adds a combobox for a setting with multiple options.
   *
   * @param panel The panel the combobox is added to.
   * @param comboBox The combobox to add.
   * @param description The text of the label to be put next to the combobox.
   * @param hint Tooltip shown when the user mouse-overs the created hint icon.
   * @param values The options to be put into the combobox.
   * @param selectedOption The index of the option to select by default.
   */
  public static void addComboBox(final JPanel panel,
      final JComboBox<String> comboBox,
      final String description,
      final String hint,
      final String[] values,
      final int selectedOption) {
    Preconditions.checkNotNull(panel, "IE01595: Panel argument can not be null");
    Preconditions.checkNotNull(comboBox, "IE01596: Combo box argument can not be null");
    Preconditions.checkNotNull(description, "IE01597: Description argument can not be null");
    Preconditions.checkNotNull(values, "IE01598: Values argument can not be null");
    for (final String string : values) {
      comboBox.addItem(string);
    }
    comboBox.setSelectedIndex(selectedOption);
    comboBox.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    addComponent(panel, comboBox, description, hint);
  }

  /**
   * Adds a double-labeled slider to a panel.
   *
   * @param panel The panel the text field is added to.
   * @param slider The slider to be added to the panel.
   * @param description The text of the label to be put next to the slider.
   * @param hint Tooltip shown when the user mouse-overs the created hint icon.
   * @param value The initial value of the slider.
   */
  public static void addDoubleSlider(final JPanel panel, final DoubleLabeledSlider slider,
      final String description, final String hint, final int value) {
    Preconditions.checkNotNull(panel, "IE01599: Panel argument can not be null");
    Preconditions.checkNotNull(slider, "IE01600: Slider argument can not be null");
    Preconditions.checkNotNull(description, "IE01601: Description argument can not be null");
    slider.setValue(value);
    slider.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    addComponent(panel, slider, description, hint);
  }

  /**
   * Adds a text field that is used for setting an option.
   *
   * @param panel The panel the text field is added to.
   * @param textField The text field to add to the panel.
   * @param description The text of the label to be put next to the text field.
   * @param hint Tooltip shown when the user mouse-overs the created hint icon.
   * @param value The initial value of the text field.
   */
  public static void addTextField(final JPanel panel, final JTextField textField,
      final String description, final String hint, final String value) {
    Preconditions.checkNotNull(panel, "IE01602: Panel argument can not be null");
    Preconditions.checkNotNull(textField, "IE01603: Text field argument can not be null");
    Preconditions.checkNotNull(description, "IE01604: Description argument can not be null");
    Preconditions.checkNotNull(value, "IE01605: Value argument can not be null");
    textField.setText(value);
    textField.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
    addComponent(panel, textField, description, hint);
  }

}
