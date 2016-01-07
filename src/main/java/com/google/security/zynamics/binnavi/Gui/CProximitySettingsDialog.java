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
package com.google.security.zynamics.binnavi.Gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Dialog used to select the number of node parents and children shown during proximity browsing.
 */
public final class CProximitySettingsDialog extends JDialog {
  /**
   * Settings object modified in this dialog.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7599777976161689962L;

  /**
   * Used to select the number of visible parents.
   */
  private final JSlider m_parentDepthSlider = new JSlider(0, 10, 2);

  /**
   * Used to select the number of visible children.
   */
  private final JSlider m_childDepthSlider = new JSlider(0, 10, 2);

  /**
   * Label where the number of visible parents is printed.
   */
  private final JLabel m_parentLabel = new JLabel();

  /**
   * Label where the number of visible children is printed.
   */
  private final JLabel m_childLabel = new JLabel();

  /**
   * Button used to accept the modified settings.
   */
  private final JButton m_buttonOk = new JButton("OK");

  /**
   * Button used to reject the modified settings.
   */
  private final JButton m_buttonCancel = new JButton("Cancel");

  /**
   * Checkbox used to select whether all parent nodes should be visible or only as many as selected
   * in the slider.
   */
  private final JCheckBox m_checkBoxParentDepth = new JCheckBox("Visible Parents" + ":");

  /**
   * Checkbox used to select whether all child nodes should be visible or only as many as selected
   * in the slider.
   */
  private final JCheckBox m_checkBoxChildDepth = new JCheckBox("Visible Children" + ":");

  /**
   * Handles clicks on the buttons.
   */
  private final InternalActionListener m_actionListener = new InternalActionListener();

  /**
   * Handles changes in the checkboxes.
   */
  private final InternalItemListener m_itemListener = new InternalItemListener();

