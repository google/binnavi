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

import com.google.common.io.ByteStreams;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class used to install BinExport into a given IDA Pro directory.
 */
public final class CBinExportInstaller {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBinExportInstaller() {
  }

  /**
   * Copies a file from the BinNavi source directory to the IDA Pro target directory.
   *
   * @param directory The target directory.
   * @param file Name of the file to copy.
   *
   * @throws FileNotFoundException Thrown if an input file could not be found.
   * @throws IOException Thrown if an output file could not be written.
   */
  private static void copyFile(final File directory, final String file)
      throws FileNotFoundException, IOException {
    final InputStream inFile = CMain.class.getResourceAsStream("exporters/BinExport/" + file);
    final FileOutputStream outFile =
        new FileOutputStream(directory.getAbsolutePath() + "/plugins/" + file);

    ByteStreams.copy(inFile, outFile);

    inFile.close();
    outFile.close();
  }

  /**
   * Handles an exception that occurred during installation.
   *
   * @param parent Parent window of the dialog.
   * @param exception The exception to handle.
   */
  private static void handleException(final Window parent, final Exception exception) {
    final String innerMessage = "E00041: " + "Could not install BinExport";
    final String innerDescription =
        CUtilityFunctions.createDescription(
            "BinNavi could not install the BinExport because one of more files could not "
                + "be copied.", new String[] {
                "Insufficient rights to write to the IDA Pro directory",
                "Some kind of IO problem occurred"},
            new String[] {"BinExport was not installed and can not be used to export IDB "
                + "files to the database. You can try to install BinExport manually by "
                + "copying the exporters/BinExport directory from your BinNavi installation "
                + "to the IDA Pro directory."});

    NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
  }

  /**
   * Installs all necessary BinExport files into the target directory.
   *
   * @param directory The target directory.
   *
   * @throws FileNotFoundException Thrown if an input file could not be found.
   * @throws IOException Thrown if an output file could not be written.
   */
  private static void install(final File directory) throws FileNotFoundException, IOException {
    final File pluginsDirectory = new File(directory.getAbsolutePath() + "/plugins");

    for (final String pluginFile : CBinExportFiles.getPluginFiles()) {
      if (!FileUtils.containsFile(pluginsDirectory, pluginFile)) {
        copyFile(directory, pluginFile);
      }
    }
  }

  /**
   * Installs BinExport into the given IDA Pro directory.
   *
   * @param parent Parent window used for dialogs.
   * @param directory Installation target path.
   */
  public static void install(final Window parent, final File directory) {
    try {
      install(directory);

      CMessageBox.showInformation(parent, "Installation successful");
    } catch (final FileNotFoundException e) {
      CUtilityFunctions.logException(e);

      handleException(parent, e);
    } catch (final IOException e) {
      CUtilityFunctions.logException(e);

      handleException(parent, e);
    }
  }
}
