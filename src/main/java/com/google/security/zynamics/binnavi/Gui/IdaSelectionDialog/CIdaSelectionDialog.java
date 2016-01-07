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
package com.google.security.zynamics.binnavi.Gui.IdaSelectionDialog;

import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


/**
 * Dialog used to prompt the user for the IDA Pro executable he wants to use for exporting.
 */
public final class CIdaSelectionDialog extends JDialog {
  /**
   * Used to display the state of the BinExport installation.
   */
  private final JLabel m_cppStateLabel = new JLabel("");

  /**
   * Used to install BinExport.
   */
  private final JButton m_cppButton = new JButton(new CppInstallerAction());

  /**
   * File chooser component for choosing the IDA Pro executable.
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6973685
   */
  private final JFileChooser m_chooser = new JFileChooser() {
    @Override
    public void approveSelection() {
      approve();
    }

    @Override
    public void cancelSelection() {
      cancel();
    }
  };

  /**
   * The IDA Pro executable file selected by the user.
   */
  private File m_selectedFile = null;

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param initialDirectory
   */
  private CIdaSelectionDialog(final Window parent, final String initialDirectory) {
    super(parent, "IDA Pro Selection", ModalityType.APPLICATION_MODAL);

    new CDialogEscaper(this);

    setLayout(new BorderLayout());
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    m_chooser.setControlButtonsAreShown(false);
    m_chooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY,
        new DirectoryChangedListener());
    m_chooser.setCurrentDirectory(new File(initialDirectory));
    m_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    m_chooser.setAcceptAllFileFilterUsed(false);

    add(m_chooser);

    final JPanel lowerPanel = new JPanel(new BorderLayout());

    final CPanelTwoButtons buttonPanel =
        new CPanelTwoButtons(new InternalActionListener(), "OK", "Cancel");

    lowerPanel.add(buttonPanel, BorderLayout.EAST);

    final JPanel cppPanel = new JPanel(new GridLayout(1, 2));
    cppPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel install = new JPanel();
    install.add(m_cppButton);
    final JLabel exporterLabel = new JLabel("BinExport IDA plugin: ");
    exporterLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    cppPanel.add(exporterLabel);
    cppPanel.add(m_cppStateLabel);
    cppPanel.add(install);

    lowerPanel.add(cppPanel, BorderLayout.CENTER);
    add(lowerPanel, BorderLayout.SOUTH);

    updateDialog(m_chooser.getCurrentDirectory());
    pack();
  }

  /**
   * Turns an installation state value into a printable string.
   *
   * @param state The state to convert.
   *
   * @return The printable string that represents the given state.
   */
  private static String getText(final InstallationState state) {
    switch (state) {
      case Installed:
        return "Installed";
      case InvalidIdaDirectory:
        return "Invalid IDA Directory";
      case NotInstalled:
        return "Not Installed";
      default:
        throw new IllegalStateException("IE00683: Unknown state");
    }
  }

  /**
   * Shows the IDA Pro selection dialog.
   *
   * @param parent Parent window used for dialogs.
   * @param initialDirectory The directory to select by default.
   *
   * @return The created dialog object.
   */
  public static CIdaSelectionDialog show(final Window parent, final String initialDirectory) {
    final CIdaSelectionDialog dlg = new CIdaSelectionDialog(parent, initialDirectory);

    GuiHelper.centerChildToParent(parent, dlg, true);
    dlg.setVisible(true);

    return dlg;
  }

  /**
   * Handles file selection.
   */
  private void approve() {
    if (m_chooser.getCurrentDirectory() == null) {
      return;
    }

    final ConfigManager configFile = ConfigManager.instance();

    if (InstallationState.Installed.equals(
        CBinExportInstallationChecker.getState(m_chooser.getCurrentDirectory()))
        && configFile.getGeneralSettings()
            .getIdaDirectory().equals(m_chooser.getCurrentDirectory())) {
      CMessageBox.showInformation(this, "Settings unchanged.");
      dispose();
      return;
    }

    final InstallationState cppState =
        CBinExportInstallationChecker.getState(m_chooser.getSelectedFile());
    if (cppState != InstallationState.Installed) {
      CMessageBox.showError(this, "Please install the BinExport IDA plugin.");
      return;
    }

    m_selectedFile = m_chooser.getSelectedFile();

    dispose();
  }

  /**
   * Handles dialog canceling.
   */
  private void cancel() {
    dispose();
  }

  /**
   * Updates the components of the dialog after the user changed the current directory.
   *
   * @param directory The new current directory.
   */
  private void updateDialog(final File directory) {
    final InstallationState cppState = CBinExportInstallationChecker.getState(directory);

    m_cppStateLabel.setText(getText(cppState));
    m_cppButton.setEnabled(cppState == InstallationState.NotInstalled);
  }

  /**
   * Returns the file selected by the user. This value can be null.
   *
   * @return The file selected by the user or null.
   */
  public File getSelectedFile() {
    return m_selectedFile;
  }

  /**
   * Handler for the Install BinExport button.
   */
  private class CppInstallerAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -349623644472351194L;

    /**
     * Creates a new action object.
     */
    public CppInstallerAction() {
      super("Install");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CBinExportInstaller.install(CIdaSelectionDialog.this, m_chooser.getCurrentDirectory());

      updateDialog(m_chooser.getCurrentDirectory());
    }
  }

  /**
   * Updates the dialog on changing directory selections.
   */
  private class DirectoryChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      updateDialog(m_chooser.getCurrentDirectory());
    }
  }

  /**
   * Handles clicks on the OK and Cancel buttons.
   */
  private final class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        approve();
      } else {
        cancel();
      }
    }
  }
}
