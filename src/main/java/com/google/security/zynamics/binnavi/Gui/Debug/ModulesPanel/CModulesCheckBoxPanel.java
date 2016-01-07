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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Defines a panel which contains a single checkbox
 */
public class CModulesCheckBoxPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2562913064983679707L;

  /**
   * The corresponding modules table model.
   */
  private final CModulesTableModel m_modulesTableModel;

  /**
   * Creates the checkbox panel to control if full paths should be shown in the modules table.
   *
   * @param modulesTableModel The corresponding modules table model.
   */
  public CModulesCheckBoxPanel(final CModulesTableModel modulesTableModel) {
    super(new BorderLayout());

    final JCheckBox checkBox = new JCheckBox("Show full module paths");
    checkBox.addItemListener(new InternalCheckboxListener());

    add(checkBox);

    m_modulesTableModel = modulesTableModel;
  }

  /**
   * Inner class to trigger model update when the user changes the state of the checkbox
   */
  private class InternalCheckboxListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      m_modulesTableModel.setUseFullModulePaths(event.getStateChange() == ItemEvent.SELECTED);
    }
  }
}
