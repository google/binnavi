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
package com.google.security.zynamics.binnavi.Resources;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.CUtilityFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides build version information for the project. In the case of an Ant build, this will be
 * the Git repository revision.
 */
public class BuildVersionInformation {
  /**
   * Filled with the build Git reporitory version.
   */
  private static String buildRevision = "<unknown>";

  /**
   * Load the relevant build information from the build-data {@link Properties} file. This class is
   * here to not expose google code for build information.
   */
  public static void loadBuildVersionInformation() {

    Properties properties = new Properties();

    try {
      final InputStream input =
          CMain.class.getResourceAsStream("data/build-data.properties");
      if (input == null) {
        return;
      }
      properties.load(input);

      buildRevision = properties.getProperty("build.revision");
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Returns the build revision for version information.
   *
   * @return Build revision for version information.
   */
  public static String getBuildRevision() {
    return buildRevision;
  }
}
