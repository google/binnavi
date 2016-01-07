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
package com.google.security.zynamics.binnavi.Gui.MainWindow;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.binnavi.Gui.FirstStartDialog.CFirstStartDialog;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseConnectionFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CWindowClosingFunctions;
import com.google.security.zynamics.binnavi.Gui.Progress.CProgressStatusBar;
import com.google.security.zynamics.binnavi.Gui.SettingsDialog.CActionProvider;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main window of com.google.security.zynamics.binnavi.
 */
public final class CProjectMainFrame extends JFrame {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8043160660496546969L;

  /**
   * True, if this is the first time BinNavi is started on the system.
   */
  private final boolean m_firstStart;

  /**
   * Creates a new instance of the BinNavi main window.
   *
   * @param databaseManager The database manager that keeps track of all known databases.
   * @param configFile The config file that is used to store user-specific settings.
   * @param firstStart True, if this is the first time BinNavi is started on the system.
   */
  public CProjectMainFrame(final CDatabaseManager databaseManager, final ConfigManager configFile,
      final boolean firstStart) {
    Preconditions.checkNotNull(databaseManager, "IE01828: Database manager can't be null");
    Preconditions.checkNotNull(configFile, "IE01829: Config file argument can't be null");

    m_firstStart = firstStart;

    final CProjectMainPanel mainPanel = new CProjectMainPanel(this, databaseManager);

    final JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.add(new CProgressStatusBar(), BorderLayout.EAST);

    add(mainPanel, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

    mainPanel.init();

    restoreSettings(configFile);

    setTitle(Constants.DEFAULT_WINDOW_TITLE);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent Event) {
        CWindowClosingFunctions.exit(CProjectMainFrame.this);
      }
    });

    CIconInitializer.initializeWindowIcons(this);
  }

  /**
   * Determines whether enough heap space is allocated for use with BinNavi.
   *
   * @return True, if insufficient memory is available. False, if enough memory is available.
   */
  private boolean insufficientMemory() {
    // We use 500 instead of 512 because the returned value is not exactly
    // what the user specifies in the command line.
    return Runtime.getRuntime().maxMemory() < (500 * 1024 * 1024);
  }

  /**
   * Restores the main window specific settings stored in the config file. This includes things like
   * the position of the window.
   *
   * @param configFile The config file where the data is taken from.
   */
  private void restoreSettings(final ConfigManager configFile) {
    if (configFile.getGeneralSettings().isMaximizeWindow()) {
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setSize(screenSize.width, screenSize.height);

      setExtendedState(MAXIMIZED_BOTH);
    } else {
      final GeneralSettingsConfigItem.LastOpenWindowConfigItem window =
          configFile.getGeneralSettings().getLastOpenWindow();

      final int top = window.getTop();
      final int left = window.getLeft();
      final int height = window.getHeight();
      final int width = window.getWidth();

      setLocation(left, top);
      setSize(width, height);
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);

    if (m_firstStart) {
      CFirstStartDialog.show(this);

      CMessageBox.showInformation(getRootPane(),
          "Please configure your IDA Pro settings and install the required IDA Pro plugins now."
          + "\n\nIf you want to reconfigure these settings later you can find them in the "
          + "Settings dialog of this window.");

      final String file = CActionProvider.selectIDADirectory(
          getRootPane(), ConfigManager.instance().getGeneralSettings().getIdaDirectory());
      if (file != null) {
        ConfigManager.instance().getGeneralSettings().setIdaDirectory(file);
      }
    }

    if (insufficientMemory()) {
      CMessageBox.showInformation(this,
          "You are using BinNavi with less than the recommended 512 MB of memory.\n"
          + "Please check binnavi.bat in your BinNavi directory to find out how to increase "
          + "the available memory.");
    }

    // Connect the databases to be connected automatically.
    for (final IDatabase database : CDatabaseManager.instance()) {
      if (database.getConfiguration().isAutoConnect()) {
        CDatabaseConnectionFunctions.openDatabase(this, database);
      }
    }
  }
}
