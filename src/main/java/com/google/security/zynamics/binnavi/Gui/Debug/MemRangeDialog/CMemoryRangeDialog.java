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
package com.google.security.zynamics.binnavi.Gui.Debug.MemRangeDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;



/**
 * In the memory range dialog the user can enter a range of memory that should be displayed in the
 * memory viewer during a debugging session.
 */
public final class CMemoryRangeDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2434814129494159201L;

  /**
   * Start offset of the memory range.
   */
  private IAddress m_start = null;

  /**
   * End offset of the memory range.
   */
  private IAddress m_bytes = null;

  /**
   * The user enters the start offset of the memory range here.
   */
  private final JFormattedTextField m_startField;

  /**
   * The user enters the end offset of the memory range here.
   */
  private final JFormattedTextField m_endField;

  /**
   * Creates a new memory range dialog.
   * 
   * @param owner The parent frame of the dialog.
   */
  public CMemoryRangeDialog(final JFrame owner) {
    super(owner, "Enter a memory range", true);

    setLayout(new BorderLayout());

    setSize(400, 170);

    final JLabel startLabel = new JLabel("Start Address (Hex)");

    final JLabel endLabel = new JLabel("Number of Bytes (Hex)");

    m_startField = new JFormattedTextField(new CHexFormatter(8));
    m_endField = new JFormattedTextField(new CHexFormatter(8));

    final JPanel labelPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.2;
    constraints.insets = new Insets(5, 5, 0, 0);
    constraints.fill = GridBagConstraints.HORIZONTAL;

    labelPanel.add(startLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.insets = new Insets(5, 0, 0, 0);
    labelPanel.add(m_startField, constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.weightx = 0.2;
    constraints.insets = new Insets(5, 5, 0, 0);

    labelPanel.add(endLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.weightx = 1;
    constraints.insets = new Insets(5, 0, 0, 0);
    labelPanel.add(m_endField, constraints);

    final JPanel topPanel = new JPanel(new BorderLayout());

    final JTextArea area =
        new JTextArea(
            "Please enter a memory range to display. \nBe careful. Displaying invalid memory can crash the device.");
    area.setBorder(new EmptyBorder(0, 5, 0, 0));

    area.setEditable(false);

    topPanel.add(area, BorderLayout.NORTH);
    topPanel.add(labelPanel, BorderLayout.CENTER);

    topPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK)));

    final CPanelTwoButtons buttonPanel =
        new CPanelTwoButtons(new InternalListener(), "OK", "Cancel");

    add(topPanel, BorderLayout.NORTH);
    add(buttonPanel, BorderLayout.SOUTH);

    m_startField.requestFocusInWindow();

    final InputMap windowImap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

    windowImap.put(HotKeys.APPLY_HK.getKeyStroke(), "APPLY");
    getRootPane().getActionMap().put("APPLY", CActionProxy.proxy(new ApplyAction()));

    setLocationRelativeTo(null);
  }

  /**
   * Checks whether the user input is valid and - if it is - closes the dialog.
   */
  private void closeOk() {
    try {
      m_start = new CAddress(new BigInteger(m_startField.getText(), 16));
    } catch (final NumberFormatException e) {
      CMessageBox.showError(CMemoryRangeDialog.this, "You have entered an invalid start address.");

      return;
    }

    try {
      m_bytes = new CAddress(new BigInteger(m_endField.getText(), 16));
    } catch (final NumberFormatException e) {
      CMessageBox.showError(CMemoryRangeDialog.this, "You have entered an invalid end address.");

      return;
    }

    if (m_bytes.toBigInteger().equals(BigInteger.ZERO)) {
      CMessageBox.showError(CMemoryRangeDialog.this, "You have entered an invalid memory range.");

      return;
    }

    dispose();
  }

  /**
   * Returns the number of bytes in the range.
   * 
   * @return The number of bytes in the entered range or -1 if the dialog was canceled.
   */
  public IAddress getBytes() {
    return m_bytes;
  }

  /**
   * Returns the first offset of the entered range.
   * 
   * @return The first offset of the entered range or -1 if the dialog was canceled.
   */
  public IAddress getStart() {
    return m_start;
  }

  /**
   * Action class used to saving changes.
   */
  private class ApplyAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5147201603251255198L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      closeOk();
    }
  }

  /**
   * Validates the input and fills the result variables of the object.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        closeOk();
      } else {
        dispose();
      }
    }
  }
}
