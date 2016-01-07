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
 * Action class that can be used to save the current database configuration.
 */
public final class CSaveDatabaseAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -9044865055464757942L;

  /**
   * The settings panel.
   */
  private final CDatabaseSettingsPanel m_settingsPanel;

  /**
   * Creates a new action object.
   * 
   * @param settingsPanel The settings panel.
   */
  public CSaveDatabaseAction(final CDatabaseSettingsPanel settingsPanel) {
    super("Save");

    // Preconditions.checkNotNull(settingsPanel,
    // "IE01318: Settings panel argument can not be null");

    m_settingsPanel = settingsPanel;

    putValue(SHORT_DESCRIPTION, "Saves the current database configuration (CTRL-S)");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_settingsPanel.saveConnection();
  }
}
