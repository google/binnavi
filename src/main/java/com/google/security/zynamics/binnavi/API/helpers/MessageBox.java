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

import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Component;


// / Used to display messages to the user.
/**
 * Helper class that can be used to show a message box.
 */
public final class MessageBox {
  /**
   * Do not create objects of this class.
   */
  private MessageBox() {
    // You are not supposed to instantiate this class
  }

  // ! Shows an Error message box.
  /**
   * Shows an Error message box.
   *
   * @param parent Parent component of the message box.
   * @param msg Message to be shown.
   */
  public static void showError(final Component parent, final String msg) {
    CMessageBox.showError(parent, msg);
  }

  // ! Shows an error message box with information about an exception.
  /**
   * Shows an error message box with information about an exception.
   *
   * @param parent The parent of the message box.
   * @param exception The exception to be displayed.
   * @param msg Additional message to be displayed before the exception.
   */
  public static void showException(
      final Component parent, final Exception exception, final String msg) {
    CMessageBox.showError(parent, msg + " \n\n" + "Reason" + ": "
        + exception.getLocalizedMessage());
  }

  // ! Shows an Information message box.
  /**
   * Shows an Information message box.
   *
   * @param parent Parent component of the message box.
   * @param msg Message to be shown.
   */
  public static void showInformation(final Component parent, final String msg) {
    CMessageBox.showInformation(parent, msg);
  }
}
