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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;


/**
 * Panel used to jump to offsets in the hex view that shows module data.
 */
public final class CGotoPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7910138367100407156L;

  /**
   * Hex view where the module data is shown.
   */
  private final JHexView m_hexView;

  /**
   * Address field where the user can enter the address to jump to.
   */
  private final JFormattedTextField m_addressField = new JFormattedTextField(new CHexFormatter(8)) {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5688447512051351235L;

    @Override
    protected void processFocusEvent(final FocusEvent event) {
      super.processFocusEvent(event);

      if (event.getID() == FocusEvent.FOCUS_GAINED) {
        selectAll();
      }
    }
  };

  /**
   * Creates a new panel object.
   * 
   * @param hexView Hex view where the module data is shown.
   */
  public CGotoPanel(final JHexView hexView) {
    super(new BorderLayout());

    m_hexView = hexView;

    setBorder(new TitledBorder("Goto Offset"));

    add(m_addressField);

    m_addressField.addActionListener(new InternalActionListener());

    final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(HotKeys.GOTO_HK.getKeyStroke(), "focusAction");

    final ActionMap actionMap = getActionMap();
    actionMap.put("focusAction", new AbstractAction() {
      private static final long serialVersionUID = -5995494363263076534L;

      @Override
      public void actionPerformed(final ActionEvent event) {
        m_addressField.requestFocusInWindow();
      }
    });
  }

  /**
   * Processes input into the address field.
   */
  private class InternalActionListener extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2317976040331050206L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      try {
        final int offset = Integer.valueOf(m_addressField.getText(), 16);

        if (offset > m_hexView.getData().getDataLength() + m_hexView.getBaseAddress()
            || offset < m_hexView.getBaseAddress()) {
          m_addressField.setBackground(Color.RED);
        } else {
          // if (offset <= m_hexView.getData().getDataLength()) {
          m_hexView.gotoOffset(offset);
          m_hexView.requestFocusInWindow();
          m_addressField.setBackground(Color.WHITE);
        } // else {
        // }
      } catch (final NumberFormatException exception) {
        m_addressField.setBackground(Color.RED);
      }
    }
  }
}
