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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Dialog that can be used to rename local/global variables.
 */
public class EditVariableDialog extends JDialog {

  /**
   * Field where the user enters the new variable name.
   */
  private final JTextField nameField = new JTextField();
  private boolean okClicked;
  private final Window parent;

  /**
   * Creates a new dialog to rename a variable.
   *
   * @param parent Parent window of the dialog.
   * @param title Title of the dialog.
   * @param variableName The name of an existing variable that is displayed in the dialog.
   */
  private EditVariableDialog(final Window parent, final String title, final String variableName) {
    super(parent, title, ModalityType.APPLICATION_MODAL);
    this.parent = parent;
    setLayout(new BorderLayout());
    new CDialogEscaper(this);

    nameField.setText(variableName);
    nameField.setSelectionStart(0);
    nameField.setSelectionEnd(Integer.MAX_VALUE);

    final JPanel upperPanel = new JPanel(new BorderLayout());
    upperPanel.add(nameField, BorderLayout.NORTH);
    upperPanel.setBorder(new TitledBorder(""));

    final CPanelTwoButtons panel =
        new CPanelTwoButtons(new InternalActionListener(), "OK", "Cancel");

    add(upperPanel, BorderLayout.NORTH);
    add(panel, BorderLayout.SOUTH);

    setSize(300, 100);
    setResizable(false);

    final InputMap windowImap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    windowImap.put(HotKeys.APPLY_HK.getKeyStroke(), "APPLY");
    getRootPane().getActionMap().put("APPLY", CActionProxy.proxy(new ApplyAction()));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  /**
   * Instantiates a dialog that can be used to edit the name of a variable.
   *
   * @param parent The parent window of the dialog.
   * @param existingVariableName The name of the existing variable.
   * @return A new instance of the dialog.
   */
  public static EditVariableDialog CreateEditVariableDialog(
      final JFrame parent, final String existingVariableName) {
    return new EditVariableDialog(parent, "Edit variable name", existingVariableName);
  }

  private boolean validateUserInput() {
    if (nameField.getText().isEmpty()) {
      CMessageBox.showWarning(parent, "Please enter a non-empty variable name.");
      return false;
    } else {
      return true;
    }
  }

  public String getVariableName() {
    return nameField.getText();
  }

  public boolean wasOkClicked() {
    return okClicked;
  }

  // TODO(jannewger): only one of applyAction and InternalActionListener should exist! However,
  // CPanelTwoButtons would need to be refactored to implement this change.
  /**
   * Action class used to saving changes.
   */
  private class ApplyAction extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent event) {
      okClicked = validateUserInput();
      if (okClicked) {
        dispose();
      }
    }
  }

  /**
   * Handles clicks on the dialog buttons.
   */
  private class InternalActionListener extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        okClicked = validateUserInput();
        if (okClicked) {
          dispose();
        }
      } else {
        dispose();
      }
    }
  }
}
