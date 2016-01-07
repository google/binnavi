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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.helpers;

/**
 * Contains code for normalizing size settings read from the configuration file.
 */
public final class CSizeSettingsNormalizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CSizeSettingsNormalizer() {
  }

  /**
   * Normalizes a size value read from the configuration file.
   *
   * @param settingSize The size value from the configuration file.
   * @param maximum Maximum size of the value.
   * @param defaultValue Default value if the value from the configuration file is out of bounds.
   *
   * @return The normalized size value.
   */
  public static int getSize(final int settingSize, final int maximum, final int defaultValue) {
    return settingSize == -1 || settingSize > maximum ? defaultValue : settingSize;
  }
}
