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
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Action that can be used to delete projects.
 */
public final class CDeleteProjectAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5311924222270401955L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The database from which the projects are deleted.
   */
  private final IDatabase m_database;

  /**
   * The projects to be deleted.
   */
  private final INaviProject[] m_projects;

  /**
   * Updates the project tree after the deletion.
   */
  private final ITreeUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database from which the projects are deleted.
   * @param projects The projects to be deleted.
   * @param updater Updates the project tree after the deletion.
   */
  public CDeleteProjectAction(final JFrame parent, final IDatabase database,
      final INaviProject[] projects, final ITreeUpdater updater) {
    super("Delete Project");

    m_parent = Preconditions.checkNotNull(parent, "IE01882: Parent argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01883: Database argument can't be null");
    m_projects =
        Preconditions.checkNotNull(projects, "IE01884: Projects argument can't be null").clone();
    m_updater = Preconditions.checkNotNull(updater, "IE02339: updater argument can not be null");

    for (final INaviProject project : projects) {
      Preconditions.checkNotNull(project, "IE01885: Projects list contains a null-element");
    }

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_PROJECT".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.deleteProjects(m_parent, m_database, m_projects, m_updater);
  }
}
