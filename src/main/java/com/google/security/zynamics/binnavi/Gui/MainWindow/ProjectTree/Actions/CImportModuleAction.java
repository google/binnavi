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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleFunctions;


/**
 * Action that can be used to import a module into a database using IDA Pro.
 */
public final class CImportModuleAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8750454886776173497L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The database the module is imported to.
   */
  private final IDatabase m_database;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database the module is imported to.
   */
  public CImportModuleAction(final JFrame parent, final IDatabase database) {
    super("Import IDB file");

    m_parent = Preconditions.checkNotNull(parent, "IE01896: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01897: Database argument can't be null");

    putValue(ACCELERATOR_KEY, HotKeys.IMPORT_MODULE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_IMPORT_MODULE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CModuleFunctions.importModule(m_parent, m_database);
  }
}
