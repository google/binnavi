/*
Copyright 2015 Google Inc. All Rights Reserved.

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
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.common.config.ConfigHelper;

import java.io.File;


/**
 * This class is used to make sure that the BinNavi settings exists or is created. In this
 * directory, application settings and log files are stored.
 */
public final class CSettingsDirectoryCreator {
  /**
   * Private constructor because this is a static helper class.
   */
  private CSettingsDirectoryCreator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Creates the configuration directory if it does not already exist.
   *
   * @return True, if the configuration directory exists when this function returns. False, if
   *         creating the directory failed.
   */
  private static boolean createNaviDirectory() {
    final File dir = new File(ConfigHelper.getConfigurationDirectory(
        Constants.COMPANY_NAME, Constants.PROJECT_NAME));

    return dir.exists() || dir.mkdir();
  }

  /**
   * Tries to create the general zynamics settings directory.
   *
   * @return True, if the directory was created successfully.
   */
  private static boolean createZynamicsDirectory() {
    final File dir = new File(ConfigHelper.getZynamicsDirectory(Constants.COMPANY_NAME));

    return dir.exists() || dir.mkdir();
  }

  /**
   * Shows a error dialog in case creating a directory failed.
   *
   * @param directory The name of the directory that could not be created.
   */
  private static void showDirectoryCreationError(final String directory) {
    final String message = "E00001: " + "Could not create the settings directory";
    final String description =
        CUtilityFunctions
            .createDescription(
                String.format("BinNavi could not create the settings directory '%s'.", directory),
                new String[] {"Your user account does not have sufficient rights to create this directory"},
                new String[] {"Changes to the global settings will not be saved",
                    "The error log file can not be stored"});

    NaviErrorDialog.show(null, message, description);
  }

  /**
   * Creates the settings directory.
   */
  public static void createSettingsDirectory() {
    if (createZynamicsDirectory()) {
      if (!createNaviDirectory()) {
        // zynamics/BinNavi directory could not be created
        showDirectoryCreationError(ConfigHelper.getConfigurationDirectory(
            Constants.COMPANY_NAME, Constants.PROJECT_NAME));
      }
    } else {
      // zynamics directory could not be created
      showDirectoryCreationError(ConfigHelper.getZynamicsDirectory(Constants.COMPANY_NAME));
    }
  }
}
