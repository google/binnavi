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
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Action that can be used to delete modules from databases.
 */
public final class CDeleteModuleAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7475984562405684871L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The database from which the modules are deleted.
   */
  private final IDatabase m_database;

  /**
   * The modules to be deleted.
   */
  private final INaviModule[] m_modules;

  /**
   * Updates the project tree after the action was executed.
   */
  private final ITreeUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database from which the modules are deleted.
   * @param modules The modules to be deleted.
   * @param updater Updates the project tree after the action was executed.
   */
  public CDeleteModuleAction(final JFrame parent, final IDatabase database,
      final INaviModule[] modules, final ITreeUpdater updater) {
    super("Delete Module");

    m_parent = Preconditions.checkNotNull(parent, "IE01877: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01878: Database argument can't be null");
    m_modules =
        Preconditions.checkNotNull(modules, "IE01879: Modules argument can't be null").clone();
    m_updater = Preconditions.checkNotNull(updater, "IE01880: Updater argument can not be null");

    for (final INaviModule module : modules) {
      Preconditions.checkNotNull(module, "IE01881: Modules list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_MODULE".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CModuleFunctions.deleteModules(m_parent, m_database, m_modules, m_updater);
  }
}
