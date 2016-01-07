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
package com.google.security.zynamics.binnavi.Startup;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Log.NaviLogFormatter;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.common.config.ConfigHelper;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;

import javax.swing.JOptionPane;

/**
 * Contains code for setting up the initial configuration file.
 */
public final class CConfigurationFileCreator {
  private static final String NAVI_LOG_FILE = ConfigHelper.getConfigurationDirectory(
      Constants.COMPANY_NAME, Constants.PROJECT_NAME) + "navilog.txt";

  /**
   * 5MB maximum log file size.
   */
  private static final int MAX_LOG_FILE_SIZE = 1024 * 1024 * 1024 * 5;

  /**
   * Maximum of five log files.
   */
  private static final int LOG_ROTATE_COUNT = 5;

  /**
   * You are not supposed to instantiate this class.
   */
  private CConfigurationFileCreator() {
  }

  /**
   * Loads and initializes the configuration file.
   *
   * @return False, if the configuration file already existed. True, if it was created.
   */
  public static boolean setupConfigurationFile() {
    try {
      final FileHandler fileHandler =
          new FileHandler(NAVI_LOG_FILE, MAX_LOG_FILE_SIZE, LOG_ROTATE_COUNT);
      fileHandler.setFormatter(new NaviLogFormatter());
      NaviLogger.addHandler(fileHandler);
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00002: " + "Could not create the log file";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not create the log file '%s'.", NAVI_LOG_FILE),
              new String[] {"Some kind of IO problem occurred. Please check the stack trace "
                  + "for more details"}, new String[] {"The error log file can not be stored"});

      NaviErrorDialog.show(null, message, description, exception);
    }

    NaviLogger.info("Loading configuration file");

    try {
      return ConfigManager.instance().read();
    } catch (final FileReadException exception) {
      CUtilityFunctions.logException(exception);

      final String configFileName = ConfigHelper.getConfigFileName(
          Constants.COMPANY_NAME, Constants.PROJECT_NAME, Constants.CONFIG_FILE_NAME);

      final String message = "E00003: " + "Malformed configuration file";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "The configuration file '%s' could not be read because it contains "
                  + "malformed XML.", configFileName), new String[] {
              "You edited the configuration file manually and made a mistake",
              "The configuration file was corrupted randomly"},
              new String[] {"After closing this dialog you will be asked if you want to run "
                  + "BinNavi with default settings or if you want to repair the configuration "
                  + "file manually."});

      NaviErrorDialog.show(null, message, description, exception);

      if (JOptionPane.YES_OPTION != CMessageBox.showYesNoQuestion(null,
          "Do you want to run BinNavi with default settings? If you choose 'No', "
              + "BinNavi will exit so you can repair the configuration file manually.")) {
        // Before running BinNavi, the problem with the configuration file should be fixed.
        System.exit(0);
      }

      final File file = new File(configFileName);

      if (!file.delete()) {
        final String innerMessage = "E00004: " + "Could not delete malformed configuration file";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The malformed configuration file '%s' could not be deleted.", configFileName),
                new String[] {
                "The configuration file is in use by another program",
                "Some kind of IO problem occurred"},
                new String[] {"BinNavi will exit after you close this error dialog and you "
                    + "will have to delete or fix the malformed configuration file manually."});

        NaviErrorDialog.show(null, innerMessage, innerDescription);

        System.exit(0);
      }

      try {
        // At this point the malformed configuration file was deleted. When reading
        // the configuration file again, the configuration file should be newly created.
        return ConfigManager.instance().read();
      } catch (final FileReadException e) {
        // This should actually never happen.
        final String innerMessage = "E00005: " + "Could not create configuration file";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "BinNavi could not create the new configuration file '%s'.",
                configFileName),
                new String[] {"Some kind of IO problem occurred"},
                new String[] {"BinNavi will exit after you close this error dialog. Try to "
                    + "find out why the configuration file could not be created. "
                    + "You may also want to contact the BinNavi support at this point."});

        NaviErrorDialog.show(null, innerMessage, innerDescription);

        System.exit(0);
      }

      return false;
    }
  }

}
