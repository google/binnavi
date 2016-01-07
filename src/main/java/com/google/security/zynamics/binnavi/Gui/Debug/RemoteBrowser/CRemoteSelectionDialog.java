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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser.CRemoteFileBrowser;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.ProcessBrowser.CProcessListPanel;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessDescription;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFileSystem;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Dialog that is used to let the user select between a remote file and a remote process for
 * debugging.
 */
public final class CRemoteSelectionDialog extends JDialog {
  /**
   * Tabbed pane where either a file list or a process list is shown.
   */
  private final JTabbedPane m_tabbedPane = new JTabbedPane();

  /**
   * Shows remote files.
   */
  private final CRemoteFileBrowser m_browser;

  /**
   * Shows remote processes.
   */
  private final CProcessListPanel m_processPanel;

  /**
   * Contains the selected file after the user clicked the OK button.
   */
  private File m_selectedFile;

  /**
   * Contains the selected process after the user clicked the OK button.
   */
  private ProcessDescription m_selectedProcess;

  /**
   * Creates a new remote selection dialog.
   *
   * @param parent Parent of the dialog.
   * @param debugger Remote debugger that provides the information to display.
   * @param fileList Information about the remote file system.
   * @param processList Information about the remote process list.
   */
  private CRemoteSelectionDialog(final JFrame parent, final IDebugger debugger,
      final RemoteFileSystem fileList, final ProcessList processList) {
    super(parent, "Choose a file or a process", ModalityType.APPLICATION_MODAL);
    Preconditions.checkNotNull(parent, "IE01489: Parent argument can not be null");
    Preconditions.checkNotNull(debugger, "IE01490: Debugger argument can not be null");
    Preconditions.checkNotNull(fileList, "IE01491: File list argument can not be null");
    Preconditions.checkNotNull(processList, "IE01492: Process list argument can not be null");
    setLayout(new BorderLayout());
    new CDialogEscaper(this);
    m_browser = new CRemoteFileBrowser(this, debugger, fileList) {
      @Override
      public void approveSelection() {
        m_selectedFile = m_browser.getSelectedFile();
        CRemoteSelectionDialog.this.dispose();
      }

      @Override
      public void cancelSelection() {
        CRemoteSelectionDialog.this.dispose();
      }
    };

    m_browser.setControlButtonsAreShown(false);
    // Inner panel is necessary to set background of the JFileChooser properly.
    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.add(m_browser);
    m_tabbedPane.addTab("File", innerPanel);
    m_processPanel = new CProcessListPanel(processList);
    m_tabbedPane.addTab("Process", m_processPanel);
    add(m_tabbedPane);
    add(new CPanelTwoButtons(new InternalListener(), "OK", "Cancel"), BorderLayout.SOUTH);
    pack();
  }

  /**
   * Helper function to show this dialog.
   *
   * @param parent Parent of the dialog.
   * @param debugger Remote debugger that provides the information to display.
   * @param system The simulated remote file system.
   * @param list List of processes running on the target system.
   *
   * @return The dialog that was shown.
   */
  public static CRemoteSelectionDialog show(final JFrame parent, final IDebugger debugger,
      final RemoteFileSystem system, final ProcessList list) {
    final CRemoteSelectionDialog dlg = new CRemoteSelectionDialog(parent, debugger, system, list);
    GuiHelper.centerChildToParent(parent, dlg, true);
    dlg.setVisible(true);
    return dlg;
  }

  /**
   * Returns the selected file.
   *
   * @return The selected file.
   */
  public File getSelectedFile() {
    return m_selectedFile;
  }

  /**
   * Returns the selected process.
   *
   * @return The selected process.
   */
  public ProcessDescription getSelectedProcess() {
    return m_selectedProcess;
  }

  /**
   * Action handler for the dialog buttons.
   */
  private class InternalListener extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent event) {
      if ("OK".equals(event.getActionCommand())) {
        if (m_tabbedPane.getSelectedIndex() == 0) {
          m_selectedFile = m_browser.getSelectedFile();
        } else {
          m_selectedProcess = m_processPanel.getSelectedProcess();
        }
      }
      dispose();
    }
  }
}
