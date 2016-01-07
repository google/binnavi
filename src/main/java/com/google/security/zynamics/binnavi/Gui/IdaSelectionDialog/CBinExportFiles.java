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

import com.google.security.zynamics.zylib.system.SystemHelpers;

/**
 * This class contains a list of files which are necessary for checking the completeness of a
 * BinExport installation.
 */
public final class CBinExportFiles {
  /**
   * Files to put into the plugins directory of IDA Pro on Windows.
   */
  private static final String[] PLUGIN_FILES_WINDOWS =
      new String[] {"zynamics_binexport_8.plw", "zynamics_binexport_8.p64"};

  /**
   * Files to put into the plugins directory of IDA Pro on Linux.
   */
  private static final String[] PLUGIN_FILES_LINUX =
      new String[] {"zynamics_binexport_8.plx", "zynamics_binexport_8.plx64"};

  /**
   * Files to put into the plugins directory of IDA Pro on MacOSX.
   */
  private static final String[] PLUGIN_FILES_MACOSX =
      new String[] {"zynamics_binexport_8.pmc", "zynamics_binexport_8.pmc64"};

  /**
   * You are not supposed to instantiate this class.
   */
  private CBinExportFiles() {
  }

  /**
   * Returns the file names of the files to be copied to the IDA Pro plugins directory.
   * 
   * @return A list of file names.
   */
  public static String[] getPluginFiles() {
    if (SystemHelpers.isRunningWindows()) {
      return PLUGIN_FILES_WINDOWS.clone();
    } else if (SystemHelpers.isRunningLinux()) {
      return PLUGIN_FILES_LINUX.clone();
    } else {
      return PLUGIN_FILES_MACOSX.clone();
    }
  }
}
