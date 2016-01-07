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
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Simulates remote files when displayed in the file selection dialog.
 */
public final class CRemoteFile extends File {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1913040405965661147L;

  /**
   * Path to the file on the remote system.
   */
  private final String m_pathname;

  /**
   * Flag that indicates whether the remote file is a directory.
   */
  private final boolean m_isDirectory;

  /**
   * Creates a new remote file object.
   *
   * @param pathname Path to the file on the remote system.
   * @param isDirectory Flag that indicates whether the remote file is a directory.
   */
  public CRemoteFile(final String pathname, final boolean isDirectory) {
    super(pathname);

    m_pathname = pathname;
    m_isDirectory = isDirectory;
  }

  @Override
  public boolean canExecute() {
    throw new IllegalStateException("IE01127: Not yet implemented");
  }

  @Override
  public boolean canRead() // NO_UCD
  {
    throw new IllegalStateException("IE01128: Not yet implemented");
  }

  @Override
  public boolean canWrite() // NO_UCD
  {
    return false; // Disables the option to rename files from the dialog
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public File getAbsoluteFile() {
    return new CRemoteFile(m_pathname, m_isDirectory);
  }

  @Override
  public String getAbsolutePath() {
    return m_pathname;
  }

  @Override
  public File getCanonicalFile() {
    return new CRemoteFile(m_pathname, m_isDirectory);
  }

  @Override
  public File getParentFile() {
    final String parent = this.getParent();

    if (parent == null) {
      return null;
    }

    return new CRemoteFile(parent, true);
  }

  @Override
  public String getPath() {
    return m_pathname;
  }

  @Override
  public boolean isDirectory() {
    return m_isDirectory;
  }

  @Override
  public long lastModified() // NO_UCD
  {
    return 0;
  }

  @Override
  public long length() {
    return 0;
  }

  @Override
  public File[] listFiles() {
    throw new IllegalStateException("IE01129: Not yet implemented");
  }

  @Override
  public File[] listFiles(final FileFilter filter) {
    throw new IllegalStateException("IE01130: Not yet implemented");
  }

  @Override
  public File[] listFiles(final FilenameFilter filter) {
    throw new IllegalStateException("IE01131: Not yet implemented");
  }

  @Override
  public boolean renameTo(final File dest) // NO_UCD
  {
    return false;
  }
}
