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
package com.google.security.zynamics.binnavi.Gui.errordialog;

import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.errordialog.ErrorDialog;

import java.awt.Window;

/**
 * Default error dialog for displaying all kinds of errors.
 */
public final class NaviErrorDialog extends ErrorDialog {
  /**
   * Creates a new error dialog.
   * 
   * @param owner Parent window of the dialog.
   * @param shortMessage Message shown in the title field.
   * @param description Long description of the issue.
   * @param exception Optional exception argument that provides additional information.
   */
  private NaviErrorDialog(final Window owner, final String shortMessage, final String description,
      final Throwable exception) {
    super(owner, shortMessage, description, exception);
  }

  /**
   * Shows an error message.
   *
   * @param owner Parent window of the dialog.
   * @param shortMessage Message shown in the title field.
   * @param description Long description of the issue.
   */
  public static void show(final Window owner, final String shortMessage, final String description) {
    show(owner, shortMessage, description, null);
  }

  /**
   * Shows an exceptional error dialog.
   *
   * @param owner Parent window of the dialog.
   * @param shortMessage Message shown in the title field.
   * @param description Long description of the issue.
   * @param exception Optional exception argument that provides additional information.
   */
  public static void show(final Window owner, final String shortMessage, final String description,
      final Throwable exception) {
    final NaviErrorDialog dlg = new NaviErrorDialog(owner, shortMessage, description, exception);

    if (owner == null) {
      GuiHelper.centerOnScreen(dlg);
    } else {
      GuiHelper.centerChildToParent(owner, dlg, true);
    }

    dlg.setVisible(true);
  }

  @Override
  protected void report() {
    ErrorDialogFunctions.reportBug(getOwner());
  }

  @Override
  public String getTitle() {
    return Constants.DEFAULT_WINDOW_TITLE;
  }
}
