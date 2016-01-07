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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.Loader;

import java.io.File;

import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.CRemoteSelectionDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListFilesReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListProcessesReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessDescription;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFileSystem;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * This class should be used to display a remote file browsing dialog.
 */
public final class CRemoteFileBrowserLoader {
  /**
   * Parent window of the remote browsing dialog.
   */
  private final JFrame m_parent;

  /**
   * Debugger that provides information about the remote file system.
   */
  private final IDebugger m_debugger;

  /**
   * Thread that controls the existence of the progress dialog.
   */
  private final LoaderThread m_loaderThread = new LoaderThread();

  /**
   * Listener that collects file browsing information from the remote debugger.
   */
  private final InternalDebuggerListener m_listener = new InternalDebuggerListener();

  /**
   * ID of the file system request sent to get the initial file browsing information.
   */
  private int m_fileSystemRequest = 0;

  /**
   * Message ID of the Request Process Information message sent to the debug client.
   */
  private int m_processRequest = 0;

  /**
   * Number of messages this class is expecting replies for from the debug client. Initially the
   * start value is 2 because we are waiting for process information and file system information.
   */
  private int m_waitingFor = 2;

  /**
   * The initial file browsing information is written to this variable once it arrives.
   */
  private RemoteFileSystem m_fileSystem = null;

  /**
   * Process list of the running processes to choose from.
   */
  private ProcessList m_processList = null;

  /**
   * Flag that indicates whether a target was selected.
   */
  private boolean m_selectedTarget = false;

  /**
   * Creates a new remote browsing loader.
   *
   * @param parent Parent window of the remote browsing dialog.
   * @param debugger Debugger that provides information about the remote file system.
   */
  public CRemoteFileBrowserLoader(final JFrame parent, final IDebugger debugger) {
    m_parent = Preconditions.checkNotNull(parent, "IE01498: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01499: Debugger argument can not be null");
    m_debugger.addListener(m_listener);
  }

  /**
   * Returns whether the loader is still waiting.
   *
   * @return True, if the loader is still waiting. False, otherwise.
   */
  private boolean keepWaiting() {
    return m_waitingFor != 0;
  }

  /**
   * Shows the remote browsing dialog.
   */
  private void showRemoteBrowser() {
    final CRemoteSelectionDialog dlg =
        CRemoteSelectionDialog.show(m_parent, m_debugger, m_fileSystem, m_processList);
    final File selectedFile = dlg.getSelectedFile();
    final ProcessDescription selectedProcess = dlg.getSelectedProcess();
    if (selectedFile != null) {
      try {
        m_debugger.selectFile(selectedFile.getAbsolutePath());
        m_selectedTarget = true;
      } catch (final DebugExceptionWrapper e) {
        CUtilityFunctions.logException(m_loaderThread.getException());
        final String message = "E00039: " + "Could not send target file request";
        final String description = CUtilityFunctions.createDescription(
            "BinNavi could not send the target file request to the debug client.", new String[] {
                "The connection to the debug client was closed before"
                + " the request could be sent."},
            new String[] {"There is still no debug target selected."});
        NaviErrorDialog.show(m_parent, message, description, m_loaderThread.getException());
      }
    } else if (selectedProcess != null) {
      try {
        m_debugger.selectProcess(selectedProcess.getPID());
        m_selectedTarget = true;
      } catch (final DebugExceptionWrapper e) {
        CUtilityFunctions.logException(m_loaderThread.getException());

        final String message = "E00040: " + "Could not send target process request";
        final String description = CUtilityFunctions.createDescription(
            "BinNavi could not send the target process request to the debug client.", new String[] {
                "The connection to the debug client was closed before the"
                + "request could be sent."},
            new String[] {"There is still not debug target selected."});
        NaviErrorDialog.show(m_parent, message, description, m_loaderThread.getException());
      }
    }
  }

  /**
   * Loads information about the remote file system from the debug client and shows a remote
   * browsing dialog when the information arrives.
   *
   * @return True, if a debug target was selected. False, otherwise.
   */
  public boolean load() {
    CProgressDialog.showEndless(m_parent, "Loading available drives", m_loaderThread);
    m_debugger.removeListener(m_listener);
    if (m_loaderThread.getException() != null) {
      CUtilityFunctions.logException(m_loaderThread.getException());
      final String message = "E00038: " + "Could not request remote file system information";
      final String description = CUtilityFunctions.createDescription(
          "BinNavi could not retrieve information about the remote file system.", new String[] {
              "The connection to the debug client was closed before the request could be sent."},
          new String[] {"You can not select a target file from the remote system."});

      NaviErrorDialog.show(m_parent, message, description, m_loaderThread.getException());
    } else if ((m_fileSystem != null) && (m_processList != null)) {
      showRemoteBrowser();
    }
    return m_selectedTarget;
  }

  /**
   * Listener that collects file browsing information from the remote debugger.
   */
  private class InternalDebuggerListener extends DebugEventListenerAdapter {
    @Override
    public void debuggerClosed(final int code) {
      m_waitingFor = 0;
    }

    @Override
    public void receivedReply(final ListFilesReply reply) {
      if (m_fileSystemRequest == reply.getId()) {
        m_fileSystem = reply.getFileSystem();
        m_waitingFor--;
      }
    }

    @Override
    public void receivedReply(final ListProcessesReply reply) {
      if (m_processRequest == reply.getId()) {
        m_processList = reply.getProcessList();
        m_waitingFor--;
      }
    }
  }

  /**
   * Sends a file system information request to the debug client and waits until the information has
   * arrived.
   */
  private class LoaderThread extends CEndlessHelperThread {
    @Override
    protected void runExpensiveCommand() throws Exception {
      m_fileSystemRequest = m_debugger.requestFileSystem();
      m_processRequest = m_debugger.requestProcessList();
      while (keepWaiting()) {
        Thread.sleep(100);
      }
    }
  }
}
