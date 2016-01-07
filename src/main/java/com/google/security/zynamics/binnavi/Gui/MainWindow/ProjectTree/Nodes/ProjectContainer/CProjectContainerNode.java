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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer;



import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.CProjectNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component.CProjectContainerNodeComponent;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Represents project container nodes in the project tree.
 */
public final class CProjectContainerNode extends CProjectTreeNode<Object> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6914349402634037384L;

  /**
   * Icon shown in the project tree for this node.
   */
  private static final ImageIcon ICON_PROJECTS_CONTAINER = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/modules_container4.png"));

  /**
   * The database that provides the information necessary to build the child nodes of the project
   * container node.
   */
  private final IDatabase m_database;

  /**
   * Listens on the database and updates the child nodes of the database node if necessary.
   */
  private final InternalDatabaseListener m_listener;

  /**
   * Creates a new node object.
   * 
   * @param projectTree The project tree of the main window.
   * @param database The database to search for.
   */
  public CProjectContainerNode(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CProjectContainerNodeComponent(projectTree, database);
      }
    }, new CProjectContainerNodeMenuBuilder(projectTree, database));

    Preconditions.checkNotNull(database, "IE01988: Database can't be null");

    m_database = database;

    createChildren();

    // Add a listener that keeps track of the database.
    m_listener = new InternalDatabaseListener();
    database.addListener(m_listener);
  }

  /**
   * Creates the child nodes of project container nodes. One child node is added for each project
   * found in the database.
   */
  @Override
  protected void createChildren() {
    if (m_database.isLoaded()) {
      for (final INaviProject project : m_database.getContent().getProjects()) {
        add(new CProjectNode(getProjectTree(), this, m_database, project, new CProjectContainer(
            m_database, project)));
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    m_database.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Don't do anything.
  }

  @Override
  public Icon getIcon() {
    return ICON_PROJECTS_CONTAINER;
  }

  @Override
  public String toString() {
    if (m_database.isLoaded()) {
      return "Projects" + " (" + m_database.getContent().getProjects().size() + ")";
    } else {
      return "";
    }

  }

  /**
   * This listener keeps track of relevant changes in the database and updates the tree should a
   * relevant change occur.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    /**
     * When a new project was added to the database, a new child node that represents this project
     * is added to the tree.
     */
    @Override
    public void addedProject(final IDatabase connection, final INaviProject project) {
      add(new CProjectNode(getProjectTree(), CProjectContainerNode.this, m_database, project,
          new CProjectContainer(m_database, project)));

      getTreeModel().nodeStructureChanged(CProjectContainerNode.this);
    }

    /**
     * When a project was removed from the database, the corresponding node must be removed from the
     * tree.
     */
    @Override
    public void deletedProject(final IDatabase database, final INaviProject project) {
      // Remove the node that represents the deleted project.
      for (int i = 0; i < getChildCount(); i++) {
        final CProjectNode node = (CProjectNode) getChildAt(i);

        if (node.getObject() == project) {
          node.dispose();
          remove(node);
          break;
        }
      }

      getTreeModel().nodeStructureChanged(CProjectContainerNode.this);
    }
  }
}
