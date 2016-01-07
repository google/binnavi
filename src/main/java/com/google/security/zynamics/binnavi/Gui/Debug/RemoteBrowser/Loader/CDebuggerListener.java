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

import javax.swing.SwingUtilities;

import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser.CRemoteFileBrowser;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser.CRemoteFileSystemView;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListFilesReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;


/**
 * This class updates the simulated remote file system used for selecting a target file by the debug
 * client. Whenever new information arrives from the debug client, the new information is put into
 * the simulated filesystem.
 */
public final class CDebuggerListener extends DebugEventListenerAdapter {
  /**
   * The browser that displays the simulated remote filesystem.
   */
  private final CRemoteFileBrowser m_browser;

  /**
   * The loader thread that displays a progress dialog while waiting for new messages from the debug
   * client.
   */
  private final ILoaderThread m_thread;

  /**
   * Creates a new debugger listener object.
   *
   * @param browser The browser that displays the simulated remote filesystem.
   * @param thread The loader thread that displays a progress dialog while waiting for new messages
   *        from the debug client.
   */
  public CDebuggerListener(final CRemoteFileBrowser browser, final ILoaderThread thread) {
    m_browser = browser;
    m_thread = thread;
  }

  @Override
  public void receivedReply(final ListFilesReply reply) {
    ((CRemoteFileSystemView) m_browser.getFileSystemView()).setFileSystem(reply.getFileSystem());

    m_thread.finished();

    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
        m_browser.updateUI();
      }
    });
  }
}
