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
package com.google.security.zynamics.binnavi.Gui;

import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessProgressDialog;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CStandardHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CStandardProgressDialog;

import java.awt.Window;



/**
 * Default progress dialog to be used throughout BinNavi.
 */
public final class CProgressDialog {
  /**
   * You are not supposed to instantiate this class.
   */
  private CProgressDialog() {
  }

  /**
   * Shows a new standard dialog.
   *
   * @param parent Parent window of the progress dialog. This argument can be null.
   * @param description Description shown in the progress dialog.
   * @param thread Background worker thread executed while the progress dialog is visible.
   *
   * @return The progress dialog itself.
   */
  public static CStandardProgressDialog show(
      final Window parent, final String description, final CStandardHelperThread thread) {
    final CStandardProgressDialog dlg =
        new CStandardProgressDialog(parent, Constants.DEFAULT_WINDOW_TITLE, description, thread);

    CIconInitializer.initializeWindowIcons(dlg);

    thread.start();

    dlg.setSize(400, 150);

    dlg.setVisible(true);

    return dlg;
  }

  /**
   * Shows a new progress dialog.
   *
   * @param parent Parent window of the progress dialog. This argument can be null.
   * @param description Description shown in the progress dialog.
   * @param thread Background worker thread executed while the progress dialog is visible.
   *
   * @return The progress dialog itself.
   */
  public static CEndlessProgressDialog showEndless(
      final Window parent, final String description, final CEndlessHelperThread thread) {
    final CEndlessProgressDialog dlg =
        new CEndlessProgressDialog(parent, Constants.DEFAULT_WINDOW_TITLE, description, thread);

    CIconInitializer.initializeWindowIcons(dlg);

    thread.start();

    dlg.setVisible(true);

    return dlg;
  }
}
