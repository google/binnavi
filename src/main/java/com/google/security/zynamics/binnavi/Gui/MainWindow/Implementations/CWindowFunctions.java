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

package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.Scripting.CScriptingDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpManager;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CDialogAboutEx;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.license.UpdateCheckHelper;

import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Contains helper functions for global main window functions.
 */
public final class CWindowFunctions {
  /**
   * Path to the manual file.
   */
  private static final String MANUAL_FILE = 
    "src/main/java/com/google/security/zynamics/binnavi/manual/index.htm";

  /**
   * You are not supposed to instantiate this class.
   */
  private CWindowFunctions() {}

  /**
   * Checks for available product updates.
   *
   * @param parent Parent window for modal dialogs.
   */
  public static void checkForUpdates(final JFrame parent) {
    UpdateCheckHelper.checkForUpdatesWithUi(
        parent, Constants.PROJECT_NAME, Constants.PROJECT_VERSION);
  }

  /**
   * Shows the BinNavi About dialog.
   *
   * @param parent Parent window used for dialogs.
   */
  public static void showAboutDialog(final JFrame parent) {
    try {
      final List<Pair<String, URL>> urls = new ArrayList<>();

      urls.add(new Pair<>("zynamics Website", new URL("http://www.zynamics.com")));
      urls.add(new Pair<>("BinNavi Product Site", new URL("http://www.zynamics.com/binnavi.html")));
      urls.add(new Pair<>("Report Bugs", new URL("mailto:zynamics-support@google.com")));

      final String message = Constants.PROJECT_NAME_VERSION_BUILD
          + "\n\nCopyright \u00a92004-2011 zynamics GmbH.\nCopyright \u00a92011-2015 Google Inc.\n";
      final String description =
          "\nParts of this software were created by third parties and have different licensing "
          + "requirements.\nPlease see the manual file for a complete list.\n";

      final Image appImage =
          new ImageIcon(CMain.class.getResource("data/binnavi_logo3_border.png")).getImage();
      final CDialogAboutEx dlg = new CDialogAboutEx(parent, new ImageIcon(appImage),
          Constants.PROJECT_NAME_VERSION, message, description, urls);

      GuiHelper.centerOnScreen(dlg);

      dlg.setVisible(true);
    } catch (final Exception e) {
      CUtilityFunctions.logException(e);
    }
  }

  /**
   * Shows context-sensitive help for the given window.
   *
   * @param window The window for which context-sensitive help mode is started.
   */
  public static void showContextHelp(final JFrame window) {
    CHelpManager.instance().start(window);
  }

  /**
   * Opens the BinNavi manual file.
   *
   * @param parent Parent window used for dialogs.
   */
  public static void showHelpFile(final JFrame parent) {
    try {
      Desktop.getDesktop().open(new File(MANUAL_FILE));
    } catch (final Exception e) {
      CUtilityFunctions.logException(e);

      final String message =
          "E00198: "
          + "Could not open help file";
      final String description = CUtilityFunctions.createDescription(
          String.format("The help file '%s' could not be opened.", MANUAL_FILE),
          new String[] {
              "The manual file was accidentaly deleted.",
          },
          new String[] {"BinNavi could not open the help file. Please try to open the "
              + "help file manually."});

      NaviErrorDialog.show(parent, message, description, e);
    }
  }

  /**
   * Opens the main window scripting dialog.
   *
   * @param parent Parent window used for dialogs.
   */
  public static void showScriptingDialog(final JFrame parent) {
    final String defaultLanguage =
        ConfigManager.instance().getGeneralSettings().getDefaultScriptingLanguage();

    final CScriptingDialog dlg =
        new CScriptingDialog(parent, defaultLanguage, PluginInterface.instance());

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
    GuiHelper.applyWindowFix(dlg);
  }
}
