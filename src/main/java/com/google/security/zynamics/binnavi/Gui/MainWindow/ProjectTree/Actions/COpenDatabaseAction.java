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
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseConnectionFunctions;

/**
 * Action that can be used to open a database.
 */
public final class COpenDatabaseAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2369159756337681560L;

  /**
   * Tree to be updated after the database was opened.
   */
  private final JTree m_projectTree;

  /**
   * Database to be opened.
   */
  private final IDatabase m_database;

  /**
   * Creates a new action object.
   * 
   * @param projectTree Tree to be updated after the database was opened.
   * @param database Database to be opened.
   */
  public COpenDatabaseAction(final JTree projectTree, final IDatabase database) {
    super("Connect to database");

    m_projectTree =
        Preconditions.checkNotNull(projectTree, "IE01907: Project tree argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE01908: Database argument can't be null");

    putValue(ACCELERATOR_KEY, HotKeys.OPEN_DATABASE_ACCELERATOR_KEY.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_OPEN_DATABASE".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseConnectionFunctions.openDatabase(m_projectTree, m_database);
  }
}
