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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;



import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



/**
 * Dialog for displaying the content of the BinNavi log file.
 */
public final class CLogFileDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1953001266796213165L;

  /**
   * Creates a new dialog object.
   * 
   * @param parent Parent window of the dialog.
   * @param content Log file content to be displayed.
   */
  private CLogFileDialog(final Window parent, final String content) {
    super(parent, "Log file content", ModalityType.APPLICATION_MODAL);

    new CDialogEscaper(this);

    CIconInitializer.initializeWindowIcons(this);

    setLayout(new BorderLayout());

    final JTextArea area = new JTextArea(content);
    area.setEditable(false);

    add(new JScrollPane(area));

    final JPanel buttonPanel = new JPanel(new BorderLayout());

    final JButton closeButton = new JButton(new CCloseAction());

    buttonPanel.add(closeButton);

    add(buttonPanel, BorderLayout.SOUTH);

    getRootPane().setDefaultButton(closeButton);

    setSize(800, 600);
  }

  /**
   * Shows a dialog that displays the content of the BinNavi log file.
   * 
   * @param parent Parent window of the dialog.
   * @param content Log file content to be displayed.
   */
  public static void show(final Window parent, final String content) {
    final CLogFileDialog dialog = new CLogFileDialog(parent, content);

    GuiHelper.centerChildToParent(parent, dialog, true);

    dialog.setVisible(true);
  }

  /**
   * Closes the dialog.
   */
  private class CCloseAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1234947133301037194L;

    /**
     * Creates a new action object.
     */
    public CCloseAction() {
      super("Close");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      dispose();
    }
  }
}
