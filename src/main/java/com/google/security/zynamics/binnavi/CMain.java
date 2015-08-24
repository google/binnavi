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
package com.google.security.zynamics.binnavi;

import com.google.security.zynamics.binnavi.API.gui.MainWindow;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.MainWindow.CProjectMainFrame;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Plugins.PluginRegistry;
import com.google.security.zynamics.binnavi.Resources.BuildVersionInformation;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.Startup.CBatchPluginExecuter;
import com.google.security.zynamics.binnavi.Startup.CConfigurationFileCreator;
import com.google.security.zynamics.binnavi.Startup.CGraphPanelExtender;
import com.google.security.zynamics.binnavi.Startup.CSettingsDirectoryCreator;
import com.google.security.zynamics.binnavi.Startup.CommandlineOptions;
import com.google.security.zynamics.binnavi.Startup.CommandlineParser;
import com.google.security.zynamics.binnavi.Startup.GuiInitializer;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.DatabaseConfigItem;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.io.FileUtils;

/**
 * BinNavi main class, contains the program entry point of BinNavi and sets up global settings.
 */
public final class CMain {
  static {
    // Use Apple L&F screen menu bar if available. This property must be
    // set before any frames are displayed. Setting the Apple-specific
    // properties has no effect on other platforms.
    try {
      java.lang.System.setProperty("apple.laf.useScreenMenuBar", "true");
    } catch (final Exception e) {
      // Catch-all, since we may run on an older version of OS X,
      // setting on other platforms does no harm.
      java.lang.System.setProperty("com.apple.macos.useScreenMenuBar", "true");
    }

    // Set application name
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        Constants.DEFAULT_WINDOW_TITLE);
  }

  /**
   * You are not supposed to instantiate this class.
   */
  private CMain() {}

  /**
   * Loads all database connections that are loaded automatically on startup.
   */
  private static void prepareDatabaseConnections() {
    NaviLogger.info("Initializing database connections");

    // Read database information from the configuration file.
    final CDatabaseManager manager = CDatabaseManager.instance();

    for (final DatabaseConfigItem database : ConfigManager.instance().getDatabases()) {
      manager.addDatabase(new CDatabase(database.getDescription(), database.getDriver(),
          database.getHost(), database.getName(), database.getUser(), database.getPassword(),
          database.getIdentity(), database.isSavePassword(), database.isAutoConnect()));
    }
  }

  /**
   * Program entry function.
   *
   * @param args Command line parameters that were passed to the program.
   */
  public static void main(final String[] args) {
    // ATTENTION: IF YOU'RE NOT READING THE CONFIG FILE FIRST YOU'RE PROBABLY DOING SOMETHING WRONG.
    CSettingsDirectoryCreator.createSettingsDirectory();

    final boolean firstStart = !CConfigurationFileCreator.setupConfigurationFile();
    BuildVersionInformation.loadBuildVersionInformation();

    // ESCA-JAVA0266:
    // ATTENTION: DO NOT MOVE THE LINE BELOW ABOVE THE CONFIG FILE STUFF
    // Don't remove this line, it actually works from inside JAR files
    System.out.printf("Starting %s%n", Constants.PROJECT_NAME_VERSION_BUILD);

    CGraphPanelExtender.extend();

    GuiInitializer.initialize();

    if (firstStart) {
      final CDatabase newDatabase = new CDatabase("Click and configure me", "org.postgresql.Driver",
          "localhost", "new_database", "user", "password", "identity", false, false);
      CDatabaseManager.instance().addDatabase(newDatabase);
    }

    prepareDatabaseConnections();

    final PluginRegistry pluginRegistry = new PluginRegistry();

    final String startPath = FileUtils.findLocalRootPath(CMain.class);
    Constants.startPath = startPath;
    NaviLogger.info("Defaulting startup path to %s", startPath);

    final CDatabaseManager databaseManager = CDatabaseManager.instance();

    final PluginInterface pluginInterface =
        PluginInterface.instance(startPath, databaseManager, pluginRegistry);

    final CProjectMainFrame window =
        new CProjectMainFrame(databaseManager, ConfigManager.instance(), firstStart);
    pluginInterface.setMainWindow(new MainWindow(window));
    pluginInterface.reloadPlugins();

    final CommandlineOptions options = CommandlineParser.parseCommandLine(args);

    if (options.getBatchPlugin() != null) {
      CBatchPluginExecuter.execute(options.getBatchPlugin(), pluginRegistry);
    }

    window.setVisible(true);
    GuiHelper.applyWindowFix(window);
  }
}
