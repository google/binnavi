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

import com.google.security.zynamics.binnavi.Gui.GraphSettings.CGraphSettingsDialog;
import com.google.security.zynamics.binnavi.Gui.SettingsDialog.CSettingsDialog;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Window;

import javax.swing.JFrame;

/**
 * Contains code for showing settings dialogs.
 */
public final class CSettingsDialogFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CSettingsDialogFunctions() {
  }

  /**
   * Shows the initial call graph settings dialog.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void showCallgraphSettingsDialog(final JFrame parent) {
    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultCallGraphSettings();

    final CGraphSettingsDialog dlg =
        new CGraphSettingsDialog(parent, "Initial Call graph Settings", settings, true, true);

    dlg.setVisible(true);

    ConfigManager.instance().updateCallgraphSettings(settings);
    ConfigManager.instance().saveSettings(parent);
  }

  /**
   * Shows the initial flow graph settings dialog.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void showFlowgraphSettingsDialog(final JFrame parent) {
    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultFlowGraphSettings();

    final CGraphSettingsDialog dlg =
        new CGraphSettingsDialog(parent, "Initial Flow graph Settings", settings, true, false);

    dlg.setVisible(true);

    ConfigManager.instance().updateFlowgraphSettings(settings);
    ConfigManager.instance().saveSettings(parent);
  }

  /**
   * Opens the global settings dialog.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void showSettingsDialog(final Window parent) {
    final CSettingsDialog dlg = new CSettingsDialog(parent);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
  }
}
