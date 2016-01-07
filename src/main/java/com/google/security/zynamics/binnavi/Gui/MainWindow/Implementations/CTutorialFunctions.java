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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.security.zynamics.binnavi.Gui.Tutorials.CTutorialDialog;
import com.google.security.zynamics.binnavi.Gui.Tutorials.CTutorialStartDialog;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.Tutorials.CTutorial;
import com.google.security.zynamics.binnavi.Tutorials.CTutorialLoader;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.io.File;

import javax.swing.JFrame;



/**
 * Helper classes for tutorial functions.
 */
public final class CTutorialFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTutorialFunctions() {
  }

  /**
   * Shows the dialog where the user can select and start a tutorial.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void showStartDialog(final JFrame parent) {
    final IFilledList<CTutorial> tutorials =
        CTutorialLoader.readTutorials(Constants.startPath + File.separator + "tutorials");

    final CTutorialStartDialog dialog = new CTutorialStartDialog(parent, tutorials);

    dialog.setVisible(true);

    final CTutorial selectedTutorial = dialog.getSelectedTutorial();

    if (selectedTutorial != null) {
      CTutorialDialog.instance().start(selectedTutorial);
    }
  }
}
