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
package com.google.security.zynamics.binnavi.Gui.errordialog;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Desktop;
import java.awt.Window;
import java.net.URL;

/**
 * This class contains the concrete implementations for the actions available in the error dialog.
 */
public final class ErrorDialogFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private ErrorDialogFunctions() {
  }

  /**
   * Opens the Bugtracker URL.
   *
   * @param parent Parent window used for dialogs.
   */
  public static void reportBug(final Window parent) {
    // Parent can be null because errors appear before windows are created.
    try {
      Desktop.getDesktop().browse(new URL("https://github.com/google/binnavi/issues").toURI());
    } catch (final Exception e) {
      CUtilityFunctions.logException(e);
      CMessageBox.showError(parent, "Could not open the bugtracker URL in the browser.");
    }
  }
}
