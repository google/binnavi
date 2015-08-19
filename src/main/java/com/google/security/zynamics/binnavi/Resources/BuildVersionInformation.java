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
package com.google.security.zynamics.binnavi.Resources;

import com.google.security.zynamics.binnavi.CUtilityFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides build version information for the project. In the case of a blaze build, this
 * information will be the blaze build information, in the case of a pulse build this will be the
 * pulse build information.
 */
public class BuildVersionInformation {

  /**
   * Filled with the build id from either the blaze build in the case a deploy jar has been built,
   * or from the pulse environment variables in the case of a pulse build.
   */
  private static String buildId = "<unknown>";

  /**
   * Filled with the build change list from the blaze build in the case a deploy jar has been built,
   * or from the pulse environment variables in the case of a pulse build.
   */
  private static String buildChangelist = "<unknown>";

  /**
   * Load the relevant build information from the build-data {@link Properties} file. This class is
   * here to not expose google code for build information.
   */
  public static void loadBuildVersionInformation() {

    Properties properties = new Properties();

    try {
      final InputStream input =
          BuildVersionInformation.class.getResourceAsStream("/build-data.properties");

      if (input == null) {
        return;
      }

      properties.load(input);

      buildId = properties.getProperty("build.build_id");
      buildChangelist = properties.getProperty("build.changelist");

    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Returns the {@link String build id} for version information.
   *
   * @return {@link String build id} for version information.
   */
  public static String getBuildId() {
    return buildId;
  }

  /**
   * Returns the {@link String build change list} for version information.
   *
   * @return {@link String build change list} for version information.
   */
  public static String getBuildChangeList() {
    return buildChangelist;
  }
}
