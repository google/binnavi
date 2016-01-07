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

import javax.swing.filechooser.FileView;

/**
 * Used to customize how file names are shown in the remote file browser.
 */
public final class CRemoteFileView extends FileView {
  @Override
  public String getName(final File file) {
    // Show full names for drives and only unqualified names for directories and files

    return CRemoteBrowserHelpers.isDrive(file) ? file.getAbsolutePath() : file.getName();
  }
}
