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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component;

import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component.Help.CProjectFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CProjectFilterCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;


/**
 * Component that is displayed on the right side of the main window whenever a projects node was
 * selected.
 */
public final class CProjectContainerNodeComponent extends CTablePanel<INaviProject> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -542952607254442699L;

  /**
   * Database object that contains the projects to be displayed.
   */
  private final IDatabase m_database;

  /**
   * Updates the GUI on relevant events in the database.
   */
  private final InternalDatabaseListener m_listener;

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database object that contains the projects to be displayed.
   */
  public CProjectContainerNodeComponent(final JTree projectTree, final IDatabase database) {
    super(new CProjectsTable(projectTree, database), new CProjectFilterCreator(),
        new CProjectFilterHelp());

    // CProjectsTable checks the arguments

    m_database = database;

    m_listener = new InternalDatabaseListener();
    m_database.addListener(m_listener);

    updateBorderText(getBorderText());
  }

  /**
   * Creates the border text that shows how many projects there are in the database.
   * 
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d Projects in Database '%s'", m_database.getContent().getProjects()
        .size(), m_database.getConfiguration().getDescription());
  }

  @Override
  protected void disposeInternal() {
    m_database.removeListener(m_listener);
  }

  /**
   * Updates the GUI on relevant events in the database.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedProject(final IDatabase connection, final INaviProject newProject) {
      updateBorderText(getBorderText());
    }

    @Override
    public void deletedProject(final IDatabase database, final INaviProject project) {
      updateBorderText(getBorderText());
    }
  }
}
