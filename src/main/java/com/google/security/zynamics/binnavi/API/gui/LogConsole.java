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
package com.google.security.zynamics.binnavi.API.gui;

import com.google.security.zynamics.binnavi.Gui.plugins.output.CPluginOutputDialog;

// / Global log console where plugins can write output to
/**
 * Whenever a plugin or a script needs to write output somewhere this console window can be used.
 */
public final class LogConsole {
  /**
   * You are not supposed to instantiate this class.
   */
  private LogConsole() {
  }

  // ! Clears the log console.
  /**
   * Clears all text in the log console.
   */
  public static void clear() {
    CPluginOutputDialog.instance().clear();
  }

  // ! Logs a message
  /**
   * Logs a new message to the log console.
   *
   * @param message The message to log.
   */
  public static void log(final String message) {
    CPluginOutputDialog.instance().log(message);
  }

  // ! Opens the log console window.
  /**
   * Opens the window the contains the log console.
   */
  public static void show() {
    CPluginOutputDialog.instance().showDialog();
  }
}
