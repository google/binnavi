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

import com.google.security.zynamics.zylib.io.FileUtils;

import java.io.File;


/**
 * Class used to check the completeness of a BinExport installation.
 */
public final class CBinExportInstallationChecker {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBinExportInstallationChecker() {
  }

  /**
   * Determines the installation state of a BinExport installation in the given directory.
   *
   * @param directory The directory to check.
   *
   * @return The installation state of the directory.
   */
  public static InstallationState getState(final File directory) {
    if (!FileUtils.containsDirectory(directory, "plugins")) {
      return InstallationState.InvalidIdaDirectory;
    }

    final File pluginsDirectory = new File(directory.getAbsolutePath() + "/plugins");

    for (final String pluginFile : CBinExportFiles.getPluginFiles()) {
      if (!FileUtils.containsFile(pluginsDirectory, pluginFile)) {
        return InstallationState.NotInstalled;
      }
    }

    return InstallationState.Installed;
  }
}
