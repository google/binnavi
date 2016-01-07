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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;

/**
 * This action can be used to create a new database with default values for all fields.
 */
public final class CAddDatabaseAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7789885643476931774L;

  /**
   * Project tree where the new database is selected.
   */
  private final JTree m_projectTree;

  /**
   * Adds a new database to the database manager.
   * 
   * @param projectTree Project tree where the new database is selected.
   */
  public CAddDatabaseAction(final JTree projectTree) {
    super("Add database");

    m_projectTree = Preconditions.checkNotNull(projectTree, "IE01851: Tree argument can't be null");

    putValue(MNEMONIC_KEY, (int) "HK_MENU_ADD_DATABASE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.addNewDatabase(m_projectTree);
  }
}
