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
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;

/**
 * Action that can be used to delete a database.
 */
public final class CDeleteDatabaseAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2085922855432139483L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Database to be deleted.
   */
  private final IDatabase m_database;

  /**
   * Updates the project tree after the action was executed.
   */
  private final ITreeUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database to be deleted.
   * @param updater Updates the project tree after the action was executed.
   */
  public CDeleteDatabaseAction(final JFrame parent, final IDatabase database,
      final ITreeUpdater updater) {
    super("Remove Database");

    m_parent = Preconditions.checkNotNull(parent, "IE01871: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01872: Database argument can't be null");
    m_updater = Preconditions.checkNotNull(updater, "IE02866: updater argument can not be null");

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_DATABASE".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.removeDatabase(m_parent, m_database, m_updater);
  }
}