  /**
   * Creates a new proximity settings dialog.
   * 
   * @param parent Parent window used for the dialog.
   * @param settings Settings object modified in this dialog.
   */
  private CProximitySettingsDialog(final JFrame parent, final ZyGraphViewSettings settings) {
    super(parent, "Proximity Browsing Settings", true);

    Preconditions.checkNotNull(settings, "IE01154: Settings argument can not be null");

    m_settings = settings;

    new CDialogEscaper(this);

    m_parentDepthSlider.setMinorTickSpacing(1);
    m_parentDepthSlider.setPaintTicks(true);
    m_parentDepthSlider.setSnapToTicks(true);
    m_parentDepthSlider.setPaintTrack(true);
    m_parentDepthSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent event) {
        m_parentLabel.setText(((Integer) m_parentDepthSlider.getValue()).toString());
      }
    });

    m_childDepthSlider.setMinorTickSpacing(1);
    m_childDepthSlider.setPaintTicks(true);
    m_childDepthSlider.setSnapToTicks(true);
    m_childDepthSlider.setPaintTrack(true);
    m_childDepthSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent event) {
        m_childLabel.setText(((Integer) m_childDepthSlider.getValue()).toString());
      }
    });

    m_buttonOk.addActionListener(m_actionListener);
    m_buttonCancel.addActionListener(m_actionListener);

    m_checkBoxChildDepth.addItemListener(m_itemListener);
    m_checkBoxParentDepth.addItemListener(m_itemListener);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    synchronizeGUIFromSettings();

    createPanels();

    pack();
    GuiHelper.centerChildToParent(parent, this, true);
  }

  /**
   * Shows a proximity settings dialog.
   * 
   * @param parent Parent window used for the dialog.
   * @param settings Settings object modified in this dialog.
   */
  public static void showDialog(final JFrame parent, final ZyGraphViewSettings settings) {
    final CProximitySettingsDialog dialog = new CProximitySettingsDialog(parent, settings);

    dialog.setVisible(true);
  }

  /**
   * Creates the GUI layout of the dialog.
   */
  private void createPanels() {
    setLayout(new BorderLayout(5, 5));

    final JPanel p_1 = new JPanel();
    p_1.setLayout(new BorderLayout(5, 5));
    p_1.setBorder(new TitledBorder("View Depth"));

    final JPanel p_1_1 = new JPanel();
    p_1_1.setLayout(new BorderLayout(5, 5));
    p_1_1.add(m_checkBoxParentDepth, BorderLayout.WEST);
    p_1_1.add(m_parentLabel, BorderLayout.CENTER);
    p_1_1.add(m_parentDepthSlider, BorderLayout.EAST);

    final JPanel p_1_2 = new JPanel();
    p_1_2.setLayout(new BorderLayout(5, 5));
    p_1_2.add(m_checkBoxChildDepth, BorderLayout.WEST);
    p_1_2.add(m_childLabel, BorderLayout.CENTER);
    p_1_2.add(m_childDepthSlider, BorderLayout.EAST);

    p_1.add(p_1_1, BorderLayout.NORTH);
    p_1.add(p_1_2, BorderLayout.SOUTH);

    final JPanel p_2 = new JPanel();
    p_2.setLayout(new BorderLayout(5, 0));

    final JPanel p_2_1 = new JPanel();
    final JPanel p_2_1_1 = new JPanel();
    p_2_1_1.setLayout(new GridLayout(1, 2, 5, 5));
    p_2_1_1.add(m_buttonOk);
    p_2_1_1.add(m_buttonCancel);
    p_2_1.add(p_2_1_1);
    p_2.add(p_2_1, BorderLayout.EAST);

    add(p_1, BorderLayout.NORTH);
    add(p_2, BorderLayout.SOUTH);
  }

  /**
   * Initializes the GUI with the settings from the settings object.
   */
  private void synchronizeGUIFromSettings() {
    final int parentDepth = m_settings.getProximitySettings().getProximityBrowsingParents();
    final int childDepth = m_settings.getProximitySettings().getProximityBrowsingChildren();

    if (parentDepth == -1) {
      m_parentDepthSlider.setValue(0);
      m_checkBoxParentDepth.setSelected(false);
    } else {
      m_parentDepthSlider.setValue(parentDepth);
      m_checkBoxParentDepth.setSelected(true);
    }

    m_parentLabel.setText(parentDepth == -1 ? "all" + "   " : ((Integer) m_parentDepthSlider
        .getValue()).toString() + "   ");

    m_parentDepthSlider.setEnabled(parentDepth != -1);

    if (childDepth == -1) {
      m_childDepthSlider.setValue(0);
      m_checkBoxChildDepth.setSelected(false);
    } else {
      m_childDepthSlider.setValue(childDepth);
      m_checkBoxChildDepth.setSelected(true);
    }

    m_childLabel.setText(childDepth == -1 ? "all" + "   " : ((Integer) m_childDepthSlider
        .getValue()).toString() + "   ");

    m_childDepthSlider.setEnabled(childDepth != -1);
  }

  /**
   * Handles clicks on the buttons.
   */
  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
      if (actionEvent.getSource() == m_buttonOk) {
        final int childDepth =
            m_checkBoxChildDepth.isSelected() ? m_childDepthSlider.getValue() : -1;
        final int parentDepth =
            m_checkBoxParentDepth.isSelected() ? m_parentDepthSlider.getValue() : -1;

        m_settings.getProximitySettings().setProximityBrowsingParents(parentDepth);
        m_settings.getProximitySettings().setProximityBrowsingChildren(childDepth);
      }

      dispose();
    }
  }

  /**
   * Handles changes in the checkboxes.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      if (event.getSource() == m_checkBoxParentDepth) {
        m_parentLabel.setText(m_checkBoxParentDepth.isSelected() ? ((Integer) m_parentDepthSlider
            .getValue()).toString() : "all");

        m_parentDepthSlider.setEnabled(m_checkBoxParentDepth.isSelected());

      } else if (event.getSource() == m_checkBoxChildDepth) {
        m_childLabel.setText(m_checkBoxChildDepth.isSelected() ? ((Integer) m_childDepthSlider
            .getValue()).toString() : "all");

        m_childDepthSlider.setEnabled(m_checkBoxChildDepth.isSelected());
      }
    }
  }
}
