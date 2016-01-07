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
package com.google.security.zynamics.binnavi.Gui.Database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CAutoConnectHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.DatabaseDescriptionFieldHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.DatabaseIdentityFieldHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CPasswordHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CSavePasswordHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CShowPasswordHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.CTestHelp;
import com.google.security.zynamics.binnavi.Gui.Database.Help.DatabaseUserFieldHelp;
import com.google.security.zynamics.binnavi.Gui.Database.actions.CActionShowPassword;
import com.google.security.zynamics.binnavi.Gui.Database.actions.CSaveDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.Database.actions.CTestDatabaseConnection;
import com.google.security.zynamics.binnavi.Gui.Database.implementations.CDatabaseConnector;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseConnectionFunctions;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveCheckbox;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveField;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSavePasswordField;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.Help.CHelpCheckbox;
import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.Help.CHelpSaveCheckbox;
import com.google.security.zynamics.binnavi.Help.CHelpSaveField;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * This panel provides all text fields that are necessary to display and edit the configuration of a
 * single database.
 */
public final class CDatabaseSettingsPanel extends JPanel {
  private final CHelpSaveField databaseDescriptionField =
      new CHelpSaveField(new DatabaseDescriptionFieldHelp());
  private final CSaveField databaseHostField = new CHelpSaveField(new DatabaseHostFieldHelp());
  private final CSaveField databaseNameField = new CHelpSaveField(new DatabaseNameFieldHelp());
  private final CSaveField databaseUserField = new CHelpSaveField(new DatabaseUserFieldHelp());
  private final CSaveField databaseIdentityField =
      new CHelpSaveField(new DatabaseIdentityFieldHelp());
  private final CSavePasswordField passwordField = new CDatabasePasswordField();

  /**
   * Check box that allows the user to see the password he entered in plain text.
   */
  private final JCheckBox showPasswordBox =
      new CHelpCheckbox(new CActionShowPassword(passwordField), new CShowPasswordHelp());

  /**
   * Check box to toggle the "Save Password" option
   */
  private final CSaveCheckbox savePasswordBox =
      new CHelpSaveCheckbox("Save Password to configuration file", new CSavePasswordHelp());

  /**
   * Check box to toggle the "Auto-Connect" option
   */
  private final CSaveCheckbox autoConnectBox =
      new CHelpSaveCheckbox("Connect automatically", new CAutoConnectHelp());

  /**
   * Button used to test the connection to the database.
   */
  private final JButton testButton =
      new CHelpButton(CActionProxy.proxy(new CTestDatabaseConnection(this)), new CTestHelp());

  /**
   * Button used to save the currently entered database configuration.
   */
  private final JButton saveButton =
      new CHelpButton(CActionProxy.proxy(new CSaveDatabaseAction(this)), new CSaveHelp());

  /**
   * Database object that provides the connection data.
   */
  private final IDatabase database;

  /**
   * Listener that keeps the text fields up to date when the database changes.
   */
  private final InternalDatabaseListener databaseListener = new InternalDatabaseListener();

  /**
   * Tells the user to hit the save button.
   */
  private final JLabel saveLabel;

  /**
   * Listeners that are notified about changes in the panel.
   */
  private final ListenerProvider<IDatabaseSettingsPanelListener> listeners =
      new ListenerProvider<IDatabaseSettingsPanelListener>();

