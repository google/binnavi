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
package com.google.security.zynamics.common.config;

import com.google.security.zynamics.zylib.system.SystemHelpers;

import java.io.File;

/**
 * A class that provides static settings configuration helper functions.
 *
 * @author nilsheumer@google.com (Nils Heumer)
 *         timkornau@google.com (Tim Kornau)
 */
public final class ConfigHelper {

  // Private constructor to avoid instantiation.
  private ConfigHelper() {
  }

  /**
   * Returns the directory where the settings configuration file is stored.
   *
   * @param companyName The name of the company.
   * @param productName The name of the product, e.g. "BinDiff".
   * @return Path of the applications directory.
   */
  public static String getConfigurationDirectory(
      final String companyName, final String productName) {
    return getZynamicsDirectory(companyName) + productName + File.separator;
  }

  /**
   * Returns the standard zynamics directory which is the base directory for the application
   * settings directories of all zynamics products.
   *
   * @param companyName The name of the company.
   * @return Path of the zynamics base directory.
   */
  public static String getZynamicsDirectory(final String companyName) {
    // Use lowercase for the company name, since that is customary on Linux. For "zynamics", this
    // is a no-op, but it will do the right thing for "Google".
    return SystemHelpers.getApplicationDataDirectory() + File.separator
        + (SystemHelpers.isRunningWindows() ? companyName : ("." + companyName.toLowerCase()))
        + File.separator;
  }

 /**
  * Returns the zynamics all users application directory.
  *
  * @param productName The name of the product, e.g. "BinDiff".
  * @param configFileName The name of the configuration file excluding the path.
  * @return Path of the all users application directory.
  */
  public static final String getMachineConfigFileName(
      final String productName, final String configFileName) {
    return SystemHelpers.getAllUsersApplicationDataDirectory(productName) + configFileName;
  }

  /**
   * Returns the fully qualified path to the configuration file.
   *
   * @param companyName The name of the company.
   * @param productName The name of the product, e.g. "BinDiff".
   * @param configFileName The name of the configuration file excluding the path.
   * @return The path to the configuration file.
   */
  public static String getConfigFileName(
      final String companyName, final String productName, final String configFileName) {
    return getConfigurationDirectory(companyName, productName) + configFileName;
  }
}
