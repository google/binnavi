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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseConnectionFunctions;

/**
 * Action that can be used to close a database.
 */
public final class CCloseDatabaseAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6974621584636669447L;

  /**
   * Parent window used for dialogs.
   */
  private final JComponent m_parent;

  /**
   * Database to be closed.
   */
  private final IDatabase m_database;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database to be closed.
   */
  public CCloseDatabaseAction(final JComponent parent, final IDatabase database) {
    super("Disconnect from database");

    m_parent = Preconditions.checkNotNull(parent, "IE01855: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01856: Database argument can't be null");

    putValue(ACCELERATOR_KEY, HotKeys.CLOSE_DATABASE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_CLOSE_DATABASE".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseConnectionFunctions.closeDatabase(SwingUtilities.getWindowAncestor(m_parent),
        m_database);
  }
}
