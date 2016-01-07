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
package com.google.security.zynamics.binnavi.API.helpers;

import com.google.security.zynamics.binnavi.Settings.CGlobalSettings;

// ! Used to configure global settings.
/**
 * Provides a few functions to configure global BinNavi settings.
 */
public final class Settings {
  /**
   * You are not supposed to instantiate this class.
   */
  private Settings() {
  }

  // ! Toggle warning dialogs.
  /**
   * Disables or enables warning dialogs. Disabling dialogs is useful for running scripts or plugins
   * that handle errors themselves.
   *
   * @param enabled True, to enable dialogs. False, to disable them.
   */
  public static void setShowDialogs(final boolean enabled) {
    CGlobalSettings.SHOW_DIALOGS = enabled;
  }
}
