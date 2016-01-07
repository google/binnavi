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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;

/**
 * Action class that can be used to refresh the raw modules of a database.
 */
public final class CRefreshRawModulesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1897655675643051878L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Database whose raw modules should be refreshed.
   */
  private final IDatabase m_database;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database whose raw modules should be refreshed.
   */
  public CRefreshRawModulesAction(final JFrame parent, final IDatabase database) {
    super("Refresh");
    m_parent = Preconditions.checkNotNull(parent, "IE01909: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01910: Database argument can't be null");

    putValue(ACCELERATOR_KEY, HotKeys.REFRESH_RAW_MODULES_ACCELERATOR_KEY.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_REFRESH_RAW_MODULES".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.refreshRawModules(m_parent, m_database);
  }
}
