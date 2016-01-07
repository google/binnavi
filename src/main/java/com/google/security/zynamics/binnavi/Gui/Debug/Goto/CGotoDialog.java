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
package com.google.security.zynamics.binnavi.Gui.Debug.Goto;

import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CEvaluationVisitor;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.IMemoryExpressionBinding;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.DebuggerMemoryExpressionParser;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MemoryExpressionElement;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.strings.Commafier;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.antlr.runtime.RecognitionException;



/**
 * Dialog that can be used to go to a specified hexadecimal address.
 */
public final class CGotoDialog extends JDialog {
  // TODO: Dialog is too ugly.

  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 9134316385147292718L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Text field that is used to enter the address.
   */
  private final JTextField offsetField = new JTextField();

  /**
   * Variable bindings used to evaluate expressions entered by the user.
   */
  private final IMemoryExpressionBinding m_bindings;

  /**
   * Evaluated value entered by the user or null.
   */
  private IAddress m_value = null;

  /**
   * Provides the available memory sections.
   */
  private final MemoryMap m_memoryMap;

  /**
   * Displays a Goto Address dialog.
   *
   * @param parent The parent frame of the dialog.
   * @param memoryMap Provides the available memory sections.
   * @param bindings Variable bindings used to evaluate expressions entered by the user.
   * @param address The address that is initially put into the goto field.
   */
  public CGotoDialog(final JFrame parent, final MemoryMap memoryMap,
      final IMemoryExpressionBinding bindings, final IAddress address) {
    super(parent, "Goto Address", ModalityType.APPLICATION_MODAL);

    m_memoryMap = memoryMap;
    m_bindings = bindings;

    if (address != null) {
      offsetField.setText("0x" + address.toHexString());
    }

    setResizable(false);

    new CDialogEscaper(this);

    m_parent = parent;

    setLayout(new BorderLayout());

    // Create the GUI
    final JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BorderLayout());

    inputPanel.setBorder(new TitledBorder(""));

    final JPanel fooPanel = new JPanel(new BorderLayout());

    final JLabel label = new JLabel("Address" + ": ");

    fooPanel.add(label, BorderLayout.WEST);

    fooPanel.add(offsetField, BorderLayout.CENTER);

    inputPanel.add(fooPanel, BorderLayout.NORTH);

    final CPanelTwoButtons panel = new CPanelTwoButtons(new InternalListener(), "OK", "Cancel");

    getContentPane().add(inputPanel, BorderLayout.NORTH);
    getContentPane().add(panel, BorderLayout.SOUTH);

    getRootPane().setDefaultButton(panel.getFirstButton());

    pack();

    // Set the size and center the dialog.
    setSize(300, 100);
    setLocationRelativeTo(null);
  }

  /**
   * Does everything that is necessary if the user clicked the Cancel button.
   */
  private void dialogClosedCancel() {
    dispose();
  }

  /**
   * Does everything that is necessary if the user clicked the OK button.
   */
  private void dialogClosedOk() {
    final String input = offsetField.getText();

    if (!"".equals(input)) {
      try {
        final MemoryExpressionElement expression = DebuggerMemoryExpressionParser.parse(input);

        final CEvaluationVisitor visitor = new CEvaluationVisitor(m_bindings);

        expression.visit(visitor);

        final BigInteger value = visitor.getValue(expression);

        if (value == null) {
          final String errors = Commafier.commafy(visitor.getErrorMessages(), "\n");

          CMessageBox.showError(m_parent,
              String.format("The expression you entered could not be evaluated:\n %s", errors));
        } else if (value.compareTo(BigInteger.ZERO) == -1 || value.toString(16).length() > 8) {
          // Negative or too long
          CMessageBox.showError(m_parent, String.format(
              "The expression you entered evaluates to the invalid memory address %s.",
              value.toString(16).toUpperCase()));
        } else {
          final MemorySection section = ProcessHelpers.getSectionWith(
              m_memoryMap, new CAddress(value.longValue()));

          if (section == null) {
            CMessageBox.showError(m_parent, String.format(
                "There is no memory at address %s.", value.toString(16).toUpperCase()));
          } else {
            m_value = new CAddress(value.longValue());

            dispose();
          }
        }
      } catch (final RecognitionException exception) {
        CMessageBox.showError(m_parent, "Invalid expression string.");
      }
    }
  }

  /**
   * Returns the evaluated value of the memory expression entered by the user. If the expression is
   * invalid or the user cancelled the dialog, this value is null.
   *
   * @return The evaluated memory expression value entered by the user.
   */
  public IAddress getValue() {
    return m_value;
  }

  /**
   * Action handler of the OK and Cancel buttons.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        dialogClosedOk();
      } else {
        dialogClosedCancel();
      }
    }
  }

}
