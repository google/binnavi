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
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Dialog that can be used to edit local variables.
 */
public class CDialogEditVariable extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3146437182881360087L;

  /**
   * Field where the user enters the new variable name.
   */
  private final JTextField m_nameField = new JTextField();

  /**
   * Name name of the variable. This remains null if the user cancels the dialog.
   */
  private String m_newName = null;

  /**
   * Creates a new dialog.
   *
   * @param parent Parent window of the dialog.
   * @param title Title of the dialog.
   * @param variable Variable to edit.
   */
  private CDialogEditVariable(final Window parent, final String title, final String variable) {
    super(parent, title, ModalityType.APPLICATION_MODAL);
    setLayout(new BorderLayout());
    new CDialogEscaper(this);

    m_nameField.setText(variable);
    m_nameField.setSelectionStart(0);
    m_nameField.setSelectionEnd(Integer.MAX_VALUE);

    final JPanel upperPanel = new JPanel(new BorderLayout());

    upperPanel.add(m_nameField, BorderLayout.NORTH);
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
  }

  /**
   * Sets the new variable name.
   */
  private void updateName() {
    m_newName = m_nameField.getText().equalsIgnoreCase("") ? null : m_nameField.getText();
  }

  /**
   * Returns the new variable name.
   *
   * @return The new variable name.
   */
  public String getNewName() {
    return m_newName;
  }

  /**
   * Action class used to saving changes.
   */
  private class ApplyAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 7952057201093825520L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      updateName();

      dispose();
    }
  }

  /**
   * Handles clicks on the dialog buttons.
   */
  private class InternalActionListener extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 4375053623383005657L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        updateName();
      }
      dispose();
    }
  }
}
