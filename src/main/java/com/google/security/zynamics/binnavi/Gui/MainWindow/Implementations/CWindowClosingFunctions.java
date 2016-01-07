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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Contains code for closing the main window.
 */
public final class CWindowClosingFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CWindowClosingFunctions() {
  }

  /**
   * Stores all relevant settings in the configuration file object and saves the configuration file
   * to the disk.
   * 
   * @param parent Parent window used for dialogs.
   */
  private static void saveSettings(final JFrame parent) {
    ConfigManager.instance().saveSettings(parent);
  }

  /**
   * Shuts down BinNavi after prompting the user whether he really wants to close BinNavi.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void exit(final JFrame parent) {
    if (CMessageBox.showYesNoQuestion(parent, "Really close BinNavi?") != JOptionPane.YES_OPTION) {
      return;
    }

    for (final CGraphWindow window : CWindowManager.instance()) {
      if (!window.close()) {
        return;
      }
    }

    saveSettings(parent);
    System.exit(0);
  }
}