  /**
   * Creates a new database settings panel.
   *
   * @param database Database object that provides the connection data.
   */
  public CDatabaseSettingsPanel(final IDatabase database) {
    super(new BorderLayout());

    this.database =
        Preconditions.checkNotNull(database, "IE01317: Database argument can't be null");

    this.database.addListener(databaseListener);

    final JPanel lowerPanel = new JPanel(new BorderLayout());

    final JPanel outerDescriptionPanel = new JPanel(new BorderLayout(5, 5));
    outerDescriptionPanel.setBorder(new TitledBorder("Database"));

    final JPanel innerDescriptionPanel = new JPanel(new BorderLayout());
    innerDescriptionPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

    final JLabel label = new CHelpLabel("Description" + ":", new DatabaseDescriptionFieldHelp());

    final int PREFERRED_LABEL_WIDTH = 110;
    final int PREFERRED_LABEL_HEIGHT = 25;

    label.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));
    innerDescriptionPanel.add(label, BorderLayout.WEST);
    innerDescriptionPanel.add(databaseDescriptionField, BorderLayout.CENTER);

    outerDescriptionPanel.add(innerDescriptionPanel, BorderLayout.CENTER);

    final JPanel pDB4 = new JPanel(new BorderLayout());
    pDB4.setBorder(new EmptyBorder(0, 0, 0, 0));
    final JPanel pDB3 = new JPanel(new BorderLayout());
    pDB3.setBorder(new EmptyBorder(5, 0, 5, 0));
    final JPanel pDB2 = new JPanel(new BorderLayout());
    pDB2.setBorder(new EmptyBorder(0, 0, 5, 0));
    final JPanel pDB = new JPanel(new GridLayout(4, 1, 5, 5));
    pDB.setBorder(new EmptyBorder(0, 0, 5, 0));

    final JPanel borderHelperPanel = new JPanel(new BorderLayout());
    borderHelperPanel.setBorder(new TitledBorder("Connection"));

    pDB.add(new CStandardPanel("DB host:", new DatabaseHostFieldHelp(), databaseHostField));
    pDB.add(new CStandardPanel("DB name:", new DatabaseNameFieldHelp(), databaseNameField));
    pDB.add(new CStandardPanel("DB user:", new DatabaseUserFieldHelp(), databaseUserField));
    pDB.add(new CStandardPanel("DB password:", new CPasswordHelp(), passwordField));
    pDB.add(new CStandardPanel("", null, showPasswordBox));
    borderHelperPanel.add(pDB, BorderLayout.CENTER);
    pDB2.add(borderHelperPanel, BorderLayout.CENTER);

    final JPanel mainPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    final JPanel p5_h = new JPanel(new BorderLayout());
    final JPanel p5_h2 = new JPanel(new BorderLayout());

    mainPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

    final JPanel savePasswordPanel = new JPanel(new BorderLayout());
    savePasswordPanel.add(savePasswordBox, BorderLayout.WEST);

    mainPanel.add(savePasswordPanel);

    final JPanel autoConnectPanel = new JPanel(new BorderLayout());
    autoConnectPanel.add(autoConnectBox, BorderLayout.WEST);

    mainPanel.add(autoConnectPanel);

    p5_h.setBorder(new TitledBorder(new TitledBorder("Behavior")));

    p5_h.add(mainPanel, BorderLayout.CENTER);

    p5_h2.setBorder(new EmptyBorder(0, 0, 5, 0));
    p5_h2.add(p5_h, BorderLayout.CENTER);

    pDB3.add(pDB2, BorderLayout.NORTH);

    final JPanel identityPanel = new JPanel(new BorderLayout());
    final JPanel identitySubPanel = new JPanel(new BorderLayout());
    identitySubPanel.setBorder(
        new TitledBorder(null, "Identity", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    identitySubPanel.add(new CStandardPanel("Identity" + ":", new DatabaseIdentityFieldHelp(),
        databaseIdentityField));
    identityPanel.add(identitySubPanel, BorderLayout.CENTER);

    pDB3.add(identityPanel, BorderLayout.CENTER);
    pDB3.add(p5_h2, BorderLayout.SOUTH);

    savePasswordBox.addActionListener(new InternalActionListener());

    final JPanel centerPanel = new JPanel(new BorderLayout());

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
    buttonPanel.setBorder(new EmptyBorder(0, 2, 0, 2));

    buttonPanel.add(testButton);
    buttonPanel.add(saveButton);

    final JPanel savePanel = new JPanel(new FlowLayout());

    saveLabel = new JLabel(
        "Please hit the Save button to use the current database connection from BinNavi");
    saveLabel.setBorder(
        new CompoundBorder(new LineBorder(Color.RED, 2), new EmptyBorder(5, 5, 5, 5)));
    saveLabel.setForeground(Color.RED);

    savePanel.add(saveLabel);

    centerPanel.add(savePanel, BorderLayout.NORTH);
    centerPanel.add(buttonPanel, BorderLayout.CENTER);

    pDB4.add(pDB3, BorderLayout.NORTH);
    pDB4.add(centerPanel, BorderLayout.CENTER);

    lowerPanel.add(outerDescriptionPanel, BorderLayout.NORTH);
    lowerPanel.add(pDB4, BorderLayout.CENTER);

    add(lowerPanel);

    // Initialize the text fields
    databaseDescriptionField.setText(this.database.getConfiguration().getDescription());
    databaseHostField.setText(this.database.getConfiguration().getHost());
    databaseNameField.setText(this.database.getConfiguration().getName());
    databaseUserField.setText(this.database.getConfiguration().getUser());
    databaseIdentityField.setText(this.database.getConfiguration().getIdentity());
    passwordField.setText(this.database.getConfiguration().getPassword());
    savePasswordBox.setSelected(this.database.getConfiguration().isSavePassword());
    autoConnectBox.setSelected(this.database.getConfiguration().isAutoConnect());

    updateSaveButton();
    final UpdateListener updateListener = new UpdateListener();

    databaseDescriptionField.getDocument().addDocumentListener(updateListener);
    databaseHostField.getDocument().addDocumentListener(updateListener);
    databaseNameField.getDocument().addDocumentListener(updateListener);
    databaseUserField.getDocument().addDocumentListener(updateListener);
    databaseIdentityField.getDocument().addDocumentListener(updateListener);
    passwordField.getDocument().addDocumentListener(updateListener);
    savePasswordBox.addItemListener(updateListener);
    autoConnectBox.addItemListener(updateListener);

    setupHotkeys();
  }

  /**
   * Sets up the hotkeys of the panel.
   */
  private void setupHotkeys() {
    final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    final ActionMap actionMap = getActionMap();

    inputMap.put(HotKeys.DATABASE_SETTINGS_TEST_CONNECTION_KEY.getKeyStroke(), "TEST_CONNECTION");
    actionMap.put("TEST_CONNECTION", new AbstractAction() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        if (testButton.isEnabled()) {
          testConnection();
        }
      }
    });

    inputMap.put(HotKeys.DATABASE_SETTINGS_SAVE_CONNECTION_KEY.getKeyStroke(), "SAVE_CONNECTION");
    actionMap.put("SAVE_CONNECTION", new AbstractAction() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        saveConnection();
      }
    });
  }

  /**
   * Updates the GUI depending on the current database configuration.
   */
  private void updateGui() {
    autoConnectBox.setEnabled(savePasswordBox.isSelected());

    if (!savePasswordBox.isSelected()) {
      autoConnectBox.setSelected(false);
    }
  }

  /**
   * Disables or enables the save button depending on whether changes were made to the database or
   * not.
   */
  private void updateSaveButton() {
    final boolean userChanged =
        !databaseUserField.getText().equals(database.getConfiguration().getUser());
    final boolean descriptionChanged =
        !databaseDescriptionField.getText().equals(database.getConfiguration().getDescription());
    final boolean hostChanged =
        !databaseHostField.getText().equals(database.getConfiguration().getHost());
    final boolean nameChanged =
        !databaseNameField.getText().equals(database.getConfiguration().getName());
    final boolean passwordChanged =
        !new String(passwordField.getPassword()).equals(database.getConfiguration().getPassword());
    final boolean savePasswordChanged =
        savePasswordBox.isSelected() != database.getConfiguration().isSavePassword();
    final boolean autoConnectChanged =
        autoConnectBox.isSelected() != database.getConfiguration().isAutoConnect();
    final boolean identityChanged =
        !databaseIdentityField.getText().equals(database.getConfiguration().getIdentity());

    databaseDescriptionField.setModified(descriptionChanged);
    databaseHostField.setModified(hostChanged);
    databaseNameField.setModified(nameChanged);
    databaseUserField.setModified(userChanged);
    passwordField.setModified(passwordChanged);
    savePasswordBox.setModified(savePasswordChanged);
    autoConnectBox.setModified(autoConnectChanged);
    databaseIdentityField.setModified(identityChanged);

    saveButton.setEnabled(descriptionChanged || hostChanged || nameChanged || userChanged
        || passwordChanged || savePasswordChanged || autoConnectChanged || identityChanged);

    saveLabel.setVisible(saveButton.isEnabled());

    final boolean connectionChanged = hostChanged || nameChanged || userChanged || passwordChanged;

    for (final IDatabaseSettingsPanelListener listener : listeners) {
      try {
        listener.changedConnectionSettings(this, connectionChanged);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a listener object that is notified about changes in the panel.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IDatabaseSettingsPanelListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Cleans up allocated resources.
   */
  public void delete() {
    database.removeListener(databaseListener);
  }

  /**
   * Removes a listener object that was previously notified about changes in the panel.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IDatabaseSettingsPanelListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Saves the database configuration.
   */
  public void saveConnection() {
    final boolean connectionChanged =
        !database.getConfiguration().getPassword().equals(new String(passwordField.getPassword()))
        || !database.getConfiguration().getHost().equals(databaseHostField.getText())
        || !database.getConfiguration().getName().equals(databaseNameField.getText())
        || !database.getConfiguration().getUser().equals(databaseUserField.getText());

    database.getConfiguration().setAutoConnect(autoConnectBox.isSelected());
    database.getConfiguration().setDescription(databaseDescriptionField.getText());
    database.getConfiguration().setPassword(new String(passwordField.getPassword()));
    database.getConfiguration().setSavePassword(savePasswordBox.isSelected());
    database.getConfiguration().setHost(databaseHostField.getText());
    database.getConfiguration().setName(databaseNameField.getText());
    database.getConfiguration().setUser(databaseUserField.getText());
    database.getConfiguration().setIdentity(databaseIdentityField.getText());

    if (database.isConnected() && connectionChanged
        && (
            CMessageBox
                .showYesNoQuestion(
                    SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this),
                    "To adopt the changes you have to re-connect to the database. Do you want to reconnect now?")
            == JOptionPane.YES_OPTION)) {
      if (database.close()) {
        CDatabaseConnectionFunctions.openDatabase(
            SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this), database);
      } else {
        CMessageBox.showInformation(SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this),
            "Could not close the selected database because views or other elements from the database are still open.");
      }
    }
    ConfigManager.instance().saveSettings(
        (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this));
  }

  @Override
  public void setEnabled(final boolean enabled) {
    autoConnectBox.setEnabled(enabled);
    databaseDescriptionField.setEnabled(enabled);
    passwordField.setEnabled(enabled);
    showPasswordBox.setEnabled(enabled);
    saveButton.setEnabled(enabled);
    testButton.setEnabled(enabled);
    savePasswordBox.setEnabled(enabled);
    databaseHostField.setEnabled(enabled);
    databaseNameField.setEnabled(enabled);
    databaseUserField.setEnabled(enabled);
    databaseIdentityField.setEnabled(enabled);
  }

  /**
   * Tries to establish a connection to the database.
   */
  public void testConnection() {
    testButton.setEnabled(false);

    if (databaseIdentityField.getText().isEmpty() || databaseIdentityField.getText() == null) {
      CMessageBox.showInformation(SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this),
          "Could not test the connection to the database because the identity name is not set.");
      testButton.setEnabled(true);
      return;
    }

    if (databaseUserField.getText().isEmpty() || databaseUserField.getText() == null) {
      CMessageBox.showInformation(SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this),
          "Could not test the connection to the database because the user name is not set.");
      testButton.setEnabled(true);
      return;
    }

    if (new String(passwordField.getPassword()).isEmpty()) {
      CMessageBox.showInformation(SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this),
          "Could not test the connection to the database because the password is not set.");
      testButton.setEnabled(true);
      return;
    }

    new Thread() {
      @Override
      public void run() {
        final CProgressPanel panel =
            new CProgressPanel("Testing database connection", true, false, false);

        final IProgressOperation operation = new IProgressOperation() {
          @Override
          public String getDescription() {
            return "Testing DB Connection";
          }

          @Override
          public CProgressPanel getProgressPanel() {
            return panel;
          }
        };

        CGlobalProgressManager.instance().add(operation);

        panel.start();

        final CDatabaseConfiguration configuration = new CDatabaseConfiguration(database,
            new ListenerProvider<IDatabaseListener>(),
            "CONNECTION TEST",
            database.getConfiguration().getDriver(),
            databaseHostField.getText(),
            databaseNameField.getText(),
            databaseUserField.getText(),
            new String(passwordField.getPassword()),
            databaseIdentityField.getText(),
            false,
            false);

        CDatabaseConnector.testConnection(
            SwingUtilities.getWindowAncestor(CDatabaseSettingsPanel.this), configuration);
        CGlobalProgressManager.instance().remove(operation);
        testButton.setEnabled(true);
      }
    }.start();
  }

  /**
   * Listener that updates the GUI when something significant happens.
   */
  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      updateGui();
    }
  }

  /**
   * Listener that updates the GUI when the database object changes.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void changedAutoConnect(final IDatabase database, final boolean autoConnect) {
      autoConnectBox.setSelected(autoConnect);
      updateSaveButton();
    }

    @Override
    public void changedDescription(final IDatabase database, final String description) {
      databaseDescriptionField.setText(description);
      updateSaveButton();
    }

    @Override
    public void changedDriver(final IDatabase database, final String driver) {
      // The driver is not shown in the GUI.
    }

    @Override
    public void changedHost(final IDatabase database, final String host) {
      databaseHostField.setText(database.getConfiguration().getHost());
      updateSaveButton();
    }

    @Override
    public void changedIdentity(final IDatabase database, final String identity) {
      databaseIdentityField.setText(identity);
      updateSaveButton();
    }

    @Override
    public void changedName(final IDatabase database, final String name) {
      databaseNameField.setText(database.getConfiguration().getName());
      updateSaveButton();
    }

    @Override
    public void changedPassword(final IDatabase database, final String password) {
      passwordField.setText(password);
      updateSaveButton();
    }

    @Override
    public void changedSavePassword(final IDatabase database, final boolean savePassword) {
      savePasswordBox.setSelected(savePassword);
      updateSaveButton();
    }

    @Override
    public void changedUser(final IDatabase database, final String user) {
      databaseUserField.setText(user);
      updateSaveButton();
    }
  }

  /**
   * Listener that keeps track of changes in the input fields and updates the Save button if
   * necessary.
   */
  private class UpdateListener implements DocumentListener, ItemListener {
    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateSaveButton();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateSaveButton();
    }

    @Override
    public void itemStateChanged(final ItemEvent event) {
      updateSaveButton();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateSaveButton();
    }
  }
}
