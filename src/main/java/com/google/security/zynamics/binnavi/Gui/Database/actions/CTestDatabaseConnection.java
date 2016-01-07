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
package com.google.security.zynamics.binnavi.Gui.Database.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.Database.CDatabaseSettingsPanel;


/**
 * Action class that can be used to test the current database configuration.
 */
public final class CTestDatabaseConnection extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8784926684855833953L;

  /**
   * The provider object that contains the concrete implementation of the test action.
   */
  private final CDatabaseSettingsPanel m_settingsPanel;

  /**
   * Creates a new action object.
   * 
   * @param settingsPanel The provider object that contains the concrete implementation of the test
   *        action.
   */
  public CTestDatabaseConnection(final CDatabaseSettingsPanel settingsPanel) {
    super("Test connection");

    m_settingsPanel = settingsPanel;// Preconditions.checkNotNull(settingsPanel,
                                    // "IE01319: Action provider argument can not be null");

    putValue(SHORT_DESCRIPTION,
        "Tries to establish a connection to the configured database (CTRL-T)");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_settingsPanel.testConnection();
  }
}
