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
package com.google.security.zynamics.binnavi.Gui.ErrorDialog;

import com.google.security.zynamics.binnavi.Gui.ErrorDialog.Implementations.CErrorDialogFunctions;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.errordialog.ErrorDialog;

import java.awt.Window;



/**
 * Default error dialog for displaying all kinds of errors.
 */
public final class CNaviErrorDialog extends ErrorDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2326060583167893229L;

  /**
   * Creates a new error dialog.
   * 
   * @param owner Parent window of the dialog.
   * @param shortMessage Message shown in the title field.
   * @param description Long description of the issue.
   * @param exception Optional exception argument that provides additional information.
   */
  private CNaviErrorDialog(final Window owner, final String shortMessage, final String description,
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
    final CNaviErrorDialog dlg = new CNaviErrorDialog(owner, shortMessage, description, exception);

    if (owner == null) {
      GuiHelper.centerOnScreen(dlg);
    } else {
      GuiHelper.centerChildToParent(owner, dlg, true);
    }

    dlg.setVisible(true);
  }

  @Override
  protected void report() {
    CErrorDialogFunctions.reportBug(getOwner());
  }

  @Override
  protected void send(final String description, final String message, final Throwable exception) {
    CErrorDialogFunctions.send(this, description, message, exception);
  }

  @Override
  public String getTitle() {
    return Constants.DEFAULT_WINDOW_TITLE;
  }
}
