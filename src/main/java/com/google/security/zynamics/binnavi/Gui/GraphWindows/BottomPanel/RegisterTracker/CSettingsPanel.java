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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Panel where the user can configure register tracking options.
 */
public final class CSettingsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8989462339034964836L;

  /**
   * Checkbox for toggling function call behavior.
   */
  private final JCheckBox m_clearRegisterCheckbox = new JCheckBox(
      "Function calls clear all registers");

  /**
   * Input field where the registers cleared by a function call can be added.
   */
  private final JTextArea m_clearRegisterField = new JTextArea();

  /**
   * Creates a new panel object.
   */
  public CSettingsPanel() {
    super(new BorderLayout());

    m_clearRegisterCheckbox.setSelected(true);

    m_clearRegisterField.setBorder(new LineBorder(Color.BLACK));

    updateRegisterField();

    m_clearRegisterCheckbox.addItemListener(new InternalItemListener());

    add(m_clearRegisterCheckbox, BorderLayout.NORTH);
    add(m_clearRegisterField, BorderLayout.CENTER);

    setBorder(new EmptyBorder(5, 5, 5, 5));
  }

  /**
   * Updates the register field depending on the selected tracking options.
   */
  private void updateRegisterField() {
    if (m_clearRegisterCheckbox.isSelected()) {
      m_clearRegisterField.setEditable(false);
      m_clearRegisterField.setText("Enter a list of whitespace-separated registers here");
      m_clearRegisterField.setBackground(Color.LIGHT_GRAY);
    } else {
      m_clearRegisterField.setEditable(true);
      m_clearRegisterField.setText("");
      m_clearRegisterField.setBackground(Color.WHITE);
    }
  }

  /**
   * Returns whether function calls clear all registers.
   * 
   * @return True, if function calls should clear all registers. False, otherwise.
   */
  public boolean doClearAllRegisters() {
    return m_clearRegisterCheckbox.isSelected();
  }

  /**
   * Returns the list of registers to be cleared on function calls.
   * 
   * @return A list of registers.
   */
  public Set<String> getClearedRegisters() {
    final Set<String> clearedRegisters = new HashSet<String>();

    final String[] parts = m_clearRegisterField.getText().split(" ");

    for (final String part : parts) {
      clearedRegisters.add(part);
    }

    return clearedRegisters;
  }

  /**
   * Listens on the check box and updates the GUI accordingly.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      updateRegisterField();
    }
  }
}
