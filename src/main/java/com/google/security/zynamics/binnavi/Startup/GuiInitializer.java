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
package com.google.security.zynamics.binnavi.Startup;

import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Font;
import java.util.logging.Level;

import javax.swing.InputMap;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * This class is used to initialize the appearance of the GUI. It should be used just once at the
 * beginning of the program.
 */
public final class GuiInitializer {
  /**
   * You are not supposed to instantiate this class.
   */
  private GuiInitializer() {}

  /**
   * Initializes the font sizes of the GUI.
   */
  private static void initializeFont() {
    final Font font = new Font(GuiHelper.getDefaultFont(), Font.PLAIN, GuiHelper.DEFAULT_FONTSIZE);

    NaviLogger.info("Determined default font: %s", GuiHelper.getDefaultFont());
    NaviLogger.info("Using default font: %s", font.getPSName());

    NaviLogger.info("Determined monospaced font: %s", GuiHelper.getMonospaceFont());
    NaviLogger.info("Using monospaced font: %s", GuiHelper.MONOSPACED_FONT.getPSName());

    UIManager.put("Button.font", font);
    UIManager.put("CheckBox.font", font);
    UIManager.put("CheckBoxMenuItem.font", font);
    UIManager.put("ColorChooser.font", font);
    UIManager.put("ComboBox.font", font);
    UIManager.put("DesktopIcon.font", font);
    UIManager.put("InternalFrame.font", font);
    UIManager.put("InternalFrame.titleFont", font);
    UIManager.put("Label.font", font);
    UIManager.put("List.font", font);
    UIManager.put("Menu.font", font);
    UIManager.put("MenuBar.font", font);
    UIManager.put("MenuItem.font", font);
    UIManager.put("OptionPane.font", font);
    UIManager.put("Panel.font", font);
    UIManager.put("PasswordField.font", font);
    UIManager.put("PopupMenu.font", font);
    UIManager.put("ProgressBar.font", font);
    UIManager.put("RadioButton.font", font);
    UIManager.put("RadioButtonMenuItem.font", font);
    UIManager.put("ScrollPane.font", font);
    UIManager.put("TabbedPane.font", font);
    UIManager.put("Table.font", font);
    UIManager.put("TableHeader.font", font);
    UIManager.put("Text.font", font);
    UIManager.put("TextArea.font", font);
    UIManager.put("TextField.font", font);
    UIManager.put("TitledBorder.font", font);
    UIManager.put("ToggleButton.font", font);
    UIManager.put("ToolBar.font", font);
    UIManager.put("ToolTip.font", font);
    UIManager.put("Tree.font", font);
    UIManager.put("Viewport.font", font);
  }

  /**
   * This function unregisters the keys F6 and F8 from JSplitPane components because we would rather
   * uses these keys for debugger functions.
   */
  private static void initializeHotkeys() {
    final InputMap map = (InputMap) UIManager.get("SplitPane.ancestorInputMap");
    map.remove(HotKeys.GUI_INITIALIZER_KEY_1.getKeyStroke());
    map.remove(HotKeys.GUI_INITIALIZER_KEY_2.getKeyStroke());
  }

  /**
   * Sets up the initial logging level.
   */
  private static void initializeLogging() {
    switch (ConfigManager.instance().getGeneralSettings().getLogLevel()) {
      case 0:
        NaviLogger.setLevel(Level.OFF);
        break;
      case 1:
        NaviLogger.setLevel(Level.INFO);
        break;
      case 2:
        NaviLogger.setLevel(Level.ALL);
        break;
      default:
        throw new IllegalStateException("IE00844: Unknown log level read from configuration file");
    }
  }

  /**
   * Sets up the application-wide tooltip delay.
   */
  private static void initializeTooltipDelay() {
    final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    toolTipManager.setDismissDelay(60000);
    toolTipManager.setInitialDelay(1000);
    toolTipManager.setReshowDelay(1000);
  }

  /**
   * Initializes all kinds of application-wide GUI settings.
   */
  public static void initialize() {
    initializeLogging();

    System.setProperty(CMessageBox.DEFAULT_WINDOW_TITLE_PROPERTY, Constants.DEFAULT_WINDOW_TITLE);
    initializeFont();
    initializeTooltipDelay();
    initializeHotkeys();
  }
}
