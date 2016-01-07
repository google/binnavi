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

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteDirectory;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteDrive;
import com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFileSystem;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;

/**
 * Simulates remote file systems.
 */
public final class CRemoteFileSystemView extends FileSystemView {
  /**
   * Default directory of the simulated remote file system.
   */
  private final CRemoteFile m_defaultDirectory;

  /**
   * Remote file system that is simulated.
   */
  private RemoteFileSystem m_fileSystem;

  /**
   * Creates a new remote file system view.
   *
   * @param fileSystem Information about the objects to be simulated.
   */
  public CRemoteFileSystemView(final RemoteFileSystem fileSystem) {
    Preconditions.checkNotNull(fileSystem, "IE01496: File system argument can not be null");

    m_fileSystem = fileSystem;

    m_defaultDirectory = new CRemoteFile(fileSystem.getDirectory().getName(), true);
  }

  @Override
  public File createNewFolder(final File containingDir) {
    return null;
  }

  @Override
  public File getDefaultDirectory() {
    return m_defaultDirectory;
  }

  @Override
  public File[] getFiles(final File dir, final boolean useFileHiding) // NO_UCD
  {
    final CRemoteFile[] files = CollectionHelpers.map(m_fileSystem.getFiles(),
        new ICollectionMapper<com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFile, File>() {
          @Override
          public File map(final com.google.security.zynamics.binnavi.debug.models.remotebrowser.RemoteFile item) {
            return new CRemoteFile(
                m_fileSystem.getDirectory().getName() + "/" + item.getName(), false);
          }
        }).toArray(new CRemoteFile[0]);

    final CRemoteFile[] directories = CollectionHelpers.map(
        m_fileSystem.getDirectories(), new ICollectionMapper<RemoteDirectory, File>() {
          @Override
          public File map(final RemoteDirectory item) {
            return new CRemoteFile(m_fileSystem.getDirectory().getName() + "/" + item.getName()
                + "/", true);
          }
        }).toArray(new CRemoteFile[0]);

    final File[] combined = new File[files.length + directories.length];

    System.arraycopy(files, 0, combined, 0, files.length);
    System.arraycopy(directories, 0, combined, files.length, directories.length);

    return combined;
  }

  @Override
  public File getHomeDirectory() {
    return m_defaultDirectory;
  }

  @Override
  public File getParentDirectory(final File dir) // NO_UCD
  {
    return new CRemoteFile(dir.getParent(), true);
  }

  @Override
  public File[] getRoots() {
    return CollectionHelpers.map(
        m_fileSystem.getDrives(), new ICollectionMapper<RemoteDrive, File>() {
          @Override
          public File map(final RemoteDrive item) {
            return new CRemoteFile(item.getName() + "/", true);
          }
        }).toArray(new CRemoteFile[0]);
  }

  @Override
  public String getSystemDisplayName(final File file) // NO_UCD
  {
    return isDrive(file) ? file.getAbsolutePath() : FileUtils.getFileBasename(file);
  }

  @Override
  public Icon getSystemIcon(final File file) {
    throw new IllegalStateException("IE01132: Not yet implemented");
  }

  @Override
  public String getSystemTypeDescription(final File file) {
    throw new IllegalStateException("IE01133: Not yet implemented");
  }

  @Override
  public boolean isComputerNode(final File dir) {
    return false;
  }

  @Override
  public boolean isDrive(final File dir) {
    return CRemoteBrowserHelpers.isDrive(dir);
  }

  @Override
  public boolean isFileSystem(final File file) {
    throw new IllegalStateException("IE01134: Not yet implemented");
  }

  @Override
  public boolean isFileSystemRoot(final File dir) {
    return false;
  }

  @Override
  public boolean isFloppyDrive(final File dir) {
    return false;
  }

  @Override
  public boolean isHiddenFile(final File file) {
    throw new IllegalStateException("IE01135: Not yet implemented");
  }

  @Override
  public boolean isParent(final File folder, final File file) {
    return false;
  }

  @Override
  public boolean isRoot(final File file) {
    return (file != null) && isDrive(file);
  }

  /**
   * Changes the object that contains the simulated objects.
   *
   * @param fileSystem The new file system object model.
   */
  public void setFileSystem(final RemoteFileSystem fileSystem) {
    Preconditions.checkNotNull(fileSystem, "IE01497: File system argument can not be null");

    m_fileSystem = fileSystem;
  }
}
