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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.Loader.CLoaderThread;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFileSystem;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * Special file chooser that allows the user to browse remote file systems.
 */
public class CRemoteFileBrowser extends JFileChooser {
  private final Window parent;

  /**
   * Debugger that provides information about the remote file system.
   */
  private final IDebugger debugger;

  /**
   * Creates a new remote file browser.
   *
   * @param parent Parent window of the dialog.
   * @param debugger Debugger that provides information about the remote file system.
   * @param fileSystem Remote file system object that contains information about the remote file
   *        system.
   */
  public CRemoteFileBrowser(final Window parent, final IDebugger debugger,
      final RemoteFileSystem fileSystem) {
    super(new CRemoteFileSystemView(fileSystem));
    this.parent = Preconditions.checkNotNull(parent, "IE01494: Parent argument can not be null");
    this.debugger =
        Preconditions.checkNotNull(debugger, "IE01495: Debugger argument can not be null");

    setFileView(new CRemoteFileView());
    final String lastDir = ConfigManager.instance().getGeneralSettings().getLastDirectory();
    if (!lastDir.isEmpty()) {
      setCurrentDirectory(new CRemoteFile(lastDir, true /* is directory */));
    }
  }

  @Override
  public final void setCurrentDirectory(final File directory) {
    // Whenever we are switching to a new directory, we need to load the new information
    // from the debug client.
    if (debugger != null && getFileSystemView() != null && directory != null
        && !getCurrentDirectory().equals(directory)) {
      final String normalizedDirectory = directory.getAbsolutePath();

      final CLoaderThread thread = new CLoaderThread(this, debugger, normalizedDirectory);

      CProgressDialog.showEndless(parent,
          String.format("Loading content of directory '%s'", normalizedDirectory), thread);
      ConfigManager.instance().getGeneralSettings().setLastDirectory(normalizedDirectory);
    }
    super.setCurrentDirectory(directory);
  }
}
