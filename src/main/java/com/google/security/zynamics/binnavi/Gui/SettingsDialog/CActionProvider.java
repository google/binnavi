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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;



import java.awt.Component;
import java.io.File;

import javax.swing.SwingUtilities;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.IdaSelectionDialog.CIdaSelectionDialog;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Contains the implementations of the more complex operations of the settings dialog.
 */
public final class CActionProvider {
  /**
   * You are not supposed to instantiate this class.
   */
  private CActionProvider() {
  }

  /**
   * Prompts the user for the location of the IDA executable.
   * 
   * @param parent Parent component used to display dialogs.
   * @param initialDirectory The directory to select by default.
   * 
   * @return The selected IDA executable or null if no executable was selected.
   */
  public static String selectIDADirectory(final Component parent, final String initialDirectory) {
    Preconditions.checkNotNull(parent, "IE02067: Parent argument can not be null");
    Preconditions.checkNotNull(initialDirectory, "IE02259: Initial directory can not be null");

    final CIdaSelectionDialog dialog =
        CIdaSelectionDialog.show(SwingUtilities.getWindowAncestor(parent), initialDirectory);

    final File selectedFile = dialog.getSelectedFile();

    if (selectedFile == null) {
      return null;
    } else if (!selectedFile.exists()) {
      CMessageBox.showError(parent, "File does not exist.");

      return null;
    } else if (selectedFile.canExecute()) {
      return selectedFile.getAbsolutePath();
    } else {
      CMessageBox.showError(parent, "File is not an executable file.");

      return null;
    }
  }
}
