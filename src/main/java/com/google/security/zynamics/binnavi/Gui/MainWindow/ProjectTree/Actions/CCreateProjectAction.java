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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;

/**
 * Action that can be used to create projects.
 */
public final class CCreateProjectAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 391525432360144407L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Database where the new project is created.
   */
  private final IDatabase m_database;

  /**
   * Updates the project tree after the action was executed.
   */
  private final INodeSelectionUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database where the new project is created.
   * @param updater Updates the project tree after the action was executed.
   */
  public CCreateProjectAction(final JFrame parent, final IDatabase database,
      final INodeSelectionUpdater updater) {
    super("Create Project");

    m_parent = Preconditions.checkNotNull(parent, "IE01865: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01866: Database argument can't be null");
    m_updater = Preconditions.checkNotNull(updater, "IE02865: updater argument can not be null");

    putValue(ACCELERATOR_KEY, HotKeys.CREATE_PROJECT_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_CREATE_PROJECT".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CProjectFunctions.createProject(m_parent, m_database, m_updater);
  }
}
