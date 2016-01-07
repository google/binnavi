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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;
import com.google.security.zynamics.zylib.gui.scripting.LanguageBox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.script.ScriptEngineManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * In this panel of the settings dialog, the user can configure all kinds of general settings that
 * do not fit into any of the other settings panels.
 */
public final class CGeneralSettingsPanel extends CAbstractSettingsPanel {

  /**
   * Default width of text fields in this panel.
   */
  private static final int TEXTFIELD_WIDTH = 280;

  /**
   * Default height of text fields in this panel.
   */
  private static final int TEXTFIELD_HEIGHT = 25;

  /**
   * Used to configure the support email address used when submitting bugs.
   */
  private final JTextField emailBox = new JTextField();

  /**
   * Used to select the IDA directory.
   */
  private final FileChooserPanel idaDirectoryPanel;

  /**
   * Used to select the log level.
   */
  private final JComboBox<String> logLevelBox;

  /**
   * Used to select the default Scripting language
   */
  private final LanguageBox scriptingBox;

  /**
   * Creates a new general settings panel.
   */
  public CGeneralSettingsPanel() {
    super(new BorderLayout());

    idaDirectoryPanel = new FileChooserPanel(
        ConfigManager.instance().getGeneralSettings().getIdaDirectory(),
        new InternalIDAListener(), "...", TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT, 0);

    logLevelBox = new JComboBox<String>(new String[] {"Off", "On", "Verbose"});
    logLevelBox.setPreferredSize(new Dimension(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));

    final ScriptEngineManager manager = new ScriptEngineManager();
    scriptingBox = new LanguageBox(manager);
    scriptingBox.setPreferredSize(new Dimension(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));

    final JPanel pMain = new JPanel(new BorderLayout());
    pMain.add(createEditElementsPanel(), BorderLayout.NORTH);

    emailBox.setPreferredSize(new Dimension(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));

    add(pMain);
  }

  /**
   * Creates the panels shown in the settings panel.
   *
   * @return The panel created by this function.
   */
  private JPanel createEditElementsPanel() {
    final JPanel pEdits = new JPanel(new GridLayout(6, 1, 4, 4));
    pEdits.setBorder(new TitledBorder("General Settings"));

    // IDA Pro selection
    final JPanel idaLocationPanel = new JPanel(new BorderLayout());
    idaLocationPanel.setBorder(new EmptyBorder(0, 2, 0, 2));
    final JLabel idaLabel = new JLabel("IDA Pro Installation Directory:");

    idaLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
    idaLocationPanel.add(idaLabel, BorderLayout.CENTER);
    idaLocationPanel
        .add(CHintCreator.createHintPanel(idaDirectoryPanel,
            "The location of your IDA installation."), BorderLayout.EAST);
    pEdits.add(idaLocationPanel);

    // Support Email
    final JPanel emailBoxPanel = new JPanel(new BorderLayout());
    emailBoxPanel.setBorder(new EmptyBorder(0, 2, 2, 2));
    final JLabel emailLabel = new JLabel("Your email address" + ":");

    emailBoxPanel.add(emailLabel, BorderLayout.CENTER);
    emailBoxPanel.add(CHintCreator.createHintPanel(emailBox,
        "This email address is used to contact you after you have submitted bugs."),
        BorderLayout.EAST);
    pEdits.add(emailBoxPanel, BorderLayout.CENTER);

    emailBox.setText(ConfigManager.instance().getGeneralSettings().getSupportEmailAddress());

    // Scripting language
    final JPanel scriptingPanel = new JPanel(new BorderLayout());
    scriptingPanel.setBorder(new EmptyBorder(0, 2, 2, 2));
    final JLabel scriptingLabel = new JLabel("Default Scripting Language" + ":");

    scriptingPanel.add(scriptingLabel, BorderLayout.CENTER);
    scriptingPanel.add(CHintCreator.createHintPanel(scriptingBox,
        "Scripting language that is selected by default when opening scripting dialogs."),
        BorderLayout.EAST);
    pEdits.add(scriptingPanel, BorderLayout.CENTER);

    scriptingBox.setSelectedLanguage(
        ConfigManager.instance().getGeneralSettings().getDefaultScriptingLanguage());

    // Log level
    final JPanel logLevelPanel = new JPanel(new BorderLayout());
    logLevelPanel.setBorder(new EmptyBorder(0, 2, 2, 2));
    final JLabel logLevelLabel = new JLabel("Log Level" + ":");

    logLevelPanel.add(logLevelLabel, BorderLayout.CENTER);
    logLevelPanel.add(CHintCreator.createHintPanel(logLevelBox,
        "Determines what messages are logged to the log file."), BorderLayout.EAST);
    pEdits.add(logLevelPanel, BorderLayout.CENTER);

    logLevelBox.setSelectedIndex(ConfigManager.instance().getGeneralSettings().getLogLevel());

    // Log file

    final JPanel logFilePanel = new JPanel(new BorderLayout());
    logFilePanel.setBorder(new EmptyBorder(0, 2, 2, 2));
    final JLabel logFileLabel = new JLabel("Log File" + ":");

    final JPanel fileLabel = new CLogFilePanel();
    fileLabel.setPreferredSize(new Dimension(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT));

    logFilePanel.add(logFileLabel, BorderLayout.CENTER);
    logFilePanel.add(CHintCreator.createHintPanel(fileLabel, "Location of the BinNavi log file."),
        BorderLayout.EAST);
    pEdits.add(logFilePanel, BorderLayout.CENTER);

    return pEdits;
  }

  @Override
  protected boolean save() {
    final GeneralSettingsConfigItem settings = ConfigManager.instance().getGeneralSettings();
    settings.setIdaDirectory(idaDirectoryPanel.getText());
    settings.setLogLevel(logLevelBox.getSelectedIndex());
    settings.setDefaultScriptingLanguage(scriptingBox.getSelectedLanguage());
    settings.setSupportEmailAddress(emailBox.getText());

    switch (logLevelBox.getSelectedIndex()) {
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
        throw new IllegalStateException("IE01190: Invalid log level selection");
    }

    return false;
  }

  /**
   * Prompts the user for the location of the IDA executable after the appropriate button was
   * clicked.
   */
  private class InternalIDAListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      try {
        final String idaPath =
            CActionProvider.selectIDADirectory(CGeneralSettingsPanel.this,
                idaDirectoryPanel.getText());

        if (idaPath != null) {
          idaDirectoryPanel.setText(idaPath);
        }
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }
}
