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



import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.common.config.ConfigHelper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;



/**
 * Panel for displaying the name of the BinNavi log file and a button for opening the log file.
 */
public final class CLogFilePanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 205929188653627132L;

  /**
   * Creates a new panel object.
   */
  public CLogFilePanel() {
    super(new BorderLayout());

    final JTextField fileLabel = new JTextField(ConfigHelper.getConfigurationDirectory(
        Constants.COMPANY_NAME, Constants.PROJECT_NAME));
    fileLabel.setEditable(false);
    fileLabel.setCaretPosition(0);
    add(fileLabel);
    final JButton button = new JButton(new COpenAction());
    add(button, BorderLayout.EAST);
  }

  /**
   * Shows the log file dialog.
   */
  private void showLogfileDialog() {
    final JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(ConfigHelper.getConfigurationDirectory(
        Constants.COMPANY_NAME, Constants.PROJECT_NAME)));
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        CLogFileDialog.show(SwingUtilities.getWindowAncestor(this),
            FileUtils.readTextfile(chooser.getSelectedFile().getAbsolutePath()));
      } catch (final IOException e) {
        CMessageBox.showInformation(SwingUtilities.getWindowAncestor(this),
            "The log file could not be read.");
      }
    }
  }

  /**
   * Action for displaying the content of the log file.
   */
  private final class COpenAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 7860371000019518677L;

    /**
     * Creates a new action object.
     */
    public COpenAction() {
      super("Open");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      showLogfileDialog();
    }
  }
}
